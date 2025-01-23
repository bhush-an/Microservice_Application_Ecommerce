package com.ecommerce.user_service.utils;

import com.ecommerce.user_service.service.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtUtils {

    @Value("${SECRET_KEY}")
    private String jwtSecret;

    @Value("${EXP_TIMEOUT}")
    private String jwtExpiry;

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateJwtToken(Authentication authentication) {
        CustomUserDetails userPrincipal = (CustomUserDetails) authentication.getPrincipal();
        Map<String, Object> customClaims = new HashMap<>();
        customClaims.put("roles", authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .claims(customClaims)
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime() + Long.parseLong(jwtExpiry)))
                .signWith(getSignInKey())
                .compact();
    }

    public String getUsernameFromJwtToken(String token) {
        if (token.startsWith("Bearer ")) {
            return extractClaims(token.substring(7), Claims::getSubject);
        }
        return extractClaims(token, Claims::getSubject);
    }

    public boolean validateJwtToken(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token) != null;
    }

    private <T> T extractClaims(String token, Function<Claims, T> claimsFunction) {
        return claimsFunction
                .apply(
                        Jwts.parser()
                                .verifyWith(getSignInKey())
                                .build()
                                .parseSignedClaims(token)
                                .getPayload()
                );
    }
}
