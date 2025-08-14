package dooya.see.adapter.webapi;

import dooya.see.adapter.webapi.dto.MemberAuthResponse;
import dooya.see.adapter.webapi.dto.MemberRegisterResponse;
import dooya.see.application.member.provided.MemberAuth;
import dooya.see.application.member.provided.MemberRegister;
import dooya.see.domain.member.LoginResult;
import dooya.see.domain.member.Member;
import dooya.see.domain.member.MemberAuthRequest;
import dooya.see.domain.member.MemberRegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
}
