package com.cqu.smarthome.demo.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class VerificationCodeService {
    // 存储验证码
    private final Map<String, CodeInfo> codeStore = new HashMap<>();
    private final long CODE_EXPIRATION_MINUTES = 5;
    
    // 生成6位数字验证码
    public String generateCode(String email) {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        String codeStr = String.valueOf(code);
        
        // 存储验证码和过期时间
        long expirationTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(CODE_EXPIRATION_MINUTES);
        codeStore.put(email, new CodeInfo(codeStr, expirationTime));
        
        return codeStr;
    }
    
    // 验证验证码是否有效
    public boolean verifyCode(String email, String code) {
        CodeInfo codeInfo = codeStore.get(email);
        if (codeInfo == null) {
            return false;
        }
        
        // 检查验证码是否过期
        if (System.currentTimeMillis() > codeInfo.getExpirationTime()) {
            codeStore.remove(email);
            return false;
        }
        
        // 验证验证码是否匹配
        boolean isValid = codeInfo.getCode().equals(code);
        if (isValid) {
            // 验证成功后移除验证码，防止重复使用
            codeStore.remove(email);
        }
        
        return isValid;
    }
    
    // 内部类用于存储验证码信息
    private static class CodeInfo {
        private final String code;
        private final long expirationTime;
        
        public CodeInfo(String code, long expirationTime) {
            this.code = code;
            this.expirationTime = expirationTime;
        }
        
        public String getCode() {
            return code;
        }
        
        public long getExpirationTime() {
            return expirationTime;
        }
    }
}