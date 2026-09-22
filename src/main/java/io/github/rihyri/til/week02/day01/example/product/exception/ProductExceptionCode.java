package io.github.rihyri.til.week02.day01.example.product.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ProductExceptionCode {

    // 상품을 찾지 못한 경우
    PRODUCT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "상품 정보를 찾을 수 없습니다."
    ),

    // 잘못된 가격이 들어온 경우
    INVALID_PRODUCT_PRICE(
            HttpStatus.BAD_REQUEST,
            "상품 가격이 올바르지 않습니다."
    );

    private final HttpStatus status;
    private final String message;
}
