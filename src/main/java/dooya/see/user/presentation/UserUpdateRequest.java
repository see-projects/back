package dooya.see.user.presentation;

import lombok.Builder;

/**
 * {@code UserUpdateRequest} 클래스는
 * 사용자 정보 업데이트 요청 시 클라이언트로부터 전달받는 데이터 객체입니다.
 *
 * <p>현재는 닉네임 변경을 위한 단일 필드만 포함하고 있으며,
 * 프레젠테이션 계층에서 사용됩니다.
 *
 * @author dooya
 */
@Builder
public record UserUpdateRequest(
        String nickName
) {
}
