package io.github.rihyri.til.week05.project.code.product.repository;

import io.github.rihyri.til.week05.project.code.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
