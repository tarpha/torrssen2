package com.tarpha.torrssen2.repository;

import com.tarpha.torrssen2.domain.User;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    public User findByUsername(String username);
    public User findFirstByUsernameNot (String username);
}