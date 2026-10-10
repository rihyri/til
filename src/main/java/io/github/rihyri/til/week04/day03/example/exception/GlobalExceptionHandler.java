package io.github.rihyri.til.week04.day03.example.exception;

import io.github.rihyri.til.week04.day03.example.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponse<Void>> handleDomainException (DomainException e) {

        DomainExceptionCode errorCode = e.getErrorCode();

        return ResponseEntity
            .status(errorCode.getStatus())
            .body(
                ApiResponse.fail(
                    errorCode.getMessage()
                )
            );
    }
}
