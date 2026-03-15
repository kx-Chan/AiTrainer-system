package com.aitrainer.mapper;

import com.aitrainer.model.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * User 实体的 Mapper 接口，继承 MyBatis-Plus 的 BaseMapper。
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
