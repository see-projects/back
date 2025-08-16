package dooya.see.adapter.webapi;

import dooya.see.domain.member.AuthenticateException;

/**
 * Authorization 헤더에서 JWT 토큰을 추출하는 유틸리티 클래스
 */
public class AuthTokenExtractor {
    
    private static final String BEARER_PREFIX = "Bearer ";
    
    /**
     * Authorization 헤더에서 Bearer 토큰을 추출한다
     * 
     * @param authorizationHeader Authorization 헤더 값
     * @return 추출된 JWT 토큰
     * @throws AuthenticateException 헤더가 없거나 형식이 잘못된 경우
     */
    public static String extractToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new AuthenticateException("Authorization 헤더가 올바르지 않습니다");
        }
        return authorizationHeader.substring(BEARER_PREFIX.length());
    }
    
    /**
     * Authorization 헤더가 유효한 Bearer 토큰 형식인지 확인한다
     * 
     * @param authorizationHeader Authorization 헤더 값
     * @return 유효한 형식이면 true, 그렇지 않으면 false
     */
    public static boolean isValidBearerToken(String authorizationHeader) {
        return authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX);
    }
}
