package com.ecommerce.api_gateway.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.function.Function;

@Component
@Slf4j
public class JwtUtils {

    @Value("${SECRET_KEY}")
    private String jwtSecret;

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
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

    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())  // Use the same signing key used to create the token
                .build().parseSignedClaims(token)
                .getPayload();
    }
}
