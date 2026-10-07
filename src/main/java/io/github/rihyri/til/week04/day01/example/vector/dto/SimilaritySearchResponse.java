package io.github.rihyri.til.week04.day01.example.vector.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.*;


@Getter
@Builder
public class SimilaritySearchResponse {

    private String query;
    private int resultCount;
    private List<SearchResult> results;

    @Getter
    @Builder
    public static class SearchResult {

        private String id;
        private String content;
        private Map<String, Object> metadata;
    }
}
