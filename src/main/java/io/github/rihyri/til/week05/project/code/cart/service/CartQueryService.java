package io.github.rihyri.til.week05.project.code.cart.service;

import io.github.rihyri.til.week05.project.code.cart.dto.CartItemResponse;
import io.github.rihyri.til.week05.project.code.cart.entity.CartItem;
import io.github.rihyri.til.week05.project.code.cart.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartQueryService {

    private final CartItemRepository cartItemRepository;

    /*
     *  @Cacheable: 메서드 실행 전에 Redis캐시를 확인한다.
     *
     *  Cache Hit: Redis에 데이터가 존재하면 이 메서드는 실행되지 않고, 캐시에 저장된 결과가 반환된다.
     *  Cache Miss: Redis에 데이터가 없으면 아래 메서드가 실행된다. 반환 결과는 Redis에 저장된다.
     *
     *  CacheNames(캐시 이름) = "cartItems":
     *
     *  key = "#userId":
     *  메서드의 userId 매개변수를 캐시 키로 사용한다.
     *
     *  예: userId=1 → Redis Key - cartItems::1
     */
    @Cacheable(
            cacheNames = "cartItems",
            key = "#userId"
    )
    @Transactional(readOnly = true)
    public List<CartItemResponse> getCartItems(Long userId) {

        /*
         *  Cache Hit이면 이 메서드가 실행되지 않는다.
         *
         *  따라서 아래 로그가 출력되었다면
         *  실제 메서드 실행이 이루어졌다는 뜻이다.
         *
         *  캐시가 정상 설정되어 있다면 일반적으로 Cache Miss이다.
         */
        log.info("[CACHE MISS] DB 조회 - userId = {}", userId);

        /*
         *  @EntityGraph로 Product를 함께 로딩한다.
         *
         *  EntityGraph가 적용된 Repository를 호출하여
         *  장바구니 상품 목록과 상품 정보를 조회한다.
         */
        List<CartItem> cartItems = cartItemRepository.findByUser_IdOrderByIdAsc(userId);

        /*
         *  JPA Entity를 응답 DTO로 변환한다.
         *
         *  Redis에는 CartItem Entity가 아니라
         *  Serializable을 구현한 CartItemResponse를 저장한다.
         *
         *  ArrayList를 직접 만드는 이유는 기본 JDK 직렬화 방식으로
         *  저장할 리스트 구현체를 명확하게 사용하기 위해서이다.
         */
        List<CartItemResponse> responses = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            responses.add(CartItemResponse.from(cartItem));
        }

        return responses;
    }
}
