package com.aitrainer.controller;

import com.aitrainer.common.result.Result;
import com.aitrainer.common.security.CustomUser;
import com.aitrainer.dto.CollectionActionDTO;
import com.aitrainer.service.CollectionItemService;
import com.aitrainer.vo.FavoriteStatusVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收藏明细控制器
 */
@Tag(name = "收藏操作", description = "推文与收藏夹的关联操作接口")
@Slf4j
@RestController
@RequestMapping("/api/collection")
@RequiredArgsConstructor
public final class CollectionItemController {

    private final CollectionItemService itemService;

    @Operation(summary = "检查收藏状态", description = "检查当前用户是否已收藏某篇推文")
    @GetMapping("/favorited")
    public Result<Boolean> isFavorited(
            final Authentication authentication,
            @Parameter(description = "推文ID") @RequestParam final Long postId) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        final boolean favorited = itemService.isPostFavorited(user.getId(), postId);
        return Result.success(favorited);
    }

    @Operation(summary = "获取推文所在收藏夹", description = "查询该推文被当前用户收藏在了哪些收藏夹中")
    @GetMapping("/items")
    public Result<List<String>> getPostFolderIds(
            final Authentication authentication,
            @Parameter(description = "推文ID") @RequestParam final Long postId) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        final List<String> ids = itemService.getFolderIdsByPost(user.getId(), postId);
        return Result.success(ids);
    }

    @Operation(summary = "添加收藏", description = "将推文存入指定收藏夹")
    @PostMapping("/item")
    public Result<FavoriteStatusVO> add(
            final Authentication authentication,
            @Validated @RequestBody final CollectionActionDTO dto) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        log.info("用户 {} 收藏推文 {} 到收藏夹 {}", user.getUsername(), dto.postId(), dto.folderId());
        final FavoriteStatusVO status = itemService.addFavorite(user.getId(), dto);
        return Result.success(status);
    }

    @Operation(summary = "取消收藏", description = "从指定收藏夹中移除该推文")
    @DeleteMapping("/item")
    public Result<FavoriteStatusVO> remove(
            final Authentication authentication,
            @Validated @RequestBody final CollectionActionDTO dto) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        log.info("用户 {} 从收藏夹 {} 移除推文 {}", user.getUsername(), dto.folderId(), dto.postId());
        final FavoriteStatusVO status = itemService.removeFavorite(user.getId(), dto);
        return Result.success(status);
    }
}