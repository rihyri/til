package io.github.rihyri.til.week03.day02.example.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * 사용자가 AI에게 질문할 때 사용하는 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatRequest {

    // 사용자가 입력한 질문
    String message;

    // 대화를 구분하기 위한 ID
    // 신규 대화라면 nul로 전달 가능
    String conversationId;
}
