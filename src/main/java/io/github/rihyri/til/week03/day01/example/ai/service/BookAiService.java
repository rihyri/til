package io.github.rihyri.til.week03.day01.example.ai.service;

import io.github.rihyri.til.week03.day01.example.ai.dto.BookAnswerResponse;
import io.github.rihyri.til.week03.day01.example.ai.dto.BookQuestionRequest;
import io.github.rihyri.til.week03.day01.example.ai.exception.AiServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookAiService {

    private final ChatClient chatClient;

    public BookAnswerResponse ask(BookQuestionRequest request) {

        // AI에게 전달할 질문 템플릿
        String template = """
                도서명: {title}
                질문: {question}
                
                독자가 이해하기 쉽게 설명해 주세요.
                """;

        try {

            String answer = chatClient.prompt()

                    // AI의 역할과 공통 지침 설정
                    .system("""
                            당신은 친절한 독서 도우미입니다.
                            모든 답변은 한국어로 작성하세요.
                            내용을 모르면 추측하지 마세요.
                            """)

                    // 사용자 질문과 동적 파라미터 설정
                    .user(u -> u.text(template)
                            .param("title", request.title())
                            .param("question", request.question())
                    )

                    // AI 모델 호출
                    .call()

                    // 응답에서 텍스트 추출
                    .content();

            // AI가 반환한 응답을 DTO로 변환
            return new BookAnswerResponse(answer);
        } catch (Exception e) {

            // 외부 AI 호출 중 발생한 오류 처리
            throw new AiServiceException(
                    "AI 답변을 생성하지 못했습니다.", e
            );
        }
    }
}
