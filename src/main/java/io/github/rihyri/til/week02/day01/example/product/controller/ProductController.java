package io.github.rihyri.til.week02.day01.example.product.controller;

import io.github.rihyri.til.week02.day01.example.global.response.ApiResponse;
import io.github.rihyri.til.week02.day01.example.product.dto.ProductCreateRequest;
import io.github.rihyri.til.week02.day01.example.product.dto.ProductResponse;
import io.github.rihyri.til.week02.day01.example.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // 상품 단건 조회
    // [GET] /products/1
    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> getProduct(@PathVariable Long id) {
        ProductResponse product = productService.getProduct(id);

        return ApiResponse.ok(product);
    }

    // 상품 등록
    // [POST] /products
    /*
     * Body:
     * {
     *      "name": "키보드",
     *      "price": 50000
     *  }
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProductResponse> createProduct(
            @Valid @RequestBody ProductCreateRequest request
    ) {
        ProductResponse product = productService.createProduct(request);

        return ApiResponse.ok(product);
    }

    // 상품 삭제
    // [DELETE] /products/1
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteProduct (@PathVariable Long id) {
        productService.deleteProduct(id);

        return ApiResponse.ok();
    }
}
