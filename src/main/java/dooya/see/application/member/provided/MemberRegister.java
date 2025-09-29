package dooya.see.application.member.provided;

import dooya.see.domain.member.Member;
import dooya.see.domain.member.dto.MemberInfoUpdateRequest;
import dooya.see.domain.member.dto.MemberRegisterRequest;

/**
 * 회원 등록 및 수정 관련 주요 포트를 정의하는 인터페이스
 */
public interface MemberRegister {
    /**
     * 회원을 등록합니다.
     *
     * @param registerRequest 회원 등록을 위한 요청 객체
     * @return 등록된 회원 객체
     * @throws IllegalArgumentException 요청 정보가 유효하지 않을 경우
     */
    Member register(MemberRegisterRequest registerRequest);

    /**
     * 주어진 회원 ID를 사용하여 회원을 비활성화합니다.
     *
     * @param memberId 비활성화할 회원의 ID
     * @return 비활성화된 회원 객체
     * @throws IllegalArgumentException 회원 ID가 유효하지 않거나 찾을 수 없는 경우
     */
    Member deactivate(Long memberId);

    /**
     * 회원 정보를 업데이트합니다.
     *
     * @param id 수정할 회원의 ID
     * @param memberInfoUpdateRequest 회원 정보 업데이트 요청 객체
     * @return 업데이트된 회원 객체
     * @throws IllegalArgumentException 활성화된 회원이 아니거나 유효하지 않은 정보를 제공한 경우
     */
    Member updateInfo(Long id, MemberInfoUpdateRequest memberInfoUpdateRequest);
}