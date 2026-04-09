package com.aitrainer.service;

import com.aitrainer.dto.CreateFolderDTO;
import com.aitrainer.dto.UpdateFolderDTO;
import com.aitrainer.vo.FolderVO;
import java.util.List;

public interface CollectionFolderService {
    /**
     * 初始化默认收藏夹（用于注册流程）
     */
    void initDefaultFolder(Long userId);

    /**
     * 创建自定义收藏夹
     */
    Long createFolder(Long userId, CreateFolderDTO dto);

    /**
     * 获取用户收藏夹列表（返回 VO）
     */
    List<FolderVO> listFoldersByUserId(Long userId, String keyword);

    /**
     * 逻辑删除收藏夹
     */
    boolean deleteFolder(Long folderId, Long userId);

    /**
     * 设置默认收藏夹
     * @param id
     * @param id1
     */
    void setDefaultFolder(Long id, Long id1);

    /**
     * 编辑收藏夹
     * @param id
     * @param id1
     * @param dto
     */
    void updateFolder(Long id, Long id1, UpdateFolderDTO dto);

    /**
     * 根据收藏夹id获取收藏夹
     * @param folderId
     * @param userId
     * @return
     */
    FolderVO getFolderById(final Long folderId, final Long userId);

    /**
     * 获取某个用户公开的收藏夹列表
     * @param targetUserId
     * @return
     */
    List<FolderVO> listPublicFoldersByUserId(Long targetUserId);
}