package com.rocket.chatbot.repository;

import com.rocket.chatbot.domain.Message;
import com.rocket.chatbot.dto.MessageDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findTop10ByConversationIdOrderByCreatedAtDesc(Long id);
    List<MessageDto> findMessagesByConversationId(Long id);

}
