package io.github.rihyri.til.week04.day02.example.exception;

import lombok.Getter;

@Getter
public class DomainException extends RuntimeException {

    private final DomainExceptionCode errorCode;

    public DomainException(DomainExceptionCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
