package com.aitrainer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3 配置类。
 */
@Configuration
public class OpenApiConfig {

    /**
     * 配置 OpenAPI 信息。
     *
     * @return OpenAPI 对象。
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AiTrainer API 文档")
                        .version("1.0.0")
                        .description("基于 Spring Boot 3.2 和 Java 17 的 AiTrainer 后端接口文档")
                        .contact(new Contact()
                                .name("架构师")
                                .email("admin@aitrainer.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
