package dooya.see.user.presentation;

import dooya.see.user.application.UserSignUpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @PostMapping
    public ResponseEntity<UserSignUpResponse> userSignUp(UserSignUpRequest request) {
        return ResponseEntity.created(userSignUpService.signUp(request));
    }
}
