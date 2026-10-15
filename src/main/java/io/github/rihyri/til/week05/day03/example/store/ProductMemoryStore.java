package io.github.rihyri.til.week05.day03.example.store;

import io.github.rihyri.til.week05.day03.example.dto.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ProductMemoryStore {

    private final Map<Long, Product> products = new ConcurrentHashMap<>();

    public ProductMemoryStore () {

        // 실습을 위한 초기 상품 데이터
        products.put(1L, new Product(1L, "키보드", 50000));
        products.put(2L, new Product(2L, "마우스", 30000));
        products.put(3L, new Product(3L, "모니터", 200000));
    }

    // 가상의 DB 조회
    public Product findById (Long id) {

        log.info("[Memory] 상품 조회: {}", id);
        return products.get(id);
    }

    // 가상의 DB 저장 또는 수정
    public void save (Product product) {

        log.info("[Memory] 상품 저장: {}", product.id());
        products.put(product.id(), product);
    }

    // 저장 상태 확인용
    public Collection<Product> findAll() {
        return products.values();
    }
}
