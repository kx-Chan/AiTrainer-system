package com.aitrainer.config;

import com.aitrainer.mapper.UserMapper;
import com.aitrainer.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 用于播种测试数据的初始化器。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public final class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final UserMapper userMapper;

    /**
     * 在应用程序启动时运行以播种用户。
     *
     * @param args 命令行参数。
     */
    @Override
    public void run(final String... args) {
        log.info("开始初始化测试数据...");

        // 如果数据库已有数据，则跳过初始化
        if (userMapper.selectCount(new LambdaQueryWrapper<>()) > 0) {
            log.info("测试数据已存在，跳过初始化流程。");
            return;
        }

        try {
            // 播种 'admin' 用户（老用户）
            userService.createUser("admin", "admin@aitrainer.com", "123456", false);

            // 播种 'newbie' 用户（新用户）
            userService.createUser("newbie", "newbie@aitrainer.com", "123456", true);

            log.info("测试数据初始化成功：已创建测试账号 admin 和 newbie (密码均为 123456)");
        } catch (final Exception e) {
            log.error("测试数据初始化失败", e);
        }
    }
}
