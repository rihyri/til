package io.github.rihyri.til.week05.day01.example.category.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest (
        @NotBlank(message = "카테고리 이름은 필수입니다.")
        String name, Long parentId
        ) {
}
