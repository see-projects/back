package dooya.see.user.domain;

import java.util.Optional;

/**
 * {@code UserRepository} 인터페이스는 사용자 엔티티에 대한
 * 영속성 저장소 접근 계약을 정의합니다.
 *
 * <p>주요 기능으로는 사용자 ID 또는 이메일을 기준으로 조회하고,
 * 사용자 엔티티를 저장하는 메서드를 제공합니다.
 *
 * <p>구현체는 이 인터페이스를 통해 데이터베이스 작업을 수행하며,
 * Optional을 사용해 null 안전성을 보장합니다.
 *
 * @author dooya
 */
public interface UserRepository {
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    User save(User user);
}
