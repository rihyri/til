package io.github.rihyri.til.week02.day01.example.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * ok () -> 성공 / 데이터 없음
     *
     * ok (data) -> 성공 / 데이터 있음
     *
     * fail(...) -> 실팬
     */

    // 실패했을 때 사용하는 에러 정보
    private Error error;

    // 성공했을 때 반환하는 데이터
    private T data;

    // 성공했지만 반환할 데이터가 없는 경우
    // 예) DELETE /products/1
    public static <T> ApiResponse<T> ok() {
        return ApiResponse.<T>builder()
                .build();
    }

    // 성공했고 반환할 데이터가 있는 경우
    // 예) GET /products/1
    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .data(data)
                .build();
    }

    // 요청이 실패한 경우
    // HTTP Status와 Error 정보를 함께 반환한다.
    public static<T> ResponseEntity<ApiResponse<T>> fail(
            HttpStatus httpStatus,
            String errorCode,
            String errorMessage
    ) {

        ApiResponse<T> response = ApiResponse.<T>builder()
                .error(
                        Error.of(
                                errorCode,
                                errorMessage
                        )
                )
                .build();

        return ResponseEntity
                .status(httpStatus)
                .body(response);
    }

    // 에러 응답에 들어갈 정보
    public record Error(String errorCode, String errorMessage) {

        // Error 객체를 생성하기 위한 메서드
        public static Error of(
                String errorCode,
                String errorMessage
        ) {
            return new Error(
                    errorCode,
                    errorMessage
            );
        }
    }
}
