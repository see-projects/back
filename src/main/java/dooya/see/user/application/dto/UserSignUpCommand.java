package dooya.see.user.application.dto;

import lombok.Builder;

/**
 * {@code UserSignUpCommand} 레코드는
 * 회원가입 요청에 필요한 사용자 정보를 캡슐화하는
 * 커맨드 객체입니다.
 *
 * <p>회원가입 서비스 메서드에 전달되어, 도메인 객체 생성에 필요한
 * 데이터를 담아 전달하는 역할을 합니다.
 *
 * <p>@Builder 어노테이션이 적용되어, 테스트 코드나
 * 복잡한 객체 생성 시 가독성 좋고 유연한 인스턴스 생성을 지원합니다.
 *
 * @author dooya
 */
@Builder
public record UserSignUpCommand(
        String email,
        String name,
        String password,
        String nickName
) {
}
