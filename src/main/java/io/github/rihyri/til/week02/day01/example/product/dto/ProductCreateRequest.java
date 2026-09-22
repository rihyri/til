package io.github.rihyri.til.week02.day01.example.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ProductCreateRequest {

    // 상품명은 비어있으면 안 된다. (NOT NULL, NOT BLANK)
    @NotBlank(message = "상품명은 필수입니다.")
    private String name;

    // 가격은 반드시 값이 있어야 한다.
    @NotNull(message = "상품 가격은 필수입니다.")
    @Min(value = 1, message = "상품 가격은 1원 이상이어야 합니다.")
    private Long price;
}
