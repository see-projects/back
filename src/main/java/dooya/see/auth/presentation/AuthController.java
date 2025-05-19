package dooya.see.auth.presentation;

import dooya.see.auth.application.service.AuthService;
import dooya.see.auth.application.dto.LoginCommand;
import dooya.see.auth.application.dto.LoginResult;
import dooya.see.auth.presentation.dto.AuthDtoMapper;
import dooya.see.auth.presentation.dto.LoginRequest;
import dooya.see.auth.presentation.dto.LoginResponse;
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

/**
 * {@code AuthController} 클래스는
 * 인증 관련 HTTP 요청을 처리하는 REST 컨트롤러입니다.
 *
 * <p>/api/auth 경로로 들어오는 요청을 처리하며,
 * 특히 로그인 요청을 받아 {@link AuthService}를 통해 인증을 수행합니다.
 * 로그인 성공 시, JWT 토큰을 쿠키에 담아 응답으로 전달합니다.
 *
 * <p>쿠키 관련 설정은 외부 설정 값으로 주입받아 사용합니다.
 *
 * @author dooya
 */
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
