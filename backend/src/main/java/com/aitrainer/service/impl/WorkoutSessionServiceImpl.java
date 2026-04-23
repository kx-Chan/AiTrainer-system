package com.aitrainer.service.impl;

import com.aitrainer.common.constant.MessageConstant;
import com.aitrainer.common.exception.BusinessException;
import com.aitrainer.entity.Workout;
import com.aitrainer.entity.WorkoutSession;
import com.aitrainer.entity.WorkoutSessionLike;
import com.aitrainer.mapper.UserProfileMapper;
import com.aitrainer.mapper.WorkoutMapper;
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
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkoutSessionServiceImpl implements WorkoutSessionService {

    private final WorkoutSessionMapper workoutSessionMapper;
    private final WorkoutSessionLikeMapper workoutSessionLikeMapper;
    private final UserProfileMapper userProfileMapper;
    private final OssService ossService;
    private final WorkoutMapper workoutMapper;
    private final ObjectMapper objectMapper; // Spring Boot 默认注入的 Jackson 对象

    @Override
    public WorkoutSessionVO getWorkoutSessionDetail(final Long aiReportId, final Long viewerId) {
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
        final Integer durationSeconds = session.getDurationSeconds();
        final Integer likes = session.getLikeCount() == null ? 0 : session.getLikeCount();
        final Boolean liked = viewerId == null ? null : workoutSessionLikeMapper.selectCount(
                new LambdaQueryWrapper<WorkoutSessionLike>()
                        .eq(WorkoutSessionLike::getSessionId, aiReportId)
                        .eq(WorkoutSessionLike::getUserId, viewerId)
        ) > 0;

        // 查询训练项目名称
        String workoutName = null;
        if (StringUtils.hasText(session.getWorkoutId())) {
            Workout workout = workoutMapper.selectById(session.getWorkoutId());
            if (workout != null) {
                workoutName = workout.getName();
            }
        }

        return WorkoutSessionVO.builder()
                .id(session.getId())
                .workoutId(session.getWorkoutId())
                .workoutName(workoutName)
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
    public LikeStatusVO likeWorkoutSession(final Long userId, final Long aiReportId) {
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
    public LikeStatusVO unlikeWorkoutSession(final Long userId, final Long aiReportId) {
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createWorkoutSession(final Long userId, final String workoutId) {
        // 1. 校验项目是否存在
        final Workout workout = workoutMapper.selectById(workoutId);
        if (workout == null) {
            throw BusinessException.notFound(MessageConstant.WORKOUT_NOT_FOUND);
        }

        // 2. 随机生成战报指标
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final int score = random.nextInt(60, 101); // 60-100分
        final String grade = score >= 90 ? "S" : (score >= 80 ? "A" : (score >= 70 ? "B" : "C"));

        final int validReps = random.nextInt(10, 31);
        final int invalidReps = random.nextInt(0, 6);
        final int durationSeconds = random.nextInt(300, 2701); // 5~45 分钟对应的秒数范围
        final int calories = random.nextInt(50, 301);

        // 3. 生成 JSON 数据
        String radarJson = "";
        String snapshotsJson = "";
        try {
            // 随机雷达图数据
            Map<String, Integer> radar = new HashMap<>();
            radar.put("accuracy", random.nextInt(70, 100));
            radar.put("power", random.nextInt(70, 100));
            radar.put("stamina", random.nextInt(70, 100));
            radar.put("rhythm", random.nextInt(70, 100));
            radar.put("range", random.nextInt(70, 100));
            radarJson = objectMapper.writeValueAsString(radar);

            // 随机抓拍图 Key (模拟 OSS 存储路径)
            List<String> snaps = Arrays.asList(
                    "mocks/snap_" + random.nextInt(1, 1000) + ".jpg",
                    "mocks/snap_" + random.nextInt(1, 1000) + ".jpg"
            );
            snapshotsJson = objectMapper.writeValueAsString(snaps);
        } catch (JsonProcessingException e) {
            log.error("随机战报 JSON 序列化失败", e);
        }

        // 4. 入库
        final WorkoutSession session = WorkoutSession.builder()
                .userId(userId)
                .workoutId(workoutId)
                .score(score)
                .grade(grade)
                .comment(getMockComment(grade))
                .validReps(validReps)
                .invalidReps(invalidReps)
                .durationSeconds(durationSeconds)
                .caloriesBurned(calories)
                .radarScores(radarJson)
                .snapshots(snapshotsJson)
                .likeCount(0)
                .isDeleted(0)
                .createdAt(LocalDateTime.now())
                .build();

        workoutSessionMapper.insert(session);
        log.info("用户 {} 成功生成一条 {} 战报, 分数: {}", userId, workoutId, score);

        return session.getId();
    }

    @Override
    public List<WorkoutSessionVO> listMyWorkoutSessions(final Long userId) {
        final List<WorkoutSession> sessions = workoutSessionMapper.selectList(
                new LambdaQueryWrapper<WorkoutSession>()
                        .eq(WorkoutSession::getUserId, userId)
                        .eq(WorkoutSession::getIsDeleted, 0)
                        .orderByDesc(WorkoutSession::getCreatedAt)
        );
        // 2. 使用 Stream 流进行转换，viewerId 传入当前用户 ID 即可
        return sessions.stream()
                .map(session -> convertToVO(session, userId))
                .filter(Objects::nonNull)
                .toList();
    }

    private String getMockComment(String grade) {
        return switch (grade) {
            case "S" -> "简直完美！动作标准度堪比教练。";
            case "A" -> "表现非常棒，核心力量控制得很稳。";
            case "B" -> "不错，注意呼吸节奏，继续加油。";
            default -> "基础很扎实，下蹲深度可以再加强一点。";
        };
    }

    /**
     * Tip：根据评分等级映射前端 CSS 类名
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

    /**
     * 核心转换逻辑：将实体类转换为 VO (私有提取)
     */
    private WorkoutSessionVO convertToVO(final WorkoutSession session, final Long viewerId) {
        if (session == null) return null;

        Map<String, Integer> radarScores = null;
        List<String> snapshots = null;

        try {
            // 解析雷达图
            if (StringUtils.hasText(session.getRadarScores())) {
                radarScores = objectMapper.readValue(session.getRadarScores(),
                        new TypeReference<Map<String, Integer>>() {});
            }
            // 解析并加签图片 URL
            if (StringUtils.hasText(session.getSnapshots())) {
                List<String> keys = objectMapper.readValue(session.getSnapshots(),
                        new TypeReference<List<String>>() {});
                snapshots = keys.stream()
                        .map(ossService::generatePostImageUrl)
                        .filter(Objects::nonNull)
                        .toList();
            }
        } catch (JsonProcessingException e) {
            log.error("转换战报数据失败, ID: {}", session.getId(), e);
        }

        final Integer durationSeconds = session.getDurationSeconds();
        final Integer likes = session.getLikeCount() == null ? 0 : session.getLikeCount();

        // 判断当前查看者是否点赞过
        final Boolean liked = viewerId == null ? false : workoutSessionLikeMapper.selectCount(
                new LambdaQueryWrapper<WorkoutSessionLike>()
                        .eq(WorkoutSessionLike::getSessionId, session.getId())
                        .eq(WorkoutSessionLike::getUserId, viewerId)
        ) > 0;

        // 查询训练项目名称
        String workoutName = null;
        if (StringUtils.hasText(session.getWorkoutId())) {
            Workout workout = workoutMapper.selectById(session.getWorkoutId());
            if (workout != null) {
                workoutName = workout.getName();
            }
        }

        return WorkoutSessionVO.builder()
                .id(session.getId())
                .workoutId(session.getWorkoutId())
                .workoutName(workoutName)
                .score(session.getScore())
                .grade(session.getGrade())
                .gradeLevel(mapGradeToLevel(session.getGrade()))
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

}