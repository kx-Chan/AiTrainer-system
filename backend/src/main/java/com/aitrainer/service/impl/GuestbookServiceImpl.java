package com.aitrainer.service.impl;

import com.aitrainer.common.constant.MessageConstant;
import com.aitrainer.common.exception.BusinessException;
import com.aitrainer.dto.AddGuestbookDTO;
import com.aitrainer.dto.ReplyGuestbookDTO;
import com.aitrainer.entity.Guestbook;
import com.aitrainer.mapper.GuestbookMapper;
import com.aitrainer.service.GuestbookService;
import com.aitrainer.service.ProfileService;
import com.aitrainer.service.UserService;
import com.aitrainer.vo.GuestbookVO;
import com.aitrainer.vo.PageResultVO;
import com.aitrainer.vo.UserProfileVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GuestbookServiceImpl implements GuestbookService {

    private final GuestbookMapper guestbookMapper;
    private final ProfileService profileService; // 用于获取留言者的头像和昵称
    private final UserService userService; // 用于检查用户是否已注销

    /**
     * 获取收到的留言的分页查询
     * @param userId
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageResultVO<GuestbookVO> listReceivedMessages(Long userId, long page, long size) {
        // 1. 创建 MyBatis-Plus 分页对象
        Page<Guestbook> pageParam = new Page<>(page, size);

        // 2. 执行分页查询
        IPage<Guestbook> guestbookPage = guestbookMapper.selectPage(pageParam,
                new LambdaQueryWrapper<Guestbook>()
                        .eq(Guestbook::getToUserId, userId)
                        .orderByDesc(Guestbook::getCreateTime)
        );

        // 3. 转换 Entity 到 VO
        List<GuestbookVO> voList = convertToVOList(guestbookPage.getRecords());

        // 4. 封装成通用的分页结果对象
        return PageResultVO.<GuestbookVO>builder()
                .records(voList)
                .total(guestbookPage.getTotal())
                .page(page)
                .size(size)
                .build();
    }

    /**
     * 获取发送的留言的分页查询
     * @param currentUserId
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageResultVO<GuestbookVO> listSentMessages(Long currentUserId, long page, long size) {

        // 1. 构造分页对象
        Page<Guestbook> pageParam = new Page<>(page, size);

        // 2. 执行分页查询：筛选 from_user_id 为当前用户的记录
        IPage<Guestbook> resultPage = guestbookMapper.selectPage(pageParam,
                new LambdaQueryWrapper<Guestbook>()
                        .eq(Guestbook::getFromUserId, currentUserId)
                        .orderByDesc(Guestbook::getCreateTime)
        );

        // 3. 转换并聚合用户信息 (VO 转换)
        List<GuestbookVO> voList = convertToVOList(resultPage.getRecords());

        // 4. 返回统一的分页结果包装类
        return PageResultVO.<GuestbookVO>builder()
                .records(voList)
                .total(resultPage.getTotal())
                .page(page)
                .size(size)
                .build();
    }

    /**
     * 发送留言
     * @param fromUserId
     * @param dto
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addMessage(Long fromUserId, AddGuestbookDTO dto) {
        if (fromUserId.equals(dto.toUserId())) {
            throw BusinessException.badRequest(MessageConstant.CANNOT_LEAVE_MSG_TO_SELF);
        }
        
        // 检查目标用户是否已注销
        if (userService.isDeactivated(dto.toUserId())) {
            throw BusinessException.badRequest(MessageConstant.USER_DEACTIVATED);
        }
        
        Guestbook msg = new Guestbook();
        msg.setFromUserId(fromUserId);
        msg.setToUserId(dto.toUserId());
        msg.setContent(dto.content());
        msg.setCreateTime(LocalDateTime.now());
        guestbookMapper.insert(msg);
    }

    /**
     * 回复留言
     * @param currentUserId
     * @param msgId
     * @param dto
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replyMessage(Long currentUserId, Long msgId, ReplyGuestbookDTO dto) {
        Guestbook msg = guestbookMapper.selectById(msgId);
        if (msg == null) throw BusinessException.notFound(MessageConstant.GUESTBOOK_NOT_FOUND);

        // 核心安全校验：只有留言的接收者（主人）才能回复
        if (!msg.getToUserId().equals(currentUserId)) {
            throw BusinessException.unauthorized(MessageConstant.GUESTBOOK_REPLY_FORBIDDEN);
        }

        msg.setReplyContent(dto.replyContent());
        msg.setReplyTime(LocalDateTime.now());
        guestbookMapper.updateById(msg);
    }

    /**
     * 删除留言
     * @param currentUserId
     * @param msgId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeMessage(Long currentUserId, Long msgId) {
        // 1. 获取留言详情
        Guestbook msg = guestbookMapper.selectById(msgId);
        if (msg == null) return;

        // 2. 权限校验：发件人 OR 收件人
        boolean isSender = msg.getFromUserId().equals(currentUserId);
        boolean isOwner = msg.getToUserId().equals(currentUserId);

        if (isSender || isOwner) {
            // 由于实体类加了 @TableLogic，这里会自动变成 UPDATE ... SET is_deleted = 1
            guestbookMapper.deleteById(msgId);
            log.info("留言 {} 已被用户 {} 逻辑删除", msgId, currentUserId);
        } else {
            throw BusinessException.unauthorized(MessageConstant.GUESTBOOK_DELETE_FORBIDDEN);
        }
    }

    /**
     * 删除回复
     * @param currentUserId
     * @param msgId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeReply(Long currentUserId, Long msgId) {
        // 1. 获取留言详情
        Guestbook msg = guestbookMapper.selectById(msgId);
        if (msg == null) return;

        // 2. 权限校验：只有空间主人（收件人）能撤回回复
        if (!msg.getToUserId().equals(currentUserId)) {
            throw BusinessException.unauthorized(MessageConstant.GUESTBOOK_WITHDRAW_FORBIDDEN);
        }

        // 3. 强制置空回复字段
        // MyBatis-Plus 的 updateById 会忽略 null 字段，必须用 UpdateWrapper
        int rows = guestbookMapper.update(null, new LambdaUpdateWrapper<Guestbook>()
                .set(Guestbook::getReplyContent, null)
                .set(Guestbook::getReplyTime, null)
                .eq(Guestbook::getId, msgId)
        );

        if (rows > 0) {
            log.info("用户 {} 撤回了留言 {} 的回复内容", currentUserId, msgId);
        }
    }

    // 私有辅助方法：组装 VO
    private List<GuestbookVO> convertToVOList(List<Guestbook> entities) {
        return entities.stream().map(msg -> {
            // 通过 ProfileService 获取用户信息，保证数据一致性
            UserProfileVO fromUser = profileService.getUserProfile(msg.getFromUserId());
            UserProfileVO toUser = profileService.getUserProfile(msg.getToUserId());

            return GuestbookVO.builder()
                    .id(msg.getId())
                    .fromUserId(msg.getFromUserId())
                    .fromUserName(fromUser != null ? fromUser.getNickname() : "已注销用户")
                    .fromUserAvatar(fromUser != null ? fromUser.getAvatar() : "")
                    .toUserId(msg.getToUserId())
                    .content(msg.getContent())
                    .createTime(msg.getCreateTime())
                    .replyContent(msg.getReplyContent())
                    .replyTime(msg.getReplyTime())
                    .build();
        }).collect(Collectors.toList());
    }
}
