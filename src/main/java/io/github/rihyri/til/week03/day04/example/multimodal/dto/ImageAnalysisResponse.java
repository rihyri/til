package io.github.rihyri.til.week03.day04.example.multimodal.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ImageAnalysisResponse {

    // AI가 이미지를 분석한 결과
    private String result;

    // 업로드한 이미지의 MIME Type
    // ex) image/png, image/jpeg
    private String contentType;

    // 업로드한 이미지 크기 (byte)
    private long fileSize;

    // API 사용 시 소비된 토큰 정보
    private TokenUsage tokenUsage;

    @Getter
    @Builder
    public static class TokenUsage {

        // 입력으로 사용된 토큰 수
        private Integer promptToken;

        // AI가 답변을 생성하면서 사용한 토큰 수
        private Integer completionTokens;

        // 전체 사용 토큰 수
        private Integer totalTokens;
    }
}
