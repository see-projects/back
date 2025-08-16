package dooya.see.application.member.required;

import dooya.see.domain.member.Member;

public interface TokenManager {
    String generateToken(Member member);

    String extractEmailFromToken(String token);

    Long extractMemberIdFromToken(String token);
}
