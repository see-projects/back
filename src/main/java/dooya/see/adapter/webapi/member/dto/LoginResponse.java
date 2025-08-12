package dooya.see.adapter.webapi.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * {@code LoginResponse} 레코드는
 * 로그인 성공 후 클라이언트에게 반환되는 응답 데이터를 담는 DTO입니다.
 *
 * <p>JWT 액세스 토큰(accessToken)과 함께
 * 사용자 식별자(id), 이메일(email), 이름(name), 닉네임(nickName) 정보를 포함합니다.
 *
 * <p>인증 완료 후, 클라이언트가 사용자 정보를 확인하거나
 * 인증 상태를 유지하는 데 사용됩니다.
 *
 * @author dooya
 */
@Builder
public record LoginResponse(

        @Schema(description = "JWT 엑세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken,

        @Schema(description = "사용자 ID", example = "1")
        Long id,

        @Schema(description = "사용자 이메일", example = "user@example.com")
        String email,

        @Schema(description = "사용자 이름", example = "홍길동")
        String name,

        @Schema(description = "사용자 닉네임", example = "See")
        String nickName
) {
}
