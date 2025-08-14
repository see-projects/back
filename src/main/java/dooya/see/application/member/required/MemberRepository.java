package dooya.see.application.member.required;


import dooya.see.domain.member.Member;
import dooya.see.domain.shared.Email;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface MemberRepository extends Repository<Member, Long> {
    Optional<Member> findByEmail(Email email);
}
