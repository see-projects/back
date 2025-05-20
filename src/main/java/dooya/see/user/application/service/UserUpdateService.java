package dooya.see.user.application.service;

import dooya.see.user.application.dto.PasswordUpdateCommand;
import dooya.see.user.application.dto.PasswordUpdateResult;
import dooya.see.user.application.dto.UserResult;
import dooya.see.user.application.dto.UserUpdateCommand;

/**
 * {@code UserUpdateService}는 사용자 정보 업데이트 기능을 제공하는
 * 애플리케이션 서비스 인터페이스입니다.
 *
 * <p>특정 사용자의 이메일을 기준으로 {@link UserUpdateCommand}를 받아
 * 사용자 정보를 수정하고, 수정된 결과를 {@link UserResult}로 반환합니다.
 *
 * <p>비즈니스 로직은 구현체에서 처리하며, 인터페이스는 기능 명세 역할을 합니다.
 *
 * @author dooya
 */
public interface UserUpdateService {
    /**
     * 지정된 이메일을 가진 사용자의 닉네임을 업데이트합니다.
     *
     * @param email 닉네임을 변경할 사용자의 이메일
     * @param command 변경할 닉네임 정보가 담긴 {@link UserUpdateCommand}
     * @return 업데이트된 사용자 정보를 담은 {@link UserResult}
     */
    UserResult updateNickName(String email, UserUpdateCommand command);

    /**
     * 지정된 이메일을 가진 사용자의 비밀번호를 업데이트합니다.
     *
     * @param email 비밀번호를 변경할 사용자의 이메일
     * @param command 변경할 비밀번호 정보가 담긴 {@link PasswordUpdateCommand}
     * @return 업데이트된 메시지 정보를 담은 {@link PasswordUpdateResult}
     */
    PasswordUpdateResult updatePassword(String email, PasswordUpdateCommand command);
}
