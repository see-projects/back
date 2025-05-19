package dooya.see.user.application.dto;

import dooya.see.user.domain.Role;
import dooya.see.user.domain.User;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * {@code UserApplicationMapper} 클래스는 애플리케이션 계층에서
 * 사용자 관련 커맨드 객체와 도메인 엔티티, 결과 객체 간의 변환을 담당합니다.
 *
 * <p>주요 역할은 다음과 같습니다:
 * <ul>
 *     <li>{@link UserSignUpCommand} 객체를 받아 {@link User} 도메인 엔티티로 변환</li>
 *     <li>비밀번호는 {@link PasswordEncoder}를 통해 암호화하여 엔티티 생성 시 적용</li>
 *     <li>{@link User} 도메인 엔티티를 {@link UserResult} 결과 객체로 변환</li>
 * </ul>
 *
 * <p>이 클래스는 도메인과 애플리케이션 계층 간 데이터 변환 책임만 가지며,
 * 프레젠테이션 계층의 DTO 변환은 별도 Mapper에서 처리합니다.
 *
 * @author dooya
 */
public class UserApplicationMapper {

    public static User toEntity(UserSignUpCommand command, PasswordEncoder passwordEncoder) {
        return User.signUpUser(
                command.email(),
                command.name(),
                passwordEncoder.encode(command.password()),
                command.nickName(),
                Role.of("USER")
        );
    }

    public static UserResult toResult(User user) {
        return new UserResult(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getNickName(),
                user.getRole()
        );
    }
}
