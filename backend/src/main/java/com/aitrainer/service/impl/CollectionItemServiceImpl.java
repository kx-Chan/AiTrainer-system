package com.aitrainer.service.impl;

import com.aitrainer.common.constant.MessageConstant;
import com.aitrainer.common.exception.BusinessException;
import com.aitrainer.dto.CollectionActionDTO;
import com.aitrainer.entity.CollectionFolder;
import com.aitrainer.entity.CollectionItem;
import com.aitrainer.entity.CommunityPost;
import com.aitrainer.mapper.CollectionFolderMapper;
import com.aitrainer.mapper.CollectionItemMapper;
import com.aitrainer.mapper.CommunityPostMapper;
import com.aitrainer.service.CollectionItemService;
import com.aitrainer.vo.FavoriteStatusVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollectionItemServiceImpl implements CollectionItemService {

    private final CollectionItemMapper collectionItemMapper;
    // 直接引入 Mapper，打破 Service 循环依赖
    private final CommunityPostMapper communityPostMapper;
    private final CollectionFolderMapper collectionFolderMapper;

    /**
     * 检测推文是否被收藏
     * @param userId
     * @param postId
     * @return
     */
    @Override
    public boolean isPostFavorited(final Long userId, final Long postId) {
        final LambdaQueryWrapper<CollectionItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CollectionItem::getUserId, userId)
                .eq(CollectionItem::getPostId, postId);
        return collectionItemMapper.exists(wrapper);
    }

    /**
     * 获取推文所在收藏夹
     * @param userId
     * @param postId
     * @return
     */
    @Override
    public List<String> getFolderIdsByPost(Long userId, Long postId) {
        final LambdaQueryWrapper<CollectionItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(CollectionItem::getFolderId)
                .eq(CollectionItem::getUserId, userId)
                .eq(CollectionItem::getPostId, postId);

        return collectionItemMapper.selectList(wrapper).stream()
                .map(item -> String.valueOf(item.getFolderId()))
                .collect(Collectors.toList());
    }

    /**
     * 添加收藏
     * @param userId
     * @param dto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FavoriteStatusVO addFavorite(final Long userId, final CollectionActionDTO dto) {
        final Long postId = dto.postId();
        final Long folderId = dto.folderId();

        // 1. 越权检查：直接使用 Mapper 实现
        // 逻辑：在数据库中寻找 ID 匹配且归属于当前用户的文件夹
        boolean isOwner = collectionFolderMapper.exists(new LambdaQueryWrapper<CollectionFolder>()
                .eq(CollectionFolder::getId, folderId)
                .eq(CollectionFolder::getUserId, userId)
                .eq(CollectionFolder::getIsDeleted, 0)); // 💡 加上逻辑删除检查更稳妥

        if (!isOwner) {
            // 如果没找到，说明文件夹不存在或者是别人的，直接抛出异常
            throw BusinessException.unauthorized(MessageConstant.FOLDER_NOT_FOUND);
        }

        // 2. 幂等检查
        if (isAlreadyInFolder(userId, postId, folderId)) {
            return getFavoriteStatus(userId, postId);
        }

        // 3. 核心计数逻辑：首个收藏才加一
        if (!this.isPostFavorited(userId, postId)) {
            // 直接通过 Mapper 执行原子更新 SQL
            communityPostMapper.incrementFavoriteCount(postId);
            log.debug("用户 {} 首次收藏推文 {}，Mapper 执行计数 +1", userId, postId);
        }

        // 4. 插入记录
        final CollectionItem item = CollectionItem.builder()
                .userId(userId)
                .postId(postId)
                .folderId(folderId)
                .createTime(LocalDateTime.now())
                .build();
        collectionItemMapper.insert(item);

        return getFavoriteStatus(userId, postId);
    }

    /**
     * 取消收藏
     * @param userId
     * @param dto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FavoriteStatusVO removeFavorite(final Long userId, final CollectionActionDTO dto) {
        final Long postId = dto.postId();
        final Long folderId = dto.folderId();

        // 1. 删除关联记录
        int deleted = collectionItemMapper.delete(new LambdaQueryWrapper<CollectionItem>()
                .eq(CollectionItem::getUserId, userId)
                .eq(CollectionItem::getPostId, postId)
                .eq(CollectionItem::getFolderId, folderId));

        if (deleted > 0) {
            // 2. 核心计数逻辑：如果删除后该推文不再被该用户收藏，则减一
            if (!this.isPostFavorited(userId, postId)) {
                // 直接通过 Mapper 执行原子更新 SQL
                communityPostMapper.decrementFavoriteCount(postId);
                log.debug("用户 {} 取消了对推文 {} 的最后一份收藏，计数 -1", userId, postId);
            }
        }

        return getFavoriteStatus(userId, postId);
    }

    /**
     * 整合推文收藏返回结果
     * @param userId
     * @param postId
     * @return
     */
    private FavoriteStatusVO getFavoriteStatus(final Long userId, final Long postId) {
        // 改为调用 Mapper 的 selectById
        final CommunityPost post = communityPostMapper.selectById(postId);
        final Integer totalFavorites = (post != null) ? post.getFavoriteCount() : 0;

        final List<String> folderIds = this.getFolderIdsByPost(userId, postId);

        return FavoriteStatusVO.builder()
                .favorited(!folderIds.isEmpty())
                .favorites(totalFavorites)
                .folderIds(folderIds)
                .build();
    }

    /**
     * 判断是否已经收藏过
     * @param userId
     * @param postId
     * @param folderId
     * @return
     */
    private boolean isAlreadyInFolder(Long userId, Long postId, Long folderId) {
        return collectionItemMapper.exists(new LambdaQueryWrapper<CollectionItem>()
                .eq(CollectionItem::getUserId, userId)
                .eq(CollectionItem::getPostId, postId)
                .eq(CollectionItem::getFolderId, folderId));
    }
}
