package com.libraflow.library.mapper;

import com.libraflow.library.domain.entity.User;
import com.libraflow.library.domain.entity.UserProfile;
import com.libraflow.library.dto.response.MemberProfileResponse;
import org.springframework.stereotype.Component;

@Component
public class MemberMapper {

    public MemberProfileResponse toProfileResponse(User user) {
        UserProfile profile = user.getProfile();
        
        return new MemberProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                user.getMemberTier(),
                profile != null ? profile.getFirstName() : null,
                profile != null ? profile.getLastName() : null,
                profile != null ? profile.getPhoneNumber() : null,
                profile != null ? profile.getAddress() : null
        );
    }
}