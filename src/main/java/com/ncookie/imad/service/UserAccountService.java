package com.ncookie.imad.service;

import com.ncookie.imad.domain.UserAccount;
import com.ncookie.imad.domain.type.Role;
import com.ncookie.imad.dto.UserAccountDto;
import com.ncookie.imad.dto.request.SignUpRequest;
import com.ncookie.imad.exception.BadRequestException;
import com.ncookie.imad.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserAccountService {
    private final UserAccountRepository userAccountRepository;

    @Transactional
    public String createUserAccount(SignUpRequest signUpRequest) {
        if (userAccountRepository.existsByUserIdAndAuthProvider(signUpRequest.getId(), signUpRequest.getAuthProvider())) {
            throw new BadRequestException("이미 존재하는 유저입니다");
        }

        UserAccountDto dto = UserAccountDto.of(
                signUpRequest.getId(),
                signUpRequest.getNickname(),
                "test", // TODO: 적절한 비밀번호 넣어줘야 함
                signUpRequest.getGender(),
                signUpRequest.getEmail(),
                signUpRequest.getAgeRange(),
                signUpRequest.getProfileImageUrl(),
                Role.USER,
                signUpRequest.getAuthProvider()
        );

        return userAccountRepository.save(dto.toEntity()).getUserId();
    }
}