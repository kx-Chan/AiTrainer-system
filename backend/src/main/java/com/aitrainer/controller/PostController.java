package com.aitrainer.controller;

import com.aitrainer.common.result.Result;
import com.aitrainer.common.security.CustomUser;
import com.aitrainer.dto.CreatePostDTO;
import com.aitrainer.dto.CreateCommentDTO;
import com.aitrainer.service.PostService;
import com.aitrainer.vo.CommunityPostVO;
import com.aitrainer.vo.FavoriteStatusVO;
import com.aitrainer.vo.LikeStatusVO;
import com.aitrainer.vo.PageResultVO;
import com.aitrainer.vo.PostCommentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "社区推文", description = "推文发布与查询接口")
@Validated
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @Operation(summary = "发布推文")
    @PostMapping
    public Result<CommunityPostVO> createPost(
            final Authentication authentication,
            @Validated @RequestBody final CreatePostDTO dto) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        return Result.success(postService.createPost(user.getId(), dto));
    }

    @Operation(summary = "分页获取推荐流", description = "按时间倒序，支持按话题筛选")
    @GetMapping
    public Result<PageResultVO<CommunityPostVO>> listAll(
            final Authentication authentication,
            @RequestParam(defaultValue = "1") final long page,
            @RequestParam(defaultValue = "10") final long size,
            @RequestParam(required = false) final String topic) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        return Result.success(postService.listAll(user.getId(), page, size, topic));
    }

    @Operation(summary = "分页获取关注流", description = "仅返回已关注用户的推文，按时间倒序")
    @GetMapping("/following")
    public Result<PageResultVO<CommunityPostVO>> listFollowing(
            final Authentication authentication,
            @RequestParam(defaultValue = "1") final long page,
            @RequestParam(defaultValue = "10") final long size) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        return Result.success(postService.listFollowing(user.getId(), page, size));
    }

    @Operation(summary = "搜索推文", description = "优先级 topic > 昵称 > content，均为部分匹配")
    @GetMapping("/search")
    public Result<PageResultVO<CommunityPostVO>> search(
            final Authentication authentication,
            @RequestParam("keyword") final String keyword,
            @RequestParam(defaultValue = "1") final long page,
            @RequestParam(defaultValue = "10") final long size) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        return Result.success(postService.search(user.getId(), keyword, page, size));
    }

    @Operation(summary = "点赞")
    @PostMapping("/{postId}/like")
    public Result<LikeStatusVO> like(
            final Authentication authentication,
            @PathVariable final Long postId) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        return Result.success(postService.like(user.getId(), postId));
    }

    @Operation(summary = "取消点赞")
    @DeleteMapping("/{postId}/like")
    public Result<LikeStatusVO> unlike(
            final Authentication authentication,
            @PathVariable final Long postId) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        return Result.success(postService.unlike(user.getId(), postId));
    }

    @Operation(summary = "收藏")
    @PostMapping("/{postId}/favorite")
    public Result<FavoriteStatusVO> favorite(
            final Authentication authentication,
            @PathVariable final Long postId) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        return Result.success(postService.favorite(user.getId(), postId));
    }

    @Operation(summary = "取消收藏")
    @DeleteMapping("/{postId}/favorite")
    public Result<FavoriteStatusVO> unfavorite(
            final Authentication authentication,
            @PathVariable final Long postId) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        return Result.success(postService.unfavorite(user.getId(), postId));
    }

    @Operation(summary = "发表评论")
    @PostMapping("/{postId}/comments")
    public Result<PostCommentVO> addComment(
            final Authentication authentication,
            @PathVariable final Long postId,
            @Validated @RequestBody final CreateCommentDTO dto) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        return Result.success(postService.addComment(user.getId(), postId, dto));
    }

    @Operation(summary = "分页获取评论")
    @GetMapping("/{postId}/comments")
    public Result<PageResultVO<PostCommentVO>> listComments(
            final Authentication authentication,
            @PathVariable final Long postId,
            @RequestParam(defaultValue = "1") final long page,
            @RequestParam(defaultValue = "10") final long size) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        return Result.success(postService.listComments(user.getId(), postId, page, size));
    }

    @Operation(summary = "我赞过的推文")
    @GetMapping("/me/liked")
    public Result<PageResultVO<CommunityPostVO>> meLiked(
            final Authentication authentication,
            @RequestParam(defaultValue = "1") final long page,
            @RequestParam(defaultValue = "10") final long size) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        return Result.success(postService.listMeLiked(user.getId(), page, size));
    }

    @Operation(summary = "我评论过的推文")
    @GetMapping("/me/commented")
    public Result<PageResultVO<CommunityPostVO>> meCommented(
            final Authentication authentication,
            @RequestParam(defaultValue = "1") final long page,
            @RequestParam(defaultValue = "10") final long size) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        return Result.success(postService.listMeCommented(user.getId(), page, size));
    }

    @Operation(summary = "搜索我的推文")
    @GetMapping("/me")
    public Result<PageResultVO<CommunityPostVO>> searchMyPosts(
            final Authentication authentication,
            @RequestParam(required = false) final String keyword, // 搜索关键词
            @RequestParam(defaultValue = "1") final long page,
            @RequestParam(defaultValue = "10") final long size) {

        // 从安全上下文提取当前登录用户
        final CustomUser user = (CustomUser) authentication.getPrincipal();

        // 调用 Service 逻辑
        return Result.success(postService.listMyPosts(user.getId(), keyword, page, size));
    }
}
