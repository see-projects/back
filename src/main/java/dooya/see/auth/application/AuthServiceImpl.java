package dooya.see.auth.application;

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
    public LoginResponse login(LoginRequest request) {
        User user = authValidator.validateEmailAndPassword(request.email(), request.password());

        String accessToken = jwtUtil.createAccessToken(user.getId(), user.getEmail(), user.getRole());

        return LoginResponse.from(user, accessToken);
    }
}
