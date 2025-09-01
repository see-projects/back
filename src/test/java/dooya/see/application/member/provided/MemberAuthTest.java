package dooya.see.application.member.provided;

import dooya.see.SeeTestConfiguration;
import dooya.see.domain.member.AuthenticateException;
import dooya.see.domain.member.LoginResult;
import dooya.see.domain.member.Member;
import dooya.see.domain.member.MemberFixture;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("올바른 이메일과 비밀번호로 로그인 시 회원 정보와 액세스 토큰을 반환한다")
    @Test
    void login() {
        Member member = memberRegister.register(createMemberRegisterRequest());
        entityManager.flush();
        entityManager.clear();

        LoginResult loginResult = memberAuth.login(MemberFixture.createMemberAuthRequest());

        assertThat(loginResult.member().getId()).isEqualTo(member.getId());
        assertThat(loginResult.accessToken()).isNotNull();
    }

    @DisplayName("잘못된 비밀번호로 로그인 시도 시 인증 예외가 발생한다")
    @Test
    void loginFailWithWrongPassword() {
        memberRegister.register(createMemberRegisterRequest());
        entityManager.flush();
        entityManager.clear();

        assertThatThrownBy(() -> memberAuth.login(createAuthRequestWithPassword("fakepassword")))
            .isInstanceOf(AuthenticateException.class);
    }

    @DisplayName("비활성화된 계정으로 로그인 시도 시 인증 예외가 발생한다")
    @Test
    void loginFailWithDeactivatedAccount() {
        Member member = memberRegister.register(createMemberRegisterRequest());
        entityManager.flush();
        entityManager.clear();

        memberRegister.deactivate(member.getId());

        assertThatThrownBy(() -> memberAuth.login(MemberFixture.createMemberAuthRequest()))
            .isInstanceOf(AuthenticateException.class);
    }

    @DisplayName("유효한 액세스 토큰으로 현재 로그인된 회원 정보를 조회한다")
    @Test
    void getCurrentMember() {
        memberRegister.register(createMemberRegisterRequest());
        entityManager.flush();
        entityManager.clear();

        LoginResult loginResult = memberAuth.login(MemberFixture.createMemberAuthRequest());

        Member member = memberAuth.getCurrentMember(loginResult.accessToken());

        assertThat(member.getId()).isEqualTo(loginResult.member().getId());
    }

    @DisplayName("잘못된 액세스 토큰으로 회원 정보 조회 시 인증 예외가 발생한다")
    @Test
    void getCurrentMemberFailWithInvalidToken() {
        memberRegister.register(createMemberRegisterRequest());
        entityManager.flush();
        entityManager.clear();
        
        assertThatThrownBy(() -> memberAuth.getCurrentMember("invalidToken"))
            .isInstanceOf(AuthenticateException.class);
    }
}
