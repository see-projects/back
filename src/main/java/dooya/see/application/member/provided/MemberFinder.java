package dooya.see.application.member.provided;

import dooya.see.domain.member.Member;
import dooya.see.domain.shared.Email;

/**
 * 회원 조회 Primary Port
 */
public interface MemberFinder {

    /**
     * 회원 ID로 회원 정보를 조회합니다.
     */
    Member find(Long memberId);

    /**
     * 이메일 주소로 회원 정보를 조회합니다.
     */
    Member findByEmail(Email email);
}