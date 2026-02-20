package com.rocket.chatbot.config;

import com.rocket.chatbot.exception.BusinessException;
import com.rocket.chatbot.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redisTemplate;
    private static final int limit = 10;
    private static final int seconds = 60;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        log.info("RateLimit filter hit: {}", request.getRequestURI());

        String path = request.getRequestURI();
        String apiKey = request.getHeader("X-API-Key");
        String identifier = "key:" + hashApiKey(apiKey);

        if (!path.startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (apiKey == null || apiKey.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        long now = Instant.now().getEpochSecond();
        long windowIndex = now / seconds;
        String key = String.format("%s:%s:%d", "rate_limit", identifier, windowIndex);

        List<Object> result = redisTemplate.execute(new SessionCallback<List<Object>>() {
            @Override
            public <K, V> List<Object> execute(RedisOperations<K, V> operations) throws DataAccessException {
                @SuppressWarnings("unchecked")
                RedisOperations<String, String> stringOps = (RedisOperations<String, String>) operations;
                stringOps.multi();
                stringOps.opsForValue().increment(key);
                stringOps.expire(key, seconds + 10, TimeUnit.SECONDS);
                return stringOps.exec();
            }
        });

        Long count = (result != null && !result.isEmpty() && result.get(0) instanceof Long)
                ? (Long) result.get(0) : null;

        if (count != null && count > limit) {
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("""
              {"success":false,"error":{"code":"RATE_LIMITED","message":"요청 횟수 제한 초과"}}
            """);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String hashApiKey(String apiKey) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(apiKey.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte value : digest) {
                hex.append(String.format("%02x", value));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", e);
        }
    }
}
