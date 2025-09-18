package dooya.see.adapter.webapi;

import dooya.see.domain.member.exception.AuthenticateException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthTokenExtractorTest {
    @Test
    void 올바른_Bearer_토큰에서_JWT_토큰을_추출할_수_있다() {
        String authorizationHeader = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";

        String token = AuthTokenExtractor.extractToken(authorizationHeader);

        assertThat(token).isEqualTo("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Bearer", "bearer", "BEARER", "BeArEr"})
    void Bearer_접두사의_대소문자를_구분하지_않는다(String bearerPrefix) {
        String authorizationHeader = bearerPrefix + " validToken123";

        String token = AuthTokenExtractor.extractToken(authorizationHeader);

        assertThat(token).isEqualTo("validToken123");
    }

    @Test
    void Bearer와_토큰_사이의_여러_공백을_처리할_수_있다() {
        String authorizationHeader = "Bearer    tokenWithSpaces";

        String token = AuthTokenExtractor.extractToken(authorizationHeader);

        assertThat(token).isEqualTo("tokenWithSpaces");
    }

    @Test
    void 헤더_값_앞뒤의_공백을_제거하고_토큰을_추출한다() {
        String authorizationHeader = "  Bearer validToken  ";

        String token = AuthTokenExtractor.extractToken(authorizationHeader);

        assertThat(token).isEqualTo("validToken");
    }

    @Test
    void Authorization_헤더가_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> AuthTokenExtractor.extractToken(null))
            .isInstanceOf(AuthenticateException.class)
            .hasMessage("Authorization 헤더가 올바르지 않습니다");
    }

    @Test
    void Authorization_헤더가_빈_문자열이면_예외가_발생한다() {
        assertThatThrownBy(() -> AuthTokenExtractor.extractToken(""))
            .isInstanceOf(AuthenticateException.class)
            .hasMessage("Authorization 헤더가 올바르지 않습니다");
    }

    @Test
    void Authorization_헤더가_공백만_있으면_예외가_발생한다() {
        assertThatThrownBy(() -> AuthTokenExtractor.extractToken("   "))
            .isInstanceOf(AuthenticateException.class)
            .hasMessage("Authorization 헤더가 올바르지 않습니다");
    }

    @Test
    void Bearer_접두사가_없으면_예외가_발생한다() {
        assertThatThrownBy(() -> AuthTokenExtractor.extractToken("Basic dXNlcjpwYXNz"))
            .isInstanceOf(AuthenticateException.class)
            .hasMessage("Authorization 헤더가 올바르지 않습니다");
    }

    @Test
    void Bearer만_있고_토큰이_없으면_예외가_발생한다() {
        assertThatThrownBy(() -> AuthTokenExtractor.extractToken("Bearer"))
            .isInstanceOf(AuthenticateException.class)
            .hasMessage("Authorization 헤더가 올바르지 않습니다");
    }

    @Test
    void Bearer_뒤에_공백만_있으면_예외가_발생한다() {
        assertThatThrownBy(() -> AuthTokenExtractor.extractToken("Bearer   "))
            .isInstanceOf(AuthenticateException.class)
            .hasMessage("Authorization 헤더가 올바르지 않습니다");
    }

    @Test
    void Bearer_뒤에_공백_없이_바로_토큰이_오면_예외가_발생한다() {
        assertThatThrownBy(() -> AuthTokenExtractor.extractToken("Bearertoken123"))
            .isInstanceOf(AuthenticateException.class)
            .hasMessage("Authorization 헤더가 올바르지 않습니다");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Token", "JWT", "Auth", "Bear"})
    void 잘못된_접두사를_사용하면_예외가_발생한다(String invalidPrefix) {
        String authorizationHeader = invalidPrefix + " validToken";

        assertThatThrownBy(() -> AuthTokenExtractor.extractToken(authorizationHeader))
            .isInstanceOf(AuthenticateException.class)
            .hasMessage("Authorization 헤더가 올바르지 않습니다");
    }

    @Test
    void 올바른_Bearer_토큰_형식인지_검증할_수_있다() {
        String validHeader = "Bearer validToken123";

        boolean result = AuthTokenExtractor.isValidBearerToken(validHeader);

        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Bearer token", "bearer token", "BEARER token", "BeArEr token"})
    void Bearer_토큰_형식_검증_대소문자_무관(String header) {
        boolean result = AuthTokenExtractor.isValidBearerToken(header);

        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "",
        "   ",
        "Bearer",
        "Bearer  ",
        "Bearertoken",
        "Basic token",
        "token Bearer",
        "Invalid Bearer token"
    })
    void 잘못된_형식은_false를_반환한다(String invalidHeader) {
        boolean result = AuthTokenExtractor.isValidBearerToken(invalidHeader);

        assertThat(result).isFalse();
    }

    @Test
    void null_헤더는_false를_반환한다() {
        boolean result = AuthTokenExtractor.isValidBearerToken(null);

        assertThat(result).isFalse();
    }

    @Test
    void 복잡한_JWT_토큰도_올바르게_추출할_수_있다() {
        String complexToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        String authorizationHeader = "Bearer " + complexToken;

        String token = AuthTokenExtractor.extractToken(authorizationHeader);

        assertThat(token).isEqualTo(complexToken);
    }

    @Test
    void 토큰에_특수문자가_포함되어도_올바르게_추출할_수_있다() {
        String tokenWithSpecialChars = "token-with_special.chars123";
        String authorizationHeader = "Bearer " + tokenWithSpecialChars;

        String token = AuthTokenExtractor.extractToken(authorizationHeader);

        assertThat(token).isEqualTo(tokenWithSpecialChars);
    }

    @Test
    void 매우_긴_토큰도_올바르게_추출할_수_있다() {
        String longToken = "a".repeat(1000);
        String authorizationHeader = "Bearer " + longToken;

        String token = AuthTokenExtractor.extractToken(authorizationHeader);

        assertThat(token).isEqualTo(longToken);
    }

    @Test
    void 토큰_끝에_공백이_있어도_제거하고_추출한다() {
        String authorizationHeader = "Bearer validToken   ";

        String token = AuthTokenExtractor.extractToken(authorizationHeader);

        assertThat(token).isEqualTo("validToken");
    }
}
