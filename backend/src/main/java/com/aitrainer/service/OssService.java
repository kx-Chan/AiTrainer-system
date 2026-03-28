package com.aitrainer.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * OSS 文件服务。
 *
 * <p>用于处理用户头像等静态资源的上传与访问链接生成。</p>
 */
public interface OssService {

    /**
     * 上传用户头像并返回 OSS 对象 Key。
     *
     * @param userId 用户 ID。
     * @param file   头像文件。
     * @return OSS 对象 Key（用于后续生成临时访问链接或持久化到数据库）。
     * @throws com.aitrainer.common.exception.BusinessException 当参数非法、文件不合法或上传失败时抛出。
     */
    String uploadAvatar(Long userId, MultipartFile file);

    /**
     * 根据 OSS 对象 Key 生成头像临时访问链接。
     *
     * <p>为保护数据安全，避免直接暴露公网永久链接，默认返回带有效期的签名 URL。</p>
     *
     * @param objectKey OSS 对象 Key。
     * @return 临时访问 URL；当 objectKey 或 OSS 配置为空时返回 null。
     */
    String generateAvatarUrl(String objectKey);

    /**
     * 上传帖子图片并返回 OSS 对象 Key。
     *
     * @param userId 用户 ID。
     * @param file   图片文件。
     * @return OSS 对象 Key。
     */
    String uploadPostImage(Long userId, MultipartFile file);

    /**
     * 根据 OSS 对象 Key 生成帖子图片的临时访问链接。
     *
     * @param objectKey OSS 对象 Key。
     * @return 临时访问 URL；当 objectKey 或配置缺失时返回 null。
     */
    String generatePostImageUrl(String objectKey);

    /**
     * 清除指定 ObjectKey 的 URL 缓存
     * @param objectKey
     */
    public void evictUrlCache(final String objectKey);

    /**
     * 从 OSS 物理删除指定对象。
     *
     * @param objectKey OSS 对象 Key。
     */
    void deleteObject(String objectKey);
}
