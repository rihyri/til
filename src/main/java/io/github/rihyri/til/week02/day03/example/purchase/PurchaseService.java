package io.github.rihyri.til.week02.day03.example.purchase;

import io.github.rihyri.til.week02.day03.example.product.Product;
import io.github.rihyri.til.week02.day03.example.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final ProductRepository productRepository;
    private final PurchaseRepository purchaseRepository;

    @Transactional
    public void purchase(Long productId, int quantity) {

        // 1. 상품 조회 -> 영속 상태
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 없습니다."));

        // 2. 재고 변경 -> Dirty Checking 대상
        // 이미 조회한 product는 영속성 컨텍스트에서 관리하는 객체이므로, 재고를 변경하면 JPA가 변경사항을 감지
        product.decreaseStock(quantity);
        
        // 3. 구매 내역 저장
        // 새롭게 생성한 엔티티는 save()를 호출
        Purchase purchase = new Purchase(product, quantity);
        purchaseRepository.save(purchase);
        
        // 4. 정상 종료 -> Commit
        // 변경 감지로 Product UPDATE 실행
        // 중간에 RuntimeException 발생 시 전체 Rollback
    }
}
