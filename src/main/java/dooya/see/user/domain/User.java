package dooya.see.user.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * {@code User} 엔티티 클래스는 사용자 정보를
 * 데이터베이스의 "users" 테이블과 매핑하여 표현합니다.
 *
 * <p>주요 필드로는 사용자 고유 식별자(id), 이메일, 이름,
 * 암호화된 비밀번호, 닉네임, 역할(role)이 포함됩니다.
 *
 * <p>회원가입 시 필요한 정적 팩토리 메서드 {@link #signUpUser}와
 * 닉네임을 수정하는 인스턴스 메서드 {@link #updateNickName}를 제공합니다.
 *
 * <p>JPA를 활용해 영속성 관리하며, Lombok 어노테이션으로
 * 생성자, 빌더, 게터를 자동 생성합니다.
 *
 * @author dooya
 */
@Entity
@Table(name = "users")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String name;

    private String password;

    private String nickName;

    @Enumerated(EnumType.STRING)
    private Role role;

    public static User signUpUser(String email, String name, String password, String nickName, Role role) {
        return User.builder()
                .email(email)
                .name(name)
                .password(password)
                .nickName(nickName)
                .role(role)
                .build();
    }

    public void updateNickName(String newNickName) {
        this.nickName = newNickName;
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }
}
