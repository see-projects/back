package dooya.see.adapter.security;

import dooya.see.application.member.required.TokenManager;
import dooya.see.domain.member.AuthenticateException;
import dooya.see.domain.member.Member;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenManager implements TokenManager {
    private final SecretKey key;
    private final long expiration = 24 * 60 * 60 * 1000;

    public JwtTokenManager(@Value("${jwt.secret:myDefaultSecretKeyThatIsLongEnoughForHS256}") String secret) {
        // HMAC-SHA256은 최소 32바이트(256비트) 키 필요
        if (secret.length() < 32) {
            secret = secret + "0".repeat(32 - secret.length());
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    @Override
    public String generateToken(Member member) {
        return Jwts.builder()
                .subject(member.getEmail().address())
                .claim("memberId", member.getId())
                .claim("nickname", member.getNickname())
                .claim("status", member.getStatus().name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    private Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            throw new AuthenticateException("유효하지 않은 토큰입니다");
        }
    }

    @Override
    public String extractEmailFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    @Override
    public Long extractMemberIdFromToken(String token) {
        Claims claims = parseToken(token);
        Object memberId = claims.get("memberId");

        if (memberId instanceof Integer) {
            return ((Integer) memberId).longValue();
        } else if (memberId instanceof Long) {
            return (Long) memberId;
        }

        throw new AuthenticateException("토큰에서 회원 ID를 찾을 수 없습니다");
    }
}
