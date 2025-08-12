package dooya.see.member.application.dto;

import dooya.see.member.domain.Member;
import dooya.see.member.domain.Role;

/**
 * {@code UserResult} 레코드는 사용자 조회 결과를
 * 표현하는 데이터 전달 객체(DTO)입니다.
 *
 * <p>도메인 {@link Member} 객체에서 필요한
 * 정보를 추출하여, 프레젠테이션 계층이나 서비스 계층에서
 * 전달할 목적으로 사용됩니다.
 *
 * <p>불변(immutable) 객체로 설계되어 데이터 무결성을 보장합니다.
 *
 * @author dooya
 */
public record MemberResult(
        Long id,
        String email,
        String name,
        String nickName,
        String profileImageUrl,
        Role role
) {
}
