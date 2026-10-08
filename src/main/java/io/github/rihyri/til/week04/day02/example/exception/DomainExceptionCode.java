package io.github.rihyri.til.week04.day02.example.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DomainExceptionCode {

    NOT_FOUND_DOCUMENT(HttpStatus.NOT_FOUND, "검색된 문서가 없습니다.");

    private final HttpStatus status;
    private final String message;
}
