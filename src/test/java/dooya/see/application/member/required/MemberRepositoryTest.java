package dooya.see.application.member.required;

import dooya.see.domain.member.Member;
import dooya.see.domain.member.MemberStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static dooya.see.domain.member.MemberFixture.createMemberRegisterRequest;
import static dooya.see.domain.member.MemberFixture.createPasswordEncoder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
public record MemberRepositoryTest(MemberRepository memberRepository, EntityManager entityManager) {
    @Test
    void 회원_생성_시_ID가_자동_생성되고_영속화_후_조회가_가능하다() {
        Member member = Member.register(createMemberRegisterRequest(), createPasswordEncoder());

        assertThat(member.getId()).isNull();

        memberRepository.save(member);

        assertThat(member.getId()).isNotNull();

        entityManager.flush();
        entityManager.clear();

        var found = memberRepository.findById(member.getId()).orElseThrow();
        assertThat(found.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        assertThat(found.getDetail().getRegisteredAt()).isNotNull();
    }

    @Test
    void 동일한_이메일로_회원_저장_시_데이터_무결성_예외가_발생한다() {
        Member member = Member.register(createMemberRegisterRequest(), createPasswordEncoder());
        memberRepository.save(member);

        Member member2 = Member.register(createMemberRegisterRequest(), createPasswordEncoder());
        assertThatThrownBy(() -> memberRepository.save(member2))
            .isInstanceOf(DataIntegrityViolationException.class);
    }
}
