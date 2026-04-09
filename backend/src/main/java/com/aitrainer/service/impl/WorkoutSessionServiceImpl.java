package com.aitrainer.service.impl;

import com.aitrainer.common.constant.MessageConstant;
import com.aitrainer.common.exception.BusinessException;
import com.aitrainer.entity.WorkoutSession;
import com.aitrainer.entity.WorkoutSessionLike;
import com.aitrainer.mapper.UserProfileMapper;
import com.aitrainer.mapper.WorkoutSessionMapper;
import com.aitrainer.mapper.WorkoutSessionLikeMapper;
import com.aitrainer.service.OssService;
import com.aitrainer.service.WorkoutSessionService;
import com.aitrainer.vo.LikeStatusVO;
import com.aitrainer.vo.WorkoutSessionVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkoutSessionServiceImpl implements WorkoutSessionService {

    private final WorkoutSessionMapper workoutSessionMapper;
    private final WorkoutSessionLikeMapper workoutSessionLikeMapper;
    private final UserProfileMapper userProfileMapper;
    private final OssService ossService;
    private final ObjectMapper objectMapper; // Spring Boot 默认注入的 Jackson 对象

    @Override
    public WorkoutSessionVO getReportDetail(final Long aiReportId, final Long viewerId) {
        // 1. 获取数据库原始实体
        final WorkoutSession session = workoutSessionMapper.selectById(aiReportId);
        if (session == null) {
            return null;
        }

        // 2. 解析 JSON 字段 (雷达图分数与抓拍图)
        // 数据库存的是 JSON String，VO 需要的是 Map/List
        Map<String, Integer> radarScores = null;
        List<String> snapshots = null;

        try {
            if (StringUtils.hasText(session.getRadarScores())) {
                radarScores = objectMapper.readValue(session.getRadarScores(),
                        new TypeReference<Map<String, Integer>>() {});
            }
            if (StringUtils.hasText(session.getSnapshots())) {
                // 先解析出 ObjectKey 列表
                List<String> keys = objectMapper.readValue(session.getSnapshots(),
                        new TypeReference<List<String>>() {});
                // 关键：调用 OSS 服务为每一张图生成带 Token 的临时 URL
                snapshots = keys.stream()
                        .map(ossService::generatePostImageUrl)
                        .filter(Objects::nonNull)
                        .toList();
            }
        } catch (JsonProcessingException e) {
            log.error("解析战报 JSON 数据失败, ID: {}", aiReportId, e);
        }

        // 3. 组装 VO 并返回
        final Integer durationSeconds = session.getDurationMinutes() == null ? null : session.getDurationMinutes() * 60;
        final Integer likes = session.getLikeCount() == null ? 0 : session.getLikeCount();
        final Boolean liked = viewerId == null ? null : workoutSessionLikeMapper.selectCount(
                new LambdaQueryWrapper<WorkoutSessionLike>()
                        .eq(WorkoutSessionLike::getSessionId, aiReportId)
                        .eq(WorkoutSessionLike::getUserId, viewerId)
        ) > 0;

        return WorkoutSessionVO.builder()
                .id(session.getId())
                .workoutId(session.getWorkoutId())
                .score(session.getScore())
                .grade(session.getGrade())
                .gradeLevel(mapGradeToLevel(session.getGrade())) // 映射前端样式类
                .comment(session.getComment())
                .validReps(session.getValidReps())
                .invalidReps(session.getInvalidReps())
                .durationSeconds(durationSeconds)
                .caloriesBurned(session.getCaloriesBurned())
                .radarScores(radarScores)
                .snapshots(snapshots)
                .createdAt(session.getCreatedAt())
                .likes(likes)
                .liked(liked)
                .build();
    }

    /**
     * 点赞战报。
     *
     * @param userId     当前用户 ID
     * @param aiReportId 战报 ID
     * @return 点赞状态
     */
    @Override
    @Transactional
    public LikeStatusVO likeReport(final Long userId, final Long aiReportId) {
        if (userId == null) throw BusinessException.unauthorized(MessageConstant.USER_NOT_LOGGED_IN);
        final WorkoutSession session = workoutSessionMapper.selectById(aiReportId);
        if (session == null) throw BusinessException.notFound(MessageConstant.WORKOUT_REPORT_NOT_FOUND);

        final Long exists = workoutSessionLikeMapper.selectCount(new LambdaQueryWrapper<WorkoutSessionLike>()
                .eq(WorkoutSessionLike::getSessionId, aiReportId)
                .eq(WorkoutSessionLike::getUserId, userId));

        if (exists == null || exists == 0) {
            workoutSessionLikeMapper.insert(WorkoutSessionLike.builder()
                    .sessionId(aiReportId)
                    .userId(userId)
                    .createdAt(LocalDateTime.now())
                    .build());
            workoutSessionMapper.incrementLikeCount(aiReportId);
            userProfileMapper.incrementTotalLikes(session.getUserId());
        }

        final WorkoutSession latest = workoutSessionMapper.selectById(aiReportId);
        final int likes = latest == null || latest.getLikeCount() == null ? 0 : latest.getLikeCount();
        return LikeStatusVO.builder().liked(true).likes(likes).build();
    }

    /**
     * 取消点赞战报。
     *
     * @param userId     当前用户 ID
     * @param aiReportId 战报 ID
     * @return 点赞状态
     */
    @Override
    @Transactional
    public LikeStatusVO unlikeReport(final Long userId, final Long aiReportId) {
        if (userId == null) throw BusinessException.unauthorized(MessageConstant.USER_NOT_LOGGED_IN);
        final WorkoutSession session = workoutSessionMapper.selectById(aiReportId);
        if (session == null) throw BusinessException.notFound(MessageConstant.WORKOUT_REPORT_NOT_FOUND);

        final int rows = workoutSessionLikeMapper.delete(new LambdaQueryWrapper<WorkoutSessionLike>()
                .eq(WorkoutSessionLike::getSessionId, aiReportId)
                .eq(WorkoutSessionLike::getUserId, userId));

        if (rows > 0) {
            workoutSessionMapper.decrementLikeCount(aiReportId);
            userProfileMapper.decrementTotalLikes(session.getUserId());
        }

        final WorkoutSession latest = workoutSessionMapper.selectById(aiReportId);
        final int likes = latest == null || latest.getLikeCount() == null ? 0 : latest.getLikeCount();
        return LikeStatusVO.builder().liked(false).likes(likes).build();
    }

    /**
     * 计科 Tip：根据评分等级映射前端 CSS 类名
     */
    private String mapGradeToLevel(String grade) {
        if (grade == null) return "grade-c";
        return switch (grade.toUpperCase()) {
            case "S" -> "grade-s";
            case "A" -> "grade-a";
            case "B" -> "grade-b";
            default -> "grade-c";
        };
    }

}
