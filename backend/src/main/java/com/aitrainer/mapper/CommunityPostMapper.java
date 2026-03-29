package com.aitrainer.mapper;

import com.aitrainer.entity.CommunityPost;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CommunityPostMapper extends BaseMapper<CommunityPost> {
    @Update("UPDATE community_posts SET like_count = like_count + 1 WHERE id = #{postId}")
    int incrementLikeCount(@Param("postId") Long postId);

    @Update("UPDATE community_posts SET like_count = like_count - 1 WHERE id = #{postId} AND like_count > 0")
    int decrementLikeCount(@Param("postId") Long postId);

    @Update("UPDATE community_posts SET favorite_count = favorite_count + 1 WHERE id = #{postId}")
    int incrementfavoriteCount(@Param("postId") Long postId);

    @Update("UPDATE community_posts SET favorite_count = favorite_count - 1 WHERE id = #{postId} AND favorite_count > 0")
    int decrementfavoriteCount(@Param("postId") Long postId);

    @Update("UPDATE community_posts SET comment_count = comment_count + 1 WHERE id = #{postId}")
    void incrementCommentCount(Long postId);

    @Update("UPDATE community_posts SET comment_count = comment_count - 1 WHERE id = #{postId} AND comment_count > 0")
    void decrementCommentCount(Long postId);
}

