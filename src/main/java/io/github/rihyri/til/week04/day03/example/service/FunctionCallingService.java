package io.github.rihyri.til.week04.day03.example.service;

import io.github.rihyri.til.week03.day03.example.exception.DomainException;
import io.github.rihyri.til.week03.day03.example.exception.DomainExceptionCode;
import io.github.rihyri.til.week04.day03.example.dto.AnswerResponse;
import io.github.rihyri.til.week04.day03.example.tool.FunctionTools;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FunctionCallingService {

    private final ChatClient chatClient;
    private final FunctionTools functionTools;

    // 1. 일반 Function Calling 채팅
    public AnswerResponse chat (String question) {

        log.info("[Function Chat] question: {}", question);

        try {

            /*
             *  1. 사용자 질문 전달
             *
             *  @Tool이 있다고 해서 무조건 실행되는 것은 아니다.
             *  LLM이 질문을 분석한 뒤 필요한 Tool을 선택한다.
             */
            String answer = chatClient.prompt()
                .user(question)
                .tools(functionTools)
                .call()
                .content();

            /*
             *  Step 2. 최종 AI 답변 반환
             */
            return AnswerResponse.builder()
                .answer(answer)
                .build();
        } catch (Exception e) {
            log.error("[Function Chat Error]", e);

            throw new DomainException(DomainExceptionCode.AI_RESPONSE_ERROR);
        }
    }


    /*
     *  2. System Prompt를 직접 전달하는 Function calling
     */
    public AnswerResponse chatWithSystemMessage (String question) {

        // 함수 호출과 답변 생성 방식에 대한 추가 지침을 작성한다.
        String systemMessage = """
                당신은 학습용 도구를 사용하는 AI입니다.

                계산이 필요한 질문이라면 계산기 Tool을 사용하세요.
                현재 시간이 필요한 질문이라면 시간 Tool을 사용하세요.
                날씨 조회는 실시간 정보가 아닌
                학습용 가상 날씨 데이터임을 명확히 설명하세요.

                사용자가 이해하기 쉬운 한국어로 답변하세요.
                """;

        log.info("[System Function Chat] question: {}", question);

        try {
            String answer = chatClient.prompt()
                .system(systemMessage)
                .user(question)
                .tools(functionTools)
                .call()
                .content();

            return AnswerResponse.builder()
                .answer(answer)
                .build();

        } catch (Exception e) {

            log.error("[System Function Chat Error]", e);

            throw new DomainException(DomainExceptionCode.AI_RESPONSE_ERROR);
        }
    }
}
