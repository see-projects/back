package dooya.see.application.member;

import dooya.see.application.member.provided.MemberAuth;
import dooya.see.application.member.provided.MemberFinder;
import dooya.see.domain.member.*;
import dooya.see.domain.shared.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService implements MemberAuth {
    private final MemberFinder memberFinder;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResult login(LoginRequest loginRequest) {
        Email email = new Email(loginRequest.email());

        Member member = memberFinder.findByEmail(email);

        validatePassword(loginRequest, member);
        validateAccountStatus(member);

        String token = "abc";

        return new LoginResult(member, token);
    }

    private void validatePassword(LoginRequest loginRequest, Member member) {
        if (!member.verifyPassword(loginRequest.password(), passwordEncoder)) {
            throw new AuthenticateException("비밀번호가 일치하지 않습니다");
        }
    }

    private static void validateAccountStatus(Member member) {
        if (member.getStatus() == MemberStatus.DEACTIVATED) {
            throw new AuthenticateException("비활성화된 계정입니다");
        }
    }
}
