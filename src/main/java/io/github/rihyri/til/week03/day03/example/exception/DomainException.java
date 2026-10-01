package io.github.rihyri.til.week03.day03.example.exception;

import lombok.Getter;

@Getter
public class DomainException extends RuntimeException {

    private final DomainExceptionCode errorCode;

    public DomainException(DomainExceptionCode errorCode) {

        // RuntimeException의 message에도 저장
        super(errorCode.getMessage());

        this.errorCode = errorCode;
    }
}