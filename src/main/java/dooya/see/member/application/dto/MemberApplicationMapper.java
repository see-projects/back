package dooya.see.member.application.dto;

import dooya.see.member.domain.Role;
import dooya.see.member.domain.Member;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.stream.Collectors;

/**
 * {@code UserApplicationMapper} 클래스는 애플리케이션 계층에서
 * 사용자 관련 커맨드 객체와 도메인 엔티티, 결과 객체 간의 변환을 담당합니다.
 *
 * <p>주요 역할은 다음과 같습니다:
 * <ul>
 *     <li>{@link MemberRegisterCommand} 객체를 받아 {@link Member} 도메인 엔티티로 변환</li>
 *     <li>비밀번호는 {@link PasswordEncoder}를 통해 암호화하여 엔티티 생성 시 적용</li>
 *     <li>{@link Member} 도메인 엔티티를 {@link MemberResult} 결과 객체로 변환</li>
 * </ul>
 *
 * <p>이 클래스는 도메인과 애플리케이션 계층 간 데이터 변환 책임만 가지며,
 * 프레젠테이션 계층의 DTO 변환은 별도 Mapper에서 처리합니다.
 *
 * @author dooya
 */
public class MemberApplicationMapper {

    public static Member toEntity(MemberRegisterCommand command, PasswordEncoder passwordEncoder, String defaultImageUrl) {
        return Member.signUpUser(
                command.email(),
                command.name(),
                passwordEncoder.encode(command.password()),
                command.nickName(),
                defaultImageUrl,
                Role.of("USER")
        );
    }

    public static MemberResult toResult(Member member) {
        return new MemberResult(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getNickName(),
                member.getProfileImageUrl(),
                member.getRole()
        );
    }

    public static List<MemberResult> toResults(List<Member> members) {
        return members.stream().map(MemberApplicationMapper::toResult).collect(Collectors.toList());
    }
}
