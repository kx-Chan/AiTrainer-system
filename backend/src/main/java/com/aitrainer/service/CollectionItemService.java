package com.aitrainer.service;

import com.aitrainer.dto.CollectionActionDTO;
import com.aitrainer.vo.FavoriteStatusVO;

import java.util.List;

public interface CollectionItemService {
    boolean isPostFavorited(Long id, Long postId);

    List<String> getFolderIdsByPost(Long id, Long postId);

    FavoriteStatusVO addFavorite(Long id, CollectionActionDTO dto);

    FavoriteStatusVO removeFavorite(Long id, CollectionActionDTO dto);
}
