package dooya.see.member.application.service.impl;

import dooya.see.common.exception.CustomException;
import dooya.see.common.exception.ErrorCode;
import dooya.see.common.s3.S3Uploader;
import dooya.see.member.application.dto.PasswordUpdateCommand;
import dooya.see.member.application.dto.PasswordUpdateResult;
import dooya.see.member.application.dto.MemberResult;
import dooya.see.member.application.dto.NickNameUpdateCommand;
import dooya.see.member.application.service.MemberUpdateService;
import dooya.see.member.domain.Member;
import dooya.see.member.domain.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import static dooya.see.member.application.dto.MemberApplicationMapper.*;

@Service
@RequiredArgsConstructor
public class MemberUpdateServiceImpl implements MemberUpdateService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Uploader s3Uploader;

    @Transactional
    @Override
    public MemberResult updateNickName(String email, NickNameUpdateCommand command) {
        Member member = memberRepository.findByEmail(email)
                        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        member.updateNickName(command.nickName());

        return toResult(member);
    }

    @Transactional
    @Override
    public PasswordUpdateResult updatePassword(String email, PasswordUpdateCommand command) {
        Member member = memberRepository.findByEmail(email)
                .filter(fountUser -> passwordEncoder.matches(command.currentPassword(), fountUser.getPassword()))
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_MATCH_PASSWORD_INFO));
        member.updatePassword(passwordEncoder.encode(command.newPassword()));

        return new PasswordUpdateResult("비밀번호가 성공적으로 변경되었습니다.");
    }

    @Transactional
    @Override
    public MemberResult updateProfileImage(String email, MultipartFile profileImage) {
        String uploadImageUrl = s3Uploader.upload(profileImage, "profile");

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        member.updateProfileImage(uploadImageUrl);

        return toResult(member);
    }

    @Transactional
    @Override
    public MemberResult updateProfile(String email, NickNameUpdateCommand command, MultipartFile profileImage) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (command != null && command.nickName() != null) {
            member.updateNickName(command.nickName());
        }

        if (profileImage != null && !profileImage.isEmpty()) {
            String uploadImageUrl = s3Uploader.upload(profileImage, "profile");
            member.updateProfileImage(uploadImageUrl);
        }
        return toResult(member);
    }
}
