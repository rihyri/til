package io.github.rihyri.til.week05.project.code.cart.dto;

import io.github.rihyri.til.week05.project.code.cart.entity.CartItem;
import io.github.rihyri.til.week05.project.code.product.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CartItemResponse {

    private static final long serialVersionUID = 1L;

    private final Long productId;
    private final String productName;
    private final int price;
    private final int quantity;
    private final long totalPrice;

    /*
     *  CartItem 엔티티를 API 응답 객체로 변환한다.
     *
     *  Redis는 Entity 자체를 캐싱하지 않고,
     *  필요한 정보만 가진 응답 DTO를 캐싱한다.
     *
     *  Entity를 직접 캐싱하면 JPA 지연 로딩, 영속성 컨텍스트와의 관계, 불필요한 연관 데이터 직렬화 등
     *  여러 문제가 생길 수 있다.
     */
    public static CartItemResponse from(CartItem cartItem) {

        Product product = cartItem.getProduct();

        return new CartItemResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                cartItem.getQuantity(),
                (long) product.getPrice() * cartItem.getQuantity()
        );
    }
}
