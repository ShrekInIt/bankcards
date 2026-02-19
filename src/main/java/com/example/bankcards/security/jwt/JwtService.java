package com.example.bankcards.security.jwt;

import com.example.bankcards.dto.JwtAuthenticationDto;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtService {
    @Value("${spring.security.jwt.secret}")
    private String jwtSecret;

    public JwtAuthenticationDto generateAuthToken(String email, String role) {
        return new JwtAuthenticationDto(generateJwtToken(email, role), generateRefreshToken(email, role));
    }

    public JwtAuthenticationDto refreshBaseToken(String email, String role, String refreshToken) {
        return new JwtAuthenticationDto(generateJwtToken(email, role), refreshToken);
    }

    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    public boolean validateJwtToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return true;
        }catch (ExpiredJwtException e){
            log.error("Expired JWT token {}", e.getMessage());

        }catch (UnsupportedJwtException e){
            log.error("Unsupported JWT token {}", e.getMessage());
        }
        catch (MalformedJwtException e){
            log.error("Malformed JWT token {}", e.getMessage());
        } catch (SecurityException e){
            log.error("Security exception {}", e.getMessage());
        }catch (Exception e){
            log.error("Invalid token {}", e.getMessage());
        }
        return false;
    }

    private String generateJwtToken(String email, String role) {
        Date date = new Date(LocalDateTime.now().plusHours(1).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        return Jwts.builder()
                .subject(email)
                .expiration(date)
                .signWith(getSignInKey())
                .claim("role", role)
                .compact();
    }

    private String generateRefreshToken(String email, String role) {
        Date date = new Date(LocalDateTime.now().plusDays(1).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        return Jwts.builder()
                .subject(email)
                .expiration(date)
                .signWith(getSignInKey())
                .claim("role", role)
                .compact();
    }

    private SecretKey getSignInKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}
