package com.cqu.smarthome.demo.service;

// 在类顶部添加Jackson依赖
// 在类顶部添加配置属性依赖
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Service
public class MqttMessageSender {

    @Autowired
    private MessageChannel mqttOutboundChannel;
    
    // 从配置文件中读取MQTT主题配置
    @Value("${mqtt.topic}")
    private String mqttTopics;
    
    // 注入或创建ObjectMapper实例
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 发送MQTT消息
     * @param topic 主题
     * @param payload 消息内容
     */
    public void sendMessage(String topic, String payload) {
        mqttOutboundChannel.send(MessageBuilder.withPayload(payload)
                .setHeader("mqtt_topic", topic)
                .build());
    }
    
    /**
     * 发送带QoS的MQTT消息
     * @param topic 主题
     * @param payload 消息内容
     * @param qos QoS级别
     */
    public void sendMessage(String topic, String payload, int qos) {
        mqttOutboundChannel.send(MessageBuilder.withPayload(payload)
                .setHeader("mqtt_topic", topic)
                .setHeader("mqtt_qos", qos)
                .build());
    }
    
    /**
     * 发送带保留标志的MQTT消息
     * @param topic 主题
     * @param payload 消息内容
     * @param qos QoS级别
     * @param retained 是否保留
     */
    public void sendMessage(String topic, String payload, int qos, boolean retained) {
        mqttOutboundChannel.send(MessageBuilder.withPayload(payload)
                .setHeader("mqtt_topic", topic)
                .setHeader("mqtt_qos", qos)
                .setHeader("mqtt_retained", retained)
                .build());
    }
    
    /**
     * 发送场景相关的MQTT消息（字符串命令版本）
     * @param sceneId 场景ID
     * @param action 动作类型 (activate/deactivate/execute)
     * @param commands 设备命令字符串，格式如'liv_lit=1'
     */
    public void sendSceneMessage(Long sceneId, String action, String commands) {
        try {
            // 直接发送到bigroom主题
            String targetTopic = "bigroom";
            
            // 直接发送原始命令字符串，保持'liv_lit=1'这样的格式
            sendMessage(targetTopic, commands, 1, true);
            System.out.println("已发送场景" + action + "通知消息到主题 bigroom: " + sceneId + ", 消息: " + commands);
        } catch (Exception e) {
            System.err.println("发送MQTT消息失败: " + e.getMessage());
            throw new RuntimeException("发送场景消息失败", e);
        }
    }
    
    /**
     * 发送场景相关的MQTT消息（列表版本）
     * @param sceneId 场景ID
     * @param action 动作类型 (activate/deactivate/execute)
     * @param deviceOperations 设备操作列表
     */
    public void sendSceneMessage(Long sceneId, String action, List<Map<String, Object>> deviceOperations) {
        try {
            // 直接发送到bigroom主题
            String targetTopic = "bigroom";
            
            // 遍历设备操作列表，将每个Map转换为'key=value'格式的字符串
            for (Map<String, Object> deviceOperation : deviceOperations) {
                for (Map.Entry<String, Object> entry : deviceOperation.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    // 构建'key=value'格式的消息
                    String message = key + "=" + value;
                    sendMessage(targetTopic, message, 1, true);
                    System.out.println("已发送场景" + action + "通知消息到主题 bigroom: " + sceneId + ", 消息: " + message);
                }
            }
        } catch (Exception e) {
            System.err.println("发送MQTT消息失败: " + e.getMessage());
            throw new RuntimeException("发送场景消息失败", e);
        }
    }


    // 可以保留这个方法，但我们现在不使用它了
    /**
     * 发送消息到配置文件中定义的所有MQTT主题
     */
    private void sendToConfiguredTopics(String payload) {
        // 分割配置的主题字符串
        String[] topics = mqttTopics.split(",");
        
        // 发送到每个主题
        for (String topic : topics) {
            try {
                // 移除主题字符串中的空格
                String cleanTopic = topic.trim();
                sendMessage(cleanTopic, payload, 1, true);
                System.out.println("已发送MQTT消息到主题 " + cleanTopic + ": " + payload);
            } catch (Exception e) {
                System.err.println("发送到主题 " + topic + " 失败: " + e.getMessage());
            }
        }
    }
}
