package com.tarpha.torrssen2.controller;

import com.tarpha.torrssen2.domain.User;
import com.tarpha.torrssen2.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;

@RestController
@RequestMapping(value = "/api/user/")
@Api
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // @CrossOrigin("*")
    @GetMapping(value = "/admin")
    public User getAdmin() {
        return userRepository.findByUsername("torrssen");
    }

    // @CrossOrigin("*")
    @PostMapping(value = "/admin")
    public void setAdmin(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }
}