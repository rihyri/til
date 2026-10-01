package io.github.rihyri.til.week03.day03.example.service;

import io.github.rihyri.til.week03.day03.example.dto.ContextChatResponse;
import io.github.rihyri.til.week03.day03.example.entity.ChatConversation;
import io.github.rihyri.til.week03.day03.example.entity.ChatMessage;
import io.github.rihyri.til.week03.day03.example.entity.ChatMessageType;
import io.github.rihyri.til.week03.day03.example.entity.StatusType;
import io.github.rihyri.til.week03.day03.example.exception.DomainException;
import io.github.rihyri.til.week03.day03.example.exception.DomainExceptionCode;
import io.github.rihyri.til.week03.day03.example.repository.ChatConversationRepository;
import io.github.rihyri.til.week03.day03.example.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersistenceChatService {

    // 실제 AI에게 요청을 보내는 객체
    private final ChatClient chatClient;

    // 채팅창 DB
    private final ChatConversationRepository conversationRepository;

    // 메시지 DB 접근
    private final ChatMessageRepository  messageRepository;

    // 최근 대화는 최대 20개까지만 원본 그대로 유지한다.
    private static final int MAX_HISTORY_MESSAGES = 20;

    @Transactional
    public ContextChatResponse chat (String conversationId, String userMessage) {

        /*
         * ==========================================
         * 1. Conversation 조회 또는 생성
         * ==========================================
         *
         * conversationId가 없다면 → 새로운 채팅방
         * conversationId가 있다면 → 기존 채팅방
         */
        ChatConversation conversation = getOrCreateConversation(conversationId, userMessage);

        /*
         * ==========================================
         * 2. 사용자의 질문을 DB에 먼저 저장
         * ==========================================
         *
         * 예: USER | ACTIVE | "Spring이 뭐야?"
         */
        saveUserMessage(conversation, userMessage);

        /*
         * ==========================================
         * 3. 현재 사용할 수 있는 대화 기록 조회
         * ==========================================
         *
         * INACTIVE 메시지는 이미 요약된 메시지이므로 다시 AI에게 보내지 않는다.
         * ACTIVE 메시지만 가져온다.
         */
        List<ChatMessage> activeMessages =
                messageRepository.findByConversation_IdAndStatusOrderByCreatedAtAsc(conversation.getId(), StatusType.ACTIVE);

        /*
         * ==========================================
         * 4. DB Entity를 Spring AI Message로 변환
         * ==========================================
         *
         * 메시지가 너무 많다면 오래된 메시지를 요약하는 작업도 함께 수행한다.
         */
        List<Message> contextMessages = buildContextMessages(activeMessages, conversation);

        try {

            /*
             * ==========================================
             * 5. AI 호출
             * ==========================================
             *
             * 지금까지 만들어진 Context를 AI에게 전달한다.
             */
            ChatResponse response = chatClient.prompt()
                    .messages(contextMessages)
                    .call()
                    .chatResponse();

            /*
             * ==========================================
             * 6. AI 응답 내용 가져오기
             * ==========================================
             */
            String assistantMessage = response.getResult()
                                            .getOutput()
                                            .getText();

            /*
             * ==========================================
             * AI 호출에 사용된 Token 정보
             * ==========================================
             */
            Usage usage = response.getMetadata().getUsage();

            /*
             * ==========================================
             * 7. AI 답변 DB 저장
             * ==========================================
             */
            saveAssistantMessage(
                    conversation,
                    assistantMessage,
                    usage
            );

            /*
             * ==========================================
             * 8. Controller에 결과 반환
             * ==========================================
             */
            return ContextChatResponse.builder()
                    .conversationId(conversation.getId().toString())
                    .message(assistantMessage)
                    .timestamp(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            log.error("AI 호출 중 오류 발생", e);
            throw new DomainException(
                    DomainExceptionCode.AI_RESPONSE_ERROR
            );
        }
    }

    // ========== Conversation 조회/생성 ==========
    private ChatConversation getOrCreateConversation(String conversationId, String userMessage) {

        /*
         * conversationId가 없다. → 즉 사용자가 새로운 채팅을 시작했다.
         */
        if (!StringUtils.hasText(conversationId)) {
            return createConversation(userMessage);
        }

        /*
         * conversationId가 있다. → 기존 채팅방을 DB에서 찾는다.
         */
        return conversationRepository.findById(UUID.fromString(conversationId))
                .orElseThrow(() -> new DomainException(DomainExceptionCode.CONVERSATION_NOT_FOUND));
    }

    // ========== 새로운 대화방 생성 ==========
    private ChatConversation createConversation (String userMessage) {

        /*
         * 사용자의 첫 질문을 채팅방 제목으로 사용한다.
         * 너무 길면 50자까지만 사용한다.
         */
        String title = userMessage.length() > 50 ? userMessage.substring(0, 50) + "..." : userMessage;

        ChatConversation conversation = ChatConversation.builder()
                .title(title)
                .build();

        return conversationRepository.save(conversation);
    }

    // ========== 사용자 메시지 저장 ==========
    private void saveUserMessage(ChatConversation conversation, String userMessage) {

        ChatMessage message = ChatMessage.builder()
                // 어느 채팅방의 메시지인가?
                .conversation(conversation)
                // 사용자가 작성했다.
                .role(ChatMessageType.USER)
                // 현재 Context에서 사용할 메시지
                .status(StatusType.ACTIVE)
                // 실제 질문
                .message(userMessage)
                // 아직 AI 호출 전이므로 Token 정보는 없다.
                .promptTokens(null)
                .completionTokens(null)
                .totalTokens(null)
                .build();

        messageRepository.save(message);
    }

    // ========== AI 답변 저장 ==========
    private void saveAssistantMessage (ChatConversation conversation, String assistantMessage, Usage usage) {

        ChatMessage message = ChatMessage.builder()
                .conversation(conversation)
                // AI의 응답
                .role(ChatMessageType.ASSISTANT)
                .status(StatusType.ACTIVE)
                .message(assistantMessage)
                // AI 응답에서는 실제로 사용된 Token 정보를 저장할 수 있다.
                .promptTokens(usage.getPromptTokens())
                .completionTokens(usage.getCompletionTokens())
                .totalTokens(usage.getTotalTokens())
                .build();

        messageRepository.save(message);
    }

    // ========== summary 리스트 반환 ==========
    private List<Message> buildContextMessages(List<ChatMessage> messages, ChatConversation conversation) {

        /*
         * 메시지가 하나도 없다면 빈 list 반환
         */
        if (messages == null || messages.isEmpty()) {
            return List.of();
        }

        /*
         * 메시지가 20개 이하라면 굳이 요약할 필요가 없다.
         */
        if (messages.size() <= MAX_HISTORY_MESSAGES) {
            return messages.stream()
                    .map(this::mapToSpringAiMessage)
                    .toList();
        }

        /*
         * 메시지가 20개보다 많을 경우
         */
        int start = messages.size() - MAX_HISTORY_MESSAGES;

        /*
         * 예: 전체 메시지 = 30개, MAX = 20개
         *
         * start = 30 - 20 = 10
         * 0 ~ 9 → 오래된 메시지
         * 10 ~ 29 → 최근 메시지
         */
        List<ChatMessage> oldMessages = messages.subList(0, start);
        List<ChatMessage> recentMessages = messages.subList(start, messages.size());

        // 오래된 메시지를 AI에게 요약시킨다.
        String summary = generateSummary(oldMessages);

        // 오래된 원본 메시지는 INACTIVE 처리
        oldMessages.forEach(
                ChatMessage::deactivate
        );

        // JPA Dirty Checking으로 변경될 수 있지만 공부용으로 명시적으로 saveAll을 호출
        messageRepository.saveAll(oldMessages);

        // 새로운 SUMMARY 메시지 생성
        ChatMessage summaryMessage = saveSummaryMessage(conversation, summary);

        /*
         * AI에게 전달할 최종 Context
         * =
         * SUMMARY + 최근 메시지 20개
         */
        List<ChatMessage> context = new ArrayList<>();

        context.add(summaryMessage);
        context.addAll(recentMessages);

        // DB Entity인 ChatMessage를 Spring AI Message로 변환
        return context.stream()
                .map(this::mapToSpringAiMessage)
                .toList();
    }

    // ========== DB Entity → Spring AI Message 반환 ==========
    private Message mapToSpringAiMessage (ChatMessage chatMessage) {

        String content = chatMessage.getMessage();

        return switch (chatMessage.getRole()) {

            case USER -> new UserMessage(content);

            case ASSISTANT -> new AssistantMessage(content);

            // 과거 대화 SUMMARY도 AI에게는 System Context처럼 전달한다.
            case SYSTEM, SUMMARY -> new SystemMessage(content);
        };
    }

    // ========== 오래된 대화 요약 ==========
    private String generateSummary (List<ChatMessage> messages) {

        /*
         * ChatMessage 여러 개를 하나의 String으로 만든다.
         *
         * 예:
         *
         * USER: JPA가 뭐야?
         * ASSISTANT : JPA는...
         * USER : Hibernate와 차이가 뭐야?
         */
        String conversationText = messages.stream()
                .map(message ->
                        message.getRole().name()
                                + " : "
                                + message.getMessage()
                )
                // 각각의 메시지를 줄바꿈으로 연결
                .collect(
                        Collectors.joining("\n")
                );

        String prompt = """
                다음 대화를 요약해주세요.
                
                이후 대화의 Context로 사용할 예정이므로
                사용자의 질문과 중요한 정보를 중심으로
                간결하게 정리해주세요.
                
                대화:
                %s
                """.formatted(conversationText);

        try {
            return chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

        } catch (Exception e) {
            log.error("대화 요약 실패", e);
            throw new DomainException(DomainExceptionCode.AI_RESPONSE_ERROR);

        }
    }

    // summary 저장
    private ChatMessage saveSummaryMessage (ChatConversation conversation, String summary) {

        ChatMessage summaryMessage = ChatMessage.builder()
                .conversation(conversation)
                // 일반 AI 응답이 아니라 요약
                .role(ChatMessageType.SUMMARY)
                // 다음 AI 호출에서 사용해야 한다.
                .status(StatusType.ACTIVE)
                .message(summary)
                .build();

        return messageRepository.save(summaryMessage);
    }
}
