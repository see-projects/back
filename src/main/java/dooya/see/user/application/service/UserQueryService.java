package dooya.see.user.application.service;

import dooya.see.user.application.dto.UserResult;

/**
 * {@code UserQueryService} 인터페이스는 사용자 조회와 관련된
 * 애플리케이션 계층의 서비스 계약을 정의합니다.
 *
 * <p>주요 기능은 이메일을 기반으로 사용자를 조회하는 것으로,
 * 구현체는 이 메서드를 통해 사용자 도메인에서 데이터를 가져와
 * {@link UserResult} 형태로 반환해야 합니다.
 *
 * <p>비즈니스 로직을 포함하지 않고, 단순 조회 책임만 갖습니다.
 *
 * @author dooya
 */
public interface UserQueryService {
    /**
     * 이메일을 이용해 사용자를 조회합니다.
     *
     * @param email 조회할 사용자의 이메일
     * @return 조회된 사용자 정보를 담은 {@link UserResult} 객체
     * @throws IllegalArgumentException 조회 대상이 없을 경우 발생 가능
     */
    UserResult getUserByEmail(String email);
}
