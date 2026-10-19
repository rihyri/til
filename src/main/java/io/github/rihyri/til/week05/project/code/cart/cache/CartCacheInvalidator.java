package io.github.rihyri.til.week05.project.code.cart.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class CartCacheInvalidator {

    private final CacheManager cacheManager;

    /*
     *  @TransactionalEventListener
     *  - Spring의 트랜잭션과 연결하여 이벤트를 처리한다.
     *
     *  phase = TransactionPhase.AFTER_COMMIT
     *  - DB 트랜잭션이 성공적으로 커밋된 이후 아래 메서드를 실행한다는 의미
     *  - DB 트랜잭션이 롤백되면 실행되지 않는다.
     *  - 일반적으로 활성 트랜잭션 없이 이벤트가 발행되면
     *    fallbackExecution의 기본값이 false이므로 이 리스너는 실행되지 않는다.
     */
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void evictCartCache(CartCacheChangedEvent event) {

        /*
         *  이전에 설정한 RedisCacheManager에서 CartItems라는 이름의 Cache를 가져온다.
         *  @Cacheable(cacheNames = "cartItems")에서 사용했던 이름과 일치해야 한다.
         */
        Cache cache = cacheManager.getCache("cartItems");

        if (cache == null) {
            throw new IllegalStateException("cartItems 캐시를 찾을 수 없습니다.");
        }

        /*
         *  해당 사용자의 캐시만 삭제한다.
         *
         *  userId=1이면 cartItems::1에 해당하는 캐시가 삭제된다.
         *  다른 사용자의 장바구니 캐시는 삭제하지 않는다.
         */
        cache.evict(event.getUserId());

        log.info("[CACHE EVICT] 장바구니 캐시 삭제 - userId={}", event.getUserId());
    }
}
