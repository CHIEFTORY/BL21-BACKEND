package com.bl21.config;

import com.bl21.entity.Role;
import com.bl21.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final List<String> DEFAULT_ROLES = List.of("USER", "ADMIN");

    private final RoleRepository roleRepository;

    public DataInitializer(RoleRepository roleRepository) {

        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {

        for (String roleName : DEFAULT_ROLES) {
            roleRepository
                    .findByName(roleName)
                    .orElseGet(() -> roleRepository.save(new Role(roleName)));
        }
    }
}
