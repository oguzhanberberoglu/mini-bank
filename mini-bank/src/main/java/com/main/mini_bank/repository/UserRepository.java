package com.main.mini_bank.repository;

import java.util.Optional;
import java.util.UUID;

import com.main.mini_bank.model.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
