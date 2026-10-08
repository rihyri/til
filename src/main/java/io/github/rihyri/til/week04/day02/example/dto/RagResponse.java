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
public class RagResponse {

    // LLM의 최종 답변
    String answer;

    // 해당 답변을 생성할 때 참고한 문서 목록
    List<DocumentSource> sources;

    @Getter
    @Builder
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class DocumentSource {

        // 원본 파일 이름
        String filename;

        // VectorStore Document ID
        String documentId;

        // 사용자에게 보여줄 문서 내용 일부
        String preview;
    }
}
