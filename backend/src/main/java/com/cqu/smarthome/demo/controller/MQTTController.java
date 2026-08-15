package com.cqu.smarthome.demo.controller;

import com.cqu.smarthome.demo.service.MqttMessageSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/mqtt")
public class MQTTController {
    
    @Autowired
    private MqttMessageSender mqttMessageSender;


    /**
     * 基本的消息发送接口
     */
    @PostMapping("/send")
    public String send(@RequestParam String topic, @RequestParam String message) {
        mqttMessageSender.sendMessage(topic, message);
        return "Message sent to topic " + topic;
    }
    
    /**
     * 带QoS的消息发送接口
     */
    @PostMapping("/send/qos")
    public String sendWithQos(@RequestParam String topic, @RequestParam String message, 
                             @RequestParam(defaultValue = "1") int qos) {
        mqttMessageSender.sendMessage(topic, message, qos);
        return "Message sent to topic " + topic + " with QoS " + qos;
    }
    
    /**
     * 设备控制接口
     */
    @PostMapping("/device/control")
    public String controlDevice(@RequestParam String deviceId, 
                              @RequestParam String command) {
        // 构造设备控制主题
        String topic = "device/" + deviceId + "/control";
        mqttMessageSender.sendMessage(topic, command);
        return "Control command sent to device " + deviceId;
    }
    

}
