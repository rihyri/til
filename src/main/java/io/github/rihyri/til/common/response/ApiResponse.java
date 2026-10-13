package io.github.rihyri.til.common.response;

public record ApiResponse<T> (boolean success, T data, String message) {

    // 데이터를 포함한 성공 응답
    public static <T> ApiResponse<T> ok (T data) {
        return new ApiResponse<>(true, data, "요청 성공");
    }

    // 데이터가 없는 성공 응답
    public static ApiResponse<Void> ok () {
        return new ApiResponse<>(true, null, "요청 성공");
    }

    // 실패 응답
    public static ApiResponse<Void> fail (String message) {
        return new ApiResponse<>(false, null, message);
    }
}


