package com.aitrainer.service;

import com.aitrainer.vo.LikeStatusVO;
import com.aitrainer.vo.WorkoutSessionVO;

public interface WorkoutSessionService {
    /**
     * 获取战报详情（包含访客点赞状态）。
     *
     * @param aiReportId 战报 ID
     * @param viewerId   访客用户 ID（可为 null）
     * @return 战报详情
     */
    WorkoutSessionVO getReportDetail(Long aiReportId, Long viewerId);

    /**
     * 点赞战报。
     *
     * @param userId     当前用户 ID
     * @param aiReportId 战报 ID
     * @return 点赞状态
     */
    LikeStatusVO likeReport(Long userId, Long aiReportId);

    /**
     * 取消点赞战报。
     *
     * @param userId     当前用户 ID
     * @param aiReportId 战报 ID
     * @return 点赞状态
     */
    LikeStatusVO unlikeReport(Long userId, Long aiReportId);
}
