package com.aitrainer.service.impl;

import com.aitrainer.dto.DynamicBasicDTO;
import com.aitrainer.mapper.UserDynamicsMapper;
import com.aitrainer.service.PostService;
import com.aitrainer.service.PrivacyService;
import com.aitrainer.service.UserDynamicsService;
import com.aitrainer.service.WorkoutSessionService;
import com.aitrainer.vo.DynamicVO;
import com.aitrainer.vo.PageResultVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDynamicsServiceImpl implements UserDynamicsService {

    private final UserDynamicsMapper dynamicsMapper;
    private final PrivacyService privacyService;
    private final PostService postService; // 别人的Service
    private final WorkoutSessionService workoutService; // 别人的Service

    /**
     * 动态查询动态的状况
     * @param viewerId
     * @param ownerId
     * @param category
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageResultVO<DynamicVO> listUserDynamics(Long viewerId, Long ownerId, String category, long page, long size) {
        // 1. 权限拦截
        boolean canViewReports = ownerId.equals(viewerId) || privacyService.checkReportVisibility(ownerId);
        if ("workout_report".equals(category) && !canViewReports) {
            return PageResultVO.empty(page, size);
        }

        // 2. 查出基础“壳子”
        Page<DynamicBasicDTO> pageParam = new Page<>(page, size);

        // ✅ 关键修改：用 basics 接住返回值！
        List<DynamicBasicDTO> basics = dynamicsMapper.selectMergedDynamics(pageParam, ownerId, category, canViewReports);

        // 防御性编程：如果真的没数据，直接返回
        if (basics == null || basics.isEmpty()) {
            log.warn("用户 {} 的动态查询结果为空", ownerId);
            return PageResultVO.empty(page, size);
        }

        // 3. 核心步骤：详情回填 (Hydration)
        // ✅ 修改这里：改用 basics 开启 Stream
        List<DynamicVO> records = basics.stream().map(basic -> {
            DynamicVO vo = DynamicVO.builder()
                    .id(basic.id())
                    .type(basic.type())
                    .createTime(basic.createTime())
                    .build();

            if ("post".equals(basic.type())) {
                vo.setPost(postService.getPostDetail(basic.id(), viewerId));
            } else if ("workout_report".equals(basic.type())) {
                vo.setAiReport(workoutService.getWorkoutSessionDetail(basic.id(), viewerId));
            }
            return vo;
        }).collect(Collectors.toList());

        return PageResultVO.<DynamicVO>builder()
                .records(records)
                .total(pageParam.getTotal()) // Total 是 MP 自动填进 pageParam 的，这里没问题
                .page(page)
                .size(size)
                .build();
    }
}
