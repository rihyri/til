package io.github.rihyri.til.week05.day03.example.controller;

import io.github.rihyri.til.week05.day03.example.dto.Product;
import io.github.rihyri.til.week05.day03.example.service.ProductCacheService;
import io.github.rihyri.til.week05.day03.example.store.ProductMemoryStore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductCacheController {

    private final ProductCacheService productCacheService;
    private final ProductMemoryStore memoryStore;

    // Cache-aside 조회
    @GetMapping("/{id}")
    public Product findById (@PathVariable Long id) {
        return productCacheService.findById(id);
    }

    // Write-through
    @PutMapping("/write-through")
    public ResponseEntity<Void> writeTrough (@RequestBody Product product) {
        productCacheService.saveWriteThrough(product);
        return ResponseEntity.noContent().build();
    }

    // Write-back
    @PutMapping("/write-back")
    public ResponseEntity<Void> writeBack (@RequestBody Product product) {
        productCacheService.saveWriteBack(product);
        return ResponseEntity.accepted().build();
    }

    // Write-around
    @PutMapping("/write-around")
    public ResponseEntity<Void> writeAround (@RequestBody Product product) {
        productCacheService.saveWriteAround(product);
        return ResponseEntity.noContent().build();
    }

    // 실습용: 가상의 원본 저장소 확인
    @GetMapping("/memory")
    public Collection<Product> findAllMemory() {
        return memoryStore.findAll();
    }
}
