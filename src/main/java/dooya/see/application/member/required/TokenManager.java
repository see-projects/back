package dooya.see.application.member.required;

import dooya.see.domain.member.Member;
import io.jsonwebtoken.Claims;

public interface TokenManager {
    String generateToken(Member member);

    Claims parseToken(String token);

    String extractEmailFromToken(String token);
}
