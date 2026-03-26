package com.aitrainer.controller;

import com.aitrainer.common.result.Result;
import com.aitrainer.common.security.CustomUser;
import com.aitrainer.dto.CreatePostDTO;
import com.aitrainer.service.PostService;
import com.aitrainer.vo.CommunityPostVO;
import com.aitrainer.vo.PageResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
