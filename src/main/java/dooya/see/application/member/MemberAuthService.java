package dooya.see.application.member;

import dooya.see.application.member.provided.MemberAuth;
import dooya.see.application.member.provided.MemberFinder;
import dooya.see.application.member.required.TokenManager;
import dooya.see.domain.member.*;
import dooya.see.domain.member.exception.AuthenticateException;
import dooya.see.domain.shared.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberAuthService implements MemberAuth {
    private final MemberFinder memberFinder;
    private final PasswordEncoder passwordEncoder;
    private final TokenManager tokenManager;

    @Override
    public LoginResult login(MemberAuthRequest memberAuthRequest) {
        Member member = findMemberByEmail(memberAuthRequest.email());

        validateAuthentication(memberAuthRequest, member);

        String token = generateTokenForMember(member);

        return createLoginResult(member, token);
    }

    @Override
    public Member getCurrentMember(String token) {
        String email = extractEmailFromToken(token);
        return findMemberByEmail(email);
    }

    // Authentication 관련 메서드
    private Member findMemberByEmail(String email) {
        return memberFinder.findByEmail(new Email(email));
    }

    private void validateAuthentication(MemberAuthRequest memberAuthRequest, Member member) {
        validatePassword(memberAuthRequest, member);
        validateAccountStatus(member);
    }

    private void validatePassword(MemberAuthRequest memberAuthRequest, Member member) {
        if (!member.verifyPassword(memberAuthRequest.password(), passwordEncoder))
            throw new AuthenticateException("이메일 또는 비밀번호가 일치하지 않습니다");
    }

    private static void validateAccountStatus(Member member) {
        if (member.getStatus() == MemberStatus.DEACTIVATED)
            throw new AuthenticateException("비활성화된 계정입니다");
    }

    // Token 관련 메서드
    private String generateTokenForMember(Member member) {
        return tokenManager.generateToken(member);

    }

    private String extractEmailFromToken(String token) {
        return tokenManager.extractEmailFromToken(token);
    }

    private static LoginResult createLoginResult(Member member, String token) {
        return new LoginResult(member, token);
    }
}
