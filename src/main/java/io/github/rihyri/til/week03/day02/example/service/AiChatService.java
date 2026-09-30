package io.github.rihyri.til.week03.day02.example.service;

import io.github.rihyri.til.week03.day02.example.dto.ChatResponseDto;
import io.github.rihyri.til.week03.day02.example.dto.ReviewResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatService {

    /*
     * AiChatConfig에서 @Bean으로 등록한 ChatClient를 Spring이 생성자 주입을 통해 전달한다.
     */
    private final ChatClient chatClient;

    /*
     * 대화 히스토리를 저장하기 위한 공유 Map
     *
     * key : conversationId (대화 식별자)
     * value : List<Message> (이전 대화 내용)
     *
     * 예시: "chat-001" -> [UserMessage("JPA란 무엇인가요?"), AssistantMessage("JPA는...")]
     *
     * 여러 사용자가 동시에 요청할 수 있으므로 일반 HashMap 대신 ConcurrentHashMap을 사용한다.
     * 단, Map 내부의 List는 별도로 동기화 해야한다.
     */
    private final Map<String, List<Message>> conversations = new ConcurrentHashMap<>();

    // =========== 1. 일반 AI 질문 ===========
    public ChatResponseDto chat(String question) {

        /*
         * 일반 질문은 이전 대화 히스토리를 사용하지 않는다.
         *
         * 사용자의 질문만 전달하고 AI가 완성한 최종 답변을 가져온다.
         * content() : 응답의 텍스트 변환
         */
        String answer = chatClient.prompt()
                .user(question)
                .call()
                .content();

        /*
         * UUID : 새로운 대화를 식별할 수 있도록 고유한 문자열을 생성한다.
         * 현재 chat()은 히스토리를 저장하지 않으므로 이 ID 자체로 이전 대화가 이어지지는 않는다.
         */
        String conversationId = UUID.randomUUID().toString();

        /*
         * Controller에서 반환할 DTO 생성
         *
         * 일반 호출에서는 텍스트만 추출하므로 토큰 사용량은 null로 설정한다.
         */
        return ChatResponseDto.builder()
                .message(answer)
                .conversationId(conversationId)
                .timeStamp(LocalDateTime.now())
                .tokenUsage(null)
                .build();
    }


    // =========== 2. 대화 히스토리를 포함한 AI 질문 ===========
    public ChatResponseDto chatWithHistory(String question, String conversationId) {

        /*
         * STEP 1. 대화 ID 확인
         *
         * conversationId가 없으면 신규 대화로 판단한다.
         */
        if (conversationId == null || conversationId.isBlank()) {
            conversationId = UUID.randomUUID().toString();
        }

        /*
         * STEP 2. 기존 대화 히스토리 조회
         *
         * computeIfAbsent(key, function)
         *
         * 1. key가 이미 존재한다면 기존 값을 가져온다
         * 2. key가 없다면 새로운 값을 생성한다.
         * 3. 생성한 값을 Map에 저장한다.
         *
         * 즉, 신규 대화라면 새로운 List<Message>를 생성한다.
         */
        List<Message> history = conversations.get(conversationId);

        if (history == null) {
            history = new ArrayList<>();
            conversations.put(conversationId, history);
        }

        /*
         * STEP 3. 동일한 대화의 동시 수정 방지
         *
         * ConcurrentHashMap은 Map 자체를 보호하지만, Value로 저장한 ArrayList까지 보호하지는 않는다.
         * 동일한 conversationId로 요청이 동시에 들어오면 같은 list를 수정하려고 할 수 있다.
         *
         * synchronized(history)를 통해 같은 히스토리를 사용하는 요청을 순차 처리한다.
         *
         * 서로 다른 히스토리는 각각 다른 객체이므로 이 잠금으로 모든 대화가 한꺼번에 막히지는 않는다.
         */
        synchronized (history) {

            /*
             * STEP 4. 기존 대화 복사
             *
             * 저장된 원본 List를 바로 수정하지 않고 이번 AI 호출에 사용할 복사본을 생성한다.
             * AI 호출이 실패하더라도 기존 대화 기록을 유지하기 위해서이다.
             */
            List<Message> requestMessages = new ArrayList<>(history);

            /*
             * STEP 5. 현재 사용자 질문 추가
             *
             * 사용자 질문 문자열을 Spring AI의 UserMessage 객체로 변환한다.
             */
            UserMessage userMessage = new UserMessage(question);
            requestMessages.add(userMessage);

            try {
                /*
                 * STEP 6. 이전 대화와 현재 질문 전달
                 *
                 * messages(requestMessage)
                 *
                 * 기존 히스토리 + 현재 질문을 한 번에 AI에게 전달한다.
                 * chatResponse()를 사용하면 답변 텍스트뿐 아니라 토큰 사용량 등의 메타데이터에도 접근할 수 있다.
                 */
                ChatResponse response = chatClient.prompt()
                        .messages(requestMessages)
                        .call()
                        .chatResponse();

                /*
                 * STEP 7. AI가 생성한 텍스트 추출
                 *
                 * response()
                 *  -> getResult()
                 *  -> getOutput()
                 *  -> getText()
                 *
                 * AI 응답 객체 내부에서 실제 답변 문자열을 가져오는 과정이다.
                 */
                String answer = response
                        .getResult()
                        .getOutput()
                        .getText();

                /*
                 * STEP 8. AI 응답을 Message 객체로 변환
                 *
                 * 다음 질문에서 이전 AI 답변을 다시 전달할 수 있도록 준비한다.
                 */
                AssistantMessage assistantMessage = new AssistantMessage(answer);

                /*
                 * STEP 9. 히스토리에 대화 저장
                 *
                 * 사용자의 질문과 AI 답변을 순서대로 저장한다.
                 *
                 * UserMessage -> AssistantMessage
                 * 이 두 객체를 저장해야 다음 질문에서도 이전 대화를 참고할 수 있다.
                 */
                history.add(userMessage);
                history.add(assistantMessage);

                /*
                 * STEP 10. 토큰 사용량 조회
                 *
                 * ChatResponse의 Metadata에는 AI 호출과 관련된 부가 정보가 포함된다.
                 */
                var usage = response.getMetadata().getUsage();

                /*
                 * STEP 11. 토큰 사용량 DTO 생성
                 *
                 * 모델이 제공하는 Usage 정보를 API 응답 형태로 반환한다.
                 */
                ChatResponseDto.TokenUsage tokenUsage = null;

                if (usage != null) {
                    tokenUsage = ChatResponseDto.TokenUsage.builder()
                            .promptTokens(usage.getPromptTokens())
                            .completionTokens(usage.getCompletionTokens())
                            .totalTokens(usage.getTotalTokens())
                            .build();
                }

                /*
                 * STEP 12. 최종 응답 반환
                 *
                 * AI 답변, 대화 ID, 응답 시각, 토큰 사용량
                 * 위 정보를 하나의 DTO로 묶어 반환한다.
                 */
                return ChatResponseDto.builder()
                        .message(answer)
                        .conversationId(conversationId)
                        .timeStamp(LocalDateTime.now())
                        .tokenUsage(tokenUsage)
                        .build();

            } catch (Exception e) {
                log.error("AI 호출 중 오류 발생", e);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "AI 응답을 처리하지 못했습니다.");
            }
        }
    }


    // =========== 3. 스트리밍 AI 질문 ===========
    public Flux<String> chatStream(String question) {

        /*
         * Flux<String>
         *
         * String 하나를 반환하는 대신 문자열을 여러 조각으로 전달할 수 있는 스트림을 반환한다.
         *
         * 일반 호출: .call().content()
         * 스트리밍 호출: .stream().content()
         */
        return chatClient.prompt()
                .user(question)

                .stream()
                .content();
    }


    // =========== 4. 전체 대화 초기화 ===========
    public void clearAll() {

        /*
         * ConcurrentHashMap에 저장된 모든 conversationId와 대화 기록을 삭제한다.
         */
        conversations.clear();

        log.info("모든 대화 세션 초기화 완료");
    }


    // 실습 문제
    public ReviewResponseDto analyzeReview(String productName, String review) {

        String answer = chatClient.prompt()
                .system("""
                        당신은 이커머스 플랫폼의 상품 리뷰 평가 전문가입니다.
                        
                        배송, 품질, 가격, 서비스 등에 대한
                        리뷰 내용을 종합하여 상품 만족도를 판단하세요.
                        
                        satisfaction은 반드시 다음 중 하나로 판단하세요.
                        - high
                        - medium
                        - low
                        
                        confidence는 해당 판단에 대한 확신도를
                        0.00부터 1.00 사이의 소수점 둘째 자리 숫자로 표현하세요.
                        
                        반드시 다음 형식으로만 응답하세요.
                        
                        satisfaction: low
                        confidence: 0.88
                        
                        """)
                .user("""
                            상품명: %s
                            리뷰: %s
                        """.formatted(productName, review))
                .call()
                .content();

        // AI 응답에서 만족도 값을 추출
        String satisfaction = null;

        String[] satisfactionLevels = {"high", "medium", "low"};

        for (String level : satisfactionLevels) {
            if (answer.contains(level)) {
                satisfaction = level;
                break;
            }
        }

        // confidence 값 추출
        double confidence = 0.0;

        Pattern pattern = Pattern.compile("(0\\.\\d{2}|1\\.00)");
        Matcher matcher = pattern.matcher(answer);

        if (matcher.find()) {
            confidence = Double.parseDouble(matcher.group());
        }

        if (satisfaction == null) {
            log.error("AI 만족도 분석 결과 형식 오류: {}", answer);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI 분석 결과를 처리할 수 없습니다.");
        }

        return ReviewResponseDto.builder()
                .satisfaction(satisfaction)
                .confidence(confidence)
                .detectedAt(LocalDateTime.now())
                .build();
    }
}