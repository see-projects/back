package dooya.see.application.member.provided;

import dooya.see.SeeTestConfiguration;
import dooya.see.domain.member.LoginResult;
import dooya.see.domain.member.Member;
import dooya.see.domain.member.MemberFixture;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Import(SeeTestConfiguration.class)
record MemberAuthTest(MemberRegister memberRegister, MemberFinder memberFinder, MemberAuth memberAuth, EntityManager entityManager) {
    @Test
    @DisplayName("")
    void login() {
        Member member = memberRegister.register(MemberFixture.createMemberRegisterRequest());
        entityManager.flush();
        entityManager.clear();

        LoginResult loginResult = memberAuth.login(MemberFixture.createLoginRequest());

        assertThat(loginResult.member().getId()).isEqualTo(member.getId());
        assertThat(loginResult.accessToken()).isNotNull();
    }
}
