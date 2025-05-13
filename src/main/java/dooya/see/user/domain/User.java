package dooya.see.user.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "users")
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    public static User singUpUser(String email, String name, String password, String phoneNumber, LocalDate birthDate, Role role) {
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
