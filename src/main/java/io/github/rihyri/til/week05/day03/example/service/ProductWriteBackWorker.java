package io.github.rihyri.til.week05.day03.example.service;

import io.github.rihyri.til.week05.day03.example.dto.Product;
import io.github.rihyri.til.week05.day03.example.store.ProductMemoryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductWriteBackWorker {

    private final ProductRedisStore redisStore;
    private final ProductMemoryStore memoryStore;

    // 5초마다 Redis 대기열 확인
    @Scheduled(fixedDelay = 5000)
    public void syncProducts() {

        Product product = redisStore.poll();

        if (product == null) return;

        // Redis에 먼저 저장한 데이터를 나중에 원본에 반영
        memoryStore.save(product);

        log.info("[Write-back] MemoryStore 동기화 완료: {}", product.id());
    }
}
