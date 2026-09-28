package io.github.rihyri.til.week02.day03.example.product;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void changePrice(Long productId) {

        // 1. DB에서 조회하여 영속성 컨텍스트에 저장
        Product product = productRepository.findById(productId).orElseThrow();

        // 2. 엔티티의 필드 변경
        product.changePrice(new BigDecimal("25000"));
        
        // 3. 현재 상태와 스냅샷과 비교
        // 변경된 값이 있으므로 UPDATE SQL 실행
        entityManager.flush();
        
        // 4. 아직 commit된 것은 아님
        // 여기서 예외가 발생하면 UPDATE도 rollback 가능
        // 실제로 flush()를 호출하지 않아도 일반적인 읽기, 쓰기 트랜잭션에서는 Commit 시점에 Flush가 수행된다.
    }
}
