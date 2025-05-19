package dooya.see.auth.domain;

import dooya.see.user.domain.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * {@code LoginUser} 클래스는
 * Spring Security에서 사용되는 인증된 사용자 정보를 표현하는 UserDetails 구현체입니다.
 *
 * <p>사용자의 고유 ID, 이메일, 역할 정보를 담고 있으며,
 * 권한 정보는 {@code ROLE_} 접두어를 붙여 반환합니다.
 *
 * <p>비밀번호는 JWT 기반 인증에서 별도로 관리되어 빈 문자열로 처리합니다.
 *
 * @author dooya
 */
public record LoginUser(Long id, String email, String role) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + role));
    }

    public Role getRole() {
        return Role.of(role);
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return email;
    }
}
