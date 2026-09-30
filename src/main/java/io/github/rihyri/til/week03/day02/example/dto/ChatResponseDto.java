package io.github.rihyri.til.week03.day02.example.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatResponseDto {

    // AI가 생성한 최종 답변
    String message;

    // 현재 대화를 구분하는 고유한 ID
    String conversationId;

    // 응답이 생성된 시간
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime timeStamp;

    // 이번 AI에서 호출에서 사용한 토큰 정보
    TokenUsage tokenUsage;

    /*
     * AI 호출에서 사용된 토큰 정보를 담는 내부 DTO
     *
     * ChatResponseDto와 연관된 정보이므로
     * 별도의 파일로 분리하지 않고 내부 클래스로 관리한다.
     */
    @Getter
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class TokenUsage {

        // 입력에 사용된 토큰 수
        Integer promptTokens;

        // AI가 응답을 생성하면서 사용한 토큰 수
        Integer completionTokens;

        // 입력 + 출력 토큰 수
        Integer totalTokens;
    }
}
