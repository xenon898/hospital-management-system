package com.hospital.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.security")
public class JwtProperties {

    /** Beginner note: keep this same value across all services for local demo. */
    private String secret = "dev-secret-change-me-use-at-least-32-bytes";

    /** Token validity in milliseconds (e.g., 7 days). */
    private long expirationMs = 7L * 24 * 60 * 60 * 1000;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public void setExpirationMs(long expirationMs) {
        this.expirationMs = expirationMs;
    }
}

