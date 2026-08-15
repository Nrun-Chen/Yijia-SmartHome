package com.cqu.smarthome.demo.mqtt.handler;

import com.cqu.smarthome.demo.pojo.Mqttdata;
import com.cqu.smarthome.demo.service.ConditionalHttpCallService;
import com.cqu.smarthome.demo.service.MqttDataService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Iterator;
import java.util.Map;

@MessageEndpoint
@Component
public class MqttMessageHandler {

    @Autowired
    private MqttDataService mqttDataService;
    
    // 添加新服务依赖
    @Autowired
    private ConditionalHttpCallService conditionalHttpCallService;
    
    // 添加Jackson ObjectMapper用于解析JSON
    private final ObjectMapper objectMapper = new ObjectMapper();

    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void handleMessage(Message<?> message) {
        String topic = (String) message.getHeaders().get("mqtt_receivedTopic");
        String payload = (String) message.getPayload();
        Integer qos = (Integer) message.getHeaders().get("mqtt_receivedQos");
        Boolean retained = (Boolean) message.getHeaders().get("mqtt_receivedRetained");
        
        // 打印消息信息
        System.out.println(String.format("接收消息主题: %s", topic));
        System.out.println(String.format("接收消息QoS: %d", qos));
        System.out.println(String.format("接收消息内容: %s", payload));
        System.out.println(String.format("接收消息retained: %b", retained));

        // 处理并存储MQTT数据
        Mqttdata mqttdata = processAndStoreMqttData(topic, payload);
        
        // 检查条件并触发HTTP调用
        checkConditionsAndTriggerHttpCall(mqttdata, topic, payload);
    }
    
    /**
     * 增强版处理并存储MQTT数据，支持JSON格式和多种主题格式
     * @param topic 消息主题
     * @param payload 消息内容
     */
    // 修改返回值类型，返回处理后的mqttdata对象
    private Mqttdata processAndStoreMqttData(String topic, String payload) {
        try {
            // 创建MqttData对象
            Mqttdata mqttdata = new Mqttdata();
            mqttdata.setDataTopic(topic);
            mqttdata.setRawPayload(payload);
            
            // 处理不同类型的主题
            if (topic.equals("p_bigroom")) {
                // 处理bigroom主题的数据
                mqttdata.setRoomName("bigroom");
                mqttdata.setMessageType("ROOM_SENSOR_DATA");
                
                // 解析JSON格式的payload
                if (payload != null && (payload.startsWith("{") || payload.startsWith("["))) {
                    try {
                        // 预处理payload，修复包含单位的数值字段
                        String processedPayload = preprocessPayloadWithUnits(payload);
                        JsonNode rootNode = objectMapper.readTree(processedPayload);
                        
                        // 提取各个字段并设置到对应的实体类字段
                        if (rootNode.has("temperature")) {
                            mqttdata.setTemperature(rootNode.get("temperature").asDouble());
                        }
                        if (rootNode.has("humidity")) {
                            mqttdata.setHumidity(rootNode.get("humidity").asDouble());
                        }
                        if (rootNode.has("liv_lit")) {
                            mqttdata.setLivLit(rootNode.get("liv_lit").asInt());
                        }
                        if (rootNode.has("kit_lit")) {
                            mqttdata.setKitLit(rootNode.get("kit_lit").asInt());
                        }
                        if (rootNode.has("tol_lit")) {
                            mqttdata.setTolLit(rootNode.get("tol_lit").asInt());
                        }
                        if (rootNode.has("isHuman")) {
                            mqttdata.setIsHuman(rootNode.get("isHuman").asInt());
                        }
                        if (rootNode.has("sun")) {
                            mqttdata.setSun(rootNode.get("sun").asInt());
                        }
                        if (rootNode.has("adcdata")) {
                            mqttdata.setAdcData(rootNode.get("adcdata").asInt());
                        }
                        if (rootNode.has("senser_light")) {
                            mqttdata.setSenserLight(rootNode.get("senser_light").asInt());
                        }
                        if (rootNode.has("fan_level")) {
                            mqttdata.setFanLevel(rootNode.get("fan_level").asInt());
                        }
                        if (rootNode.has("alarmbell")) {
                            mqttdata.setAlarmBell(rootNode.get("alarmbell").asInt());
                        }
                        if (rootNode.has("gas")) {
                            mqttdata.setGas(rootNode.get("gas").asText());
                        }
                        // 添加对water_pump_level字段的处理
                        if (rootNode.has("water_pump_level")) {
                            mqttdata.setWaterPumpLevel(rootNode.get("water_pump_level").asInt());
                        }
                        // 添加对senser_on字段的处理
                        if (rootNode.has("senser_on")) {
                            mqttdata.setSenserOn(rootNode.get("senser_on").asInt());
                        }
                        
                        // 仍然可以将所有数据存储在sensorData字段，便于将来的扩展
                        mqttdata.setSensorData(processedPayload);
                        
                    } catch (Exception e) {
                        System.err.println("解析JSON失败: " + e.getMessage());
                        mqttdata.setMessageType("JSON_PARSE_ERROR");
                        // 即使解析失败，也存储原始数据以便后续分析
                        mqttdata.setSensorData("解析失败: " + e.getMessage());
                    }
                }
            } else {
                // 处理原有的device/{deviceId}/...格式主题
                Long deviceId = extractDeviceIdFromTopic(topic);
                mqttdata.setDeviceId(deviceId);
                mqttdata.setMessageType("DEVICE_DATA");
                
                // 尝试解析payload为整数
                try {
                    mqttdata.setDataValue(Integer.parseInt(payload.trim()));
                } catch (NumberFormatException e) {
                    // 如果不是整数，尝试解析为JSON
                    try {
                        if (payload != null && (payload.startsWith("{") || payload.startsWith("["))) {
                            JsonNode rootNode = objectMapper.readTree(payload);
                            mqttdata.setSensorData(payload);
                            mqttdata.setMessageType("DEVICE_JSON_DATA");
                            // 在处理bigroom主题的JSON数据部分添加
                            if (rootNode.has("senser_on")) {
                                mqttdata.setSenserOn(rootNode.get("senser_on").asInt());
                            }
                            
                            // 为了确保对所有可能的主题都能处理senser_on字段，在处理其他主题部分也添加
                            // 在尝试解析JSON的代码块中添加
                            if (rootNode.has("senser_on")) {
                                mqttdata.setSenserOn(rootNode.get("senser_on").asInt());
                            }
                        }
                    } catch (Exception je) {
                        // 既不是整数也不是JSON，存储为原始文本
                        mqttdata.setMessageType("RAW_TEXT");
                    }
                }
            }
            
            // 存储MQTT数据
            mqttDataService.saveMqttDataEnhanced(mqttdata);
            System.out.println("MQTT数据已存储: " + mqttdata);
            return mqttdata;
        } catch (Exception e) {
            System.err.println("处理MQTT数据失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 预处理包含单位的payload，将如"gas":332 ppm转换为标准JSON格式
     * @param payload 原始payload
     * @return 处理后的标准JSON字符串
     */
    private String preprocessPayloadWithUnits(String payload) {
        try {
            // 使用正则表达式替换带单位的数值字段，如"gas":332 ppm -> "gas":"332 ppm"
            // 注意在Java中需要对反斜杠进行转义
            String processed = payload.replaceAll(":(\\d+(\\.\\d+)?)\\s+([a-zA-Z]+)", ":\"$1 $3\"").trim();
            return processed;
        } catch (Exception e) {
            // 预处理失败时返回原始payload
            return payload;
        }
    }
    
    /**
     * 从主题中提取设备ID
     * @param topic 消息主题
     * @return 设备ID
     */
    private Long extractDeviceIdFromTopic(String topic) {
        try {
            // 假设主题格式为：device/{deviceId}/...
            String[] parts = topic.split("/");
            if (parts.length >= 2 && "device".equals(parts[0])) {
                return Long.parseLong(parts[1]);
            }
            // 可以根据实际主题格式调整解析逻辑
            return null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * 将payload解析为整数值
     * @param payload 消息内容
     * @return 整数值
     */
    private Integer parsePayloadToInteger(String payload) {
        try {
            // 直接尝试将payload解析为整数
            return Integer.parseInt(payload.trim());
        } catch (NumberFormatException e) {
            // 如果payload是JSON格式，可以在这里添加JSON解析逻辑
            return null;
        }
    }
    
    /**
     * 检查条件并触发HTTP调用
     */
    private void checkConditionsAndTriggerHttpCall(Mqttdata mqttdata, String topic, String payload) {
        if (mqttdata == null) {
            return;
        }
        
        // 这里可以根据需要自定义条件判断逻辑
        // 示例1: 当检测到燃气泄漏时触发
        boolean gasLeakDetected = false;
        try {
            gasLeakDetected = mqttdata.getGas() != null && 
                             !mqttdata.getGas().isEmpty() &&
                             Integer.parseInt(mqttdata.getGas().split(" ")[0]) > 500; // 假设500是阈值
        } catch (Exception e) {
            // 处理可能的解析异常
        }
        
        // 示例2: 当检测到人且时间在晚上时触发
        boolean nightHumanDetected = false;
        if ("1".equals(String.valueOf(mqttdata.getIsHuman()))) {
            int hour = java.time.LocalTime.now().getHour();
            nightHumanDetected = hour >= 22 || hour <= 6; // 晚上10点到早上6点
        }
        
        // 当满足任一条件时调用HTTP接口
        if (gasLeakDetected || nightHumanDetected) {
            String commandMessage = "liv_lit=0, kit_lit=0, tol_lit=0, fan_level=0";
            conditionalHttpCallService.callDeviceControlApi(
                true, 
                "bigroom", 
                commandMessage
            );
        }
    }
}