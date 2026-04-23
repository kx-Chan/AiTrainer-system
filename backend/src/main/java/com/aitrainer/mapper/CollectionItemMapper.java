package com.aitrainer.mapper;

import com.aitrainer.entity.CollectionItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CollectionItemMapper extends BaseMapper<CollectionItem> {

    /**
     * 查询包含已逻辑删除的记录（绕过 @TableLogic 自动过滤）
     */
    @Select("SELECT * FROM collection_item WHERE user_id = #{userId} AND post_id = #{postId} AND folder_id = #{folderId} LIMIT 1")
    CollectionItem selectOneIncludingDeleted(@Param("userId") Long userId, @Param("postId") Long postId, @Param("folderId") Long folderId);

    /**
     * 恢复已逻辑删除的记录（绕过 @TableLogic 自动过滤）
     */
    @Update("UPDATE collection_item SET is_deleted = 0, create_time = #{createTime} WHERE id = #{id}")
    int restoreById(@Param("id") Long id, @Param("createTime") java.time.LocalDateTime createTime);
}
