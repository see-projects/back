package dooya.see.member.application.service;

import dooya.see.member.application.dto.MemberResult;
import dooya.see.member.application.dto.MemberRegisterCommand;

/**
 * {@code UserSignUpService} 인터페이스는
 * 사용자 회원가입 관련 비즈니스 로직을 추상화한 서비스 계층의 계약을 정의합니다.
 *
 * <p>회원가입 요청에 따른 {@link MemberRegisterCommand}를 받아
 * 처리 후, {@link MemberResult}를 반환하는 메서드를 제공합니다.
 *
 * <p>구현체는 이 인터페이스를 구현하여 실제 회원가입 로직(예: 유효성 검사,
 * 비밀번호 암호화, 사용자 저장 등)을 담당합니다.
 *
 * @author dooya
 */
public interface MemberRegisterService {
    /**
     * 회원가입 요청을 처리합니다.
     *
     * @param command 회원가입에 필요한 정보가 담긴 {@link MemberRegisterCommand} 객체
     * @return 회원가입이 완료된 사용자의 정보를 담은 {@link MemberResult} 객체
     */
    MemberResult userSignUp(MemberRegisterCommand command);
}
