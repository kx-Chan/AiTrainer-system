package com.aitrainer.service;

import com.aitrainer.dto.CreatePostDTO;
import com.aitrainer.vo.CommunityPostVO;
import com.aitrainer.vo.PageResultVO;

public interface PostService {

    CommunityPostVO createPost(Long userId, CreatePostDTO dto);

    PageResultVO<CommunityPostVO> listAll(Long viewerId, long page, long size, String topic);

    PageResultVO<CommunityPostVO> listFollowing(Long userId, long page, long size);
}
