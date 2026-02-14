package com.nikku.web_security.config;

import com.nikku.web_security.entity.Privilege;
import com.nikku.web_security.entity.Role;
import com.nikku.web_security.entity.User;
import com.nikku.web_security.repository.PrivilegeRepository;
import com.nikku.web_security.repository.RoleRepository;
import com.nikku.web_security.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PrivilegeRepository privilegeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        // ===== Privileges =====
        Privilege userRead = createPrivilegeIfNotFound("USER_READ");
        Privilege userDelete = createPrivilegeIfNotFound("USER_DELETE");
        Privilege userCreate = createPrivilegeIfNotFound("USER_CREATE");

        // ===== Roles =====
        Role roleUser = createRoleIfNotFound("ROLE_USER", Set.of(userRead));
        Role roleAdmin = createRoleIfNotFound("ROLE_ADMIN",
                Set.of(userRead, userDelete, userCreate));

        // ===== Users =====
        createUserIfNotFound("admin", "admin123", "Admin", "One", roleAdmin);

        createUserIfNotFound("nikku", "password", "Nikku", "Das", roleUser);
        createUserIfNotFound("vishal", "password", "Vishal", "Das", roleUser);
        createUserIfNotFound("ritik", "password", "Ritik", "Singh", roleUser);
        createUserIfNotFound("rajesh", "password", "Rajesh", "Kumar", roleUser);
    }

    private Privilege createPrivilegeIfNotFound(String name) {
        return privilegeRepository.findByName(name)
                .orElseGet(() -> privilegeRepository.save(
                        Privilege.builder()
                                .name(name)
                                .build()));
    }

    private Role createRoleIfNotFound(String name, Set<Privilege> privileges) {
        return roleRepository.findByName(name)
                .orElseGet(() -> roleRepository.save(
                        Role.builder()
                                .name(name)
                                .privileges(privileges)
                                .build()));
    }

    private void createUserIfNotFound(String username,
                                      String rawPassword,
                                      String firstName,
                                      String lastName,
                                      Role role) {

        userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .username(username)
                                .password(passwordEncoder.encode(rawPassword))
                                .firstName(firstName)
                                .lastName(lastName)
                                .roles(Set.of(role))
                                .build()
                ));
    }
}
