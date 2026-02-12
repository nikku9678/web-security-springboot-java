package com.nikku.web_security.repository;

import com.nikku.web_security.entity.Privilege;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PrivilegeRepository extends JpaRepository<Privilege, String> {
    Optional<Privilege> findByName(String name);
}
