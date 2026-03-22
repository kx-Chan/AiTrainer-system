package com.aitrainer.service.impl;

import com.aitrainer.common.exception.BusinessException;
import com.aitrainer.service.OssService;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

/**
 * 阿里云 OSS 实现类。
 *
 * <p>安全策略：</p>
 * <ul>
 *     <li>访问凭证通过环境变量注入（application.yml 中仅保留占位符），避免硬编码与泄露。</li>
 *     <li>前端不直接拿到对象 Key，仅返回带时效的签名 URL 用于展示。</li>
 *     <li>仅允许图片类型与限定大小，避免上传任意可执行文件。</li>
 * </ul>
 */
@Slf4j
@Service
public class OssServiceImpl implements OssService {

    /**
     * 允许上传的头像 MIME 类型白名单。
     */
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    /**
     * 头像最大大小：2MB。
     */
    private static final long MAX_AVATAR_BYTES = 2L * 1024 * 1024;

    /**
     * OSS Endpoint，例如：https://oss-cn-hangzhou.aliyuncs.com
     */
    @Value("${aliyun.oss.endpoint:}")
    private String endpoint;

    /**
     * OSS Bucket 名称。
     */
    @Value("${aliyun.oss.bucket:}")
    private String bucket;

    /**
     * 阿里云 AccessKeyId（仅从环境变量读取）。
     */
    @Value("${aliyun.oss.access-key-id:}")
    private String accessKeyId;

    /**
     * 阿里云 AccessKeySecret（仅从环境变量读取）。
     */
    @Value("${aliyun.oss.access-key-secret:}")
    private String accessKeySecret;

    /**
     * 头像临时访问链接有效期（秒）。
     */
    @Value("${aliyun.oss.avatar-url-expire-seconds:3600}")
    private long avatarUrlExpireSeconds;

    /**
     * 上传用户头像到 OSS，并返回对象 Key。
     *
     * @param userId 用户 ID。
     * @param file   头像文件。
     * @return OSS 对象 Key。
     * @throws BusinessException 参数非法、文件不合法或上传失败时抛出。
     */
    @Override
    public String uploadAvatar(final Long userId, final MultipartFile file) {
        // 1) 参数与文件校验（防止空请求与超大文件）
        if (userId == null) {
            throw new BusinessException("用户 ID 不能为空");
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的头像文件");
        }
        if (file.getSize() > MAX_AVATAR_BYTES) {
            throw new BusinessException("头像文件不能超过 2MB");
        }

        // 2) OSS 配置校验（凭证缺失时不允许继续）
        if (!StringUtils.hasText(endpoint) || !StringUtils.hasText(bucket)
                || !StringUtils.hasText(accessKeyId) || !StringUtils.hasText(accessKeySecret)) {
            throw new BusinessException("OSS 配置不完整，请检查环境变量配置");
        }

        // 3) 类型白名单校验（仅允许图片 MIME 类型）
        final String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException("头像格式不支持，仅支持 JPG/PNG/WEBP/GIF");
        }

        // 4) 生成对象 Key：按用户分目录 + 随机文件名，避免覆盖
        final String extension = resolveExtension(file.getOriginalFilename(), contentType);
        final String objectKey = "avatars/" + userId + "/" + UUID.randomUUID() + extension;

        OSS ossClient = null;
        try {
            // 5) 上传文件到 OSS
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            final ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(contentType);

            try (InputStream inputStream = file.getInputStream()) {
                ossClient.putObject(bucket, objectKey, inputStream, metadata);
            }
            return objectKey;
        } catch (final Exception e) {
            // 注意：日志仅记录 userId，不输出 endpoint/AK 等敏感信息
            log.error("上传头像失败 userId={}", userId, e);
            throw new BusinessException("头像上传失败，请稍后重试");
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 生成头像对象的临时访问链接（签名 URL）。
     *
     * @param objectKey OSS 对象 Key。
     * @return 临时访问 URL；当 objectKey 或配置缺失时返回 null。
     */
    @Override
    public String generateAvatarUrl(final String objectKey) {
        // objectKey 为空时直接返回
        if (!StringUtils.hasText(objectKey)) {
            return null;
        }

        // OSS 配置缺失时不生成 URL（避免误用）
        if (!StringUtils.hasText(endpoint) || !StringUtils.hasText(bucket)
                || !StringUtils.hasText(accessKeyId) || !StringUtils.hasText(accessKeySecret)) {
            return null;
        }

        OSS ossClient = null;
        try {
            // 使用签名 URL 控制访问有效期，避免永久暴露对象
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            final Duration expire = Duration.ofSeconds(Math.max(60, avatarUrlExpireSeconds));
            final Date expiration = new Date(System.currentTimeMillis() + expire.toMillis());
            final URL url = ossClient.generatePresignedUrl(bucket, objectKey, expiration);
            return url == null ? null : url.toString();
        } catch (final Exception e) {
            log.error("生成头像临时链接失败 objectKey={}", objectKey, e);
            return null;
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 根据 contentType 推断扩展名，尽量避免依赖用户传入的文件名。
     *
     * @param originalFilename 原始文件名（可能为空）。
     * @param contentType      MIME 类型。
     * @return 文件扩展名（包含点号），无法识别则返回空字符串。
     */
    private static String resolveExtension(final String originalFilename, final String contentType) {
        if ("image/jpeg".equals(contentType)) {
            return ".jpg";
        }
        if ("image/png".equals(contentType)) {
            return ".png";
        }
        if ("image/webp".equals(contentType)) {
            return ".webp";
        }
        if ("image/gif".equals(contentType)) {
            return ".gif";
        }

        if (StringUtils.hasText(originalFilename) && originalFilename.contains(".")) {
            final String ext = originalFilename.substring(originalFilename.lastIndexOf('.'));
            if (ext.length() <= 10) {
                return ext.toLowerCase();
            }
        }
        return "";
    }
}
