package com.rocket.chatbot.service;

import com.rocket.chatbot.domain.Conversation;
import com.rocket.chatbot.dto.ChatRequest;
import com.rocket.chatbot.dto.ConversationDto;
import com.rocket.chatbot.dto.MessageDto;
import com.rocket.chatbot.repository.ConversationRepository;
import com.rocket.chatbot.domain.Message;
import com.rocket.chatbot.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final OpenAIService openAIService;

    @Transactional
    public MessageDto processChat(ChatRequest request) {
        // 1. 대화 조회 또는 생성
        Conversation conversation;
        if (request.getConversationId() != null) {
            conversation = conversationRepository.findById(request.getConversationId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));
        } else {
            conversation = new Conversation();
            conversation.setTitle(request.getMessage().substring(0, Math.min(50, request.getMessage().length())));
            conversation = conversationRepository.save(conversation);
        }

        // 2. 사용자 메시지 저장
        Message userMessage = new Message();
        userMessage.setConversation(conversation);
        userMessage.setRole("user");
        userMessage.setContent(request.getMessage());
        messageRepository.save(userMessage);

        // 3. 이전 대화 컨텍스트 조회 (최근 10개)
        List<Message> contextMessages = messageRepository
                .findTop10ByConversationIdOrderByCreatedAtDesc(conversation.getId());
        Collections.reverse(contextMessages);

        // 4. OpenAI API 호출
        String aiResponse = openAIService.chat(contextMessages);

        // 5. AI 응답 저장
        Message assistantMessage = new Message();
        assistantMessage.setConversation(conversation);
        assistantMessage.setRole("assistant");
        assistantMessage.setContent(aiResponse);
        messageRepository.save(assistantMessage);

        return MessageDto.from(assistantMessage);
    }

    public List<ConversationDto> getAllConversations(){
        return conversationRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Conversation::getCreatedAt).reversed())
                .map(ConversationDto::from)
                .toList();
    }

    public List<ConversationDto> getMessagesByConversationId(Long id){

        Conversation c = conversationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));

        List<Message> messages = messageRepository.findMessagesByConversationId(id);

        return Collections.singletonList(ConversationDto.detail(c, messages));
    }

    public void deleteConversation(Long id){
        conversationRepository.deleteById(id);
    }
}
