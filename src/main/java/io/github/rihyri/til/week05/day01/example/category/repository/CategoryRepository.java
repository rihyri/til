package io.github.rihyri.til.week05.day01.example.category.repository;

import io.github.rihyri.til.week05.day01.example.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository <Category, Long> {
}
