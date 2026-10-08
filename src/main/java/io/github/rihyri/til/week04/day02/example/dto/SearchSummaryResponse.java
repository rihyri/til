package io.github.rihyri.til.week04.day02.example.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchSummaryResponse {

    // 사용자가 입력한 검색어
    String query;

    // 검색 결과를 LLM이 요약한 내용
    String summary;
}
