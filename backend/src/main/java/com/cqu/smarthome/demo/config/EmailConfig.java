package com.cqu.smarthome.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailConfig {

    @Bean
    public String fromEmail() {
        // 这里返回实际的发件人邮箱地址
        return "your-email@example.com";
    }
}