package dooya.see.auth.application.service.impl;

import dooya.see.auth.application.dto.LoginCommand;
import dooya.see.auth.application.dto.LoginResult;
import dooya.see.auth.application.service.AuthService;
import dooya.see.auth.application.service.AuthValidator;
import dooya.see.auth.util.JwtUtil;
import dooya.see.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtUtil jwtUtil;
    private final AuthValidator authValidator;

    @Override
    public LoginResult login(LoginCommand command) {
        User user = authValidator.validateEmailAndPassword(command.email(), command.password());

        String accessToken = jwtUtil.createAccessToken(user.getId(), user.getEmail(), user.getRole());

        return LoginResult.of(user, accessToken);
    }
}
