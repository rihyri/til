package io.github.rihyri.til.week05.day01.example.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpRequest (
        @NotBlank(message = "이름을 입력해 주세요.")
        String name,

        @NotBlank
        @Email(message = "올바른 이메일을 입력해 주세요.")
        String email,

        @NotBlank
        @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
        String password
) {
}
