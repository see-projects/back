package dooya.see.auth.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import dooya.see.common.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * {@code CustomAccessDeniedHandler} 클래스는
 * Spring Security의 AccessDeniedHandler를 구현하여
 * 인증은 되었으나 권한이 부족한 사용자의 접근 시
 * 커스텀 JSON 형태의 에러 응답을 반환하는 핸들러입니다.
 *
 * <p>403 상태 코드와 함께 적절한 에러 메시지를 JSON으로 응답합니다.
 *
 * @author dooya
 */
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        String result = objectMapper.writeValueAsString(ErrorResponse.of("접근 권한이 없는 사용자입니다."));
        response.setStatus(403);
        response.getWriter().write(result);
    }
}
