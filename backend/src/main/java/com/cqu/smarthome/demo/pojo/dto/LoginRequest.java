package com.cqu.smarthome.demo.pojo.dto;

import org.springframework.stereotype.Component;

@Component
public class LoginRequest {
    private String identifier; // 可以是手机号或邮箱
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }


}