package com.aitrainer.mapper;

import com.aitrainer.dto.DynamicBasicDTO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserDynamicsMapper {
    /**
     * 聚合查询动态流（支持自动分页）
     * * @param page        MyBatis-Plus 的分页对象，传入后会自动处理 LIMIT 和 COUNT
     * @param ownerId     空间主人ID
     * @param category    分类 (all/post/workout_report)
     * @param showPrivate 是否包含私密战报
     * @return 基础动态列表
     */
    List<DynamicBasicDTO> selectMergedDynamics(
            IPage<DynamicBasicDTO> page,
            @Param("ownerId") Long ownerId,
            @Param("category") String category,
            @Param("showPrivate") boolean showPrivate
    );
}
