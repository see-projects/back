package dooya.see.adapter.webapi;

import dooya.see.domain.member.exception.AuthenticateException;

/**
 * Authorization 헤더에서 JWT 토큰을 추출하는 유틸리티 클래스
 */
public class AuthTokenExtractor {

    private static final String BEARER_SCHEME = "Bearer";

    private AuthTokenExtractor() {}


    /**
     * Authorization 헤더에서 Bearer 토큰을 추출한다
     * 
     * @param authorizationHeader Authorization 헤더 값
     * @return 추출된 JWT 토큰
     * @throws AuthenticateException 헤더가 없거나 형식이 잘못된 경우
     */
    public static String extractToken(String authorizationHeader) {
        if (authorizationHeader == null) {
            throw new AuthenticateException("Authorization 헤더가 올바르지 않습니다");
        }
        String trimmed = authorizationHeader.trim();
        if (!trimmed.regionMatches(true, 0, BEARER_SCHEME, 0, BEARER_SCHEME.length())) {
            throw new AuthenticateException("Authorization 헤더가 올바르지 않습니다");
        }
        int spaceIdx = trimmed.indexOf(' ');
        if (spaceIdx < 0) {
            throw new AuthenticateException("Authorization 헤더가 올바르지 않습니다");
        }
        String token = trimmed.substring(spaceIdx + 1).trim();
        if (token.isEmpty()) {
            throw new AuthenticateException("Authorization 헤더가 올바르지 않습니다");
        }
        return token;
    }
    
    /**
     * Authorization 헤더가 유효한 Bearer 토큰 형식인지 확인한다
     * 
     * @param authorizationHeader Authorization 헤더 값
     * @return 유효한 형식이면 true, 그렇지 않으면 false
     */
    public static boolean isValidBearerToken(String authorizationHeader) {
        if (authorizationHeader == null) return false;
        String trimmed = authorizationHeader.trim();
        if (!trimmed.regionMatches(true, 0, BEARER_SCHEME, 0, BEARER_SCHEME.length())) return false;
        int spaceIdx = trimmed.indexOf(' ');
        return spaceIdx > 0 && !trimmed.substring(spaceIdx + 1).trim().isEmpty();
    }
}
