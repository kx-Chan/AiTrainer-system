package com.aitrainer.service;

/**
 * 验证码服务接口。
 */
public interface VerificationService {

    /**
     * 发送 6 位数字验证码到指定邮箱。
     *
     * @param email 电子邮箱。
     */
    void sendCode(String email);

    /**
     * 校验验证码是否正确（不消耗）。
     *
     * @param email 电子邮箱。
     * @param code  6位验证码。
     * @return 校验通过返回 true。
     */
    boolean verifyCode(String email, String code);

    /**
     * 消耗（删除）指定邮箱的验证码。
     *
     * @param email 电子邮箱。
     */
    void consumeCode(String email);
}
