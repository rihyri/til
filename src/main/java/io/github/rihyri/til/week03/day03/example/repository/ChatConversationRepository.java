package io.github.rihyri.til.week03.day03.example.repository;

import io.github.rihyri.til.week03.day03.example.entity.ChatConversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChatConversationRepository extends JpaRepository<ChatConversation, UUID> {
}
