package dooya.see.member.infrastructure;

import dooya.see.member.domain.Role;
import dooya.see.member.domain.Member;
import dooya.see.member.domain.MemberRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Builder
public class MemberRepositoryImpl implements MemberRepository {

    private final MemberJpaRepository memberJpaRepository;

    @Override
    public Optional<Member> findById(Long id) {
        return memberJpaRepository.findById(id);
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        return memberJpaRepository.findByEmail(email);
    }

    @Override
    public List<Member> findByRole(Role role) {
        return memberJpaRepository.findByRole(role);
    }

    @Override
    public Member save(Member member) {
        return memberJpaRepository.save(member);
    }
}
