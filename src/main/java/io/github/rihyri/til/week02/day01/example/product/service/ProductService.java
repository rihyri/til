package io.github.rihyri.til.week02.day01.example.product.service;

import io.github.rihyri.til.week02.day01.example.product.dto.ProductCreateRequest;
import io.github.rihyri.til.week02.day01.example.product.dto.ProductResponse;
import io.github.rihyri.til.week02.day01.example.product.exception.ProductException;
import io.github.rihyri.til.week02.day01.example.product.exception.ProductExceptionCode;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    // 상품 단건 조회
    public ProductResponse getProduct(Long id) {

        if (id <= 0) {
            throw new ProductException(
                    ProductExceptionCode.PRODUCT_NOT_FOUND
            );
        }

        // Repository는 아직 구현되지 않아 간단한 샘플 데이터 반환
        return new ProductResponse(
                id,
                "샘플 상품",
                10000L
        );
    }

    // 상품 등록
    public ProductResponse createProduct(ProductCreateRequest request) {
        return new ProductResponse(
                1L,
                request.getName(),
                request.getPrice()
        );
    }

    // 상품 삭제
    public void deleteProduct(Long id) {

        if (id < 0) {
            throw new ProductException(
                    ProductExceptionCode.PRODUCT_NOT_FOUND
            );
        }

        // 실제로는 productRepository.delete(...) 등 사용
    }
}
