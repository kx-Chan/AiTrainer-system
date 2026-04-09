package com.aitrainer.service.impl;

import com.aitrainer.common.constant.MessageConstant;
import com.aitrainer.common.exception.BusinessException;
import com.aitrainer.dto.UpdatePrivacyDTO;
import com.aitrainer.entity.UserPrivacySettings;
import com.aitrainer.mapper.PrivacySettingsMapper;
import com.aitrainer.service.PrivacyService;
import com.aitrainer.vo.PrivacySettingsVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * PrivacyServiceImpl 实现。
 * 处理用户隐私配置的持久化与业务逻辑。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PrivacyServiceImpl implements PrivacyService {

    private final PrivacySettingsMapper privacyMapper;

    /**
     * 获取用户隐私设置。
     * 如果数据库中没有记录，则返回系统默认配置（默认公开）。
     *
     * @param userId 用户 ID。
     * @return 隐私设置视图对象。
     */
    @Override
    @Transactional(readOnly = true)
    public PrivacySettingsVO getUserSettings(final Long userId) {
        log.info("获取用户 {} 的隐私设置", userId);

        final UserPrivacySettings settings = privacyMapper.selectById(userId);

        // 如果没有记录，返回默认值 (1: 公开)
        if (settings == null) {
            return PrivacySettingsVO.builder()
                    .publicAiReport(true)
                    .build();
        }

        return PrivacySettingsVO.builder()
                .publicAiReport(settings.getPublicAiReport() == 1)
                .build();
    }

    /**
     * 更新隐私设置。
     * 使用“先查后改”逻辑，确保数据存在时更新，不存在时插入。
     *
     * @param userId 用户 ID。
     * @param dto    包含新设置的 DTO。
     */
    @Override
    @Transactional
    public void updateSettings(final Long userId, final UpdatePrivacyDTO dto) {
        log.info("用户 {} 正在更新隐私设置, publicAiReport={}", userId, dto.publicAiReport());

        UserPrivacySettings settings = privacyMapper.selectById(userId);

        if (settings == null) {
            // 插入新记录
            settings = new UserPrivacySettings();
            settings.setUserId(userId);
            settings.setPublicAiReport(dto.publicAiReport());
            settings.setUpdatedAt(LocalDateTime.now());
            privacyMapper.insert(settings);
            log.info("用户 {} 初始隐私设置已创建", userId);
        } else {
            // 更新现有记录
            settings.setPublicAiReport(dto.publicAiReport());
            settings.setUpdatedAt(LocalDateTime.now());
            privacyMapper.updateById(settings);
            log.info("用户 {} 隐私设置已更新", userId);
        }
    }

    /**
     * 检查目标用户的战报是否对外部可见。
     * 供其他服务（如战报列表）调用，确保符合 Service 间调用规范。
     *
     * @param userId 目标用户 ID。
     * @return true 表示可见，false 表示不可见。
     */
    @Override
    @Transactional(readOnly = true)
    public boolean checkReportVisibility(final Long userId) {
        final UserPrivacySettings settings = privacyMapper.selectById(userId);

        // 默认公开原则：若无设置记录，视为公开
        if (settings == null) {
            return true;
        }

        return settings.getPublicAiReport() == 1;
    }
}