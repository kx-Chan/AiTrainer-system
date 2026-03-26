package com.aitrainer.service.impl;

import com.aitrainer.common.constant.MessageConstant;
import com.aitrainer.common.exception.BusinessException;
import com.aitrainer.dto.CreatePostDTO;
import com.aitrainer.entity.CommunityPost;
import com.aitrainer.entity.User;
import com.aitrainer.entity.PostImage;
import com.aitrainer.entity.UserProfile;
import com.aitrainer.mapper.CommunityPostMapper;
import com.aitrainer.mapper.PostImageMapper;
import com.aitrainer.service.PostService;
import com.aitrainer.service.ProfileService;
import com.aitrainer.service.UserService;
import com.aitrainer.service.OssService;
import com.aitrainer.vo.CommunityPostVO;
import com.aitrainer.vo.UserProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.aitrainer.vo.PageResultVO;
import com.aitrainer.service.FollowService;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private static final String DEFAULT_DEVICE = "AiTrainer App";

    private final CommunityPostMapper communityPostMapper;
    private final UserService userService;
    private final ProfileService profileService;
    private final PostImageMapper postImageMapper;
    private final OssService ossService;
    private final FollowService followService;

    /**
     * 发送推文
     */
    @Override
    @Transactional
    public CommunityPostVO createPost(final Long userId, final CreatePostDTO dto) {
        final User user = userService.getById(userId);
        if (user == null) {
            throw BusinessException.notFound(MessageConstant.USER_NOT_FOUND);
        }

        final String content = dto.content() == null ? "" : dto.content().trim();
        if (content.isBlank()) {
            throw BusinessException.badRequest(MessageConstant.POST_CANNOT_BE_EMPTY);
        }

        final String topic = normalizeTopic(dto.topic());
        final String device = normalizeDevice(dto.device());

        final CommunityPost post = CommunityPost.builder()
                .userId(userId)
                .content(content)
                .topic(topic)
                .device(device)
                .aiReportId(null)
                .build();

        communityPostMapper.insert(post);

        // 处理帖子上传的图片
        final List<String> images = new ArrayList<>();
        if (dto.imageKeys() != null && !dto.imageKeys().isEmpty()) {
            int sort = 0;
            for (final String key : dto.imageKeys()) {
                if (key == null || key.isBlank()) continue;
                final PostImage img = PostImage.builder()
                        .postId(post.getId())
                        .objectKey(key)
                        .sortOrder(sort++)
                        .build();
                postImageMapper.insert(img);
                final String url = ossService.generatePostImageUrl(key);
                if (url != null) {
                    images.add(url);
                }
            }
        }

        final UserProfileVO profile = profileService.getUserProfile(userId);
        final String author = normalizeAuthor(profile == null ? null : profile.getNickname(), user.getUsername());
        final String avatar = profile == null ? null : profile.getAvatar();

        return CommunityPostVO.builder()
                .id(post.getId())
                .author(author)
                .avatar(avatar)
                .isPro(user.isPro())
                .time(post.getCreatedAt())
                .device(device)
                .topic(topic == null ? "" : "#" + topic)
                .content(content)
                .likes(0)
                .comments(0)
                .isLiked(false)
                .isFollowing(true)
                .images(images)
                .build();
    }

    /**
     * 分页获取全部推文（可选话题筛选），按时间倒序
     * @param viewerId 当前查看用户ID，用于判定是否已关注作者
     * @param page 页码（从1开始）
     * @param size 每页条数
     * @param topic 话题（不带#），可空
     * @return 分页结果
     */
    @Override
    public PageResultVO<CommunityPostVO> listAll(final Long viewerId, final long page, final long size, final String topic) {
        final Page<CommunityPost> mpPage = new Page<>(page, size, true);
        final LambdaQueryWrapper<CommunityPost> wrapper = new LambdaQueryWrapper<CommunityPost>()
                .eq(topic != null && !topic.isBlank(), CommunityPost::getTopic, topic == null ? null : topic.trim())
                .orderByDesc(CommunityPost::getCreatedAt);
        communityPostMapper.selectPage(mpPage, wrapper);
        return buildPageVO(viewerId, mpPage);
    }

    /**
     * 分页获取关注用户的推文，按时间倒序
     * @param userId 当前用户ID
     * @param page 页码
     * @param size 每页
     * @return 分页结果
     */
    @Override
    public PageResultVO<CommunityPostVO> listFollowing(final Long userId, final long page, final long size) {
        final List<Long> followingIds = followService.listFollowingIds(userId);
        if (followingIds.isEmpty()) {
            return PageResultVO.<CommunityPostVO>builder()
                    .records(List.of())
                    .total(0)
                    .page(page)
                    .size(size)
                    .build();
        }
        final Page<CommunityPost> mpPage = new Page<>(page, size, true);
        final LambdaQueryWrapper<CommunityPost> wrapper = new LambdaQueryWrapper<CommunityPost>()
                .in(CommunityPost::getUserId, followingIds)
                .orderByDesc(CommunityPost::getCreatedAt);
        communityPostMapper.selectPage(mpPage, wrapper);
        return buildPageVO(userId, mpPage);
    }

    /**
     * 生成分页查询结果
     * @param viewerId
     * @param mpPage
     * @return
     */
    private PageResultVO<CommunityPostVO> buildPageVO(final Long viewerId, final Page<CommunityPost> mpPage) {
        // 获取分页查询到的所有推文
        final List<CommunityPost> posts = mpPage.getRecords();
        // 如果查询到的结果是空
        if (posts == null || posts.isEmpty()) {
            return PageResultVO.<CommunityPostVO>builder()
                    .records(List.of()) // 创建一个只读的空列表
                    .total(mpPage.getTotal())
                    .page(mpPage.getCurrent())
                    .size(mpPage.getSize())
                    .build();
        }

        // 获取所有userId
        final List<Long> userIds = posts.stream().map(CommunityPost::getUserId).distinct().toList();
        // 获取所有user，并放入map
        final Map<Long, User> userMap = new HashMap<>();
        for (final User u : userService.listByIds(userIds)) {
            userMap.put(u.getId(), u);
        }
        // 获取所有用户信息，并放入map
        final Map<Long, UserProfile> profileMap = new HashMap<>();
        for (final var p : profileService.listProfilesByIds(userIds)) {
            profileMap.put(p.getUserId(), p);
        }
        // 获取推文id的list
        final List<Long> postIds = posts.stream().map(CommunityPost::getId).toList();
        final Map<Long, List<String>> postImages = new HashMap<>();
        // 将获取到的所有图片都放入images变量里
        final var images = postImageMapper.selectList(new LambdaQueryWrapper<PostImage>()
                .in(PostImage::getPostId, postIds)
                .orderByAsc(PostImage::getSortOrder));
        // 根据postId进行分拣
        // 逻辑判断：它会检查 postImages 这个 Map 里是否已经有了这个 postId。
        // 自动初始化：如果没有（第一次遇到这张图），它会执行 k -> new ArrayList<>()，帮你创建一个空的 List 塞进去。
        // 链式添加：拿到（或创建好）List 后，立刻把经过 ossService 签名后的 完整 URL add 进去。
        for (final var img : images) {
            postImages.computeIfAbsent(img.getPostId(), k -> new ArrayList<>())
                    .add(ossService.generatePostImageUrl(img.getObjectKey()));
        }
        // 让前端判断是要显示+关注还是显示已关注
        final Set<Long> followingSet = (viewerId == null) ? Set.of() // 如果是游客登录，肯定是都是+关注
                : new java.util.HashSet<>(followService.listFollowingIds(viewerId)); // 获取改用户关注的id，并转成set，便于后续的contain判断是否已经关注

        final List<CommunityPostVO> records = posts.stream()
                .map(p -> {
                    final User u = userMap.get(p.getUserId());
                    final var prof = profileMap.get(p.getUserId());
                    final String author = normalizeAuthor(prof == null ? null : prof.getNickname(), u == null ? null : u.getUsername());
                    final String avatar = u == null ? null : ossService.generateAvatarUrl(u.getAvatar());
                    return CommunityPostVO.builder()
                            .id(p.getId())
                            .author(author)
                            .avatar(avatar)
                            .isPro(u != null && Boolean.TRUE.equals(u.isPro()))
                            .time(p.getCreatedAt())
                            .device(p.getDevice())
                            .topic(p.getTopic() == null ? "" : "#" + p.getTopic())
                            .content(p.getContent())
                            .likes(0)
                            .comments(0)
                            .isLiked(false)
                            .isFollowing(followingSet.contains(p.getUserId()))
                            .images(postImages.getOrDefault(p.getId(), List.of()))
                            .build();
                })
                .collect(Collectors.toList());
        return PageResultVO.<CommunityPostVO>builder()
                .records(records)
                .total(mpPage.getTotal())
                .page(mpPage.getCurrent())
                .size(mpPage.getSize())
                .build();
    }

    /**
     * 统一话题格式，便于搜索热门话题
     * @param raw
     * @return
     */
    private static String normalizeTopic(final String raw) {
        if (raw == null) return null;
        final String name = raw.replaceFirst("^#", "").trim();
        return name.isBlank() ? null : name;
    }

    /**
     * 统一设备格式，便于统计设备使用情况
     * @param raw
     * @return
     */
    private static String normalizeDevice(final String raw) {
        if (raw == null) return DEFAULT_DEVICE;
        final String v = raw.trim();
        return v.isBlank() ? DEFAULT_DEVICE : v;
    }

    /**
     * 显示作者，优先级 昵称>用户名>用户（默认）
     * @param nickname
     * @param username
     * @return
     */
    private static String normalizeAuthor(final String nickname, final String username) {
        if (nickname != null && !nickname.isBlank()) return nickname;
        if (username != null && !username.isBlank()) return username;
        return "用户";
    }
}
