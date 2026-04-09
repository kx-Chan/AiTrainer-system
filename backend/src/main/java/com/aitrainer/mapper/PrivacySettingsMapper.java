package com.aitrainer.mapper;

import com.aitrainer.entity.UserPrivacySettings;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PrivacySettingsMapper extends BaseMapper<UserPrivacySettings> {
}
