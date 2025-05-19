package dooya.see.auth.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import dooya.see.common.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * {@code CustomAuthenticationEntryPoint} 클래스는
 * 인증되지 않은 사용자가 보호된 자원에 접근할 때
 * Spring Security의 AuthenticationEntryPoint를 구현하여
 * 커스텀 JSON 에러 응답을 반환하는 진입점 핸들러입니다.
 *
 * <p>401 상태 코드와 함께 "인증에 실패하였습니다. 다시 로그인 해주세요." 메시지를 JSON으로 응답합니다.
 *
 * 이는 주로 토큰이 없거나 유효하지 않을 때 발생하는 상황을 처리합니다.
 *
 * @author dooya
 */
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        String result = objectMapper.writeValueAsString(ErrorResponse.of("인증에 실패하였습니다. 다시 로그인 해주세요."));
        response.setStatus(401);
        response.getWriter().write(result);
    }
}
