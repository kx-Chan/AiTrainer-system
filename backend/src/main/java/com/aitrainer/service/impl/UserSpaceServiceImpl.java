package com.aitrainer.service.impl;

import com.aitrainer.common.constant.MessageConstant;
import com.aitrainer.common.exception.BusinessException;
import com.aitrainer.entity.UserProfile;
import com.aitrainer.service.CollectionFolderService;
import com.aitrainer.service.FollowService;
import com.aitrainer.service.ProfileService;
import com.aitrainer.service.UserSpaceService;
import com.aitrainer.vo.FolderVO;
import com.aitrainer.vo.UserProfileVO;
import com.aitrainer.vo.UserSpaceVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSpaceServiceImpl implements UserSpaceService {

    // 注入其他原子 Service
    private final ProfileService profileService;
    private final FollowService followService;
    private final CollectionFolderService folderService;

    @Override
    public UserSpaceVO getSpaceProfile(Long currentUserId, Long targetUserId) {
        // 获取基础资料 (通过 ProfileService)
        UserProfileVO profile = profileService.getUserProfile(targetUserId);
        if (profile == null) {
            throw BusinessException.notFound(MessageConstant.USER_PROFILE_NOT_FOUND);
        }

        Boolean isFollowing = followService.checkIfFollowing(currentUserId, targetUserId);

        // 组装 VO
        return UserSpaceVO.builder()
                .userId(targetUserId)
                .nickname(profile.getNickname())
                .avatar(profile.getAvatar())
                .bio(profile.getBio())
                .goal(profile.getGoal())
                .followingCount(profile.getFollowing())
                .followerCount(profile.getFollowers())
                .totalLikes(profile.getTotalLikes())
                .isFollowing(isFollowing)
                .build();
    }

    @Override
    public List<FolderVO> listPublicFolders(Long targetUserId) {
        return folderService.listPublicFoldersByUserId(targetUserId);
    }
}