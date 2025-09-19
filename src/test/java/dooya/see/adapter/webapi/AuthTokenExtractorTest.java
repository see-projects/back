package dooya.see.adapter.webapi;

import dooya.see.domain.member.exception.AuthenticateException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static dooya.see.adapter.webapi.AuthTokenExtractor.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthTokenExtractorTest {
    private static final String VALID_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
    private static final String COMPLEX_JWT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    private static final String TOKEN_WITH_SPECIAL_CHARS = "token-with_special.chars123";
    private static final String BEARER_PREFIX = "Bearer";
    private static final String INVALID_HEADER_MESSAGE = "Authorization 헤더가 올바르지 않습니다";

    @Nested
    class 정상적인_토큰_추출 {
        @Test
        void 올바른_Bearer_토큰에서_JWT_토큰을_추출할_수_있다() {
            String authorizationHeader = createBearerHeader(VALID_TOKEN);

            String token = extractToken(authorizationHeader);

            assertThatTokenExtracted(token, VALID_TOKEN);
        }

        @ParameterizedTest
        @ValueSource(strings = {"Bearer", "bearer", "BEARER", "BeArEr"})
        void Bearer_접두사의_대소문자를_구분하지_않는다(String bearerPrefix) {
            String authorizationHeader = bearerPrefix + " validToken123";

            String token = extractToken(authorizationHeader);

            assertThatTokenExtracted(token, "validToken123");
        }

        @Test
        void Bearer와_토큰_사이의_여러_공백을_처리할_수_있다() {
            String authorizationHeader = "Bearer    tokenWithSpaces";

            String token = extractToken(authorizationHeader);

            assertThatTokenExtracted(token, "tokenWithSpaces");
        }

        @Test
        void 헤더_값_앞뒤의_공백을_제거하고_토큰을_추출한다() {
            String authorizationHeader = "  Bearer validToken  ";

            String token = extractToken(authorizationHeader);

            assertThatTokenExtracted(token, "validToken");
        }

        @Test
        void 토큰_끝에_공백이_있어도_제거하고_추출한다() {
            String authorizationHeader = "Bearer validToken   ";

            String token = extractToken(authorizationHeader);

            assertThatTokenExtracted(token, "validToken");
        }

        private void assertThatTokenExtracted(String actualToken, String expectedToken) {
            assertThat(actualToken).isEqualTo(expectedToken);
        }
    }

    @Nested
    class 복잡한_토큰_처리 {
        @Test
        void 복잡한_JWT_토큰도_올바르게_추출할_수_있다() {
            String authorizationHeader = createBearerHeader(COMPLEX_JWT_TOKEN);

            String token = extractToken(authorizationHeader);

            assertThatComplexTokenExtracted(token);
        }

        @Test
        void 토큰에_특수문자가_포함되어도_올바르게_추출할_수_있다() {
            String authorizationHeader = createBearerHeader(TOKEN_WITH_SPECIAL_CHARS);

            String token = extractToken(authorizationHeader);

            assertThatSpecialCharTokenExtracted(token);
        }

        @Test
        void 매우_긴_토큰도_올바르게_추출할_수_있다() {
            String longToken = "a".repeat(1000);
            String authorizationHeader = createBearerHeader(longToken);

            String token = extractToken(authorizationHeader);

            assertThatLongTokenExtracted(token, longToken);
        }

        private void assertThatComplexTokenExtracted(String token) {
            assertThat(token).isEqualTo(COMPLEX_JWT_TOKEN);
        }

        private void assertThatSpecialCharTokenExtracted(String token) {
            assertThat(token).isEqualTo(TOKEN_WITH_SPECIAL_CHARS);
        }

        private void assertThatLongTokenExtracted(String token, String expectedToken) {
            assertThat(token).isEqualTo(expectedToken);
        }
    }

    @Nested
    class 잘못된_헤더_예외_처리 {
        @Test
        void Authorization_헤더가_null이면_예외가_발생한다() {
            assertThatAuthenticateExceptionThrown(() -> extractToken(null));
        }

        @Test
        void Authorization_헤더가_빈_문자열이면_예외가_발생한다() {
            assertThatAuthenticateExceptionThrown(() -> extractToken(""));
        }

        @Test
        void Authorization_헤더가_공백만_있으면_예외가_발생한다() {
            assertThatAuthenticateExceptionThrown(() -> extractToken("   "));
        }

        @Test
        void Bearer_접두사가_없으면_예외가_발생한다() {
            assertThatAuthenticateExceptionThrown(() -> extractToken("Basic dXNlcjpwYXNz"));
        }

        @Test
        void Bearer만_있고_토큰이_없으면_예외가_발생한다() {
            assertThatAuthenticateExceptionThrown(() -> extractToken("Bearer"));
        }

        @Test
        void Bearer_뒤에_공백만_있으면_예외가_발생한다() {
            assertThatAuthenticateExceptionThrown(() -> extractToken("Bearer   "));
        }

        @Test
        void Bearer_뒤에_공백_없이_바로_토큰이_오면_예외가_발생한다() {
            assertThatAuthenticateExceptionThrown(() -> extractToken("Bearertoken123"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"Token", "JWT", "Auth", "Bear"})
        void 잘못된_접두사를_사용하면_예외가_발생한다(String invalidPrefix) {
            String authorizationHeader = invalidPrefix + " validToken";

            assertThatAuthenticateExceptionThrown(() -> extractToken(authorizationHeader));
        }

        private void assertThatAuthenticateExceptionThrown(Runnable extractionCode) {
            assertThatThrownBy(extractionCode::run)
                    .isInstanceOf(AuthenticateException.class)
                    .hasMessage(INVALID_HEADER_MESSAGE);
        }
    }

    @Nested
    class Bearer_토큰_형식_검증 {
        @Test
        void 올바른_Bearer_토큰_형식인지_검증할_수_있다() {
            String validHeader = createBearerHeader("validToken123");

            boolean result = isValidBearerToken(validHeader);

            assertThatTokenValidationSucceeded(result);
        }

        @ParameterizedTest
        @ValueSource(strings = {"Bearer token", "bearer token", "BEARER token", "BeArEr token"})
        void Bearer_토큰_형식_검증_시_대소문자를_구분하지_않는다(String header) {
            boolean result = isValidBearerToken(header);

            assertThatTokenValidationSucceeded(result);
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
            boolean result = isValidBearerToken(invalidHeader);

            assertThatTokenValidationFailed(result);
        }

        @Test
        void null_헤더는_false를_반환한다() {
            boolean result = isValidBearerToken(null);

            assertThatTokenValidationFailed(result);
        }

        private void assertThatTokenValidationSucceeded(boolean result) {
            assertThat(result).isTrue();
        }

        private void assertThatTokenValidationFailed(boolean result) {
            assertThat(result).isFalse();
        }
    }

    // 헬퍼 메서드들
    private String createBearerHeader(String token) {
        return BEARER_PREFIX + " " + token;
    }
}