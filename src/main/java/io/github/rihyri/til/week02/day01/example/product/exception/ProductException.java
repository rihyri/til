package io.github.rihyri.til.week02.day01.example.product.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ProductException extends RuntimeException {

    // HTTP 상태 코드
    private final HttpStatus httpStatus;

    private final String code;

    public ProductException(ProductExceptionCode exceptionCode) {

        // RuntimeExceptino의 message에 ProductExceptionCode의 emssage를 전달한다.
        super(exceptionCode.getMessage());

        this.httpStatus = exceptionCode.getStatus();

        // enum 이름을 문자열로 저장 ex) PRODUCT_NOT_FOUND
        this.code = exceptionCode.name();
    }
}
