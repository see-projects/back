package dooya.see.auth.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

/**
 * {@code LoginRequest} 레코드는
 * 로그인 요청 시 클라이언트로부터 전달되는 데이터를 담는 DTO입니다.
 *
 * <p>이메일(email)과 비밀번호(password)를 필수 값으로 가지며,
 * {@link jakarta.validation.constraints.NotBlank} 애노테이션으로
 * 입력값 검증을 수행합니다.
 *
 * <p>이 객체는 컨트롤러에서 클라이언트 요청을 받을 때 사용됩니다.
 *
 * @author dooya
 */
@Builder(toBuilder = true)
public record LoginRequest(
        @NotBlank(message = "이메일은 비어 있을 수 없습니다")
        String email,

        @NotBlank(message = "비밀번호는 비어 있을 수 없습니다")
        String password
) {
}
