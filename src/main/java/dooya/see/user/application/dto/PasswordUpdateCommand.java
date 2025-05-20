package dooya.see.user.application.dto;

import lombok.Builder;

/**
 * {@code PasswordUpdateCommand}는 사용자 정보 업데이트를 위한
 * 애플리케이션 계층의 커맨드 객체입니다.
 *
 * <p>이 객체는 사용자 비밀번호 변경 요청 시 필요한 데이터를 캡슐화하여
 * 서비스 계층에 전달하는 역할을 합니다.
 *
 * <p>불변 객체로 설계되어 안전하게 데이터를 전달할 수 있습니다.
 *
 * @author dooya
 */
@Builder
public record PasswordUpdateCommand(
        String currentPassword,
        String newPassword
) {
}
