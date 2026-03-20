package com.aitrainer.common.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 自定义用户 Principal 对象，包含 ID 和用户名。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomUser implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID。
     */
    private Long id;

    /**
     * 用户名。
     */
    private String username;
}
