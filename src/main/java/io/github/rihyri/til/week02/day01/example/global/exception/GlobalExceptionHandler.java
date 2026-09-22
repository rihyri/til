package io.github.rihyri.til.week02.day01.example.global.exception;

import io.github.rihyri.til.week02.day01.example.global.response.ApiResponse;
import io.github.rihyri.til.week02.day01.example.product.exception.ProductException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Product에서 직접 발생시킨 예외 처리
    // ex)
    // throw new ProductException (
    //      ProductException.PRODUCT_NOT_FOUND
    // );
    @ExceptionHandler(ProductException.class)
    public ResponseEntity<ApiResponse<Void>> handleProductException (ProductException ex) {
        return ApiResponse.fail(
                ex.getHttpStatus(),
                ex.getCode(),
                ex.getMessage()
        );
    }

    // @Valid 검증에 실패했을 때 발생하는 예외 처리
    // ex) @NotBlank, @NotNull, @Min
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException (MethodArgumentNotValidException ex) {

        // validation에서 발생한 여러 에러 메시지를 하나의 문자열로 합친다.
        String errorMessage =
                ex.getBindingResult()
                        .getAllErrors()
                        .stream()
                        .map(
                                DefaultMessageSourceResolvable
                                        ::getDefaultMessage
                        )
                        .collect(
                                Collectors.joining(", ")
                        );

        return ApiResponse.fail(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                errorMessage
        );
    }

    // 위에서 처리하지 못한 예상치 못한 Exception 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
        return ApiResponse.fail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "SERVER_ERROR",
                "서버 오류가 발생하였습니다."
        );
    }
}
