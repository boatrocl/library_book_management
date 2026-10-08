package com.libraflow.library.service.impl;

import com.libraflow.library.domain.entity.User;
import com.libraflow.library.dto.request.UpdateMemberProfileRequest;
import com.libraflow.library.dto.response.MemberProfileResponse;
import com.libraflow.library.exception.ResourceNotFoundException;
import com.libraflow.library.mapper.MemberMapper;
import com.libraflow.library.repository.UserRepository;
import com.libraflow.library.service.MemberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberServiceImpl implements MemberService {

    private final UserRepository userRepository;
    private final MemberMapper memberMapper;

    public MemberServiceImpl(UserRepository userRepository, MemberMapper memberMapper) {
        this.userRepository = userRepository;
        this.memberMapper = memberMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public MemberProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบข้อมูลผู้ใช้รหัส: " + userId));
        
        return memberMapper.toProfileResponse(user);
    }

    @Override
    @Transactional
    public MemberProfileResponse updateProfile(Long userId, UpdateMemberProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบข้อมูลผู้ใช้รหัส: " + userId));

        if (user.getProfile() != null) {
            user.getProfile().setFirstName(request.firstName());
            user.getProfile().setLastName(request.lastName());
            user.getProfile().setPhoneNumber(request.phoneNumber());
            user.getProfile().setAddress(request.address());
        }

        userRepository.save(user);

        return memberMapper.toProfileResponse(user);
    }
}