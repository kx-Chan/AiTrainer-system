package com.aitrainer.service;

import com.aitrainer.dto.OnboardingProfileDTO;
import com.aitrainer.vo.UserProfileVO;

/**
 * 用户资料服务接口。
 */
public interface ProfileService {

    /**
     * 保存首次引导页提交的用户资料，并更新用户的首次登录状态。
     *
     * @param userId 用户 ID。
     * @param dto    用户资料数据。
     */
    void saveOnboardingProfile(Long userId, OnboardingProfileDTO dto);

    /**
     * 获取用户个人资料。
     *
     * @param userId 用户 ID。
     * @return 用户资料视图对象。
     */
    UserProfileVO getUserProfile(Long userId);

    /**
     * 更新用户个人资料。
     *
     * @param userId 用户 ID。
     * @param vo     用户资料视图对象。
     */
    void updateProfile(Long userId, UserProfileVO vo);
}
