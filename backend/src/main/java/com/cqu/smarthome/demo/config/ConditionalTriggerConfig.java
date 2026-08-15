package com.cqu.smarthome.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "conditional.trigger")
public class ConditionalTriggerConfig {
    
    // HTTP API配置
    private String apiUrl = "http://localhost:8088/api/mqtt/send";
    
    // 默认控制指令
    private String defaultCommand = "liv_lit=0, kit_lit=0, tol_lit=0, fan_level=0";
    
    // 默认主题
    private String defaultTopic = "bigroom";
    
    // 燃气泄漏阈值
    private int gasLeakThreshold = 500;
    
    // 夜间时间范围 (开始小时)
    private int nightStartHour = 22;
    
    // 夜间时间范围 (结束小时)
    private int nightEndHour = 6;
    
    // getter和setter方法
    public String getApiUrl() {
        return apiUrl;
    }
    
    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }
    
    public String getDefaultCommand() {
        return defaultCommand;
    }
    
    public void setDefaultCommand(String defaultCommand) {
        this.defaultCommand = defaultCommand;
    }
    
    public String getDefaultTopic() {
        return defaultTopic;
    }
    
    public void setDefaultTopic(String defaultTopic) {
        this.defaultTopic = defaultTopic;
    }
    
    public int getGasLeakThreshold() {
        return gasLeakThreshold;
    }
    
    public void setGasLeakThreshold(int gasLeakThreshold) {
        this.gasLeakThreshold = gasLeakThreshold;
    }
    
    public int getNightStartHour() {
        return nightStartHour;
    }
    
    public void setNightStartHour(int nightStartHour) {
        this.nightStartHour = nightStartHour;
    }
    
    public int getNightEndHour() {
        return nightEndHour;
    }
    
    public void setNightEndHour(int nightEndHour) {
        this.nightEndHour = nightEndHour;
    }
}