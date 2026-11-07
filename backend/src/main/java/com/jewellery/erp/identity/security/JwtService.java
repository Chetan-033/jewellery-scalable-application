package com.jewellery.erp.identity.security;

import com.jewellery.erp.config.JewelleryProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class JwtService {

    private final JewelleryProperties properties;

    public JwtService(JewelleryProperties properties) {
        this.properties = properties;
    }

    public String createToken(String userId, String username, List<String> roles, List<String> permissions) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(properties.getJwt().getExpiryMinutes() * 60);
        return Jwts.builder()
                .subject(userId)
                .claims(Map.of(
                        "username", username,
                        "roles", roles,
                        "permissions", permissions
                ))
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key())
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey key() {
        byte[] bytes = properties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(bytes);
    }
}
