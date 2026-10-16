package io.github.rihyri.til.week05.day04.example.auth.dto;

import io.github.rihyri.til.week05.day04.example.security.CustomUserDetails;
import io.github.rihyri.til.week05.day04.example.user.UserRole;

public record LoginResponse (
        Long userId, String email, UserRole role
) {

    /*
     *  인증에 성공한 사용자 정보를 응답 DTO로 변환한다.
     *  비밀번호는 반환하지 않는다.
     */
    public static LoginResponse from(CustomUserDetails details) {
        return new LoginResponse(
                details.getUserId(),
                details.getEmail(),
                details.getRole()
        );
    }
}