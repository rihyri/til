package io.github.rihyri.til.week05.day04.example.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 이메일을 로그인 ID로 사용한다.
    @Column(nullable = false, unique = true)
    private String email;

    // 비밀번호 원문이 아닌 BCrypt 해시를 저장한다.
    @Column(nullable = false)
    private String password;

    // 사용자 권한
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    public User (String email, String password, UserRole role) {
        this.email = email;
        this.password = password;
        this.role = role;
    }
}
