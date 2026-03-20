package com.aitrainer.service.impl;

import com.aitrainer.entity.User;
import com.aitrainer.entity.UserProfile;
import com.aitrainer.mapper.UserMapper;
import com.aitrainer.mapper.UserProfileMapper;
import com.aitrainer.service.ProfileService;
import com.aitrainer.dto.OnboardingProfileDTO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;

    @Override
    @Transactional
    public void saveOnboardingProfile(Long userId, OnboardingProfileDTO dto) {
        // 1. 校验用户是否存在（可选，根据业务需求，通常 Security 保证了用户存在）
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalStateException("无法找到当前登录用户 ID: " + userId);
        }

        // 2. 创建或更新用户资料
        UserProfile profile = UserProfile.builder()
                .userId(userId)
                .nickname(dto.nickname())
                .gender(dto.gender())
                .goal(dto.goal())
                .height(dto.height())
                .weight(dto.weight())
                .bodyFat(dto.bodyFat())
                .build();

        // 使用 MyBatis-Plus 的 saveOrUpdate 方法，它会根据主键是否存在来决定是插入还是更新
        // 这里我们使用 insert，因为 onboarding 总是创建新记录
        userProfileMapper.insert(profile);
        log.info("已为用户 ID: {} 创建了个人资料", userId);

        // 3. 更新用户的 isFirstLogin 状态
        user.setFirstLogin(false);
        userMapper.updateById(user);
        log.info("已更新用户 ID: {} 的 isFirstLogin 状态为 false", userId);
    }
}
