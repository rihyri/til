package io.github.rihyri.til.week01.day02.example.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

@Entity // JPA가 관리하는 Entity
@Getter
@DynamicUpdate  // 변경된 컬럼을 중심으로 UPDATE 쿼리 생성
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // JPA 기본 생성자
@Table(name = "members")
public class Member {

    @Id // Primary Key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String nickname;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @CreationTimestamp  // Entity가 처음 저장될 때 생성 시간 기록
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Member(String nickname, String email) {
        this.nickname = nickname;
        this.email = email;
    }
}
