package io.github.rihyri.til.week03.day03.example.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 하나의 Conversation에는 여러 ChatMessage가 존재한다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private ChatConversation conversation;

    // USER / ASSISTANT / SYSTEM / SUMMARY
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatMessageType role;

    // 현재 AI Context에서 사용할 메시지인지 표시
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusType status;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public ChatMessage(
            ChatConversation conversation,
            ChatMessageType role,
            StatusType status,
            String message,
            Integer promptTokens,
            Integer completionTokens,
            Integer totalTokens
    ) {
        this.conversation = conversation;
        this.role = role;
        this.status = status;
        this.message = message;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.totalTokens = totalTokens;
    }

    // 오래된 메시지를 더 이상 Context에 사용하지 않을 때 호출한다.
    public void deactivate() {
        this.status = StatusType.INACTIVE;
    }
}