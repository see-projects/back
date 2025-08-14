package dooya.see.application.member;

import dooya.see.application.member.provided.MemberRegister;
import dooya.see.domain.member.Member;
import dooya.see.domain.member.MemberRegisterRequest;
import org.springframework.stereotype.Service;

@Service
public class MemberModifyService implements MemberRegister {
    @Override
    public Member register(MemberRegisterRequest registerRequest) {
        return null;
    }
}
