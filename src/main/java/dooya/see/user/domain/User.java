package dooya.see.user.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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

    private LocalDate birthDate;

    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private Role role;

    public static User signUpUser(String email, String name, String password, LocalDate birthDate, String phoneNumber, Role role) {
        return User.builder()
                .email(email)
                .name(name)
                .password(password)
                .birthDate(birthDate)
                .phoneNumber(phoneNumber)
                .role(role)
                .build();
    }
}
