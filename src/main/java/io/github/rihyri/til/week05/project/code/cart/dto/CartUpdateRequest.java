package io.github.rihyri.til.week05.project.code.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CartUpdateRequest {

    @NotNull
    @Min(1)
    private Integer quantity;
}
