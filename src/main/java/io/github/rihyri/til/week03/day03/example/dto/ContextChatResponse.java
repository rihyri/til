package io.github.rihyri.til.week03.day03.example.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Builder
public record ContextChatResponse (
        String message,
        String conversationId,
        LocalDateTime timestamp,
        TokenUsage tokenUSage
) {

    @Builder
    public record TokenUsage(
            Long promptTokens,
            Long completionTokens,
            Long totalTokens
    ) {
    }
}
