package dooya.see.application.member.provided;

import dooya.see.domain.member.Member;

public interface MemberFinder {
    Member find(Long memberId);
}
