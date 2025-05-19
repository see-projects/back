package dooya.see.user.presentation.dto;

import dooya.see.user.domain.Role;
import lombok.Builder;

/**
 * {@code UserSignUpResponse} 클래스는
 * 사용자 회원가입 처리 후 클라이언트에 반환되는 응답 데이터 객체입니다.
 *
 * <p>회원가입이 완료된 사용자의 ID, 이메일, 이름, 닉네임, 역할 정보를 포함합니다.
 *
 * <p>주로 프레젠테이션 계층에서 사용되며, 외부에 노출할 사용자 정보를 캡슐화합니다.
 *
 * @author dooya
 */
@Builder
public record UserSignUpResponse(
        Long id,
        String email,
        String name,
        String nickName,
        Role role
) {
}
