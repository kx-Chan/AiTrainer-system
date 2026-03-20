package com.aitrainer.service.impl;

import com.aitrainer.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * MailService 的实现类。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Override
    public void sendSimpleMail(String to, String subject, String content) {
        log.info("正在发送邮件给: {}, 主题: {}", to, subject);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        mailSender.send(message);
        log.info("邮件发送成功给: {}", to);
    }

    @Override
    public void sendVerificationCode(String to, String code) {
        String subject = "【AiTrainer】注册验证码";
        String content = "您的注册验证码为：" + code + "，有效期为 5 分钟。如非本人操作，请忽略此邮件。";
        sendSimpleMail(to, subject, content);
    }
}
