package com.cqu.smarthome.demo.controller;

import com.cqu.smarthome.demo.pojo.Mqttdata;
import com.cqu.smarthome.demo.pojo.ResponseMessage;
import com.cqu.smarthome.demo.service.MqttDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
// 修改请求映射路径
@RequestMapping("/mqtt-data")
public class MqttDataController {

    @Autowired
    private MqttDataService mqttDataService;

    /**
     * 获取最新的MQTT数据（默认）
     * @return 最新的MQTT数据记录
     */
    @GetMapping("/latest")
    public ResponseMessage<Mqttdata> getLatestMqttData() {
        try {
            Mqttdata latestData = mqttDataService.getLatestMqttData();
            if (latestData != null) {
                return ResponseMessage.success(latestData);
            } else {
                return new ResponseMessage<>(HttpStatus.NOT_FOUND.value(), "暂无MQTT数据", null);
            }
        } catch (Exception e) {
            return new ResponseMessage<>(HttpStatus.BAD_REQUEST.value(), "获取最新数据失败：" + e.getMessage(), null);
        }
    }

    /**
     * 根据条件查询MQTT数据
     * 支持查询任意列数据
     */
    @GetMapping
    public ResponseMessage<List<Mqttdata>> queryMqttData(
            @RequestParam(required = false) Long deviceId,
            @RequestParam(required = false) String roomName,
            @RequestParam(required = false) String dataTopic,
            @RequestParam(required = false) String messageType,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        try {
            List<Mqttdata> dataList = mqttDataService.queryMqttData(deviceId, roomName, dataTopic, messageType, startTime, endTime, limit);
            return ResponseMessage.success(dataList);
        } catch (Exception e) {
            return new ResponseMessage<>(HttpStatus.BAD_REQUEST.value(), "查询数据失败：" + e.getMessage(), null);
        }
    }

    /**
     * 按列查询数据（支持动态条件查询）
     */
    @PostMapping("/custom-query")
    public ResponseMessage<List<Map<String, Object>>> customQueryMqttData(@RequestBody Map<String, Object> queryParams) {
        try {
            List<Map<String, Object>> results = mqttDataService.customQueryMqttData(queryParams);
            return ResponseMessage.success(results);
        } catch (Exception e) {
            return new ResponseMessage<>(HttpStatus.BAD_REQUEST.value(), "自定义查询失败：" + e.getMessage(), null);
        }
    }

    /**
     * 查询特定列的数据
     */
    @GetMapping("/specific-columns")
    public ResponseMessage<List<Map<String, Object>>> getMqttDataWithSpecificColumns(
            @RequestParam List<String> columns,
            @RequestParam(required = false) Long deviceId,
            @RequestParam(required = false) String roomName,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        try {
            List<Map<String, Object>> results = mqttDataService.getMqttDataWithSpecificColumns(
                    columns, deviceId, roomName, startTime, endTime, limit);
            return ResponseMessage.success(results);
        } catch (Exception e) {
            return new ResponseMessage<>(HttpStatus.BAD_REQUEST.value(), "查询特定列数据失败：" + e.getMessage(), null);
        }
    }
}