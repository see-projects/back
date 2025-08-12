package dooya.see.application.member.required;

import dooya.see.domain.member.Role;
import dooya.see.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberJpaRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    List<Member> findByRole(Role role);
}
