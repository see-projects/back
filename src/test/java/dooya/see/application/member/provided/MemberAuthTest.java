package dooya.see.application.member.provided;

import dooya.see.SeeTestConfiguration;
import dooya.see.domain.member.AuthenticateException;
import dooya.see.domain.member.LoginResult;
import dooya.see.domain.member.Member;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static dooya.see.domain.member.MemberFixture.*;
import static dooya.see.domain.member.MemberFixture.createLoginRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@Import(SeeTestConfiguration.class)
record MemberAuthTest(MemberRegister memberRegister, MemberAuth memberAuth, EntityManager entityManager) {
    @Test
    @DisplayName("")
    void login() {
        Member member = memberRegister.register(createMemberRegisterRequest());
        entityManager.flush();
        entityManager.clear();

        LoginResult loginResult = memberAuth.login(createLoginRequest());

        assertThat(loginResult.member().getId()).isEqualTo(member.getId());
        assertThat(loginResult.accessToken()).isNotNull();
    }

    @Test
    @DisplayName("")
    void passwordNotMatheLoginFail() {
        memberRegister.register(createMemberRegisterRequest());
        entityManager.flush();
        entityManager.clear();

        assertThatThrownBy(() -> memberAuth.login(createLoginRequestWithPassword("fakepassword")))
            .isInstanceOf(AuthenticateException.class);
    }

    @Test
    @DisplayName("")
    void no() {
        Member member = memberRegister.register(createMemberRegisterRequest());
        entityManager.flush();
        entityManager.clear();

        memberRegister.deactivate(member.getId());

        assertThatThrownBy(() -> memberAuth.login(createLoginRequest()))
            .isInstanceOf(AuthenticateException.class);
    }
}
