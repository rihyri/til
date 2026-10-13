package io.github.rihyri.til.week05.day01.example.category.dto;

import java.util.*;

public record CategoryResponse (
        Long id,
        String name,
        List<CategoryResponse> children
) {

    public static CategoryResponse of (Long id, String name) {
        return new CategoryResponse(id, name, new ArrayList<>());
    }
}
