package dooya.see.user.presentation;

import dooya.see.user.application.dto.UserResult;
import dooya.see.user.application.dto.UserSignUpCommand;
import dooya.see.user.application.dto.UserUpdateCommand;

/**
 * {@code UserPresentationMapper} 클래스는
 * 프레젠테이션 계층에서 요청(Request) 및 응답(Response) 객체와
 * 애플리케이션 계층의 커맨드(Command) 및 결과(Result) 객체 간 변환을 담당합니다.
 *
 * <p>주요 역할은 다음과 같습니다:
 * <ul>
 *     <li>클라이언트 요청 객체를 애플리케이션 커맨드 객체로 변환</li>
 *     <li>애플리케이션 결과 객체를 클라이언트 응답 객체로 변환</li>
 * </ul>
 *
 * <p>이를 통해 프레젠테이션 계층과 애플리케이션 계층 간의 의존성을 최소화하며,
 * 객체 변환 책임을 명확히 분리합니다.
 *
 * @author dooya
 */
public class UserPresentationMapper {

    public static UserSignUpCommand toSignCommand(UserSignUpRequest request) {
        return new UserSignUpCommand(
                request.email(),
                request.name(),
                request.password(),
                request.nickName());
    }

    public static UserSignUpResponse toSignResponse(UserResult userResult) {
        return new UserSignUpResponse(
                userResult.id(),
                userResult.email(),
                userResult.name(),
                userResult.nickName(),
                userResult.role()
        );
    }

    public static UserUpdateCommand toUpdateCommand(UserUpdateRequest request) {
        return new UserUpdateCommand(request.nickName());
    }

    public static UserUpdateResponse toUpdateResponse(UserResult userResult) {
        return new UserUpdateResponse(userResult.nickName());
    }
}
