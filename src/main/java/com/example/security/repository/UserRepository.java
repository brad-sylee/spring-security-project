package com.example.security.repository;

import com.example.security.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// 1. abstract class ➔ interface 로 변경!
// 2. implements ➔ extends 로 변경!
public interface UserRepository extends JpaRepository<User, String> {

    // abstract 키워드도 필요 없습니다.
    Optional<User> findByEmail(String email);
}