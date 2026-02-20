package com.rocket.chatbot.repository;

import com.rocket.chatbot.domain.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    Optional<Conversation> findByIdAndUser_Id(Long id, Long userId);
}
