package com.rocket.chatbot.service;

import com.rocket.chatbot.domain.Conversation;
import com.rocket.chatbot.domain.User;
import com.rocket.chatbot.dto.ChatRequest;
import com.rocket.chatbot.dto.ConversationDto;
import com.rocket.chatbot.dto.MessageDto;
import com.rocket.chatbot.dto.OpenAIMessage;
import com.rocket.chatbot.exception.BusinessException;
import com.rocket.chatbot.repository.ConversationRepository;
import com.rocket.chatbot.domain.Message;
import com.rocket.chatbot.repository.MessageRepository;
import com.rocket.chatbot.exception.ErrorCode;
import com.rocket.chatbot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final OpenAIService openAIService;
    private final StringRedisTemplate redis;
    private static final Duration LAST_CONV_TTL = Duration.ofHours(6);

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

    @Transactional
    public SseEmitter createChatCompletionStream(ChatRequest request) {
        SseEmitter emitter = new SseEmitter(60000L);

        Long userId = 1L;
        Long conversationId = resolveConversationId(request.getConversationId(), userId);

        Conversation conversation = getOrCreateConversation(conversationId, userId, request.getMessage());

        redis.opsForValue().set(
                lastConvKey(userId),
                String.valueOf(conversation.getId()),
                LAST_CONV_TTL
        );

        try {
            emitter.send(SseEmitter.event()
                    .name("meta")
                    .data(Collections.singletonMap("conversationId", conversation.getId())));
        } catch (IOException e) {
            emitter.completeWithError(e);
            return emitter;
        }

        messageRepository.save(new Message("user", request.getMessage(), conversation));

        List<OpenAIMessage> openAIMessages =
                buildOpenAiContextMessages(conversation.getId());

        StringBuilder gatheredContent = new StringBuilder();

        openAIService.ChatStream(openAIMessages)
                .subscribe(
                        content -> {
                            if (content != null) {
                                gatheredContent.append(content);
                                try {
                                    Object eventData = Collections.singletonMap("text", content);
                                    emitter.send(SseEmitter.event()
                                            .name("token")
                                            .data(eventData));
                                } catch (IOException e) {}
                            }
                        },
                        streamError -> {
                            emitter.completeWithError(streamError);
                        },
                        () -> {
                            try {
                                emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                                emitter.complete();

                                String fullContent = gatheredContent.toString();
                                if (!fullContent.isEmpty()) {
                                    messageRepository.save(new Message("assistant", fullContent, conversation));
                                }
                            } catch (IOException e) {}
                        }
                );

        return emitter;
    }

    private Long resolveConversationId(Long id, Long userId) {

        if(id != null) return id;

        String cached = redis.opsForValue().get(lastConvKey(userId));

        if (cached == null || cached.isBlank()) return null;
        try{
            return Long.parseLong(cached);
        } catch (BusinessException e){
            redis.delete(lastConvKey(userId));
            return null;
        }
    }

    @Transactional
    public Conversation getOrCreateConversation(Long conversationId, Long userId, String firstMessage) {
        if (conversationId == null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
            String title = firstMessage.length() > 50
                    ? firstMessage.substring(0, 50)
                    : firstMessage;
            return conversationRepository.save(new Conversation(user, title));
        }

        return conversationRepository.findByIdAndUser_Id(conversationId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    }

    public List<OpenAIMessage> buildOpenAiContextMessages(Long conversationId) {
        List<Message> messages = messageRepository.findTop10ByConversationIdOrderByCreatedAtDesc(conversationId);
        Collections.reverse(messages);

        List<OpenAIMessage> openAIMessages = new ArrayList<>();
        for (Message message : messages) {
            openAIMessages.add(new OpenAIMessage(message.getRole(), message.getContent()));
        }
        return openAIMessages;
    }

    private String lastConvKey(Long userId) {
        return "chat:lastConversation:" + userId;
    }
}
