package dooya.see.application.member.provided;

import dooya.see.SeeTestConfiguration;
import dooya.see.domain.member.LoginResult;
import dooya.see.domain.member.Member;
import dooya.see.domain.member.exception.AuthenticateException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static dooya.see.domain.member.MemberFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@Import(SeeTestConfiguration.class)
record MemberAuthTest(MemberRegister memberRegister, MemberAuth memberAuth, EntityManager entityManager) {
    @BeforeEach
    void setUp() {
        entityManager.clear();
    }

    @Nested
    class 회원_로그인 {
        @Test
        void 유효한_자격증명으로_로그인한다() {
            Member registeredMember = registerMemberAndClearContext();

            LoginResult loginResult = memberAuth.login(createMemberAuthRequest());

            assertThatLoginSuccessful(loginResult, registeredMember);
        }

        @Test
        void 잘못된_비밀번호로_로그인_시_예외가_발생한다() {
            registerMemberAndClearContext();

            assertThatThrownBy(() -> memberAuth.login(createAuthRequestWithPassword("fakepassword")))
                    .isInstanceOf(AuthenticateException.class);
        }

        @Test
        void 비활성화된_계정으로_로그인_시_예외가_발생한다() {
            Member member = registerMemberAndClearContext();
            memberRegister.deactivate(member.getId());

            assertThatThrownBy(() -> memberAuth.login(createMemberAuthRequest()))
                    .isInstanceOf(AuthenticateException.class);
        }

        private void assertThatLoginSuccessful(LoginResult loginResult, Member expectedMember) {
            assertThat(loginResult.member().getId()).isEqualTo(expectedMember.getId());
            assertThat(loginResult.accessToken()).isNotNull();
        }
    }

    @Nested
    class 현재_회원_조회 {
        @Test
        void 유효한_토큰으로_회원_정보를_조회한다() {
            registerMemberAndClearContext();
            LoginResult loginResult = memberAuth.login(createMemberAuthRequest());

            Member currentMember = memberAuth.getCurrentMember(loginResult.accessToken());

            assertThat(currentMember.getId()).isEqualTo(loginResult.member().getId());
        }

        @Test
        void 잘못된_토큰으로_조회_시_예외가_발생한다() {
            registerMemberAndClearContext();

            assertThatThrownBy(() -> memberAuth.getCurrentMember("invalidToken"))
                    .isInstanceOf(AuthenticateException.class);
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