package com.libraflow.library.security;

import com.libraflow.library.domain.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private static final String ISSUER = "libraflow-api";

    private final SecretKey signingKey;

    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String base64Secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs
    ) {

        byte[] keyBytes;

        try {
            keyBytes = Decoders.BASE64.decode(base64Secret);
        } catch (RuntimeException ex) {
            throw new IllegalStateException(
                    "JWT_SECRET must be a valid Base64 string",
                    ex
            );
        }

        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET must contain at least 32 bytes (256 bits)"
            );
        }

        if (expirationMs <= 0) {
            throw new IllegalStateException(
                    "JWT_EXPIRATION must be greater than 0"
            );
        }

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    public String generateToken(User user) {

        Instant now = Instant.now();

        Instant expiresAt =
                now.plusMillis(expirationMs);

        return Jwts.builder()
                .issuer(ISSUER)
                .subject(user.getUsername())
                .claim(
                        "role",
                        user.getRole().name()
                )
                .issuedAt(
                        Date.from(now)
                )
                .expiration(
                        Date.from(expiresAt)
                )
                .signWith(signingKey)
                .compact();
    }

    public String extractUsername(String token) {

        return parseClaims(token)
                .getSubject();
    }

    public String extractRole(String token) {

        return parseClaims(token)
                .get(
                        "role",
                        String.class
                );
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    private Claims parseClaims(String token) {

        return Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(ISSUER)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}