package com.pixely.domain.generation;

import com.pixely.global.config.PixelyProperties;
import com.pixely.global.exception.BusinessException;
import com.pixely.global.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** 사용자별 1분 슬라이딩 윈도우 생성 횟수 제한 (단일 인스턴스 기준, 인메모리) */
@Component
public class GenerationRateLimiter {

    private static final long WINDOW_MS = 60_000;
    private final int limit;
    private final Map<Long, Deque<Long>> history = new ConcurrentHashMap<>();

    public GenerationRateLimiter(PixelyProperties properties) {
        this.limit = properties.ai().rateLimitPerMinute();
    }

    public void acquire(Long userId) {
        if (limit <= 0) return;
        long now = System.currentTimeMillis();
        Deque<Long> q = history.computeIfAbsent(userId, k -> new ArrayDeque<>());
        synchronized (q) {
            while (!q.isEmpty() && now - q.peekFirst() >= WINDOW_MS) q.pollFirst();
            if (q.size() >= limit) {
                long waitSec = Math.max(1, (WINDOW_MS - (now - q.peekFirst())) / 1000);
                throw new BusinessException(ErrorCode.RATE_LIMITED,
                        "이미지 생성은 1분에 %d회까지 가능합니다. %d초 후 다시 시도해 주세요.".formatted(limit, waitSec));
            }
            q.addLast(now);
        }
    }
}
