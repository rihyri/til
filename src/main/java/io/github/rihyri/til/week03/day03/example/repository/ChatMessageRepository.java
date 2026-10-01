package io.github.rihyri.til.week03.day03.example.repository;

import io.github.rihyri.til.week03.day03.example.entity.ChatMessage;
import io.github.rihyri.til.week03.day03.example.entity.StatusType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 특정 채팅방에서 현재 활성화되어 있는 메시지만 가져온다
    List<ChatMessage> findByConversation_IdAndStatusOrderByCreatedAtAsc(
            UUID conversationId, StatusType status
    );
}
