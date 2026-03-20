package com.aitrainer.service;

/**
 * 邮件发送服务接口。
 */
public interface MailService {

    /**
     * 发送简单的文本邮件。
     *
     * @param to      收件人邮箱。
     * @param subject 邮件主题。
     * @param content 邮件内容。
     */
    void sendSimpleMail(String to, String subject, String content);

    /**
     * 发送注册验证码邮件。
     *
     * @param to   收件人邮箱。
     * @param code 6位验证码。
     */
    void sendVerificationCode(String to, String code);
}
