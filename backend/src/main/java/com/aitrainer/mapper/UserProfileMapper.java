package com.aitrainer.mapper;

import com.aitrainer.entity.UserProfile;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * UserProfile 表的 Mapper 接口。
 */
@Mapper
public interface UserProfileMapper extends BaseMapper<UserProfile> {
    /**
     * 增加作者总获赞数
     */
    @Update("UPDATE user_profiles SET total_likes = total_likes + 1 WHERE user_id = #{userId}")
    void incrementTotalLikes(Long userId);

    /**
     * 减少作者总获赞数 (保底为 0)
     */
    @Update("UPDATE user_profiles SET total_likes = GREATEST(0, total_likes - 1) WHERE user_id = #{userId}")
    void decrementTotalLikes(Long userId);
}
