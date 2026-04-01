package com.aitrainer.controller;

import com.aitrainer.common.result.Result;
import com.aitrainer.common.security.CustomUser;
import com.aitrainer.dto.CreateFolderDTO;
import com.aitrainer.dto.UpdateFolderDTO;
import com.aitrainer.service.CollectionFolderService;
import com.aitrainer.service.PostService;
import com.aitrainer.vo.CommunityPostVO;
import com.aitrainer.vo.FolderVO;
import com.aitrainer.vo.PageResultVO;
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
 * 收藏夹管理控制器
 */
@Tag(name = "收藏夹管理", description = "个人收藏夹的创建、查询与维护接口")
@Slf4j
@RestController
@RequestMapping("/api/collection/folders")
@RequiredArgsConstructor
public final class CollectionFolderController {

    private final CollectionFolderService folderService;
    private final PostService postService;

    @Operation(summary = "获取收藏夹列表", description = "获取当前登录用户的所有收藏夹，支持名称模糊搜索")
    @GetMapping
    public Result<List<FolderVO>> listFolders(
            final Authentication authentication,
            // 增加可选的 keyword 参数
            @Parameter(description = "搜索关键字")
            @RequestParam(required = false) final String keyword) {

        final CustomUser user = (CustomUser) authentication.getPrincipal();
        log.info("用户 {} 查询收藏夹列表, 关键字: {}", user.getUsername(), keyword);

        // 将 keyword 传给 Service
        final List<FolderVO> list = folderService.listFoldersByUserId(user.getId(), keyword);
        return Result.success(list);
    }

    @Operation(summary = "创建收藏夹", description = "用户自定义新建收藏夹")
    @PostMapping
    public Result<Long> create(
            final Authentication authentication,
            @Validated @RequestBody final CreateFolderDTO dto) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        log.info("用户 {} 创建收藏夹: {}", user.getUsername(), dto.name());
        final Long folderId = folderService.createFolder(user.getId(), dto);
        return Result.success(folderId);
    }

    @Operation(summary = "设为默认收藏夹", description = "将指定收藏夹设为默认，原默认收藏夹将取消")
    @PatchMapping("/{id}/default")
    public Result<Void> setDefault(
            final Authentication authentication,
            @Parameter(description = "收藏夹ID") @PathVariable final Long id) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        log.info("用户 {} 设置默认收藏夹: {}", user.getUsername(), id);
        folderService.setDefaultFolder(user.getId(), id);
        return Result.success();
    }

    @Operation(summary = "删除收藏夹", description = "逻辑删除收藏夹，默认收藏夹不允许删除")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            final Authentication authentication,
            @Parameter(description = "收藏夹ID") @PathVariable final Long id) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        log.info("用户 {} 请求删除收藏夹: {}", user.getUsername(), id);
        folderService.deleteFolder(id, user.getId());
        return Result.success();
    }

    @Operation(summary = "编辑收藏夹", description = "修改收藏夹的名称、描述或公开状态")
    @PutMapping("/{id}")
    public Result<Void> update(
            final Authentication authentication,
            @Parameter(description = "收藏夹ID") @PathVariable final Long id,
            @Validated @RequestBody final UpdateFolderDTO dto) {

        final CustomUser user = (CustomUser) authentication.getPrincipal();
        log.info("用户 {} 请求修改收藏夹 {}: {}", user.getUsername(), id, dto.name());

        folderService.updateFolder(user.getId(), id, dto);
        return Result.success();
    }

    @Operation(summary = "获取收藏夹的推文", description = "根据ID获取单个收藏夹的推文信息")
    @GetMapping("/{id}/posts")
    public Result<PageResultVO<CommunityPostVO>> getFolderPosts(
            final Authentication authentication,
            @PathVariable("id") Long folderId,
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size) {

        final CustomUser user = (CustomUser) authentication.getPrincipal();
        return Result.success(postService.getFolderPosts(user.getId(), folderId, page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取收藏夹详情页上的收藏夹名称")
    public Result<FolderVO> getFolderById(
            final Authentication authentication,
            @PathVariable final Long id) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();

        // 去 Service 查一下这个文件夹的信息
        FolderVO vo = folderService.getFolderById(id,user.getId());
        return Result.success(vo);
    }
}