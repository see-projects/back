package dooya.see.auth.presentation;

import dooya.see.auth.application.AuthService;
import dooya.see.auth.application.LoginCommand;
import dooya.see.auth.application.LoginResult;
import dooya.see.auth.util.CookieUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    @Value("${cookie.valid-time}")
    private long COOKIE_VALID_TIME;

    @Value("${cookie.name}")
    private String COOKIE_NAME;

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> userLogin(@Valid @RequestBody LoginRequest request) {
        LoginCommand command = AuthDtoMapper.toCommand(request);
        LoginResult result = authService.login(command);

        ResponseCookie cookie = CookieUtil.createCookie(COOKIE_NAME, result.accessToken(), COOKIE_VALID_TIME);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(AuthDtoMapper.toResponse(result));
    }
}
