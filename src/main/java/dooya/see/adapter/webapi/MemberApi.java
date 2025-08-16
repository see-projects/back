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
    public MemberProfileResponse getCurrentMember(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token) {
        String extractedToken = extractTokenFromHeader(token);
        Member member = memberAuth.getCurrentMember(extractedToken);

        return MemberProfileResponse.from(member);
    }

    @PatchMapping("/api/members/my/deactivate")
    public void deactivate(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token) {
        String extractedToken = extractTokenFromHeader(token);
        Member member = memberAuth.getCurrentMember(extractedToken);

        memberRegister.deactivate(member.getId());
    }

    @PutMapping("/api/members/my/updateInfo")
    public MemberProfileResponse updateInfo(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token,
                                            @RequestBody @Valid MemberInfoUpdateRequest request) {
        String extractedToken = extractTokenFromHeader(token);
        Member currentMember = memberAuth.getCurrentMember(extractedToken);

        Member updatedMember = memberRegister.updateInfo(currentMember.getId(), request);

        return MemberProfileResponse.from(updatedMember);
    }

    private String extractTokenFromHeader(String token) {
        return AuthTokenExtractor.extractToken(token);
    }
}
