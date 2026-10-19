package io.github.rihyri.til.week05.project.code.cart.controller;

import io.github.rihyri.til.common.response.ApiResponse;
import io.github.rihyri.til.week05.project.code.auth.security.CustomUserDetails;
import io.github.rihyri.til.week05.project.code.cart.dto.CartAddRequest;
import io.github.rihyri.til.week05.project.code.cart.dto.CartItemResponse;
import io.github.rihyri.til.week05.project.code.cart.dto.CartUpdateRequest;
import io.github.rihyri.til.week05.project.code.cart.service.CartQueryService;
import io.github.rihyri.til.week05.project.code.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/carts")
public class CartController {

    private final CartService cartService;
    private final CartQueryService cartQueryService;

    /*
     *  1. 장바구니 목록 조회
     *
     *  @AuthenticationPrincipal:
     *  Spring Security의 현재 Authentication에서 principal 객체를 가져온다.
     *
     *  현재 프로젝트에서는 로그인 후 SpringContext에 CustomUserDetails가 저장되어 있다고 가정한다.
     *  따라서 클라이언트가 userId를 직접 전달하지 않아도 인증된 사용자의 ID를 사용할 수 있다.
     */
    @GetMapping
    public ApiResponse<List<CartItemResponse>> getCartItems(@AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUserId();

        List<CartItemResponse> response = cartQueryService.getCartItems(userId);

        return ApiResponse.ok(response);
    }

    /*
     *  2. 장바구니 상품 추가
     *
     *  요청 예시:
     *  {
     *      "productId": 1,
     *      "quantity": 2
     *  }
     */
    @PostMapping
    public ApiResponse<Void> addItem(
            @AuthenticationPrincipal CustomUserDetails userDetails, @Valid @RequestBody CartAddRequest request
            ) {

        cartService.addItem(userDetails.getUserId(), request);
        return ApiResponse.ok();
    }

    /*
     *  3. 장바구니 상품 수량 수정
     *
     *  예: PATCH /api/carts/1
     *  productId=1인 상품의 장바구니 수량을 변경한다.
     */
    @PatchMapping("/{productId}")
    public ApiResponse<Void> updateQuantity(
            @AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long productId,
            @Valid @RequestBody CartUpdateRequest request
            ) {

        cartService.updateQuantity(userDetails.getUserId(), productId, request);

        return ApiResponse.ok();
    }

    /*
     *  4. 장바구니 상품 삭제
     *
     *  예: DELETE /api/carts/1
     *
     *  현재 로그인 사용자가 담은 productId=1 상품을 장바구니에서 제거한다.
     */
    @DeleteMapping("/{productId}")
    public ApiResponse<Void> deleteItem(
            @AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long productId
    ) {

        cartService.deleteItem(userDetails.getUserId(), productId);

        return ApiResponse.ok();
    }
}
