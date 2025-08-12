package dooya.see.application.auth;

import dooya.see.domain.auth.AuthApplicationMapper;
import dooya.see.domain.auth.LoginCommand;
import dooya.see.domain.auth.LoginResult;
import dooya.see.application.auth.provided.AuthService;
import dooya.see.application.auth.provided.AuthValidator;
import dooya.see.adapter.JwtUtil;
import dooya.see.domain.member.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtUtil jwtUtil;
    private final AuthValidator authValidator;

    @Override
    public LoginResult login(LoginCommand command) {
        Member member = authValidator.validateEmailAndPassword(command.email(), command.password());

        String accessToken = jwtUtil.createAccessToken(member.getId(), member.getEmail(), member.getRole());

        return AuthApplicationMapper.toResult(member, accessToken);
    }
}
