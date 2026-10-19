package io.github.rihyri.til.week05.project.code.user.repository;

import io.github.rihyri.til.week05.project.code.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
