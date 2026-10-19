package io.github.rihyri.til.week05.project.code.product.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ProductStatus {

    FOR_SALE("판매 중"),
    STOP_SALE("판매 중지"),
    OUT_OF_STOCK("품절");

    String description;
}
