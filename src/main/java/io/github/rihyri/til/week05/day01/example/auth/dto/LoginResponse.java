package io.github.rihyri.til.week05.day01.example.auth.dto;

public record LoginResponse (
        Long memberId,
        String name,
        String email
) {
}
