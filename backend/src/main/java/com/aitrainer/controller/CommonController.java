package com.aitrainer.controller;

import com.aitrainer.common.exception.BusinessException;
import com.aitrainer.common.result.Result;
import com.aitrainer.common.security.CustomUser;
import com.aitrainer.entity.User;
import com.aitrainer.mapper.UserMapper;
import com.aitrainer.service.OssService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

/**
 * 通用控制器。
 *
 * <p>用于放置与具体业务解耦的通用能力接口，例如文件上传。</p>
 */
@Tag(name = "通用能力", description = "文件上传等通用接口")
@Slf4j
@RestController
@RequestMapping("/api/common")
@RequiredArgsConstructor
public class CommonController {

    private final OssService ossService;
    private final UserMapper userMapper;

    /**
     * 上传用户头像。
     *
     * <p>流程：</p>
     * <ol>
     *     <li>从 JWT Principal 获取 userId。</li>
     *     <li>上传文件到 OSS，返回对象 Key。</li>
     *     <li>将对象 Key 保存到 users.avatar（不直接保存公网永久链接）。</li>
     *     <li>生成带有效期的签名 URL 返回给前端用于展示。</li>
     * </ol>
     *
     * @param authentication 当前登录用户认证信息。
     * @param file           头像文件，字段名必须为 file。
     * @return 统一响应载体，data 为头像临时访问链接。
     * @throws BusinessException 当用户不存在或上传失败时抛出。
     */
    @Operation(summary = "上传头像", description = "上传用户头像到阿里云 OSS，并返回临时访问链接")
    @PostMapping(value = "/upload/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<String> uploadAvatar(Authentication authentication, @RequestPart("file") MultipartFile file) {
        // 1) 获取当前登录用户
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        final Long userId = user.getId();

        // 2) 上传到 OSS，拿到对象 Key（用于数据库持久化）
        final String objectKey = ossService.uploadAvatar(userId, file);

        // 3) 将对象 Key 保存到用户表，避免前端或日志暴露真实存储路径与权限信息
        final User dbUser = userMapper.selectById(userId);
        if (dbUser == null) {
            throw new BusinessException("无法找到当前登录用户");
        }
        dbUser.setAvatar(objectKey);
        dbUser.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(dbUser);

        // 4) 返回签名 URL（有时效），用于前端展示
        final String url = ossService.generateAvatarUrl(objectKey);
        log.info("用户 {} (ID: {}) 上传了新头像", user.getUsername(), userId);
        return Result.success(url);
    }
}
