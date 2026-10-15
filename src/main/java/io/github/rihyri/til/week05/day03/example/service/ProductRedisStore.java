package io.github.rihyri.til.week05.day03.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.rihyri.til.week05.day03.example.dto.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductRedisStore {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final Duration TTL = Duration.ofMinutes(10);

    private static final String PENDING_QUEUE = "day03:product:pending";

    // 상품마다 다른 Redis Key를 생성한다.
    private String key (Long id) {
        return "day03:product:" + id;
    }

    // Redis에 저장된 상품 조회
    public Product get (Long id) {

        String json = redisTemplate.opsForValue().get(key(id));

        if (json == null) return null;

        try {
            return objectMapper.readValue(json, Product.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Redis 역직렬화 실패");
        }
    }

    // Redis에 상품 저장 (TTL 10분)
    public void put (Product product) {

        try {
            String json
                    = objectMapper.writeValueAsString(product);

            redisTemplate.opsForValue().set(
                    key(product.id()),
                    json,
                    TTL
            );
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Redis 역직렬화 실패", e);
        }
    }

    // 특정 상품의 캐시 삭제
    public void evict (Long id) {
        redisTemplate.delete(key(id));
    }

    // Write-back을 위한 Redis 작업 대기열에 추가
    public void enqueue (Product product) {

        try {
            String json =
                    objectMapper.writeValueAsString(product);

            redisTemplate.opsForList()
                    .leftPush(PENDING_QUEUE, json);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("대기열 등록 실패", e);
        }
    }

    // Redis 대기열에 있는 가장 오래된 작업 가져오기
    public Product poll () {

        String json = redisTemplate.opsForList().rightPop(PENDING_QUEUE);

        if (json == null) return null;

        try {
            return objectMapper.readValue(json, Product.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("대기열 변환 실패", e);
        }
    }
}
