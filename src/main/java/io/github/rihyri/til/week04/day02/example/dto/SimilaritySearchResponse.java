package io.github.rihyri.til.week04.day02.example.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import java.util.*;

@Getter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SimilaritySearchResponse {

    // 검색에 사용한 질문
    String query;

    // 실제 검색된 문서 개수
    int resultCount;

    // 검색된 문서 목록
    List<SearchResult> results;

    @Getter
    @Builder
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class SearchResult {

        String id;

        // VectorStore에 저장된 실제 Chunk 내용
        String content;

        // filename, document_id, chunk_index 등의 부가 정보
        Map<String, Object> metadata;
    }
}
