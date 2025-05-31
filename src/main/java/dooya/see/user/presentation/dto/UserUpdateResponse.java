package dooya.see.user.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * {@code UserUpdateResponse} 클래스는
 * 사용자 정보 업데이트 요청 처리 후 반환되는 응답 데이터 객체입니다.
 *
 * <p>현재는 변경된 닉네임 정보를 포함하며,
 * 프레젠테이션 계층에서 클라이언트에게 전달됩니다.
 *
 * @author dooya
 */
public record UserUpdateResponse(

        @Schema(description = "업데이트 된 사용자 닉네임", example = "See")
        String nickName
) {
}
