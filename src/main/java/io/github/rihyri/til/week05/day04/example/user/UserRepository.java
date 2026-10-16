package io.github.rihyri.til.week05.day04.example.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Data JPA가 메서드 이름을 분석하여
    // 이메일로 회원을 검색하는 쿼리를 생성한다.
    Optional<User> findByEmail (String email);

    // 회원가입 시 이메일 중복 여부를 확인합니다.
    boolean existsByEmail (String email);
}
