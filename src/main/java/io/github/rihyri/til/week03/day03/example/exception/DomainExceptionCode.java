package io.github.rihyri.til.week03.day03.example.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum DomainExceptionCode {

    // AI 응답 생성 중 오류
    AI_RESPONSE_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "AI 응답 처리 중 오류가 발생했습니다."
    ),

    // 존재하지 않는 대화방 조회
    CONVERSATION_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "대화방을 찾을 수 없습니다."
    ),

    // 잘못된 conversationId 형식
    INVALID_CONVERSATION_ID(
            HttpStatus.BAD_REQUEST,
            "잘못된 대화방 ID 형식입니다."
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