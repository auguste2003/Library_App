package com.library.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Custom In-Memory Rate Limiter using Token Bucket Algorithm.
 * No external dependencies (Redis/Resilience4j) required.
 */
@Component
@Slf4j
public class RateLimitFilter implements GlobalFilter, Ordered {

    // Config: 10 requests per second
    private static final int REPLENISH_RATE = 10;
    private static final int BURST_CAPACITY = 20;

    // Store user buckets: Key = IP/User, Value = TokenBucket
    private final Map<String, TokenBucket> cache = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String ipAddress = getClientIp(exchange);

        TokenBucket bucket = cache.computeIfAbsent(ipAddress, k -> new TokenBucket(BURST_CAPACITY, REPLENISH_RATE));

        if (bucket.tryConsume(1)) {
            return chain.filter(exchange);
        } else {
            log.warn("Rate limit exceeded for IP: {}", ipAddress);
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            return exchange.getResponse().setComplete();
        }
    }

    private String getClientIp(ServerWebExchange exchange) {
        // Simple IP extraction (can be improved to check X-Forwarded-For)
        if (exchange.getRequest().getRemoteAddress() != null) {
            return exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        }
        return "unknown";
    }

    @Override
    public int getOrder() {
        return -1; // High priority
    }

    /**
     * Simple Token Bucket Implementation
     */
    private static class TokenBucket {
        private final int capacity;
        private final int refillRate;
        private final AtomicInteger tokens;
        private volatile long lastRefillTimestamp;

        public TokenBucket(int capacity, int refillRate) {
            this.capacity = capacity;
            this.refillRate = refillRate;
            this.tokens = new AtomicInteger(capacity);
            this.lastRefillTimestamp = Instant.now().getEpochSecond();
        }

        public synchronized boolean tryConsume(int numTokens) {
            refill();
            if (tokens.get() >= numTokens) {
                tokens.addAndGet(-numTokens);
                return true;
            }
            return false;
        }

        private void refill() {
            long now = Instant.now().getEpochSecond();
            long timeElapsed = now - lastRefillTimestamp;

            if (timeElapsed > 0) {
                int tokensToAdd = (int) (timeElapsed * refillRate);
                if (tokensToAdd > 0) {
                    int newLevel = Math.min(capacity, tokens.get() + tokensToAdd);
                    tokens.set(newLevel);
                    lastRefillTimestamp = now;
                }
            }
        }
    }
}
