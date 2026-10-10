package com.libraflow.library.service;

import com.libraflow.library.dto.request.UpdateMemberProfileRequest;
import com.libraflow.library.dto.response.MemberProfileResponse;

public interface MemberService {
    void assertCanAccessMember(Long userId, String requesterUsername, boolean staff);
    MemberProfileResponse getProfile(Long userId);
    MemberProfileResponse updateProfile(Long userId, UpdateMemberProfileRequest request);
}
