package com.aitrainer.service.impl;

import com.aitrainer.service.MailService;
import com.aitrainer.service.VerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * VerificationService 的实现类，基于 Redis 存储。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {

    private final StringRedisTemplate redisTemplate;
    private final MailService mailService;

    private static final String CODE_PREFIX = "verify_code:";
    private static final long EXPIRE_TIME = 5; // 5 分钟有效期

    @Override
    public void sendCode(String email) {
        // 1. 生成 6 位随机验证码
        String code = String.format("%06d", new Random().nextInt(1000000));
        log.info("生成验证码: {} 给邮箱: {}", code, email);

        // 2. 存储到 Redis，设置有效期
        redisTemplate.opsForValue().set(CODE_PREFIX + email, code, EXPIRE_TIME, TimeUnit.MINUTES);

        // 3. 发送邮件
        mailService.sendVerificationCode(email, code);
    }

    @Override
    public boolean verifyCode(String email, String code) {
        String storedCode = redisTemplate.opsForValue().get(CODE_PREFIX + email);
        if (storedCode == null) {
            log.warn("验证码校验失败：验证码已过期或未发送至 {}", email);
            return false;
        }
        return storedCode.equals(code);
    }

    @Override
    public void consumeCode(String email) {
        redisTemplate.delete(CODE_PREFIX + email);
        log.info("已消耗邮箱 {} 的验证码", email);
    }
}
