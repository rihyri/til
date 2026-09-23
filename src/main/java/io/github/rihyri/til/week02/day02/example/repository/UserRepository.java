package io.github.rihyri.til.week02.day02.example.repository;

import io.github.rihyri.til.week02.day02.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}
