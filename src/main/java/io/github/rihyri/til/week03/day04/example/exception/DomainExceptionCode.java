package io.github.rihyri.til.week03.day04.example.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum DomainExceptionCode {

    // AI 응답 생성 중 오류
    AI_RESPONSE_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "AI 응답 처리 중 오류가 발생했습니다."
    );

    private final HttpStatus status;
    private final String message;

    DomainExceptionCode(
            HttpStatus status,
            String message
    ) {
        this.status = status;
        this.message = message;
    }
}