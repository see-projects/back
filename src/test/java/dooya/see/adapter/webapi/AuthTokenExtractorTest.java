package dooya.see.adapter.webapi;

import dooya.see.domain.member.exception.AuthenticateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class AuthTokenExtractorTest {

    @DisplayName("올바른 Bearer 토큰에서 JWT 토큰을 추출할 수 있다")
    @Test
    void extractValidBearerToken() {
        String authorizationHeader = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";

        String token = AuthTokenExtractor.extractToken(authorizationHeader);

        assertThat(token).isEqualTo("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9");
    }

    @DisplayName("Bearer 접두사의 대소문자를 구분하지 않는다")
    @ParameterizedTest
    @ValueSource(strings = {"Bearer", "bearer", "BEARER", "BeArEr"})
    void extractTokenIgnoreCase(String bearerPrefix) {
        String authorizationHeader = bearerPrefix + " validToken123";

        String token = AuthTokenExtractor.extractToken(authorizationHeader);

        assertThat(token).isEqualTo("validToken123");
    }

    @DisplayName("Bearer와 토큰 사이의 여러 공백을 처리할 수 있다")
    @Test
    void extractTokenWithMultipleSpaces() {
        String authorizationHeader = "Bearer    tokenWithSpaces";

        String token = AuthTokenExtractor.extractToken(authorizationHeader);

        assertThat(token).isEqualTo("tokenWithSpaces");
    }

    @DisplayName("헤더 값 앞뒤의 공백을 제거하고 토큰을 추출한다")
    @Test
    void extractTokenWithTrimming() {
        String authorizationHeader = "  Bearer validToken  ";

        String token = AuthTokenExtractor.extractToken(authorizationHeader);

        assertThat(token).isEqualTo("validToken");
    }

    @DisplayName("Authorization 헤더가 null이면 예외가 발생한다")
    @Test
    void throwExceptionWhenHeaderIsNull() {
        assertThatThrownBy(() -> AuthTokenExtractor.extractToken(null))
            .isInstanceOf(AuthenticateException.class)
            .hasMessage("Authorization 헤더가 올바르지 않습니다");
    }

    @DisplayName("Authorization 헤더가 빈 문자열이면 예외가 발생한다")
    @Test
    void throwExceptionWhenHeaderIsEmpty() {
        assertThatThrownBy(() -> AuthTokenExtractor.extractToken(""))
            .isInstanceOf(AuthenticateException.class)
            .hasMessage("Authorization 헤더가 올바르지 않습니다");
    }

    @DisplayName("Authorization 헤더가 공백만 있으면 예외가 발생한다")
    @Test
    void throwExceptionWhenHeaderIsBlank() {
        assertThatThrownBy(() -> AuthTokenExtractor.extractToken("   "))
            .isInstanceOf(AuthenticateException.class)
            .hasMessage("Authorization 헤더가 올바르지 않습니다");
    }

    @DisplayName("Bearer 접두사가 없으면 예외가 발생한다")
    @Test
    void throwExceptionWhenNoBearerPrefix() {
        assertThatThrownBy(() -> AuthTokenExtractor.extractToken("Basic dXNlcjpwYXNz"))
            .isInstanceOf(AuthenticateException.class)
            .hasMessage("Authorization 헤더가 올바르지 않습니다");
    }

    @DisplayName("Bearer만 있고 토큰이 없으면 예외가 발생한다")
    @Test
    void throwExceptionWhenOnlyBearer() {
        assertThatThrownBy(() -> AuthTokenExtractor.extractToken("Bearer"))
            .isInstanceOf(AuthenticateException.class)
            .hasMessage("Authorization 헤더가 올바르지 않습니다");
    }

    @DisplayName("Bearer 뒤에 공백만 있으면 예외가 발생한다")
    @Test
    void throwExceptionWhenOnlySpacesAfterBearer() {
        assertThatThrownBy(() -> AuthTokenExtractor.extractToken("Bearer   "))
            .isInstanceOf(AuthenticateException.class)
            .hasMessage("Authorization 헤더가 올바르지 않습니다");
    }

    @DisplayName("Bearer 뒤에 공백 없이 바로 토큰이 오면 예외가 발생한다")
    @Test
    void throwExceptionWhenNoSpaceAfterBearer() {
        assertThatThrownBy(() -> AuthTokenExtractor.extractToken("Bearertoken123"))
            .isInstanceOf(AuthenticateException.class)
            .hasMessage("Authorization 헤더가 올바르지 않습니다");
    }

    @DisplayName("잘못된 접두사를 사용하면 예외가 발생한다")
    @ParameterizedTest
    @ValueSource(strings = {"Token", "JWT", "Auth", "Bear"})
    void throwExceptionWithInvalidPrefix(String invalidPrefix) {
        String authorizationHeader = invalidPrefix + " validToken";

        assertThatThrownBy(() -> AuthTokenExtractor.extractToken(authorizationHeader))
            .isInstanceOf(AuthenticateException.class)
            .hasMessage("Authorization 헤더가 올바르지 않습니다");
    }

    @DisplayName("올바른 Bearer 토큰 형식인지 검증할 수 있다")
    @Test
    void isValidBearerTokenForValidFormat() {
        String validHeader = "Bearer validToken123";

        boolean result = AuthTokenExtractor.isValidBearerToken(validHeader);

        assertThat(result).isTrue();
    }

    @DisplayName("Bearer 토큰 형식 검증 - 대소문자 무관")
    @ParameterizedTest
    @ValueSource(strings = {"Bearer token", "bearer token", "BEARER token", "BeArEr token"})
    void isValidBearerTokenIgnoreCase(String header) {
        boolean result = AuthTokenExtractor.isValidBearerToken(header);

        assertThat(result).isTrue();
    }

    @DisplayName("잘못된 형식은 false를 반환한다")
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
    void isValidBearerTokenForInvalidFormat(String invalidHeader) {
        boolean result = AuthTokenExtractor.isValidBearerToken(invalidHeader);

        assertThat(result).isFalse();
    }

    @DisplayName("null 헤더는 false를 반환한다")
    @Test
    void isValidBearerTokenForNullHeader() {
        boolean result = AuthTokenExtractor.isValidBearerToken(null);

        assertThat(result).isFalse();
    }

    @DisplayName("복잡한 JWT 토큰도 올바르게 추출할 수 있다")
    @Test
    void extractComplexJwtToken() {
        String complexToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        String authorizationHeader = "Bearer " + complexToken;

        String token = AuthTokenExtractor.extractToken(authorizationHeader);

        assertThat(token).isEqualTo(complexToken);
    }

    @DisplayName("토큰에 특수문자가 포함되어도 올바르게 추출할 수 있다")
    @Test
    void extractTokenWithSpecialCharacters() {
        String tokenWithSpecialChars = "token-with_special.chars123";
        String authorizationHeader = "Bearer " + tokenWithSpecialChars;

        String token = AuthTokenExtractor.extractToken(authorizationHeader);

        assertThat(token).isEqualTo(tokenWithSpecialChars);
    }

    @DisplayName("매우 긴 토큰도 올바르게 추출할 수 있다")
    @Test
    void extractVeryLongToken() {
        String longToken = "a".repeat(1000);
        String authorizationHeader = "Bearer " + longToken;

        String token = AuthTokenExtractor.extractToken(authorizationHeader);

        assertThat(token).isEqualTo(longToken);
    }

    @DisplayName("토큰 끝에 공백이 있어도 제거하고 추출한다")
    @Test
    void extractTokenWithTrailingSpaces() {
        String authorizationHeader = "Bearer validToken   ";

        String token = AuthTokenExtractor.extractToken(authorizationHeader);

        assertThat(token).isEqualTo("validToken");
    }
}
