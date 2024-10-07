package com.example.demo.service;

import com.example.demo.entity.User;

public interface UserService {
    User findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    User saveOrUpdate(User user);
}
