package io.github.rihyri.til.week05.project.code.cart.repository;

import io.github.rihyri.til.week05.project.code.cart.entity.CartItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /*
     *  userId와 productId가 모두 일치하는 장바구니 항목을 조회한다.
     *
     *  장바구니 상품 추가:
     *  이미 담긴 상품인지 확인할 때 사용한다.
     *
     *  수량 수정/삭제:
     *  현재 로그인한 사용자의 항목인지 확인하는 데 사용한다.
     */
    Optional<CartItem> findByUser_IdAndProduct_Id(Long userId, Long productId);

    /*
     *  사용자 장바구니 목록 조회
     *
     *  @EntityGraph:
     *  CartItem을 조회할 때 연결된 Product도 함께 로딩하도록 JPA에 조회 계획을 지정한다.
     *
     *  이 덕분에 CartItemResponse에서
     *  product.getName(), product.getPrice()를 사용하더라도
     *  상품마다 추가 SELECT가 발생하는 상황을 방지할 수 있다.
     */
    @EntityGraph(attributePaths = "products")
    List<CartItem> findByUser_IdOrderByIdAsc(Long userId);
}
