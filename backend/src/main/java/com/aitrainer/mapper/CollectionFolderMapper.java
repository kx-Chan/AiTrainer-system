package com.aitrainer.mapper;

import com.aitrainer.entity.CollectionFolder;
import com.aitrainer.vo.FolderVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CollectionFolderMapper extends BaseMapper<CollectionFolder> {
    /**
     * 查询用户收藏夹列表并包含推文数量统计
     */
    List<FolderVO> selectFoldersWithCount(@Param("userId") Long userId);
}
