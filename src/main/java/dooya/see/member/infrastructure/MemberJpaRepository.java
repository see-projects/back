package dooya.see.member.infrastructure;

import dooya.see.member.domain.Role;
import dooya.see.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberJpaRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    List<Member> findByRole(Role role);
}
