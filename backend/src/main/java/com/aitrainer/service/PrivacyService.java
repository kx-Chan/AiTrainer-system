package com.aitrainer.service;

import com.aitrainer.dto.UpdatePrivacyDTO;
import com.aitrainer.vo.PrivacySettingsVO;

public interface PrivacyService {
    /**
     * 获取用户隐私设置
     * @param id
     * @return
     */
    PrivacySettingsVO getUserSettings(Long id);

    /**
     * 更新用户隐私设置
     * @param id
     * @param dto
     */
    void updateSettings(Long id, UpdatePrivacyDTO dto);

    /**
     * 检查他人的战报是否可见
     * @param userId
     * @return
     */
    boolean checkReportVisibility(Long userId);
}
