package io.github.rihyri.til.week02.day03.example.product;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ProductDto {

    private final String name;
    private final BigDecimal price;
    private final int stock;

    // 생성자를 기반으로 QProductDTO 자동 생성
    @QueryProjection
    public ProductDto(String name, BigDecimal price, int stock) {
        this.name = name;
        this.price = price;
        this.stock = stock;
    }
}
