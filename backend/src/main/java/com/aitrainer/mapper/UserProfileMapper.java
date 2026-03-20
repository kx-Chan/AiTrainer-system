package com.aitrainer.mapper;

import com.aitrainer.entity.UserProfile;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * UserProfile 表的 Mapper 接口。
 */
@Mapper
public interface UserProfileMapper extends BaseMapper<UserProfile> {
}
