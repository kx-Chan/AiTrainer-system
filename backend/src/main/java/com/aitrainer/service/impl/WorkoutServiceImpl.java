package com.aitrainer.service.impl;

import com.aitrainer.entity.Workout;
import com.aitrainer.mapper.WorkoutMapper;
import com.aitrainer.service.WorkoutService;
import com.aitrainer.vo.WorkoutVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkoutServiceImpl implements WorkoutService {

    private final WorkoutMapper workoutMapper;
    private final ObjectMapper objectMapper; // Spring Boot 自动注入的 Jackson 全局实例

    /**
     * 获取健身项目列表
     * 说明：MyBatis-Plus 的 @TableLogic 注解依然会在 Mapper 层面生效，
     * 所以 selectList 依然会自动过滤掉 is_deleted = 1 的数据。
     */
    @Override
    public List<WorkoutVO> listWorkouts() {
        // 1. 手动通过 Mapper 查询列表，并按照难度升序排列
        final List<Workout> workouts = workoutMapper.selectList(
                new LambdaQueryWrapper<Workout>()
                        .orderByAsc(Workout::getDifficulty)
        );

        // 2. 判空保底并执行流式转换
        if (workouts == null || workouts.isEmpty()) {
            return Collections.emptyList();
        }

        return workouts.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 根据 ID 获取项目详情
     */
    @Override
    public WorkoutVO getWorkoutById(final String id) {
        if (!StringUtils.hasText(id)) {
            return null;
        }

        // 直接通过 Mapper 的 selectById 查询
        final Workout entity = workoutMapper.selectById(id);

        return convertToVO(entity);
    }

    /**
     * 核心转换逻辑：Entity -> VO
     * 重点：处理 Tags 字段的从 JSON String 到 List<String> 的转换
     */
    private WorkoutVO convertToVO(final Workout entity) {
        if (entity == null) {
            return null;
        }

        // 1. 初始化 Tags 列表，确保不返回 null 给前端
        List<String> tagList = Collections.emptyList();

        // 2. 解析 JSON 字符串
        try {
            if (StringUtils.hasText(entity.getTags())) {
                // 数据库存储格式示例: ["核心", "力量训练", "减脂"]
                tagList = objectMapper.readValue(entity.getTags(),
                        new TypeReference<List<String>>() {});
            }
        } catch (JsonProcessingException e) {
            // 计科 Tip：这种属于数据质量问题，记个 Error 日志很有必要
            log.error("解析健身项目标签(Tags) JSON 失败, ID: {}, 原始值: {}",
                    entity.getId(), entity.getTags(), e);
        }

        // 3. 构建并返回漂亮的对象
        return WorkoutVO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .enName(entity.getEnName())
                .difficulty(entity.getDifficulty())
                .tags(tagList)
                .description(entity.getDescription())
                .themeColor(entity.getThemeColor())
                .coverUrl(entity.getCoverUrl())
                .build();
    }
}