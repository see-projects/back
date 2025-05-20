package dooya.see.user.presentation.dto;

import lombok.Builder;

/**
 * {@code PasswordUpdateResponse} 레코드는
 * 사용자 정보 업데이트 요청 처리 후 반환되는 응답 메시지 입니다.
 *
 * <p>현재는 변경후 메시지를 포함하며,
 * 프레젠테이션 계층에서 클라이언트에게 전달됩니다.
 *
 * @author dooya
 */
@Builder
public record PasswordUpdateResponse(
        String message
) {
}
