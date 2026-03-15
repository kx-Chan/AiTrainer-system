package com.aitrainer.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

/**
 * JWT 令牌处理工具类。
 */
@Slf4j
@Component
public final class JwtUtils {

    private final Key key;
    private final long expiration;

    /**
     * JwtUtils 构造函数。
     *
     * @param secret     JWT 密钥。
     * @param expiration JWT 过期时间（毫秒）。
     */
    public JwtUtils(@Value("${jwt.secret}") final String secret, @Value("${jwt.expiration}") final long expiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expiration = expiration;
    }

    /**
     * 为指定用户名生成 JWT 令牌。
     *
     * @param username 用户名。
     * @return JWT 令牌。
     */
    public String generateToken(final String username) {
        log.info("正在为用户生成令牌: {}", username);
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 验证 JWT 令牌是否有效。
     *
     * @param token JWT 令牌。
     * @return 如果有效返回 true，否则返回 false。
     */
    public boolean validateToken(final String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (final Exception e) {
            log.error("令牌验证失败", e);
            return false;
        }
    }

    /**
     * 从 JWT 令牌中提取用户名。
     *
     * @param token JWT 令牌。
     * @return 用户名。
     */
    public String getUsernameFromToken(final String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
    }
}
