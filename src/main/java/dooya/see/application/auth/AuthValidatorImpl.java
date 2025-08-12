package dooya.see.application.auth;

import dooya.see.application.auth.provided.AuthValidator;
import dooya.see.domain.shared.CustomException;
import dooya.see.domain.shared.ErrorCode;
import dooya.see.domain.member.Member;
import dooya.see.application.member.required.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthValidatorImpl implements AuthValidator {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Member validateEmailAndPassword(String email, String password) {
        return memberRepository.findByEmail(email)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_MATCH_LOGIN_INFO));
    }
}
