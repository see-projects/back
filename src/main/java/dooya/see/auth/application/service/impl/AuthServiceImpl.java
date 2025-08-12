package dooya.see.auth.application.service.impl;

import dooya.see.auth.application.dto.AuthApplicationMapper;
import dooya.see.auth.application.dto.LoginCommand;
import dooya.see.auth.application.dto.LoginResult;
import dooya.see.auth.application.service.AuthService;
import dooya.see.auth.application.service.AuthValidator;
import dooya.see.auth.util.JwtUtil;
import dooya.see.member.domain.Member;
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
