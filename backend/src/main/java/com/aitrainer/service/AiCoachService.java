package com.aitrainer.service;

import com.aitrainer.dto.AiCoachAnalyzeRequestDTO;
import com.aitrainer.vo.AiCoachAnalyzeResponseVO;

/**
 * AI 私教服务接口。
 */
public interface AiCoachService {

    /**
     * 分析用户数据并给出建议。
     *
     * @param userId 用户 ID
     * @param dto    分析请求参数
     * @return 分析结果
     */
    AiCoachAnalyzeResponseVO analyze(Long userId, AiCoachAnalyzeRequestDTO dto);
}
