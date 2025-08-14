package dooya.see.application.member;

import dooya.see.application.member.provided.MemberFinder;
import dooya.see.domain.member.Member;
import org.springframework.stereotype.Service;

@Service
public class MemberQueryService implements MemberFinder {
    @Override
    public Member find(Long memberId) {
        return null;
    }
}
