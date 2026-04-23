package com.aitrainer.service.impl;

import com.aitrainer.common.constant.MessageConstant;
import com.aitrainer.common.exception.BusinessException;
import com.aitrainer.dto.CreateFolderDTO;
import com.aitrainer.dto.UpdateFolderDTO;
import com.aitrainer.entity.CollectionFolder;
import com.aitrainer.entity.CollectionItem;
import com.aitrainer.mapper.CollectionFolderMapper;
import com.aitrainer.mapper.CollectionItemMapper;
import com.aitrainer.service.CollectionFolderService;
import com.aitrainer.service.CollectionItemService;
import com.aitrainer.vo.FolderVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollectionFolderServiceImpl implements CollectionFolderService {

    private final CollectionFolderMapper folderMapper;
    private final CollectionItemMapper itemMapper;
    private final CollectionItemService collectionItemService;

    /**
     * 初始化一个默认收藏夹
     * @param userId
     */
    @Override
    @Transactional
    public void initDefaultFolder(Long userId) {
        CollectionFolder folder = CollectionFolder.builder()
                .userId(userId)
                .name("默认收藏")
                .isDefault(1)
                .isPublic(0)
                .build();
        folderMapper.insert(folder);
    }

    /**
     * 用户创建收藏夹
     * @param userId
     * @param dto
     * @return
     */
    @Override
    @Transactional // 开启事务，确保数据一致性
    public Long createFolder(final Long userId, final CreateFolderDTO dto) {
        log.info("用户 {} 正在创建收藏夹: {}", userId, dto.name());

        // 1. 基础校验 (防御性编程)
        if (!StringUtils.hasText(dto.name())) {
            throw BusinessException.badRequest(MessageConstant.FOLDER_NAME_EMPTY);
        }

        // 2. 业务规则校验：限制单个用户的收藏夹数量
        // 防止数据库因为大量垃圾数据而膨胀（Database Bloating）
        LambdaQueryWrapper<CollectionFolder> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(CollectionFolder::getUserId, userId);
        Long count = folderMapper.selectCount(countWrapper);

        if (count >= 50) { // 假设上限是 50 个
            throw BusinessException.conflict(MessageConstant.FOLDER_NUMS_LIMITS);
        }

        // 3. 构建实体对象
        // 使用 Builder 模式
        final CollectionFolder folder = CollectionFolder.builder()
                .userId(userId)
                .name(dto.name().trim())
                .isPublic(dto.isPublic() != null ? dto.isPublic() : 0) // 默认私密
                .isDefault(0) // 用户手动创建的永远不是“默认”
                .isDeleted(0)
                .createTime(LocalDateTime.now())
                .build();

        // 4. 持久化到数据库
        // MyBatis-Plus 执行 insert 后会自动回填 ID 到 folder 对象中
        folderMapper.insert(folder);

        log.info("收藏夹创建成功，ID: {}", folder.getId());
        return folder.getId();
    }

    /**
     * 获取收藏夹列表
     * @param userId
     * @return
     */
    @Override
    public List<FolderVO> listFoldersByUserId(final Long userId, final String keyword) {
        // 1. 查询收藏夹基本信息
        final List<CollectionFolder> folders = folderMapper.selectList(
                new LambdaQueryWrapper<CollectionFolder>()
                        .eq(CollectionFolder::getUserId, userId)
                        .like(StringUtils.hasText(keyword), CollectionFolder::getName, keyword)
                        .orderByDesc(CollectionFolder::getIsDefault)
                        .orderByDesc(CollectionFolder::getCreateTime)
        );

        if (folders.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 提取 ID 列表用于批量查询
        final List<Long> folderIds = folders.stream()
                .map(CollectionFolder::getId)
                .collect(Collectors.toList());

        // 3. 直接通过私有方法调 Mapper 获取计数 Map
        final Map<Long, Integer> countMap = this.batchFetchItemCounts(folderIds);

        // 4. 组装 VO
        return folders.stream().map(f -> FolderVO.builder()
                .id(f.getId())
                .name(f.getName())
                .userId(userId)
                .isDefault(f.getIsDefault())
                .isPublic(f.getIsPublic())
                .itemCount(countMap.getOrDefault(f.getId(), 0))
                .build()).collect(Collectors.toList());
    }

    /**
     * 逻辑删除收藏夹
     * @param folderId
     * @param userId
     * @return
     */
    @Override
    @Transactional
    public boolean deleteFolder(Long folderId, Long userId) {
        LambdaQueryWrapper<CollectionFolder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CollectionFolder::getId, folderId)
                .eq(CollectionFolder::getUserId, userId)
                .eq(CollectionFolder::getIsDefault, 0); // 默认收藏夹不允许删除

        int rows = folderMapper.delete(wrapper);
        return rows > 0;
    }

    /**
     * 设置默认收藏夹
     * @param userId
     * @param folderId
     */
    @Override
    @Transactional
    public void setDefaultFolder(final Long userId, final Long folderId) {
        log.info("用户 {} 正在将收藏夹 {} 设为默认", userId, folderId);

        // 1. 安全检查：确保该收藏夹确实属于该用户（防止越权漏洞）
        LambdaQueryWrapper<CollectionFolder> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(CollectionFolder::getId, folderId)
                .eq(CollectionFolder::getUserId, userId);

        if (folderMapper.selectCount(checkWrapper) == 0) {
            throw BusinessException.unauthorized(MessageConstant.FOLDER_NOT_FOUND);
        }

        // 2. 排他逻辑：将该用户所有收藏夹的 is_default 置为 0
        // SQL: UPDATE collection_folder SET is_default = 0 WHERE user_id = ?
        LambdaUpdateWrapper<CollectionFolder> resetWrapper = new LambdaUpdateWrapper<>();
        resetWrapper.eq(CollectionFolder::getUserId, userId)
                .set(CollectionFolder::getIsDefault, 0);
        folderMapper.update(null, resetWrapper);

        // 3. 赋权逻辑：将指定的收藏夹 is_default 置为 1
        // SQL: UPDATE collection_folder SET is_default = 1 WHERE id = ?
        LambdaUpdateWrapper<CollectionFolder> setWrapper = new LambdaUpdateWrapper<>();
        setWrapper.eq(CollectionFolder::getId, folderId)
                .set(CollectionFolder::getIsDefault, 1);

        int rows = folderMapper.update(null, setWrapper);

        if (rows > 0) {
            log.info("用户 {} 的默认收藏夹已更新为 {}", userId, folderId);
        }
    }

    /**
     * 编辑收藏夹
     * @param userId
     * @param folderId
     * @param dto
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFolder(final Long userId, final Long folderId, final UpdateFolderDTO dto) {
        // 1. 构建更新条件：必须满足 ID 和 UserId 双重匹配（核心安全逻辑）
        // 这样即便 folderId 传错了，只要不属于该 userId，SQL 也不会生效
        final LambdaUpdateWrapper<CollectionFolder> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CollectionFolder::getId, folderId)
                .eq(CollectionFolder::getUserId, userId);

        // 2. 动态设置字段
        updateWrapper.set(StringUtils.hasText(dto.name()), CollectionFolder::getName, dto.name().trim())
                .set(dto.isPublic() != null, CollectionFolder::getIsPublic, dto.isPublic());

        // 3. 执行更新
        final int rows = folderMapper.update(null, updateWrapper);

        // 4. 结果检查
        if (rows == 0) {
            log.warn("用户 {} 尝试修改不存在或不属于自己的收藏夹 {}", userId, folderId);
            throw BusinessException.unauthorized(MessageConstant.FOLDER_NOT_FOUND);
        }

        log.info("收藏夹 {} 修改成功", folderId);
    }

    /**
     * 根据 ID 获取单个收藏夹详情
     * @param folderId 收藏夹ID
     * @param currentUserId 当前登录用户ID（用于安全校验）
     * @return FolderVO
     */
    @Override
    public FolderVO getFolderById(final Long folderId, final Long currentUserId) {
        log.info("用户 {} 正在尝试获取收藏夹 {} 的详情", currentUserId, folderId);

        // 1. 扩权查询：先只根据 ID 查出收藏夹实体（不带 UserId 过滤）
        final CollectionFolder folder = folderMapper.selectById(folderId);

        // 2. 存在性检查
        if (folder == null || folder.getIsDeleted() == 1) {
            log.warn("收藏夹 {} 不存在或已被删除", folderId);
            throw BusinessException.notFound(MessageConstant.FOLDER_NOT_FOUND);
        }

        // 3. 核心可见性逻辑校验 (Visibility Check)
        boolean isOwner = folder.getUserId().equals(currentUserId);
        boolean isPublic = folder.getIsPublic() == 1; // 1 为公开

        // 如果“我不是主人”且“它不是公开的”，则无权访问
        if (!isOwner && !isPublic) {
            log.warn("用户 {} 越权尝试访问私密收藏夹 {}", currentUserId, folderId);
            // 计科细节：在这种情况下，抛出“未找到”通常比“无权访问”更安全，防止黑客通过 ID 探测私密文件夹的存在
            throw BusinessException.unauthorized(MessageConstant.FOLDER_NOT_FOUND);
        }

        // 4. 获取推文总数 (既然可见，就可以查数量)
        final Long count = itemMapper.selectCount(
                new LambdaQueryWrapper<CollectionItem>()
                        .eq(CollectionItem::getFolderId, folderId)
                        .eq(CollectionItem::getIsDeleted, 0)
        );

        // 5. 组装返回
        return FolderVO.builder()
                .id(folder.getId())
                .name(folder.getName())
                .isDefault(folder.getIsDefault())
                .isPublic(folder.getIsPublic())
                .itemCount(count.intValue())
                .userId(folder.getUserId()) // 返回真正的作者 ID
                .build();
    }

    @Override
    public List<FolderVO> listPublicFoldersByUserId(Long targetUserId) {
        // 1. 防御性检查
        if (targetUserId == null) {
            return Collections.emptyList();
        }

        // 2. 构造查询条件：指定用户 + 必须公开 + 逻辑删除过滤（MP自动处理）
        List<CollectionFolder> folders = folderMapper.selectList(
                new LambdaQueryWrapper<CollectionFolder>()
                        .eq(CollectionFolder::getUserId, targetUserId)
                        .eq(CollectionFolder::getIsPublic, 1) // 强制过滤私密文件夹
                        .orderByDesc(CollectionFolder::getIsDefault) // 默认文件夹排前面
                        .orderByDesc(CollectionFolder::getCreateTime) // 最近创建的排前面
        );

        if (folders.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. 提取 ID 列表用于批量查询
        final List<Long> folderIds = folders.stream()
                .map(CollectionFolder::getId)
                .collect(Collectors.toList());

        // 4. 直接通过私有方法调 Mapper 获取计数 Map
        final Map<Long, Integer> countMap = this.batchFetchItemCounts(folderIds);


        // 5. 转换为 FolderVO 列表
        return folders.stream().map(folder -> {
            FolderVO vo = new FolderVO();
            vo.setId(folder.getId());
            vo.setUserId(folder.getUserId());
            vo.setName(folder.getName());
            vo.setIsDefault(folder.getIsDefault());
            vo.setIsPublic(folder.getIsPublic());
            vo.setItemCount(countMap.getOrDefault(folder.getId(), 0));

            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 私有辅助方法：直接通过 Mapper 批量获取文件夹内的推文数量
     */
    private Map<Long, Integer> batchFetchItemCounts(List<Long> folderIds) {
        if (folderIds == null || folderIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // 使用 QueryWrapper 配合聚合函数
        final QueryWrapper<CollectionItem> wrapper = new QueryWrapper<>();
        wrapper.select("folder_id", "COUNT(1) AS total")
                .in("folder_id", folderIds)
                .eq("is_deleted", 0)
                .groupBy("folder_id");

        final List<Map<String, Object>> mapList = itemMapper.selectMaps(wrapper);

        // 转换结果为 Map<FolderId, Count>
        return mapList.stream().collect(Collectors.toMap(
                m -> (Long) m.get("folder_id"),
                m -> ((Number) m.get("total")).intValue(),
                (existing, replacement) -> existing
        ));
    }
}