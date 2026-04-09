package com.aitrainer.service;

import com.aitrainer.dto.AddGuestbookDTO;
import com.aitrainer.dto.ReplyGuestbookDTO;
import com.aitrainer.vo.GuestbookVO;
import com.aitrainer.vo.PageResultVO;

import java.util.List;

public interface GuestbookService {
    /**
     * 查询用户收到的留言
     * @param userId
     * @param page
     * @param size
     * @return
     */
    PageResultVO<GuestbookVO> listReceivedMessages(Long userId, long page, long size);

    /**
     * 查询用户发出的留言
     * @param id
     * @param page
     * @param size
     * @return
     */
    PageResultVO<GuestbookVO> listSentMessages(Long id, long page, long size);

    /**
     * 发送留言
     * @param id
     * @param dto
     */
    void addMessage(Long id, AddGuestbookDTO dto);

    /**
     * 回复留言
     * @param id
     * @param id1
     * @param dto
     */
    void replyMessage(Long id, Long id1, ReplyGuestbookDTO dto);

    /**
     * 删除留言
     * @param id
     * @param id1
     */
    void removeMessage(Long id, Long id1);

    /**
     * 删除回复
     * @param id
     * @param id1
     */
    void removeReply(Long id, Long id1);
}
