package io.github.rihyri.til.week03.day02.example.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewRequest {

    @NotBlank(message = "상품명은 필수입니다.")
    String productName;

    @NotBlank(message = "리뷰는 필수입니다.")
    String review;
}
