package io.github.rihyri.til.week05.project.code.cart.entity;

import io.github.rihyri.til.week05.project.code.product.entity.Product;
import io.github.rihyri.til.week05.project.code.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@Entity
/*
 *  user_id만 UNIQUE로 지정하면 한 사용자가 상품을 하나만 담을 수 있게 된다.
 *  product_id만 UNIQUE로 지정하면 같은 상품을 여러 사용자가 담을 수 없게 된다.
 *
 *  따라서 두 컬럼을 하나의 조합으로 UNIQUE 처리해야 한다.
 */
@Table(
        name = "carts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_cart_product",
                        columnNames = {"user_id", "product_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    /*
     *  CartItem 여러 개가 하나의 User를 참조한다.
     *
     *  예를 들어 userId=1의 장바구니에 상품 3종류가 있다면
     *  CartItem 행은 3개이지만 모두 같은 User를 참조한다.
     *
     *  fetch Lazy:
     *  CartItem 조회 시 User 객체를 반드시 함께 조회하지 않고,
     *  실제로 필요할 때 로딩하도록 설정한다.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    User user;


    // 상품 하나는 여러 사용자의 장바구니에 담길 수 있다.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    Product product;

    // 장바구니에 담긴 상품 수량
    @Column(nullable = false)
    int quantity;

    @Builder
    private CartItem(User user, Product product, int quantity) {
        this.user = user;
        this.product = product;
        this.quantity = quantity;
    }

    /*
     *  기존 장바구니 수량 증가
     *  예: 기존 2개 + 추가 3개 = 5개
     *
     *  Math.addExact()는 정수 범위를 초과하면 예외를 발생시킨다.
     */
    public void increaseQuantity(int amount) {
        this.quantity = Math.addExact(this.quantity, amount);
    }

    /*
     *  기존 수량을 전달받은 수량으로 변경한다.
     *  예: 기존 5개 -> 변경 요청 2개 -> 최종 2개
     *
     *  Service에 @Transaction이 적용되어 있다면
     *  JPA Dirty Checking을 통해 UPDATE SQL이 실행된다.
     */
    public void changeQuantity(int quantity) {
        this.quantity = quantity;
    }
}
