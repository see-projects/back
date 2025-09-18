package dooya.see.application.member.provided;

import dooya.see.SeeTestConfiguration;
import dooya.see.domain.member.Member;
import dooya.see.domain.member.exception.MemberNotFoundException;
import dooya.see.domain.shared.Email;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static dooya.see.domain.member.MemberFixture.createMemberRegisterRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@Import(SeeTestConfiguration.class)
record MemberFinderTest(MemberFinder memberFinder, MemberRegister memberRegister, EntityManager entityManager) {
    @BeforeEach
    void setUp() {
        entityManager.clear();
    }

    @Nested
    class ID로_회원_조회 {
        @Test
        void 존재하는_ID로_회원을_조회할_수_있다() {
            Member registeredMember = registerMemberAndClearContext();

            Member foundMember = memberFinder.find(registeredMember.getId());

            assertThat(foundMember.getId()).isEqualTo(registeredMember.getId());
        }

        @Test
        void 존재하지_않는_ID로_조회_시_예외가_발생한다() {
            assertThatThrownBy(() -> memberFinder.find(999L))
                    .isInstanceOf(MemberNotFoundException.class);
        }

        @Test
        void null_ID로_조회_시_예외가_발생한다() {
            assertThatThrownBy(() -> memberFinder.find(null))
                    .isInstanceOf(Exception.class);
        }
    }

    @Nested
    class 이메일로_회원_조회 {
        @Test
        void 존재하는_이메일로_회원을_조회할_수_있다() {
            Member registeredMember = registerMemberAndClearContext();
            Email email = registeredMember.getEmail();

            Member foundMember = memberFinder.findByEmail(email);

            assertThat(foundMember.getId()).isEqualTo(registeredMember.getId());
            assertThat(foundMember.getEmail()).isEqualTo(email);
        }

        @Test
        void 존재하지_않는_이메일로_조회_시_예외가_발생한다() {
            Email nonExistentEmail = new Email("notfound@example.com");

            assertThatThrownBy(() -> memberFinder.findByEmail(nonExistentEmail))
                    .isInstanceOf(MemberNotFoundException.class);
        }

        @Test
        void null_이메일로_조회_시_예외가_발생한다() {
            assertThatThrownBy(() -> memberFinder.findByEmail(null))
                    .isInstanceOf(Exception.class);
        }
    }

    // 헬퍼 메서드들
    private Member registerMemberAndClearContext() {
        Member member = memberRegister.register(createMemberRegisterRequest());
        flushAndClearContext();
        return member;
    }

    private void flushAndClearContext() {
        entityManager.flush();
        entityManager.clear();
    }
}