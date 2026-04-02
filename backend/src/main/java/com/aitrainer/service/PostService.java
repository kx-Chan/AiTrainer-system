package com.aitrainer.service;

import com.aitrainer.dto.CreatePostDTO;
import com.aitrainer.dto.CreateCommentDTO;
import com.aitrainer.entity.CommunityPost;
import com.aitrainer.vo.CommunityPostVO;
import com.aitrainer.vo.FavoriteStatusVO;
import com.aitrainer.vo.LikeStatusVO;
import com.aitrainer.vo.PageResultVO;
import com.aitrainer.vo.PostCommentVO;

public interface PostService {

    CommunityPostVO createPost(Long userId, CreatePostDTO dto);

    PageResultVO<CommunityPostVO> listAll(Long viewerId, long page, long size, String topic);

    PageResultVO<CommunityPostVO> listFollowing(Long userId, long page, long size);

    LikeStatusVO like(Long userId, Long postId);

    LikeStatusVO unlike(Long userId, Long postId);

    FavoriteStatusVO favorite(Long userId, Long postId);

    FavoriteStatusVO unfavorite(Long userId, Long postId);

    PostCommentVO addComment(Long userId, Long postId, CreateCommentDTO dto);

    PageResultVO<PostCommentVO> listComments(Long userId, Long postId, long page, long size);

    PageResultVO<CommunityPostVO> search(Long viewerId, String keyword, long page, long size);

    PageResultVO<CommunityPostVO> listMeLiked(Long userId, long page, long size);

    PageResultVO<CommunityPostVO> listMeCommented(Long userId, long page, long size);

    PageResultVO<CommunityPostVO> listMyPosts(Long id, String keyword, long page, long size);

    void secureDeleteComment(Long id, Long commentId);

    public PageResultVO<CommunityPostVO> getFolderPosts(final Long userId, final Long folderId, final long page, final long size);

    void deletePost(Long id, Long postId);
}
