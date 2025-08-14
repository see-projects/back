package dooya.see.application.member.provided;

import dooya.see.domain.member.Member;
import dooya.see.domain.member.MemberRegisterRequest;

public interface MemberRegister {
    Member register(MemberRegisterRequest registerRequest);

    Member deactivate(Long memberId);
}
