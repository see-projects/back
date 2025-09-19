package dooya.see.application.member.provided;

import dooya.see.domain.member.Member;
import dooya.see.domain.member.MemberInfoUpdateRequest;
import dooya.see.domain.member.MemberRegisterRequest;

/**
 * 회원 등록 및 관리 Primary Port
 */
public interface MemberRegister {
    Member register(MemberRegisterRequest registerRequest);

    Member deactivate(Long memberId);

    Member updateInfo(Long id, MemberInfoUpdateRequest memberInfoUpdateRequest);
}