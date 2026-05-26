package com.example.iisdrugcrm.security;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class TokenBlacklistService {

    private final ConcurrentHashMap<String, Instant> blacklistedTokens = new ConcurrentHashMap<>();

    public void blacklist(String token, Date expiration) {
        if (token == null || token.isBlank() || expiration == null) {
            return;
        }

        blacklistedTokens.put(token, expiration.toInstant());
        cleanupExpiredEntries();
    }

    public boolean isBlacklisted(String token) {
        cleanupExpiredEntries();
        Instant expiration = blacklistedTokens.get(token);
        return expiration != null && expiration.isAfter(Instant.now());
    }

    private void cleanupExpiredEntries() {
        Instant now = Instant.now();
        blacklistedTokens.entrySet().removeIf(entry -> !entry.getValue().isAfter(now));
    }
}