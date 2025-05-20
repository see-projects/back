package dooya.see.user.application.dto;

/**
 * {@code PasswordUpdateResult}는 사용자 비밀번호 변경 작업에 대한
 * 결과 메시지를 전달하기 위한 DTO입니다.
 *
 * <p>비즈니스 로직 수행 후 클라이언트에 성공 여부나 메시지를 전달하는 데 사용됩니다.
 *
 * <p>예: "비밀번호가 성공적으로 변경되었습니다."
 *
 * @author dooya
 */
public record PasswordUpdateResult(
        String message
) {
}
