package com.tarpha.torrssen2.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.Data;

@Entity
@Data
public class User {
    @Id
    @GeneratedValue
    private Long id;
 
    @Column(nullable = false, unique = true)
    private String username;
 
    private String password;
}