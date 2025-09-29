package dooya.see.application.member.provided;

import dooya.see.domain.member.Member;
import dooya.see.domain.shared.Email;

/**
 * 회원 정보를 조회하기 위한 인터페이스
 */
public interface MemberFinder {

    /**
     * 주어진 회원 ID로 회원 정보를 조회합니다.
     *
     * @param memberId 조회할 회원의 ID
     * @return 조회된 회원 객체
     * @throws dooya.see.domain.member.exception.MemberNotFoundException 해당 ID를 갖는 회원이 존재하지 않을 경우
     * @throws IllegalArgumentException ID가 null인 경우
     */
    Member find(Long memberId);

    /**
     * 주어진 이메일을 사용하여 회원 정보를 조회합니다.
     *
     * @param email 조회할 회원의 이메일 주소
     * @return 조회된 회원 객체
     * @throws dooya.see.domain.member.exception.MemberNotFoundException 해당 이메일을 갖는 회원이 존재하지 않을 경우
     * @throws IllegalArgumentException 이메일이 null이거나 올바르지 않은 형식인 경우
     */
    Member findByEmail(Email email);
}