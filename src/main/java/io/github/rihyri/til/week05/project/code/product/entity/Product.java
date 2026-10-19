package io.github.rihyri.til.week05.project.code.product.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@Entity
@Table(name = "products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, length = 100)
    String name;

    @Column(columnDefinition = "TEXT")
    String description;

    @Column(nullable = false)
    int price;

    @Column(nullable = false)
    int stock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    ProductStatus status;

    @Builder
    private Product(String name, String description, int price, int stock) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.status = ProductStatus.FOR_SALE;
    }

    public void updateInfo(String name, String description, int price, int stock) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
    }

    public void changeStatus(ProductStatus status) {
        this.status = status;
    }
}
