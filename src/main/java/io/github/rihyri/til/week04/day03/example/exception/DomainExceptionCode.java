package io.github.rihyri.til.week04.day03.example.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DomainExceptionCode {

    AI_RESPONSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AI 응답 처리 중 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;
}
