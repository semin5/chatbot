package com.rocket.chatbot.repository;

import com.rocket.chatbot.domain.Conversation;
import com.rocket.chatbot.dto.ConversationDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    @Query("select c from Conversation c")
    List<ConversationDto> findAllConversations();
}
