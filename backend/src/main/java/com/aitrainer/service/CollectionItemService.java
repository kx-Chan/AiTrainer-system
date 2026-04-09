package com.aitrainer.service;

import com.aitrainer.dto.CollectionActionDTO;
import com.aitrainer.vo.FavoriteStatusVO;

import java.util.List;

public interface CollectionItemService {
    /**
     * 判断这个推文是否被收藏
     * @param id
     * @param postId
     * @return
     */
    boolean isPostFavorited(Long id, Long postId);

    /**
     * 获取多少个收藏夹里有收藏这篇推文
     * @param id
     * @param postId
     * @return
     */
    List<String> getFolderIdsByPost(Long id, Long postId);

    /**
     * 添加收藏
     * @param id
     * @param dto
     * @return
     */
    FavoriteStatusVO addFavorite(Long id, CollectionActionDTO dto);

    /**
     * 取消收藏
     * @param id
     * @param dto
     * @return
     */
    FavoriteStatusVO removeFavorite(Long id, CollectionActionDTO dto);
}
