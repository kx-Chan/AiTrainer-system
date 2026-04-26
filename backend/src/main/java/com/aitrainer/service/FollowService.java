package com.aitrainer.service;

import com.aitrainer.vo.FollowUserVO;
import com.aitrainer.vo.PageResultVO;

public interface FollowService {
    /**
     * 获取关注名单
     * @param userId
     * @param page
     * @param size
     * @return
     */
    PageResultVO<FollowUserVO> getFollowing(Long userId, long page, long size);

    /**
     * 获取粉丝名单
     * @param userId
     * @param page
     * @param size
     * @return
     */
    PageResultVO<FollowUserVO> getFollowers(Long userId, long page, long size);

    /**
     * 实现关注功能
     * @param userId
     * @param targetUserId
     */
    void follow(Long userId, Long targetUserId);

    /**
     * 实现取消关注功能
     * @param userId
     * @param targetUserId
     */
    void unfollow(Long userId, Long targetUserId);

    /**
     * 获取关注的用户ID列表（不分页）
     * @param userId 当前用户ID
     * @return 关注的用户ID列表
     */
    java.util.List<Long> listFollowingIds(Long userId);

    /**
     * 判断用户是否关注某个用户
     * @param currentUserId
     * @param targetUserId
     * @return
     */
    Boolean checkIfFollowing(Long currentUserId, Long targetUserId);

    /**
     * 解绑用户的所有关注关系（注销时调用）
     * 包括：删除该用户的关注记录、删除该用户的粉丝记录
     * @param userId 用户 ID
     */
    void unbindAllFollowRelations(Long userId);
}
