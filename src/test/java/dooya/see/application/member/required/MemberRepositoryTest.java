package dooya.see.application.member.required;

import dooya.see.domain.member.Member;
import dooya.see.domain.member.MemberStatus;
import dooya.see.domain.shared.Email;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static dooya.see.domain.member.MemberFixture.createMemberRegisterRequest;
import static dooya.see.domain.member.MemberFixture.createPasswordEncoder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
record MemberRepositoryTest(MemberRepository memberRepository, EntityManager entityManager) {
    @BeforeEach
    void setUp() {
        entityManager.clear();
    }

    @Nested
    class 회원_저장 {
        @Test
        void 저장_시_ID가_자동_생성되고_영속화된다() {
            Member member = createMemberForTest();
            assertThat(member.getId()).isNull();

            Member savedMember = memberRepository.save(member);

            assertThatMemberSaved(savedMember);
        }

        @Test
        void 저장_후_조회가_가능하다() {
            Member member = saveAndClearContext(createMemberForTest());

            Member foundMember = memberRepository.findById(member.getId()).orElseThrow();

            assertThatMemberPersisted(foundMember);
        }

        @Test
        void 동일한_이메일로_저장_시_데이터_무결성_예외가_발생한다() {
            saveAndClearContext(createMemberForTest());
            Member duplicateMember = createMemberForTest();

            assertThatThrownBy(() -> {
                memberRepository.save(duplicateMember);
                entityManager.flush();
            })
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        private void assertThatMemberSaved(Member member) {
            assertThat(member.getId()).isNotNull();
            assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        }

        private void assertThatMemberPersisted(Member member) {
            assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
            assertThat(member.getDetail().getRegisteredAt()).isNotNull();
        }
    }

    @Nested
    class 회원_조회 {
        @Test
        void ID로_회원을_조회할_수_있다() {
            Member savedMember = saveAndClearContext(createMemberForTest());

            Member foundMember = memberRepository.findById(savedMember.getId()).orElseThrow();

            assertThat(foundMember.getId()).isEqualTo(savedMember.getId());
        }

        @Test
        void 존재하지_않는_ID로_조회_시_빈_Optional을_반환한다() {
            assertThat(memberRepository.findById(999L)).isEmpty();
        }

        @Test
        void 이메일로_회원을_조회할_수_있다() {
            Member savedMember = saveAndClearContext(createMemberForTest());
            Email email = savedMember.getEmail();

            Member foundMember = memberRepository.findByEmail(email).orElseThrow();

            assertThat(foundMember.getId()).isEqualTo(savedMember.getId());
            assertThat(foundMember.getEmail()).isEqualTo(email);
        }

        @Test
        void 존재하지_않는_이메일로_조회_시_빈_Optional을_반환한다() {
            Email nonExistentEmail = new Email("notfound@example.com");

            assertThat(memberRepository.findByEmail(nonExistentEmail)).isEmpty();
        }
    }

    // 헬퍼 메서드들
    private Member createMemberForTest() {
        return Member.register(createMemberRegisterRequest(), createPasswordEncoder());
    }

    private Member saveAndClearContext(Member member) {
        Member savedMember = memberRepository.save(member);
        flushAndClearContext();
        return savedMember;
    }

    private void flushAndClearContext() {
        entityManager.flush();
        entityManager.clear();
    }
}