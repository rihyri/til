package io.github.rihyri.til.week05.day01.example.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest (
    @NotBlank
    String email,

    @NotBlank
    String password
) {
}
