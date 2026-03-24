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
}

