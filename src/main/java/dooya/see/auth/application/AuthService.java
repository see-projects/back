package dooya.see.auth.application;

public interface AuthService {
    LoginResult login(LoginCommand command);
}
