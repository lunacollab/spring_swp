package com.example.demo.service;

import com.example.demo.entity.ERole;
import com.example.demo.entity.Role;

import java.util.Optional;

public interface RoleService {
    Optional<Role> findByRoleName(ERole roleName);
}
