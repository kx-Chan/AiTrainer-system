package com.aitrainer.service.impl;

import com.aitrainer.common.constant.MessageConstant;
import com.aitrainer.common.exception.BusinessException;
import com.aitrainer.entity.User;
import com.aitrainer.entity.UserFollows;
import com.aitrainer.mapper.UserFollowsMapper;
import com.aitrainer.service.FollowService;
import com.aitrainer.service.OssService;
import com.aitrainer.service.ProfileService;
import com.aitrainer.service.UserService;
import com.aitrainer.vo.FollowUserVO;
import com.aitrainer.vo.PageResultVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private static final String DEFAULT_AVATAR_URL = "https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png";

    private final UserFollowsMapper userFollowsMapper;
    private final UserService userService;
    private final ProfileService profileService;
    private final OssService ossService;

    /**
     * 获取关注名单
     * @param userId
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageResultVO<FollowUserVO> getFollowing(final Long userId, final long page, final long size) {
        // 设置为false，不查询总数，因为总数已经保存在user表里
        final Page<UserFollows> mpPage = new Page<>(page, size,false);
        // 取出关注者id为当前userId的所有数据
        final LambdaQueryWrapper<UserFollows> wrapper = new LambdaQueryWrapper<UserFollows>()
                .eq(UserFollows::getFollowerId, userId)
                .orderByDesc(UserFollows::getCreatedAt);
        userFollowsMapper.selectPage(mpPage, wrapper);
        // 先从 User 实体类拿到这个 count
        final User self = userService.getById(userId);
        long totalCount = self == null || self.getFollowingCount() == null ? 0 : self.getFollowingCount();

        // 把被关注者的id取出来，放在一个list
        final List<Long> ids = mpPage.getRecords().stream()
                .map(UserFollows::getFollowedId)
                .toList();
        // 去user表补全用户信息
        final List<FollowUserVO> records = buildUsers(ids, Collections.emptySet(), true);
        return PageResultVO.<FollowUserVO>builder()
                .records(records)
                .total(totalCount)
                .page(mpPage.getCurrent())
                .size(mpPage.getSize())
                .build();
    }

    /**
     * 获取粉丝名单
     * @param userId
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageResultVO<FollowUserVO> getFollowers(final Long userId, final long page, final long size) {
        final Page<UserFollows> mpPage = new Page<>(page, size, false);
        final LambdaQueryWrapper<UserFollows> wrapper = new LambdaQueryWrapper<UserFollows>()
                .eq(UserFollows::getFollowedId, userId)
                .orderByDesc(UserFollows::getCreatedAt);
        userFollowsMapper.selectPage(mpPage, wrapper);
        final User self = userService.getById(userId);
        long totalCount = self == null || self.getFollowerCount() == null ? 0 : self.getFollowerCount();

        final List<Long> ids = mpPage.getRecords().stream()
                .map(UserFollows::getFollowerId)
                .toList();

        final Set<Long> followingSet;

        // 多了判断是否回关的代码
        if (ids.isEmpty()) {
            followingSet = Collections.emptySet();
        } else {
            followingSet = userFollowsMapper.selectList(new LambdaQueryWrapper<UserFollows>()
                            .eq(UserFollows::getFollowerId, userId)
                            .in(UserFollows::getFollowedId, ids))
                    .stream()
                    .map(UserFollows::getFollowedId)
                    .collect(Collectors.toSet());
        }

        final List<FollowUserVO> records = buildUsers(ids, followingSet, false);
        return PageResultVO.<FollowUserVO>builder()
                .records(records)
                .total(totalCount)
                .page(mpPage.getCurrent())
                .size(mpPage.getSize())
                .build();
    }

    /**
     * 实现关注功能
     * @param userId
     * @param targetUserId
     */
    @Override
    @Transactional
    public void follow(final Long userId, final Long targetUserId) {
        if (userId == null) {
            throw BusinessException.unauthorized(MessageConstant.USER_NOT_LOGGED_IN);
        }
        if (targetUserId == null) {
            throw BusinessException.badRequest(MessageConstant.USER_NOT_FOUND);
        }
        if (userId.equals(targetUserId)) {
            throw BusinessException.badRequest(MessageConstant.CANNOT_FOLLOW_SELF);
        }

        final Long count = userFollowsMapper.selectCount(new LambdaQueryWrapper<UserFollows>()
                .eq(UserFollows::getFollowerId, userId)
                .eq(UserFollows::getFollowedId, targetUserId));
        if (count != null && count > 0) {
            throw BusinessException.conflict(MessageConstant.ALREADY_FOLLOWED);
        }

        userFollowsMapper.insert(UserFollows.builder()
                .followerId(userId)
                .followedId(targetUserId)
                .createdAt(LocalDateTime.now())
                .build());

        final User follower = userService.getById(userId);
        if (follower == null) {
            throw BusinessException.notFound(MessageConstant.USER_NOT_FOUND);
        }
        final User followed = userService.getById(targetUserId);
        if (followed == null) {
            throw BusinessException.notFound(MessageConstant.USER_NOT_FOUND);
        }

        userService.increaseFollowingCount(userId);
        userService.increaseFollowerCount(targetUserId);
    }

    /**
     * 实现取消关注功能
     * @param userId
     * @param targetUserId
     */
    @Override
    @Transactional
    public void unfollow(final Long userId, final Long targetUserId) {
        if (userId == null) {
            throw BusinessException.unauthorized(MessageConstant.USER_NOT_LOGGED_IN);
        }
        if (targetUserId == null) {
            throw BusinessException.badRequest(MessageConstant.USER_NOT_FOUND);
        }
        if (userId.equals(targetUserId)) {
            throw BusinessException.badRequest(MessageConstant.CANNOT_FOLLOW_SELF);
        }

        // 检查用户是否关注了目标用户
        final Long count = userFollowsMapper.selectCount(new LambdaQueryWrapper<UserFollows>()
                .eq(UserFollows::getFollowerId, userId)
                .eq(UserFollows::getFollowedId, targetUserId));
        if (count == null || count <= 0) {
            throw BusinessException.badRequest(MessageConstant.NOT_FOLLOWED);
        }

        userFollowsMapper.delete(new LambdaQueryWrapper<UserFollows>()
                .eq(UserFollows::getFollowerId, userId)
                .eq(UserFollows::getFollowedId, targetUserId));

        final User follower = userService.getById(userId);
        if (follower == null) {
            throw BusinessException.notFound(MessageConstant.USER_NOT_FOUND);
        }
        final User followed = userService.getById(targetUserId);
        if (followed == null) {
            throw BusinessException.notFound(MessageConstant.USER_NOT_FOUND);
        }

        userService.decreaseFollowingCount(userId);
        userService.decreaseFollowerCount(targetUserId);
    }

    /**
     * 获取当前用户关注的所有用户ID
     * @param userId
     * @return
     */
    @Override
    public List<Long> listFollowingIds(final Long userId) {
        if (userId == null) {
            throw BusinessException.unauthorized(MessageConstant.USER_NOT_LOGGED_IN);
        }
        return userFollowsMapper.selectList(new LambdaQueryWrapper<UserFollows>()
                        .select(UserFollows::getFollowedId)
                        .eq(UserFollows::getFollowerId, userId))
                .stream()
                .map(UserFollows::getFollowedId)
                .toList();
    }

    // 构建关注用户VO列表
    private List<FollowUserVO> buildUsers(final List<Long> userIds, final Set<Long> followingSet, final boolean followingList) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }

        // 获取user对象列表
        final List<User> users = userService.listByIds(userIds);
        final Map<Long, User> userMap = new HashMap<>();
        for (final User u : users) {
            userMap.put(u.getId(), u);
        }
        
        // 获取user对应的个人信息
        final var profiles = profileService.listProfilesByIds(userIds);
        final Map<Long, com.aitrainer.entity.UserProfile> profileMap = new HashMap<>();
        for (final var p : profiles) {
            profileMap.put(p.getUserId(), p);
        }

        // 判断查询的是关注列表还是粉丝列表，如果是粉丝列表就使用传入的set，显示哪些粉丝有回关；否则直接将所有关注的都传入即可
        final Set<Long> following = followingList ? new HashSet<>(userIds) : followingSet;

        return userIds.stream()
                .map(id -> {
                    // 取出map里的user和userProfile
                    final User user = userMap.get(id);
                    final com.aitrainer.entity.UserProfile profile = profileMap.get(id);
                    // 如果没有昵称则显示用户名，如果没有用户名则显示用户，防止显示null
                    final String name = profile != null && profile.getNickname() != null && !profile.getNickname().isBlank()
                            ? profile.getNickname()
                            : (user == null ? "用户" : user.getUsername());
                    // 获取个性签名
                    final String bio = profile == null ? "" : (profile.getBio() == null ? "" : profile.getBio());
                    // 获取用户头像，临时url
                    final String avatar = resolveAvatarUrl(user == null ? null : user.getAvatar());
                    return FollowUserVO.builder()
                            .id(id)
                            .name(name)
                            .bio(bio)
                            .avatar(avatar)
                            .isFollowing(following.contains(id)) // 此处用hashset的时间复杂度为O（1）
                            .build();
                })
                .toList();
    }

    private String resolveAvatarUrl(final String objectKey) {
        final String url = ossService.generateAvatarUrl(objectKey);
        if (url == null || url.isBlank()) {
            return DEFAULT_AVATAR_URL;
        }
        return url;
    }
}
