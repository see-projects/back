package dooya.see.application.member.required;


import dooya.see.domain.member.Member;
import dooya.see.domain.member.Profile;
import dooya.see.domain.shared.Email;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface MemberRepository extends Repository<Member, Long> {
    Optional<Member> findByEmail(Email email);

    Member save(Member member);

    Optional<Member> findById(Long memberId);

    @Query("select m from Member m where m.detail.profile = :profile")
    Optional<Member> findByProfile(Profile profile);
}
