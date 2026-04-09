package com.aitrainer.service;

import com.aitrainer.vo.FolderVO;
import com.aitrainer.vo.UserSpaceVO;

import java.util.List;

public interface UserSpaceService {
    /**
     * 获取用户空间的资料
     * @param id
     * @param userId
     * @return
     */
    UserSpaceVO getSpaceProfile(Long id, Long userId);

    /**
     * 获取用户的公开收藏夹列表
     * @param userId
     * @return
     */
    List<FolderVO> listPublicFolders(Long userId);
}
