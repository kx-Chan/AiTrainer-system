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
import com.aitrainer.entity.PostLike;
import com.aitrainer.entity.PostFavorite;
import com.aitrainer.entity.PostComment;
import com.aitrainer.mapper.PostLikeMapper;
import com.aitrainer.mapper.PostFavoriteMapper;
import com.aitrainer.mapper.PostCommentMapper;
import com.aitrainer.vo.LikeStatusVO;
import com.aitrainer.vo.FavoriteStatusVO;
import com.aitrainer.dto.CreateCommentDTO;
import com.aitrainer.vo.PostCommentVO;
import org.springframework.util.StringUtils;

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
    private final PostLikeMapper postLikeMapper;
    private final PostFavoriteMapper postFavoriteMapper;
    private final PostCommentMapper postCommentMapper;

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
                .authorId(userId)
                .avatar(avatar)
                .isPro(user.isPro())
                .time(post.getCreatedAt())
                .device(device)
                .topic(topic == null ? "" : "#" + topic)
                .content(content)
                .likes(0)
                .comments(0)
                .isLiked(false)
                .favorites(0)
                .isFavorited(false)
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

        // 1. 获取当前用户对这批推文的交互状态 (这是没法冗余的，必须查表)
        final Set<Long> likedByViewer = new java.util.HashSet<>();
        final Set<Long> favoritedByViewer = new java.util.HashSet<>();

        if (viewerId != null) {
            // 批量查询我点赞过的 ID
            postLikeMapper.selectList(new LambdaQueryWrapper<PostLike>()
                            .eq(PostLike::getUserId, viewerId).in(PostLike::getPostId, postIds))
                    .forEach(lk -> likedByViewer.add(lk.getPostId()));

            // 批量查询我收藏过的 ID
            postFavoriteMapper.selectList(new LambdaQueryWrapper<PostFavorite>()
                            .eq(PostFavorite::getUserId, viewerId).in(PostFavorite::getPostId, postIds))
                    .forEach(fav -> favoritedByViewer.add(fav.getPostId()));
        }

        final List<CommunityPostVO> records = posts.stream()
                .map(p -> {
                    final User u = userMap.get(p.getUserId());
                    final var prof = profileMap.get(p.getUserId());
                    final String author = normalizeAuthor(prof == null ? null : prof.getNickname(), u == null ? null : u.getUsername());
                    final String avatar = u == null ? null : ossService.generateAvatarUrl(u.getAvatar());
                    return CommunityPostVO.builder()
                            .id(p.getId())
                            .author(author)
                            .authorId(p.getUserId())
                            .avatar(avatar)
                            .isPro(u != null && Boolean.TRUE.equals(u.isPro()))
                            .time(p.getCreatedAt())
                            .device(p.getDevice())
                            .topic(p.getTopic() == null ? "" : "#" + p.getTopic())
                            .content(p.getContent())
                            .likes(p.getLikeCount())
                            .comments(p.getCommentCount())
                            .isLiked(likedByViewer.contains(p.getId()))
                            .favorites(p.getFavoriteCount())
                            .isFavorited(favoritedByViewer.contains(p.getId()))
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
     * 点赞推文
     * @param userId
     * @param postId
     * @return
     */
    @Override
    @Transactional
    public LikeStatusVO like(final Long userId, final Long postId) {
        if (userId == null) throw BusinessException.unauthorized(MessageConstant.USER_NOT_LOGGED_IN);
        final Long c = postLikeMapper.selectCount(new LambdaQueryWrapper<PostLike>()
                .eq(PostLike::getUserId, userId).eq(PostLike::getPostId, postId));
        if (c == null || c == 0) {
            postLikeMapper.insert(PostLike.builder().postId(postId).userId(userId).createdAt(LocalDateTime.now()).build());
        }
        // 增加点赞数量
        communityPostMapper.incrementLikeCount(postId);
        // 获取总的点赞数
        final int likes = communityPostMapper.selectById(postId).getLikeCount();
        return LikeStatusVO.builder().liked(true).likes(likes).build();
    }

    /**
     * 取消点赞
     * @param userId
     * @param postId
     * @return
     */
    @Override
    @Transactional
    public LikeStatusVO unlike(final Long userId, final Long postId) {
        if (userId == null) throw BusinessException.unauthorized(MessageConstant.USER_NOT_LOGGED_IN);
        postLikeMapper.delete(new LambdaQueryWrapper<PostLike>().eq(PostLike::getUserId, userId).eq(PostLike::getPostId, postId));
        // 减少点赞数量
        communityPostMapper.decrementLikeCount(postId);
        // 获取总的点赞数
        final int likes = communityPostMapper.selectById(postId).getLikeCount();
        return LikeStatusVO.builder().liked(false).likes(likes).build();
    }

    /**
     * 收藏推文
     * @param userId
     * @param postId
     * @return
     */
    @Override
    @Transactional
    public FavoriteStatusVO favorite(final Long userId, final Long postId) {
        if (userId == null) throw BusinessException.unauthorized(MessageConstant.USER_NOT_LOGGED_IN);
        final Long c = postFavoriteMapper.selectCount(new LambdaQueryWrapper<PostFavorite>()
                .eq(PostFavorite::getUserId, userId).eq(PostFavorite::getPostId, postId));
        if (c == null || c == 0) {
            postFavoriteMapper.insert(PostFavorite.builder().postId(postId).userId(userId).createdAt(LocalDateTime.now()).build());
        }
        // 增加点赞数量
        communityPostMapper.incrementfavoriteCount(postId);
        // 获取总的点赞数
        final int favorites = communityPostMapper.selectById(postId).getFavoriteCount();
        return FavoriteStatusVO.builder().favorited(true).favorites(favorites).build();
    }

    /**
     * 取消收藏
     * @param userId
     * @param postId
     * @return
     */
    @Override
    @Transactional
    public FavoriteStatusVO unfavorite(final Long userId, final Long postId) {
        if (userId == null) throw BusinessException.unauthorized(MessageConstant.USER_NOT_LOGGED_IN);
        postFavoriteMapper.delete(new LambdaQueryWrapper<PostFavorite>().eq(PostFavorite::getUserId, userId).eq(PostFavorite::getPostId, postId));
        // 增加点赞数量
        communityPostMapper.decrementfavoriteCount(postId);
        // 获取总的点赞数
        final int favorites = communityPostMapper.selectById(postId).getFavoriteCount();
        return FavoriteStatusVO.builder().favorited(false).favorites(favorites).build();
    }

    /**
     * 发表评论
     * @param userId
     * @param postId
     * @param dto
     * @return
     */
    @Override
    @Transactional
    public PostCommentVO addComment(final Long userId, final Long postId, final CreateCommentDTO dto) {
        // 判断用户是否登录
        if (userId == null) throw BusinessException.unauthorized(MessageConstant.USER_NOT_LOGGED_IN);
        final String content = dto.content() == null ? "" : dto.content().trim();
        // 判断该推文内容是否为空
        if (content.isBlank()) throw BusinessException.badRequest(MessageConstant.POST_CANNOT_BE_EMPTY);
        final PostComment c = PostComment.builder()
                .postId(postId).userId(userId).parentId(dto.parentId()).content(content).createdAt(LocalDateTime.now()).build();
        // 插入评论
        postCommentMapper.insert(c);
        communityPostMapper.incrementCommentCount(postId);

        final User u = userService.getById(userId);
        final var prof = profileService.getUserProfile(userId);
        final String author = normalizeAuthor(prof == null ? null : prof.getNickname(), u == null ? null : u.getUsername());
        final String avatar = u == null ? null : ossService.generateAvatarUrl(u.getAvatar());
        return PostCommentVO.builder()
                .id(c.getId()).userId(userId).author(author).avatar(avatar)
                .isPro(u != null && Boolean.TRUE.equals(u.isPro())).time(c.getCreatedAt()).content(content).parentId(dto.parentId())
                .build();
    }

    /**
     * 展示评论
     * @param userId
     * @param postId
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageResultVO<PostCommentVO> listComments(final Long userId, final Long postId, final long page, final long size) {
        final Page<PostComment> mp = new Page<>(page, size, true);
        // 通过postId获取该post对应的所有评论
        final LambdaQueryWrapper<PostComment> w = new LambdaQueryWrapper<PostComment>()
                .eq(PostComment::getPostId, postId).isNull(PostComment::getDeletedAt).orderByAsc(PostComment::getCreatedAt);
        postCommentMapper.selectPage(mp, w);
        final List<PostComment> rows = mp.getRecords();
        // 判断是否得到的评论是空
        if (rows == null || rows.isEmpty()) {
            return PageResultVO.<PostCommentVO>builder().records(List.of()).total(mp.getTotal()).page(mp.getCurrent()).size(mp.getSize()).build();
        }
        // 获取评论的用户
        final List<Long> uids = rows.stream().map(PostComment::getUserId).distinct().toList();
        final Map<Long, User> userMap = new HashMap<>();
        for (final User u : userService.listByIds(uids)) userMap.put(u.getId(), u);
        // 获取评论的用户信息
        final Map<Long, UserProfile> profileMap = new HashMap<>();
        for (final var p : profileService.listProfilesByIds(uids)) profileMap.put(p.getUserId(), p);
        final List<PostCommentVO> vos = rows.stream().map(r -> {
            final User u = userMap.get(r.getUserId());
            final var prof = profileMap.get(r.getUserId());
            final String author = normalizeAuthor(prof == null ? null : prof.getNickname(), u == null ? null : u.getUsername());
            final String avatar = u == null ? null : ossService.generateAvatarUrl(u.getAvatar());
            return PostCommentVO.builder()
                    .id(r.getId()).userId(r.getUserId()).author(author).avatar(avatar)
                    .isPro(u != null && Boolean.TRUE.equals(u.isPro())).time(r.getCreatedAt()).content(r.getContent()).parentId(r.getParentId())
                    .build();
        }).toList();
        return PageResultVO.<PostCommentVO>builder().records(vos).total(mp.getTotal()).page(mp.getCurrent()).size(mp.getSize()).build();
    }

    /**
     * 搜索功能
     * @param viewerId
     * @param keywordRaw
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageResultVO<CommunityPostVO> search(final Long viewerId, final String keywordRaw, final long page, final long size) {
        // 1. 基础清洗：去除空格
        if (keywordRaw == null || keywordRaw.trim().isEmpty()) {
            return listAll(viewerId, page, size, null);
        }
        final String kw = keywordRaw.trim();

        // --- 优先级 1：搜索话题 (Topic) ---
        // 假设数据库里存的是 "深蹲"，用户搜 "深" 也能匹配
        final Page<CommunityPost> p1 = new Page<>(page, size);
        communityPostMapper.selectPage(p1, new LambdaQueryWrapper<CommunityPost>()
                .like(CommunityPost::getTopic, kw)
                .orderByDesc(CommunityPost::getCreatedAt));

        if (p1.getTotal() > 0) {
            return buildPageVO(viewerId, p1);
        }

        // --- 优先级 2：搜索昵称 (Nickname) ---
        // 先去 profileService 查出所有名字里带 kw 的用户 ID
        final List<Long> matchedUserIds = profileService.searchUserIdsByNicknameLike(kw);
        if (!matchedUserIds.isEmpty()) {
            final Page<CommunityPost> p2 = new Page<>(page, size);
            communityPostMapper.selectPage(p2, new LambdaQueryWrapper<CommunityPost>()
                    .in(CommunityPost::getUserId, matchedUserIds)
                    .orderByDesc(CommunityPost::getCreatedAt));

            if (p2.getTotal() > 0) {
                return buildPageVO(viewerId, p2);
            }
        }

        // --- 优先级 3：搜索推文正文内容 (Content) ---
        // 直接在当前表里进行全文本模糊匹配
        final Page<CommunityPost> p3 = new Page<>(page, size);
        communityPostMapper.selectPage(p3, new LambdaQueryWrapper<CommunityPost>()
                .like(CommunityPost::getContent, kw)
                .orderByDesc(CommunityPost::getCreatedAt));

        if (p3.getTotal() > 0) {
            return buildPageVO(viewerId, p3);
        }

        // --- 最终兜底：啥也没搜到，返回空页面 ---
        return buildPageVO(viewerId, new Page<>(page, size));
    }

    /**
     * 展示我点赞的推文
     * @param userId
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageResultVO<CommunityPostVO> listMeLiked(final Long userId, final long page, final long size) {
        // 1. 先查点赞记录，按点赞时间倒序 (最新点赞的在最前)
        final Page<PostLike> mp = new Page<>(page, size, true);
        postLikeMapper.selectPage(mp, new LambdaQueryWrapper<PostLike>()
                .eq(PostLike::getUserId, userId)
                .orderByDesc(PostLike::getCreatedAt));

        final List<Long> ids = mp.getRecords().stream().map(PostLike::getPostId).toList();
        if (ids.isEmpty()) {
            return PageResultVO.<CommunityPostVO>builder().records(List.of()).total(mp.getTotal()).page(mp.getCurrent()).size(mp.getSize()).build();
        }

        // 2. 批量查出推文内容 (此时 SQL 的 IN 语句查出来的顺序通常是按 ID 排的，不是按 ids 列表排的)
        final List<CommunityPost> unsortedPosts = communityPostMapper.selectList(
                new LambdaQueryWrapper<CommunityPost>().in(CommunityPost::getId, ids)
        );

        // 3. 【核心步骤】按 ids 的顺序在内存中重排
        // 先转成 Map 方便 O(1) 查找
        Map<Long, CommunityPost> postMap = unsortedPosts.stream()
                .collect(Collectors.toMap(CommunityPost::getId, p -> p));

        // 按照原始 ids 的顺序重新构建 List
        List<CommunityPost> sortedPosts = ids.stream()
                .map(postMap::get)
                .filter(Objects::nonNull) // 防御性编程：万一对应的推文被物理删除了
                .toList();

        // 4. 将排好序的 List 塞回 Page 对象，交给 buildPageVO 处理
        final Page<CommunityPost> postPage = new Page<>(page, size, mp.getTotal());
        postPage.setRecords(sortedPosts);

        return buildPageVO(userId, postPage);
    }

    /**
     * 展示我评论的推文
     * @param userId
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageResultVO<CommunityPostVO> listMeCommented(final Long userId, final long page, final long size) {
        // 1. 调用自定义 SQL 获取这一页的 ID
        long offset = (page - 1) * size;
        List<Long> slice = postCommentMapper.selectCommentedPostIds(userId, offset, size);
        long total = postCommentMapper.countCommentedPosts(userId);

        if (slice.isEmpty()) {
            return PageResultVO.<CommunityPostVO>builder()
                    .records(List.of())
                    .total(total)
                    .page(page)
                    .size(size)
                    .build();
        }

        // 2. 批量查出推文实体
        List<CommunityPost> unsorted = communityPostMapper.selectList(
                new LambdaQueryWrapper<CommunityPost>().in(CommunityPost::getId, slice)
        );

        // 3. 【关键】由于数据库 IN 查询不保序，这里依然需要用 Map 在内存里“对齐”一次顺序
        Map<Long, CommunityPost> map = unsorted.stream()
                .collect(Collectors.toMap(CommunityPost::getId, p -> p));

        List<CommunityPost> sorted = slice.stream()
                .map(map::get)
                .filter(Objects::nonNull)
                .toList();

        // 4. 封装并返回
        Page<CommunityPost> postPage = new Page<>(page, size, total);
        postPage.setRecords(sorted);

        PageResultVO<CommunityPostVO> vo = buildPageVO(userId, postPage);
        vo.setTotal(total);
        return vo;
    }

    /**
     * 展示我发送的推文
     * @param userId
     * @param keyword
     * @param page
     * @param size
     * @return
     */
    public PageResultVO<CommunityPostVO> listMyPosts(final Long userId, final String keyword, final long page, final long size) {
        // 1. 初始化 MyBatis-Plus 分页对象
        Page<CommunityPost> postPage = new Page<>(page, size);

        // 2. 构建查询条件
        LambdaQueryWrapper<CommunityPost> wrapper = new LambdaQueryWrapper<>();

        // 核心条件：作者必须是当前用户
        wrapper.eq(CommunityPost::getUserId, userId);

        // 搜索条件：如果 keyword 不为空，则模糊匹配推文内容
        // 使用 StringUtils.hasText 也是计科中防止空字符串干扰的常用手段
        if (StringUtils.hasText(keyword)) {
            wrapper.like(CommunityPost::getContent, keyword);
        }

        // 排序：按发布时间倒序排列（最新发布的在最上面）
        wrapper.orderByDesc(CommunityPost::getCreatedAt);

        // 3. 执行分页查询
        // 这里直接使用 baseMapper 或 communityPostMapper 提供的 selectPage 方法
        communityPostMapper.selectPage(postPage, wrapper);

        // 4. 复用你代码中已有的 buildPageVO 逻辑进行转换
        // buildPageVO 内部应该已经处理了将 Entity 转换为 VO，以及点赞状态的注入
        PageResultVO<CommunityPostVO> vo = buildPageVO(userId, postPage);

        // 5. 显式设置分页元数据，确保前端分页组件正常工作
        vo.setTotal(postPage.getTotal());
        vo.setPage(page);
        vo.setSize(size);

        return vo;
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
