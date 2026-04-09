package com.aitrainer.controller;

import com.aitrainer.common.security.CustomUser;
import com.aitrainer.dto.AddGuestbookDTO;
import com.aitrainer.dto.ReplyGuestbookDTO;
import com.aitrainer.service.GuestbookService;
import com.aitrainer.vo.GuestbookVO;
import com.aitrainer.vo.PageResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import com.aitrainer.common.result.Result;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "留言板管理")
@Slf4j
@RestController
@RequestMapping("/api/guestbook")
@RequiredArgsConstructor
public final class GuestbookController {

    private final GuestbookService guestbookService;

    @Operation(summary = "分页查询收到的留言")
    @GetMapping("/received/{userId}")
    public Result<PageResultVO<GuestbookVO>> listReceived(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        return Result.success(guestbookService.listReceivedMessages(userId, page, size));
    }

    @Operation(summary = "分页查询我发出的留言")
    @GetMapping("/sent")
    public Result<PageResultVO<GuestbookVO>> listSent(
            final Authentication authentication,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        return Result.success(guestbookService.listSentMessages(user.getId(), page, size));
    }

    @Operation(summary = "发布留言", description = "在他人空间留下足迹")
    @PostMapping
    public Result<Void> add(
            final Authentication authentication,
            @Validated @RequestBody final AddGuestbookDTO dto) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        guestbookService.addMessage(user.getId(), dto);
        return Result.success();
    }

    @Operation(summary = "回复留言", description = "空间主人对留言进行回复")
    @PutMapping("/reply/{id}")
    public Result<Void> reply(
            final Authentication authentication,
            @PathVariable Long id,
            @Validated @RequestBody final ReplyGuestbookDTO dto) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        guestbookService.replyMessage(user.getId(), id, dto);
        return Result.success();
    }

    @Operation(summary = "删除留言", description = "留言者或空间主人均可删除，执行逻辑删除")
    @DeleteMapping("/{id}")
    public Result<Void> remove(
            final Authentication authentication,
            @PathVariable Long id) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        log.info("用户 {} 请求删除留言 {}", user.getId(), id);
        guestbookService.removeMessage(user.getId(), id);
        return Result.success();
    }

    @Operation(summary = "删除/撤回回复", description = "仅空间主人可操作，将回复内容置空")
    @DeleteMapping("/reply/{id}")
    public Result<Void> removeReply(
            final Authentication authentication,
            @PathVariable Long id) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        log.info("用户 {} 请求撤回回复 {}", user.getId(), id);
        guestbookService.removeReply(user.getId(), id);
        return Result.success();
    }
}
