package dooya.see.user.application.service;

/**
 * {@code UserValidator}는 사용자 관련 검증 로직을 담당하는
 * 애플리케이션 계층의 인터페이스입니다.
 *
 * <p>이 인터페이스는 주로 중복 이메일 검사와 같은 사용자 입력 검증을 수행하며,
 * 비즈니스 규칙에 따라 예외를 발생시킬 수 있습니다.
 *
 * <p>구현체에서 실제 검증 로직을 작성합니다.
 *
 * @author dooya
 */
public interface UserValidator {
    /**
     * 주어진 이메일이 이미 존재하는지 중복 검사를 수행합니다.
     *
     * @param email 중복 여부를 검사할 사용자 이메일
     */
    void validateDuplicateEmail(String email);
}
