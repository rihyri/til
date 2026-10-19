package io.github.rihyri.til.week05.project.code.cart.service;

import io.github.rihyri.til.common.exception.BusinessException;
import io.github.rihyri.til.common.exception.ErrorCode;
import io.github.rihyri.til.week05.project.code.cart.cache.CartCacheChangedEvent;
import io.github.rihyri.til.week05.project.code.cart.dto.CartAddRequest;
import io.github.rihyri.til.week05.project.code.cart.dto.CartUpdateRequest;
import io.github.rihyri.til.week05.project.code.cart.entity.CartItem;
import io.github.rihyri.til.week05.project.code.cart.repository.CartItemRepository;
import io.github.rihyri.til.week05.project.code.product.entity.Product;
import io.github.rihyri.til.week05.project.code.product.entity.ProductStatus;
import io.github.rihyri.til.week05.project.code.product.repository.ProductRepository;
import io.github.rihyri.til.week05.project.code.user.entity.User;
import io.github.rihyri.til.week05.project.code.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    /*
     *  Spring 이벤트 발행기
     *
     *  장바구니 DB 데이터가 변경되었음을 알리는 CartCacheChangedEvent를 발행한다.
     */
    private final ApplicationEventPublisher eventPublisher;

    // ==================================================
    // 1. 장바구니 상품 추가
    // ==================================================
    @Transactional
    public void addItem(Long userId, CartAddRequest request) {

        // 1. 존재하는 상품인지 확인
        Product product = findProduct(request.getProductId());

        // 2. 판매 가능한 상품인지 확인
        validateProduct(product);

        /*
         *  3. 동일한 사용자가 동일 상품을 이미 담았는지 확인
         *  userId + productId로 하나의 CartItem을 찾는다.
         */
        Optional<CartItem> existingItem =
                cartItemRepository.findByUser_IdAndProduct_Id(userId, request.getProductId());

        if (existingItem.isPresent()) {

            // 4-1. 이미 담긴 상품이면 수량만 증가
            CartItem cartItem = existingItem.get();

            /*
             *  더한 결과를 long으로 계산한다.
             *
             *  int끼리 먼저 더한 후 Long으로 변환하면
             *  int 연산 과정에서 오버플로가 발생할 수 있다.
             */
            long newQuantity = (long) cartItem.getQuantity() + request.getQuantity();

            // 추가 후 수량이 재고보다 많지 않은지 확인
            validateQuantity(product, newQuantity);

            /*
             *  수량 변경
             *
             *  현재 메서드에 @Transactional이 있으모로 CartItem은 영속 상태이다.
             *  JPA Dirty Checking에 으해 커밋 시 UPDATE SQL이 실행된다.
             */
            cartItem.increaseQuantity(request.getQuantity());
        } else {

            // 4-2. 처음 담는 상품이면 CartItem 생성
            validateQuantity(product, request.getQuantity());

            /*
             *  getReferenceById():
             *
             *  실제 User 엔티티 전체가 필요하지 않고 userId로 연관관계만 설정하면 되므로
             *  JPA 참조 객체를 사용한다.
             *
             *  일반적으로 즉시 SELECT를 실행하지 않고 엔티티 참조를 얻을 수 있다.
             *
             *  Spring Security에서 이미 인증된 사용자의 ID를 사용한다고 가정한다.
             */
            User user = userRepository.getReferenceById(userId);

            CartItem cartItem = CartItem.builder()
                    .user(user)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();

            // 새 장바구니 항목 저장
            cartItemRepository.save(cartItem);
        }

        /*
         *  5. 캐시 무효화 이벤트 발행
         *
         *  이 코드 자체는 Redis 캐시를 삭제하지 않는다.
         *  CartCacheInvalidator가 DB 커밋 성공 이후 이벤트를 처리하여 캐시를 삭제한다.
         */
        eventPublisher.publishEvent(
                new CartCacheChangedEvent(userId)
        );

        log.info("장바구니 상품 추가 - userId={}, productId = {}", userId, request.getProductId());
    }

    // ==================================================
    // 2. 장바구니 수량 수정
    // ==================================================
    @Transactional
    public void updateQuantity(Long userId, Long productId, CartUpdateRequest request) {

        /*
         *  userId와 productId를 함께 조회 조건으로 사용한다.
         *
         *  따라서 다른 사용자 장바구니의 CartITem을
         *  임의로 수정하는 것을 방지할 수 있다.
         */
        CartItem cartItem = findCartItem(userId, productId);

        /*
         *  변경할 최종 수량을 확인한다.
         *
         *  수정 API는 수량을 더하는 것이 아니라 요청 값으로 덮어쓴다.
         */
        validateQuantity(cartItem.getProduct(), cartItem.getQuantity());

        // JPA Dirty Checking으로 UPDATE 처리
        cartItem.changeQuantity(request.getQuantity());

        // DB 커밋 성공 이후 해당 사용자 캐시 삭제
        eventPublisher.publishEvent(
                new CartCacheChangedEvent(userId)
        );

        log.info("장바구니 수량 수정 - userId={}, productId={}", userId, productId);
    }

    // ==================================================
    // 3. 장바구니 상품 삭제
    // ==================================================
    @Transactional
    public void deleteItem(Long userId, Long productId) {

        // 현재 사용자의 장바구니 항목만 조회
        CartItem cartItem = findCartItem(userId, productId);

        /*
         *  CartItem만 삭제한다.
         *
         *  CartItem이 참조하는 Product나 User 자체는 삭제하지 않습니다.
         */
        cartItemRepository.delete(cartItem);

        // DB 커밋 성공 후 캐시 삭제
        eventPublisher.publishEvent(
                new CartCacheChangedEvent(userId)
        );

        log.info("장바구니 상품 삭제 - userId={}, product={}", userId, productId);
    }

    // ==================================================
    // 공통 메서드
    // ==================================================

    private Product findProduct(Long productId) {

        return productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.PRODUCT_NOT_FOUND
                ));
    }

    private CartItem findCartItem(Long userId, Long productId) {

        return cartItemRepository
                .findByUser_IdAndProduct_Id(userId, productId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.CART_ITEM_NOT_FOUND
                ));
    }

    private void validateProduct(Product product) {

        if (product.getStatus() != ProductStatus.FOR_SALE) {
            throw new BusinessException(
                    ErrorCode.PRODUCT_NOT_AVAILABLE
            );
        }
    }

    private void validateQuantity(Product product, long quantity) {

        /*
         *  추가/수정 요청 DTO에서 @Min(1)로 검증하지만
         *  Service에서도 유효 범위를 확인한다.
         */
        if (quantity < 1 || quantity > product.getStock()) {
            throw new BusinessException(
                    ErrorCode.CART_QUANTITY_EXCEEDED
            );
        }
    }
}
