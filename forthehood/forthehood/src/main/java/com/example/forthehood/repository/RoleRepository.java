package com.example.forthehood.repository;

import com.example.forthehood.entity.Role;
import com.example.forthehood.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}
