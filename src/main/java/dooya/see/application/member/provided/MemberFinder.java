package dooya.see.application.member.provided;

import dooya.see.domain.member.Member;

import java.util.List;

public interface MemberFinder {
    Member find(Long memberId);

    List<Member> findMembers();
}
