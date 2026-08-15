package com.cqu.smarthome.demo.service;

import com.cqu.smarthome.demo.config.ConditionalTriggerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

@Service
public class ConditionalHttpCallService {

    private static final Logger logger = LoggerFactory.getLogger(ConditionalHttpCallService.class);
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private ConditionalTriggerConfig triggerConfig;
    
    /**
     * 当条件满足时调用HTTP接口发送设备控制指令
     * @param conditionMet 条件是否满足
     * @param topic MQTT主题 (可选，为null时使用配置中的默认主题)
     * @param message 控制消息内容 (可选，为null时使用配置中的默认指令)
     * @return 是否调用成功
     */
    public boolean callDeviceControlApi(boolean conditionMet, String topic, String message) {
        if (!conditionMet) {
            logger.info("条件未满足，不执行HTTP调用");
            return false;
        }
        
        try {
            // 使用传入的值或默认配置
            String targetTopic = (topic != null) ? topic : triggerConfig.getDefaultTopic();
            String targetMessage = (message != null) ? message : triggerConfig.getDefaultCommand();
            
            // 构建请求URL
            String url = String.format("%s?topic=%s&message=%s", 
                                      triggerConfig.getApiUrl(),
                                      encodeParameter(targetTopic), 
                                      encodeParameter(targetMessage));
            
            logger.info("执行HTTP调用: {}", url);
            
            // 发送GET请求
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("HTTP调用成功，响应: {}", response.getBody());
                return true;
            } else {
                logger.error("HTTP调用失败，状态码: {}", response.getStatusCodeValue());
                return false;
            }
        } catch (Exception e) {
            logger.error("HTTP调用异常: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 简单的URL参数编码
     */
    private String encodeParameter(String param) {
        if (param == null) {
            return "";
        }
        // 在实际项目中应使用URLEncoder.encode
        return param.replace(" ", "%20").replace(",", "%2C");
    }
}