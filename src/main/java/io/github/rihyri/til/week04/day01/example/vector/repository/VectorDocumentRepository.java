package io.github.rihyri.til.week04.day01.example.vector.repository;

import io.github.rihyri.til.week04.day01.example.vector.entity.VectorDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VectorDocumentRepository extends JpaRepository<VectorDocument, UUID> {
}
