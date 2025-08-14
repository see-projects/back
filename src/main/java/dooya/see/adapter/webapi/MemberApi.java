package dooya.see.adapter.webapi;

import dooya.see.adapter.webapi.dto.MemberAuthResponse;
import dooya.see.adapter.webapi.dto.MemberProfileResponse;
import dooya.see.adapter.webapi.dto.MemberRegisterResponse;
import dooya.see.application.member.provided.MemberAuth;
import dooya.see.application.member.provided.MemberRegister;
import dooya.see.domain.member.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;

@RestController
@RequiredArgsConstructor
public class MemberApi {
    private final MemberRegister memberRegister;
    private final MemberAuth memberAuth;

    @PostMapping("/api/members")
    public MemberRegisterResponse register(@RequestBody @Valid MemberRegisterRequest request) {
        Member member = memberRegister.register(request);

        return MemberRegisterResponse.of(member);
    }

    @PostMapping("/api/members/login")
    public MemberAuthResponse login(@RequestBody @Valid MemberAuthRequest request) {
        LoginResult result = memberAuth.login(request);

        return MemberAuthResponse.from(result);
    }

    @GetMapping("/api/members/my")
    public MemberProfileResponse getCurrentMember(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader) {
        String token = extractTokenFromHeader(authorizationHeader);
        Member member = memberAuth.getCurrentMember(token);

        return MemberProfileResponse.from(member);
    }

    private String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new AuthenticateException("Authorization 헤더가 올바르지 않습니다");
        }
        return authorizationHeader.substring(7);
    }
}
