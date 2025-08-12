package dooya.see.member.presentation.dto;

import dooya.see.member.application.dto.*;

import java.util.List;

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
public class MemberPresentationMapper {

    public static MemberRegisterCommand toSignCommand(MemberRegisterRequest request) {
        return new MemberRegisterCommand(
                request.email(),
                request.name(),
                request.password(),
                request.nickName());
    }

    public static MemberRegisterResponse toSignResponse(MemberResult memberResult) {
        return new MemberRegisterResponse(
                memberResult.id(),
                memberResult.email(),
                memberResult.name(),
                memberResult.nickName(),
                memberResult.profileImageUrl(),
                memberResult.role()
        );
    }

    public static NickNameUpdateCommand toUpdateCommand(NickNameUpdateRequest request) {
        return new NickNameUpdateCommand(request.nickName());
    }

    public static NickNameUpdateResponse toUpdateResponse(MemberResult memberResult) {
        return new NickNameUpdateResponse(memberResult.nickName());
    }

    public static PasswordUpdateCommand toPasswordUpdateCommand(PasswordUpdateRequest request) {
        return new PasswordUpdateCommand(
                request.currentPassword(),
                request.newPassword()
        );
    }

    public static PasswordUpdateResponse toPasswordUpdateResponse(PasswordUpdateResult result) {
        return new PasswordUpdateResponse(result.message());
    }

    public static MemberResponse toResponse(MemberResult result) {
        return new MemberResponse(
                result.id(),
                result.email(),
                result.name(),
                result.nickName(),
                result.profileImageUrl(),
                result.role()
        );
    }

    public static List<MemberResponse> toResponses(List<MemberResult> results) {
        return results.stream()
                .map(MemberPresentationMapper::toResponse)
                .toList();
    }
}
