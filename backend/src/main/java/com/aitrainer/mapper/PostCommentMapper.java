package com.aitrainer.mapper;

import com.aitrainer.entity.PostComment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PostCommentMapper extends BaseMapper<PostComment> {
    // 获取用户评论过的推文 ID 列表（按最后评论时间倒序）
    List<Long> selectCommentedPostIds(@Param("userId") Long userId,
                                      @Param("offset") long offset,
                                      @Param("size") long size);

    // 获取用户评论过的推文总数（去重后）
    long countCommentedPosts(@Param("userId") Long userId);

}
