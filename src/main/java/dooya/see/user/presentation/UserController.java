package dooya.see.user.presentation;

import dooya.see.user.application.UserSignUpRequest;
import dooya.see.user.application.UserSignUpResponse;
import dooya.see.user.application.UserSignUpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserSignUpService userSignUpService;

    @PostMapping
    public ResponseEntity<UserSignUpResponse> userSignUp(@RequestBody UserSignUpRequest request) {
        UserSignUpResponse response = userSignUpService.userSignUp(request);
        URI location = URI.create("/api/users/" + response.id());
        return ResponseEntity.created(location).body(response);
    }
}
