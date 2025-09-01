package dooya.see.adapter.security;

import dooya.see.application.member.provided.MemberRegister;
import dooya.see.application.member.required.TokenManager;
import dooya.see.domain.member.AuthenticateException;
import dooya.see.domain.member.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static dooya.see.domain.member.MemberFixture.createMemberRegisterRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
record JwtTokenManagerTest(MemberRegister memberRegister, TokenManager tokenManager) {
    @DisplayName("회원 정보로 JWT 토큰 생성 후 파싱하면 동일한 회원 정보를 얻을 수 있다")
    @Test
    void generateAndParseToken() {
        Member member = memberRegister.register(createMemberRegisterRequest());

        String token = tokenManager.generateToken(member);
        String email = tokenManager.extractEmailFromToken(token);

        assertThat(email).isEqualTo(member.getEmail().address());
    }

    @DisplayName("유효하지 않은 JWT 토큰 파싱 시 인증 예외가 발생한다")
    @Test
    void parseInvalidToken() {
        String invalidToken = "invalid.jwt.token";

        assertThatThrownBy(() -> tokenManager.extractEmailFromToken(invalidToken))
            .isInstanceOf(AuthenticateException.class);
    }
}
