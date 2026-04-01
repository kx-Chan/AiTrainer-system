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

    void setDefaultFolder(Long id, Long id1);

    void updateFolder(Long id, Long id1, UpdateFolderDTO dto);

}