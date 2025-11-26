package com.example.demo.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.UUID;

public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
}
