package io.github.rihyri.til.week04.day03.example.service;

import io.github.rihyri.til.week04.day03.example.dto.AnswerResponse;
import io.github.rihyri.til.week04.day03.example.exception.DomainException;
import io.github.rihyri.til.week04.day03.example.exception.DomainExceptionCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdvisorService {

    private final ChatClient chatClient;

    // 1. 이전 대화를 기록하는 Chat AI
    public AnswerResponse chat(String question) {

        log.info("[Advisor Chat] question: {}", question);

        try {

            /*
             *  Step 1. 사용자의 질문을 ChatClient에 전달한다.
             *
             *  - 실제 내부 실행 흐름
             *
             *  1. ChatClient 호출
             *  2. ChatMemoryAdvisor.before() 실행
             *  3. 이전 대화 + 현재 질문을 LLM에 전달
             *  4. LLM 응답 생성
             *  5. ChatMemoryAdvisor.after() 실행
             *  6. 대화 기록 저장
             *  7. 답변 반환
             *
             *  Service에서 before(), after()를 직접 호출할 필요가 없다.
             */
            String answer = chatClient.prompt()
                .user(question)
                .call()
                .content();

            /*
             *  Step 2. LLM 응답을 AnswerResponse DTO로 변환
             */
            return AnswerResponse.builder()
                .answer(answer)
                .build();

        } catch (Exception e) {

            log.error("[Advisor Chat Error]", e);

            throw new DomainException(DomainExceptionCode.AI_RESPONSE_ERROR);
        }
    }
}
