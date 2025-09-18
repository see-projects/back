package dooya.see.application.member.provided;

import dooya.see.SeeTestConfiguration;
import dooya.see.domain.member.LoginResult;
import dooya.see.domain.member.Member;
import dooya.see.domain.member.MemberFixture;
import dooya.see.domain.member.exception.AuthenticateException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static dooya.see.domain.member.MemberFixture.createAuthRequestWithPassword;
import static dooya.see.domain.member.MemberFixture.createMemberRegisterRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@Import(SeeTestConfiguration.class)
record MemberAuthTest(MemberRegister memberRegister, MemberAuth memberAuth, EntityManager entityManager) {
    @Test
    void 올바른_이메일과_비밀번호로_로그인_시_회원_정보와_액세스_토큰을_반환한다() {
        Member member = memberRegister.register(createMemberRegisterRequest());
        entityManager.flush();
        entityManager.clear();

        LoginResult loginResult = memberAuth.login(MemberFixture.createMemberAuthRequest());

        assertThat(loginResult.member().getId()).isEqualTo(member.getId());
        assertThat(loginResult.accessToken()).isNotNull();
    }

    @Test
    void 잘못된_비밀번호로_로그인_시도_시_인증_예외가_발생한다() {
        memberRegister.register(createMemberRegisterRequest());
        entityManager.flush();
        entityManager.clear();

        assertThatThrownBy(() -> memberAuth.login(createAuthRequestWithPassword("fakepassword")))
            .isInstanceOf(AuthenticateException.class);
    }

    @Test
    void 비활성화된_계정으로_로그인_시도_시_인증_예외가_발생한다() {
        Member member = memberRegister.register(createMemberRegisterRequest());
        entityManager.flush();
        entityManager.clear();

        memberRegister.deactivate(member.getId());

        assertThatThrownBy(() -> memberAuth.login(MemberFixture.createMemberAuthRequest()))
            .isInstanceOf(AuthenticateException.class);
    }

    @Test
    void 유효한_액세스_토큰으로_현재_로그인된_회원_정보를_조회한다() {
        memberRegister.register(createMemberRegisterRequest());
        entityManager.flush();
        entityManager.clear();

        LoginResult loginResult = memberAuth.login(MemberFixture.createMemberAuthRequest());

        Member member = memberAuth.getCurrentMember(loginResult.accessToken());

        assertThat(member.getId()).isEqualTo(loginResult.member().getId());
    }

    @Test
    void 잘못된_액세스_토큰으로_회원_정보_조회_시_인증_예외가_발생한다() {
        memberRegister.register(createMemberRegisterRequest());
        entityManager.flush();
        entityManager.clear();
        
        assertThatThrownBy(() -> memberAuth.getCurrentMember("invalidToken"))
            .isInstanceOf(AuthenticateException.class);
    }
}
