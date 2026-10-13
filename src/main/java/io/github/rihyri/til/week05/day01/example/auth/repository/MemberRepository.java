package io.github.rihyri.til.week05.day01.example.auth.repository;

import io.github.rihyri.til.week05.day01.example.auth.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository <Member, Long> {

    Optional<Member> findByEmail (String email);

    boolean existsByEmail (String email);
}
