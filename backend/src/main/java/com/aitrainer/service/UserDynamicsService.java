package com.aitrainer.service;

import com.aitrainer.vo.DynamicVO;
import com.aitrainer.vo.PageResultVO;

public interface UserDynamicsService {
    /**
     * 动态聚合展示动态的信息
     * @param id
     * @param userId
     * @param category
     * @param page
     * @param size
     * @return
     */
    PageResultVO<DynamicVO> listUserDynamics(Long id, Long userId, String category, long page, long size);
}
