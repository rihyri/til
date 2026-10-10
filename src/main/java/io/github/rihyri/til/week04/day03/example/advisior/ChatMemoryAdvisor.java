package io.github.rihyri.til.week04.day03.example.advisior;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;

@Slf4j
public class ChatMemoryAdvisor implements BaseAdvisor {

    /*
     *  Advisor의 before()와 after() 사이에서 사용할 Context Key
     *
     *  before() 에서는 사용자 질문을 알 수 있지만,
     *  after() 에서는 요청 객체를 직접 전달받지 않는다.
     *
     *  따라서 사용자 질문을 Context에 저장하고, after()에서 꺼내어 사용한다.
     */
    private static final String CONTEXT_USER_TEXT = "memory_current_question";

    /*
     *  대화 기록을 메모리에 저장하는 Map
     *
     *  예시
     *  {
     *      "room-01": [UserMessage, AssistantMessage, ...]
     *  }
     *
     *  ConcurrentHashMap
     *  - 여러 스레드가 접근할 수 있는 Map 구현체
     *  - 일반 HashMap보다 동시 접근에 안전하도록 설계됨
     */
    private final Map<String, List<Message>> conversationStore = new ConcurrentHashMap<>();

    private final String conversationId;

    private final int maxMessages;

    public ChatMemoryAdvisor (String conversationId, int maxMessages) {

        if (maxMessages <= 0) {
            throw new IllegalArgumentException("최대 메시지 개수는 1 이상이어야 합니다.");
        }

        this.conversationId = conversationId;
        this.maxMessages = maxMessages;
    }

    /*
     *  Step 1. before()
     *
     *  LLM에게 질문을 보내기 전에 실행한다.
     *
     *  주요 역할
     *  : 이전 대화 기록 가져오기, 현재 질문과 이전 대화 합치기, 현재 사용자 질문 추출하기, 변경된 요청 변환하기
     */
    @Override
    public ChatClientRequest before (ChatClientRequest request, AdvisorChain advisorChain) {

        log.info("[Memory Before] 이전 대화 불러오기");

        /*
         *  1. 기존 대화 기록을 가져온다.
         *
         *  Map에 conversationId가 존재한다면 -> 해당 대화 기록 반환
         *  존재하지 않는다면 -> 비어있는 새 list 반환
         *
         *  CopyOnWriteArrayList : 내부 배열을 복사하는 장식, 조회가 많고 수정이 적은 상황에서 유용
         */
        List<Message> history = conversationStore.getOrDefault(conversationId, new CopyOnWriteArrayList<>());

        /*
         *  1-2. 이전 대화 기록과 현재 질문을 합친다.
         *
         *  왜 history.addAll()을 사용하지 않을까?
         */
        List<Message> fullMessages = new ArrayList<>(history);

        /*
         *  현재 요청에 포함된 메시지들을 추가한다.
         *
         *  합친 결과: [이전 질문, 이전 답변, 현재 질문]
         */
        fullMessages.addAll(request.prompt().getInstructions());

        // 1-3. 현재 질문에서 사용자가 입력한 문자열 추출
        String userText =
            request.prompt().getInstructions()
                .stream()
                .filter(message ->
                    message.getMessageType() == MessageType.USER
                )
                .map(Message::getText)
                .findFirst()
                .orElse("");

        return request.mutate()
            .prompt(new Prompt(fullMessages))
            .context(CONTEXT_USER_TEXT, userText)
            .build();
    }

    /*
     *  Step 2. after()
     *
     *  주요 역할
     *  : 대화 기록을 가져오거나 생성, 사용자 질문 저장, AI 응답 저장, 오래된 대화 제거
     */
     @Override
    public ChatClientResponse after (ChatClientResponse response, AdvisorChain advisorChain) {

         log.info("[Memory After] 현재 대화 저장");

         /*
          * Step 2-1. 대화 기록을 가져오거나 새로 만든다.
          *
          * 1. Key가 이미 있다면
          * -> 기존 Value 반환
          *
          * 2. Key가 없다면
          * -> function 실행
          * -> 새 Value를 Map에 저장
          * -> 생성된 Value 반환
          */
         List<Message> history = conversationStore.computeIfAbsent(conversationId, key -> new CopyOnWriteArrayList<>());

         /*
          * Step 2-2. before()에서 Context에 저장한 질문 가져오기
          *
          * Context에 사용자 질문이 존재하고, 비어 있는 문자열이 아니라면 UserMessage 객체를 생성하여 저장한다.
          */
         Optional.ofNullable(
                 response.context()
                     .get(CONTEXT_USER_TEXT)
             )
             .map(Object::toString)
             .filter(text -> !text.isBlank())
             .ifPresent(text ->
                 history.add(new UserMessage(text))
             );

         /*
          * Step 2-3. AI 응답 저장
          *
          * AI 답변을 Message 형태로 저장한다.
          */
         if (response.chatResponse() != null && response.chatResponse().getResult() != null) {

             history.add(
                 response.chatResponse().getResult().getOutput()
             );
         }

         /*
          * Step 2-4. 최대 메시지 개수를 초과하면 오래된 메시지 제거
          *
          * FIFO : 선입선출
          */
         while (history.size() > maxMessages) {
             history.remove(0);
         }

         return response;
     }


    /*
     *  Advisor 이름
     *
     *  getName() : 현재 Advisor를 식별하기 위한 이름
     *
     *  - getName() : advisor 식별
     *  - getOrder() : advisor 실행 순서
     */
    @Override
    public String getName() {
        return getClass().getSimpleName();
    }


    /*
     *  Advisor 실행 순서
     *
     *  순서가 작을수록 요청을 먼저 처리한다.
     *
     *  Advisor A: order = 0
     *  Advisor B: order = 10
     *
     *  요청 : A -> B -> LLM
     *  응답 : LLM -> B -> A
     */
    @Override
    public int getOrder() {
        return 0;
    }

}
