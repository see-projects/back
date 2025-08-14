package dooya.see.application.member.provided;

import dooya.see.domain.member.Member;
import dooya.see.domain.shared.Email;

public interface MemberFinder {
    Member find(Long memberId);

    Member findByEmail(Email email);
}
