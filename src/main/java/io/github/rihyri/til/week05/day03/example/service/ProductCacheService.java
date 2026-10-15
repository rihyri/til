package io.github.rihyri.til.week05.day03.example.service;

import io.github.rihyri.til.week05.day03.example.dto.Product;
import io.github.rihyri.til.week05.day03.example.store.ProductMemoryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductCacheService {

    private final ProductMemoryStore memoryStore;
    private final ProductRedisStore redisStore;


    //  =========================================
    //  1. Cache-aside : 캐시 우선 조회
    //  =========================================
    public Product findById (Long id) {

        // 1. Redis에 캐시가 있는지 확인
        Product cached = redisStore.get(id);

        if (cached != null) {
            log.info("[Cache Hit] 상품 ID: {}", id);
            return cached;
        }

        // 2. 캐시가 없다면 원본 저장소 조회
        log.info("[Cache Miss] 상품 ID: {}", id);

        Product product = memoryStore.findById(id);

        if (product == null) {
            throw new IllegalArgumentException("상품을 찾을 수 없습니다.");
        }

        // 3. 조회한 상품을 Redis에 저장
        redisStore.put(product);

        return product;
    }

    //  =========================================
    //  2. Write-through : 양쪽에 저장
    //  =========================================
    public void saveWriteThrough (Product product) {

        // 1. 원본 저장소에 상품 저장
        memoryStore.save(product);

        // 2. Redis 캐시도 함께 갱신
        redisStore.put(product);

        log.info("[Write-through] 저장 및 캐시 갱신 완료");
    }

    //  =========================================
    //  3. Write-back : Redis에 먼저 저장
    //  =========================================
    public void saveWriteBack (Product product) {

        // 1. Redis에 최신 상품 데이터를 저장
        redisStore.put(product);

        // 2. 나중에 반영한 작업을 Redis 대기열에 등록
        redisStore.enqueue(product);

        // MemoryStore 저장은 Worker가 나중에 수행한다.
        log.info("[Write-back] Redis 저장 및 동기화 예약 완료");
    }

    //  =========================================
    //  4. Write-around : 원본 저장 후 캐시 삭제
    //  =========================================
    public void saveWriteAround (Product product) {

        // 1. 원본 저장소에 상품 저장
        memoryStore.save(product);

        // 2. 오래된 캐시가 남지 않도록 삭제
        redisStore.evict(product.id());

        log.info("[Write-around] 원본 저장 및 캐시 삭제 완료");
    }
}
