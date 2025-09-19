package dooya.see.adapter.security;

import dooya.see.application.member.provided.MemberRegister;
import dooya.see.application.member.required.TokenManager;
import dooya.see.domain.member.Member;
import dooya.see.domain.member.exception.AuthenticateException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static dooya.see.domain.member.MemberFixture.createMemberRegisterRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
record JwtTokenManagerTest(MemberRegister memberRegister, TokenManager tokenManager) {
    @Test
    void 회원_정보로_JWT_토큰_생성_후_파싱하면_동일한_회원_정보를_얻을_수_있다() {
        Member member = memberRegister.register(createMemberRegisterRequest());

        String token = tokenManager.generateToken(member);
        String email = tokenManager.extractEmailFromToken(token);

        assertThat(email).isEqualTo(member.getEmail().address());
    }

    @Test
    void 유효하지_않은_JWT_토큰_파싱_시_인증_예외가_발생한다() {
        String invalidToken = "invalid.jwt.token";

        assertThatThrownBy(() -> tokenManager.extractEmailFromToken(invalidToken))
            .isInstanceOf(AuthenticateException.class);
    }
}
