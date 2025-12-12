package com.itheima.activiti.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;
import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;
    
    private SecretKey signingKey;
    
    // 使用Jakarta EE的@PostConstruct注解确保在依赖注入完成后初始化密钥
    @PostConstruct
    public void init() {
        // 确保secret不为空，如果为空使用默认密钥
        if (secret == null || secret.trim().isEmpty()) {
            secret = "default_jwt_secret_key_change_in_production";
        }
        
        // 使用配置的secret或默认密钥生成符合HS512算法要求的密钥
        // 如果secret长度不够，Keys类会自动处理
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    /**
     * 生成JWT token - 支持传入自定义claims
     */
    public String generateToken(String username, Map<String, Object> customClaims) {
        Map<String, Object> claims = new HashMap<>(customClaims);
        if (!claims.containsKey("sub")) {
            claims.put("sub", username);
        }
        if (!claims.containsKey("created")) {
            claims.put("created", new Date());
        }
        
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000)) // 转换为毫秒
                .signWith(signingKey) // 使用安全的密钥对象
                .compact();
    }
    
    /**
     * 生成JWT token - 基础版本
     */
    public String generateToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", username);
        claims.put("role", role);
        claims.put("created", new Date());
        
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000)) // 转换为毫秒
                .signWith(signingKey) // 使用安全的密钥对象
                .compact();
    }

    /**
     * 从Token中获取Claims
     */
    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder() // 使用parserBuilder代替parser
                    .setSigningKey(signingKey) // 使用安全的密钥对象
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            // Token过期
            throw e;
        } catch (MalformedJwtException e) {
            // Token格式错误
            throw e;
        } catch (SignatureException e) {
            // 签名错误
            throw e;
        } catch (Exception e) {
            // 其他错误
            return null;
        }
    }

    /**
     * 从Token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.getSubject() : null;
    }

    /**
     * 验证token是否有效（带用户名验证）
     */
    public boolean validateToken(String token, String username) {
        String tokenUsername = getUsernameFromToken(token);
        return (tokenUsername != null && tokenUsername.equals(username) && !isTokenExpired(token));
    }
    
    /**
     * 验证token是否有效（仅验证token本身）
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims != null && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 检查Token是否过期
     */
    private boolean isTokenExpired(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null && claims.getExpiration().before(new Date());
    }

    /**
     * 刷新Token
     */
    public String refreshToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            return null;
        }
        claims.setIssuedAt(new Date());
        return generateToken(claims.getSubject(), claims);
    }
}