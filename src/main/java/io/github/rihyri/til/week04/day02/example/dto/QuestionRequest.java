package io.github.rihyri.til.week04.day02.example.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuestionRequest {

    // 사용자가 입력한 질문
    String question;
}
