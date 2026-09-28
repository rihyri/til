package io.github.rihyri.til.week02.day03.example.product;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.*;

// QProduct의 static 필드 product를 바로 사용
import static io.github.rihyri.til.week02.day03.example.product.QProduct.product;

@Repository
@RequiredArgsConstructor
public class ProductQueryRepository {

    private final JPAQueryFactory queryFactory;

    // 1. 동적 조건을 사용한 상품 검색
    public List<ProductDto> search (
            String name, BigDecimal minPrice, BigDecimal maxPrice
    ) {
        return queryFactory
                .select(new QProductDto(
                        product.name,
                        product.price,
                        product.stock
                ))
                .from(product)
                .where(
                        nameContains(name),
                        priceGoe(minPrice),
                        priceLoe(maxPrice)
                )
                .fetch();
    }

    // 2. 상품명 검색 조건
    private BooleanExpression nameContains(String name) {
        return name != null && !name.isBlank()
                ? product.name.contains(name) : null;
    }

    // 3. 최소 가격 조건
    private BooleanExpression priceGoe(BigDecimal minPrice) {
        return minPrice != null
                ? product.price.goe(minPrice) : null;
    }

    // 4. 최대 가격 조건
    private BooleanExpression priceLoe(BigDecimal maxPrice) {
        return maxPrice != null
                ? product.price.loe(maxPrice) : null;
    }

    // 페이징
    public Page<ProductDto> searchPages(String name, Pageable pageable) {
        // 1. 현재 페이지에 해당하는 데이터 조회
        List<ProductDto> content = queryFactory
                .select(new QProductDto(
                        product.name,
                        product.price,
                        product.stock
                ))
                .from(product)
                .where(nameContains(name))
                .orderBy(product.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 2. 전체 데이터 수 조회
        Long total = queryFactory
                .select(product.count())
                .from(product)
                .where(nameContains(name))
                .fetchOne();

        // 3. 페이지 객체 반환
        return new PageImpl<>(
                content,
                pageable,
                total != null ? total : 0L
        );
    }
}
