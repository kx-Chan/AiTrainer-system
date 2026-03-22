package com.aitrainer.service.impl;

import com.aitrainer.entity.User;
import com.aitrainer.entity.UserProfile;
import com.aitrainer.mapper.UserMapper;
import com.aitrainer.mapper.UserProfileMapper;
import com.aitrainer.service.OssService;
import com.aitrainer.service.ProfileService;
import com.aitrainer.dto.OnboardingProfileDTO;
import com.aitrainer.vo.UserProfileVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private static final String DEFAULT_AVATAR_URL = "https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png";

    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final OssService ossService;

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
                .updatedAt(LocalDateTime.now())
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

    @Override
    public UserProfileVO getUserProfile(Long userId) {
        final User user = userMapper.selectById(userId);
        final String avatarUrl = resolveAvatarUrl(user == null ? null : user.getAvatar());

        final UserProfile profile = userProfileMapper.selectById(userId);
        if (profile == null) {
            return UserProfileVO.builder()
                    .userId(userId)
                    .nickname("新用户")
                    .avatar(avatarUrl)
                    .build();
        }

        // 转换 Entity 为 VO
        return UserProfileVO.builder()
                .userId(profile.getUserId())
                .nickname(profile.getNickname())
                .gender(profile.getGender())
                .goal(profile.getGoal())
                .bio(profile.getBio())
                .height(profile.getHeight())
                .weight(profile.getWeight())
                .bodyFat(profile.getBodyFat())
                .avatar(avatarUrl)
                .following(24) // 模拟数据
                .followers(128) // 模拟数据
                 .totalDays(45) // 模拟数据
                 .build();
     }

    @Override
    @Transactional
    public void updateProfile(Long userId, UserProfileVO vo) {
        final UserProfile existingProfile = userProfileMapper.selectById(userId);
        UserProfile profile = existingProfile;
        if (profile == null) {
            // 如果不存在，则创建
            profile = UserProfile.builder()
                    .userId(userId)
                    .build();
        }

        // 更新字段
        profile.setNickname(vo.getNickname());
        profile.setGender(vo.getGender());
        profile.setGoal(vo.getGoal());
        profile.setBio(vo.getBio());
        profile.setHeight(vo.getHeight());
        profile.setWeight(vo.getWeight());
        profile.setBodyFat(vo.getBodyFat());
        profile.setUpdatedAt(LocalDateTime.now());

        // 使用 saveOrUpdate 方法或判断后操作
        if (existingProfile == null) {
            userProfileMapper.insert(profile);
        } else {
            userProfileMapper.updateById(profile);
        }
        log.info("用户 ID: {} 的个人资料已更新", userId);
    }

    private String resolveAvatarUrl(final String objectKey) {
        final String url = ossService.generateAvatarUrl(objectKey);
        if (url == null || url.isBlank()) {
            return DEFAULT_AVATAR_URL;
        }
        return url;
    }
}
