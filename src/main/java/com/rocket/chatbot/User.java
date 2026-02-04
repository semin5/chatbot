package com.rocket.chatbot;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Getter
@Setter
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long api_key;
    private Timestamp created_at;
    private Timestamp updated_at;
    private String login;
    private String password;
    private String username;
}
