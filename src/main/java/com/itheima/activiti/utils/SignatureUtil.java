package com.itheima.activiti.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;

/**
 * 签名工具类，用于生成和验证API签名
 */
public class SignatureUtil {

    private static final Logger logger = LoggerFactory.getLogger(SignatureUtil.class);
    
    /**
     * 签名算法
     */
    private static final String HMAC_SHA256 = "HmacSHA256";
    
    /**
     * 生成API签名
     * @param secretKey 密钥
     * @param params 请求参数
     * @param timestamp 时间戳
     * @return 签名结果
     */
    public static String generateSignature(String secretKey, Map<String, String> params, String timestamp) {
        try {
            // 将参数按字典顺序排序
            TreeMap<String, String> sortedParams = new TreeMap<>(params);
            
            // 构建待签名字符串
            StringBuilder sb = new StringBuilder();
            sb.append(secretKey);
            for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
                sb.append(entry.getKey()).append(entry.getValue());
            }
            sb.append(timestamp);
            
            // 打印调试信息
            logger.info("生成签名的参数: {}", sortedParams);
            logger.info("生成签名的时间戳: {}", timestamp);
            logger.info("生成签名的密钥: {}", secretKey);
            logger.info("生成签名的字符串: {}", sb.toString());
            
            // 生成HMAC-SHA256签名
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(secretKeySpec);
            byte[] signatureBytes = mac.doFinal(sb.toString().getBytes(StandardCharsets.UTF_8));
            
            // 转换为Base64编码
            String signature = Base64.getEncoder().encodeToString(signatureBytes);
            logger.info("生成的签名: {}", signature);
            
            return signature;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            logger.error("生成签名失败: {}", e.getMessage(), e);
            throw new RuntimeException("生成签名失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 验证API签名
     * @param secretKey 密钥
     * @param params 请求参数
     * @param timestamp 时间戳
     * @param signature 待验证的签名
     * @return 是否验证通过
     */
    public static boolean verifySignature(String secretKey, Map<String, String> params, String timestamp, String signature) {
        try {
            // 验证时间戳，允许5分钟的时间差
            long now = System.currentTimeMillis();
            long requestTime = Long.parseLong(timestamp);
            if (Math.abs(now - requestTime) > 5 * 60 * 1000) {
                logger.warn("签名时间戳已过期: {}", timestamp);
                return false;
            }
            
            // 生成预期签名
            String expectedSignature = generateSignature(secretKey, params, timestamp);
            
            // 比较签名
            return expectedSignature.equals(signature);
        } catch (NumberFormatException e) {
            logger.error("无效的时间戳格式: {}", timestamp, e);
            return false;
        }
    }
}
