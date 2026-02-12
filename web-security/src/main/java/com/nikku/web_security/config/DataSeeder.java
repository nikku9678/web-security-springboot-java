package com.nikku.web_security.config;

import com.nikku.web_security.entity.Privilege;
import com.nikku.web_security.entity.Role;
import com.nikku.web_security.repository.PrivilegeRepository;
import com.nikku.web_security.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PrivilegeRepository privilegeRepository;

    @Override
    public void run(String... args) {

        // 🔹 Privileges
        Privilege userRead =
                createPrivilegeIfNotFound("USER_READ");

        Privilege userDelete =
                createPrivilegeIfNotFound("USER_DELETE");

        Privilege appointmentCreate =
                createPrivilegeIfNotFound("APPOINTMENT_CREATE");

        // 🔹 Roles
        createRoleIfNotFound(
                "ROLE_USER",
                Set.of(userRead)
        );

        createRoleIfNotFound(
                "ROLE_ADMIN",
                Set.of(userRead, userDelete, appointmentCreate)
        );
    }

    private Privilege createPrivilegeIfNotFound(String name) {

        return privilegeRepository.findByName(name)
                .orElseGet(() ->
                        privilegeRepository.save(
                                Privilege.builder()
                                        .name(name)
                                        .build()
                        )
                );
    }

    private void createRoleIfNotFound(
            String name,
            Set<Privilege> privileges) {

        roleRepository.findByName(name)
                .orElseGet(() ->
                        roleRepository.save(
                                Role.builder()
                                        .name(name)
                                        .privileges(privileges)
                                        .build()
                        )
                );
    }
}
