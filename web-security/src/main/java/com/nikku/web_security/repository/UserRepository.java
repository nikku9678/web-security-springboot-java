package com.nikku.web_security.repository;

import com.nikku.web_security.entity.User;
import com.nikku.web_security.entity.type.AuthProviderType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
}
