package com.aitrainer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AiTrainer 后端主启动类。
 */
@SpringBootApplication
public class AiTrainerApplication {

    /**
     * 启动 Spring Boot 应用程序的主方法。
     *
     * @param args 命令行参数。
     */
    public static void main(final String[] args) {
        SpringApplication.run(AiTrainerApplication.class, args);
    }
}
