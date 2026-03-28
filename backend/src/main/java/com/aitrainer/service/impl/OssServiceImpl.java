package com.aitrainer.service.impl;

import com.aitrainer.common.exception.BusinessException;
import com.aitrainer.common.constant.MessageConstant;
import com.aitrainer.service.OssService;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
@RequiredArgsConstructor
public class OssServiceImpl implements OssService {

    private final StringRedisTemplate redisTemplate;

    // Redis Key 的统一前缀，方便管理
    private static final String OSS_URL_CACHE_PREFIX = "aitrainer:oss:url:";

    // 缓存时间：1 天（单位：秒）
    private static final long CACHE_SECONDS = 24 * 3600L;

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
     * 帖子图片最大大小：5MB。
     */
    private static final long MAX_POST_IMAGE_BYTES = 5L * 1024 * 1024;

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
            throw BusinessException.badRequest(MessageConstant.OSS_USER_ID_EMPTY);
        }
        if (file == null || file.isEmpty()) {
            throw BusinessException.badRequest(MessageConstant.AVATAR_FILE_EMPTY);
        }
        if (file.getSize() > MAX_AVATAR_BYTES) {
            throw BusinessException.badRequest(MessageConstant.AVATAR_FILE_TOO_LARGE);
        }

        // 2) OSS 配置校验（凭证缺失时不允许继续）
        if (!StringUtils.hasText(endpoint) || !StringUtils.hasText(bucket)
                || !StringUtils.hasText(accessKeyId) || !StringUtils.hasText(accessKeySecret)) {
            throw new BusinessException(MessageConstant.OSS_CONFIG_INCOMPLETE);
        }

        // 3) 类型白名单校验（仅允许图片 MIME 类型）
        final String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw BusinessException.badRequest(MessageConstant.AVATAR_TYPE_NOT_SUPPORTED);
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
            throw new BusinessException(MessageConstant.AVATAR_UPLOAD_FAILED);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 上传帖子图片，返回对象 Key。
     *
     * @param userId
     * @param file
     * @return
     */
    @Override
    public String uploadPostImage(final Long userId, final MultipartFile file) {
        if (userId == null) {
            throw BusinessException.badRequest(MessageConstant.OSS_USER_ID_EMPTY);
        }
        if (file == null || file.isEmpty()) {
            throw BusinessException.badRequest(MessageConstant.AVATAR_FILE_EMPTY);
        }
        if (file.getSize() > MAX_POST_IMAGE_BYTES) {
            throw BusinessException.badRequest(MessageConstant.AVATAR_FILE_TOO_LARGE);
        }
        if (!StringUtils.hasText(endpoint) || !StringUtils.hasText(bucket)
                || !StringUtils.hasText(accessKeyId) || !StringUtils.hasText(accessKeySecret)) {
            throw new BusinessException(MessageConstant.OSS_CONFIG_INCOMPLETE);
        }
        final String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw BusinessException.badRequest(MessageConstant.AVATAR_TYPE_NOT_SUPPORTED);
        }

        final String extension = resolveExtension(file.getOriginalFilename(), contentType);
        final String objectKey = "posts/" + userId + "/" + UUID.randomUUID() + extension;

        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            final ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(contentType);
            try (InputStream inputStream = file.getInputStream()) {
                ossClient.putObject(bucket, objectKey, inputStream, metadata);
            }
            return objectKey;
        } catch (final Exception e) {
            log.error("上传帖子图片失败 userId={}", userId, e);
            throw new BusinessException(MessageConstant.AVATAR_UPLOAD_FAILED);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 生成头像临时链接（带 Redis 缓存）
     */
    @Override
    public String generateAvatarUrl(final String objectKey) {
        return getCacheOrGenerateUrl(objectKey, "avatar");
    }

    /**
     * 生成帖子图片临时链接（带 Redis 缓存）
     */
    @Override
    public String generatePostImageUrl(final String objectKey) {
        return getCacheOrGenerateUrl(objectKey, "post");
    }

    /**
     * 核心逻辑：先查 Redis，没有则生成并存入 Redis
     */
    private String getCacheOrGenerateUrl(final String objectKey, final String type) {
        // 1. 基本校验，检查是否有传入key
        if (!StringUtils.hasText(objectKey)) {
            return null;
        }

        // 2. 构造 Redis Key (例如 aitrainer:oss:url:avatars/1/xxx.jpg)
        final String cacheKey = OSS_URL_CACHE_PREFIX + objectKey;

        try {
            // 3. 尝试从 Redis 获取
            String cachedUrl = redisTemplate.opsForValue().get(cacheKey);
            if (StringUtils.hasText(cachedUrl)) {
                log.debug("OSS 链接命中缓存 type={}, key={}", type, objectKey);
                return cachedUrl;
            }

            // 4. 缓存未命中，执行 OSS 签名逻辑
            if (!isConfigComplete()) {
                return null;
            }

            String newUrl = generatePresignedUrl(objectKey);

            if (newUrl != null) {
                // 5. 存入 Redis，设置 1 天过期
                redisTemplate.opsForValue().set(cacheKey, newUrl, CACHE_SECONDS, TimeUnit.SECONDS);
                log.debug("生成新 OSS 链接并存入缓存 type={}, key={}", type, objectKey);
            }
            return newUrl;

        } catch (Exception e) {
            // 如果 Redis 挂了，降级处理：直接生成链接返回，不影响业务
            log.error("Redis 缓存处理失败，尝试直接生成 OSS 链接 objectKey={}", objectKey, e);
            return generatePresignedUrl(objectKey);
        }
    }

    /**
     * 内部封装：OSS 签名生成逻辑
     */
    private String generatePresignedUrl(final String objectKey) {
        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            // 注意：签名有效期必须 >= Redis 缓存时间，这里设为 25 小时（多留 1 小时缓冲）
            final long expirationMillis = System.currentTimeMillis() + (CACHE_SECONDS + 3600) * 1000;
            final Date expiration = new Date(expirationMillis);

            final URL url = ossClient.generatePresignedUrl(bucket, objectKey, expiration);
            return url == null ? null : url.toString();
        } catch (final Exception e) {
            log.error("调用 OSS 生成签名失败 objectKey={}", objectKey, e);
            return null;
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 检查 OSS 配置是否完整
     */
    private boolean isConfigComplete() {
        return StringUtils.hasText(endpoint) && StringUtils.hasText(bucket)
                && StringUtils.hasText(accessKeyId) && StringUtils.hasText(accessKeySecret);
    }

    /**
     * 清除指定 ObjectKey 的 URL 缓存
     */
    @Override
    public void evictUrlCache(final String objectKey) {
        if (StringUtils.hasText(objectKey)) {
            String cacheKey = "aitrainer:oss:url:" + objectKey;
            redisTemplate.delete(cacheKey);
            log.debug("已清除 OSS 缓存 Key: {}", cacheKey);
        }
    }

    @Override
    public void deleteObject(final String objectKey) {
        // 1) 基本校验：Key 为空则直接返回
        if (!StringUtils.hasText(objectKey)) {
            return;
        }

        // 2) 配置校验
        if (!isConfigComplete()) {
            log.warn("OSS 配置不完整，无法执行删除操作 objectKey={}", objectKey);
            return;
        }

        OSS ossClient = null;
        try {
            // 3) 构建客户端并执行删除
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            ossClient.deleteObject(bucket, objectKey);

            log.info("OSS 文件物理删除成功: {}", objectKey);
        } catch (final Exception e) {
            // 这里的异常通常只记录日志，不建议抛出，以免因为清理垃圾失败导致换头像等主流程崩溃
            log.error("物理删除 OSS 文件失败 objectKey={}", objectKey, e);
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
