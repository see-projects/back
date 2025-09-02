package dooya.see.application.member.required;

import dooya.see.domain.member.Member;
import dooya.see.domain.member.MemberStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static dooya.see.domain.member.MemberFixture.createMemberRegisterRequest;
import static dooya.see.domain.member.MemberFixture.createPasswordEncoder;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public record MemberRepositoryTest(MemberRepository memberRepository, EntityManager entityManager) {
    @DisplayName("회원 생성 시 ID가 자동 생성되고 영속화 후 조회가 가능하다")
    @Test
    void createMember() {
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

    @DisplayName("동일한 이메일로 회원 저장 시 데이터 무결성 예외가 발생한다")
    @Test
    void duplicateEmailFail() {
        Member member = Member.register(createMemberRegisterRequest(), createPasswordEncoder());
        memberRepository.save(member);

        Member member2 = Member.register(createMemberRegisterRequest(), createPasswordEncoder());
        assertThatThrownBy(() -> memberRepository.save(member2))
            .isInstanceOf(DataIntegrityViolationException.class);
    }
}
