package com.rocket.chatbot.user;

import com.rocket.chatbot.conversation.Conversation;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long api_key;
    @CreationTimestamp
    private LocalDateTime created_at;
    @CreationTimestamp
    private LocalDateTime updated_at;
    private String login;
    private String password;
    private String username;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Conversation> conversations = new ArrayList<>();
}
