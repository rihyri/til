package io.github.rihyri.til.week01.day03.example.repository;

import io.github.rihyri.til.week01.day03.example.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository <Member, Long> {

    Optional<Member> findByEmail(String email);
}
