package dooya.see.application.member;

import dooya.see.application.member.provided.MemberAuth;
import dooya.see.application.member.provided.MemberFinder;
import dooya.see.application.member.required.TokenManager;
import dooya.see.domain.member.*;
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
        Email email = new Email(memberAuthRequest.email());

        Member member = memberFinder.findByEmail(email);

        validatePassword(memberAuthRequest, member);
        validateAccountStatus(member);

        String token = tokenManager.generateToken(member);

        return new LoginResult(member, token);
    }

    private void validatePassword(MemberAuthRequest memberAuthRequest, Member member) {
        if (!member.verifyPassword(memberAuthRequest.password(), passwordEncoder)) {
            throw new AuthenticateException("이메일 또는 비밀번호가 일치하지 않습니다");
        }
    }

    private static void validateAccountStatus(Member member) {
        if (member.getStatus() == MemberStatus.DEACTIVATED) {
            throw new AuthenticateException("비활성화된 계정입니다");
        }
    }
}
