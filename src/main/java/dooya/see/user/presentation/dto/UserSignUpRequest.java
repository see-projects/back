package dooya.see.user.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

/**
 * {@code UserSignUpRequest} 클래스는
 * 사용자 회원가입을 위한 클라이언트 요청 데이터 객체입니다.
 *
 * <p>이 객체는 입력 값 검증을 위해 Jakarta Bean Validation 어노테이션을 사용하며,
 * 회원가입에 필요한 이메일, 이름, 비밀번호, 닉네임 정보를 포함합니다.
 *
 * <p>유효성 검증 실패 시, 지정한 메시지와 함께 예외가 발생합니다.
 *
 * @author dooya
 */
@Builder(toBuilder = true)
public record UserSignUpRequest(
        @NotBlank(message = "이메일은 비어 있을 수 없습니다")
        @Email
        String email,

        @NotBlank(message = "이름은 비어 있을 수 없습니다")
        String name,

        @NotBlank(message = "비밀번호는 비어 있을 수 없습니다")
        String password,

        @NotBlank(message = "닉네임은 비어 있을 수 없습니다")
        String nickName
) {
}
