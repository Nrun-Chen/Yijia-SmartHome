package com.cqu.smarthome.demo.service;

import com.cqu.smarthome.demo.dao.MqttdataDao;
import com.cqu.smarthome.demo.pojo.Mqttdata;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class MqttDataService {

    @Autowired
    private MqttdataDao mqttDataDao;

    /**
     * 存储MQTT消息数据
     * @param deviceId 设备ID
     * @param topic 主题
     * @param value 数据值
     * @return 存储的Mqttdata对象
     */
    @Transactional
    public Mqttdata saveMqttData(Long deviceId, String topic, Integer value) {
        Mqttdata mqttdata = new Mqttdata();
        mqttdata.setDeviceId(deviceId);
        mqttdata.setDataTime(LocalDateTime.now());
        mqttdata.setDataTopic(topic);
        mqttdata.setDataValue(value);
        return mqttDataDao.save(mqttdata);
    }
    
    /**
     * 增强版存储MQTT消息数据，支持复杂JSON格式和房间主题
     * @param mqttdata 完整的MqttData对象
     * @return 存储的Mqttdata对象
     */
    @Transactional
    public Mqttdata saveMqttDataEnhanced(Mqttdata mqttdata) {
        if (mqttdata.getDataTime() == null) {
            mqttdata.setDataTime(LocalDateTime.now());
        }
        return mqttDataDao.save(mqttdata);
    }

    /**
     * 根据设备ID和时间范围查询MQTT数据
     * @param deviceId 设备ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return MQTT数据列表
     */
    public List<Mqttdata> getMqttDataByDeviceIdAndTimeRange(Long deviceId, LocalDateTime startTime, LocalDateTime endTime) {
        return mqttDataDao.findByDeviceIdAndDataTimeBetweenOrderByDataTimeDesc(deviceId, startTime, endTime);
    }

    /**
     * 根据设备ID和主题查询MQTT数据
     * @param deviceId 设备ID
     * @param topic 主题
     * @return MQTT数据列表
     */
    public List<Mqttdata> getMqttDataByDeviceIdAndTopic(Long deviceId, String topic) {
        return mqttDataDao.findByDeviceIdAndDataTopicOrderByDataTimeDesc(deviceId, topic);
    }

    /**
     * 获取设备的最新MQTT数据
     * @param deviceId 设备ID
     * @return 最新的MQTT数据
     */
    public Mqttdata getLatestMqttDataByDeviceId(Long deviceId) {
        return mqttDataDao.findTopByDeviceIdOrderByDataTimeDesc(deviceId);
    }
    
    /**
     * 根据房间名称查询MQTT数据
     * @param roomName 房间名称
     * @return MQTT数据列表
     */
    public List<Mqttdata> getMqttDataByRoomName(String roomName) {
        return mqttDataDao.findByRoomName(roomName);
    }
    
    /**
     * 根据房间名称和时间范围查询MQTT数据
     * @param roomName 房间名称
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return MQTT数据列表
     */
    public List<Mqttdata> getMqttDataByRoomNameAndTimeRange(String roomName, LocalDateTime startTime, LocalDateTime endTime) {
        return mqttDataDao.findByRoomNameAndDataTimeBetween(roomName, startTime, endTime);
    }
    
    /**
     * 获取房间的最新MQTT数据
     * @param roomName 房间名称
     * @return 最新的MQTT数据
     */
    public Mqttdata getLatestMqttDataByRoomName(String roomName) {
        List<Mqttdata> data = getMqttDataByRoomName(roomName);
        if (data != null && !data.isEmpty()) {
            return data.stream()
                .max(Comparator.comparing(Mqttdata::getDataTime))
                .orElse(null);
        }
        return null;
    }
    
    /**
     * 根据消息类型查询MQTT数据
     * @param messageType 消息类型
     * @return MQTT数据列表
     */
    public List<Mqttdata> getMqttDataByMessageType(String messageType) {
        return mqttDataDao.findByMessageType(messageType);
    }

    public Mqttdata getLatestMqttData() {
        // 由于CrudRepository不支持直接查询所有记录中的最新一条，我们先查询所有记录
        // 实际项目中应该使用JdbcTemplate或自定义SQL查询来优化性能
        List<Mqttdata> allData = new ArrayList<>();
        mqttDataDao.findAll().forEach(allData::add);
        
        if (allData.isEmpty()) {
            return null;
        }
        
        // 按时间降序排序并返回第一条
        return allData.stream()
                .max(Comparator.comparing(Mqttdata::getDataTime))
                .orElse(null);
    }

    public List<Mqttdata> queryMqttData(Long deviceId, String roomName, String dataTopic, String messageType,
                                    LocalDateTime startTime, LocalDateTime endTime, Integer limit) {
        // 构建查询条件，实际实现需要根据MqttdataDao的能力进行调整
        // 这里提供一个简化版本的实现
        List<Mqttdata> result = new ArrayList<>();
        
        // 首先尝试使用最具体的查询条件
        if (deviceId != null && dataTopic != null && startTime != null && endTime != null) {
            result = mqttDataDao.findByDeviceIdAndDataTopicAndDataTimeBetween(deviceId, dataTopic, startTime, endTime);
        } else if (deviceId != null && startTime != null && endTime != null) {
            result = mqttDataDao.findByDeviceIdAndDataTimeBetweenOrderByDataTimeDesc(deviceId, startTime, endTime);
        } else if (roomName != null && startTime != null && endTime != null) {
            result = mqttDataDao.findByRoomNameAndDataTimeBetween(roomName, startTime, endTime);
        } else if (deviceId != null && dataTopic != null) {
            result = mqttDataDao.findByDeviceIdAndDataTopicOrderByDataTimeDesc(deviceId, dataTopic);
        } else if (deviceId != null) {
            result = mqttDataDao.findByDeviceId(deviceId);
        } else if (roomName != null) {
            result = mqttDataDao.findByRoomName(roomName);
        } else if (messageType != null) {
            result = mqttDataDao.findByMessageType(messageType);
        } else {
            // 如果没有具体条件，返回最新的记录
            mqttDataDao.findAll().forEach(result::add);
        }
        
        // 按时间降序排序
        result.sort(Comparator.comparing(Mqttdata::getDataTime).reversed());
        
        // 限制返回记录数
        if (result.size() > limit) {
            return result.subList(0, limit);
        }
        
        return result;
    }

    public List<Map<String, Object>> customQueryMqttData(Map<String, Object> queryParams) {
        // 设置默认值
        Integer limit = 10;
        if (queryParams.containsKey("limit")) {
            Object limitObj = queryParams.get("limit");
            limit = limitObj instanceof Integer ? (Integer) limitObj : Integer.valueOf(limitObj.toString());
        }
         
        // 提取查询参数，添加更安全的类型转换
        Long deviceId = null;
        if (queryParams.containsKey("deviceId")) {
            Object idObj = queryParams.get("deviceId");
            deviceId = idObj instanceof Long ? (Long) idObj : Long.valueOf(idObj.toString());
        }
         
        String roomName = queryParams.containsKey("roomName") ? queryParams.get("roomName").toString() : null;
        String dataTopic = queryParams.containsKey("dataTopic") ? queryParams.get("dataTopic").toString() : null;
        String messageType = queryParams.containsKey("messageType") ? queryParams.get("messageType").toString() : null;
        
        LocalDateTime startTime = null;
        if (queryParams.containsKey("startTime")) {
            try {
                startTime = LocalDateTime.parse(queryParams.get("startTime").toString());
            } catch (Exception e) {
                // 处理时间解析异常
            }
        }
        
        LocalDateTime endTime = null;
        if (queryParams.containsKey("endTime")) {
            try {
                endTime = LocalDateTime.parse(queryParams.get("endTime").toString());
            } catch (Exception e) {
                // 处理时间解析异常
            }
        }
         
        // 查询数据
        List<Mqttdata> dataList = queryMqttData(deviceId, roomName, dataTopic, messageType, startTime, endTime, limit);
         
        // 转换为Map格式
        List<Map<String, Object>> result = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        for (Mqttdata data : dataList) {
            try {
                Map<String, Object> dataMap = mapper.convertValue(data, Map.class);
                // 处理列过滤
                if (queryParams.containsKey("columns")) {
                    Object columnsObj = queryParams.get("columns");
                    if (columnsObj instanceof List) {
                        List<String> columns = (List<String>) columnsObj;
                        Map<String, Object> filteredMap = new HashMap<>();
                        for (String column : columns) {
                            if (dataMap.containsKey(column)) {
                                filteredMap.put(column, dataMap.get(column));
                            }
                        }
                        result.add(filteredMap);
                    } else {
                        result.add(dataMap);
                    }
                } else {
                    result.add(dataMap);
                }
            } catch (Exception e) {
                // 处理转换异常
            }
        }
         
        return result;
    }

    public List<Map<String, Object>> getMqttDataWithSpecificColumns(List<String> columns, Long deviceId, String roomName,
                                                                  LocalDateTime startTime, LocalDateTime endTime, Integer limit) {
        // 先查询符合条件的数据
        List<Mqttdata> dataList = queryMqttData(deviceId, roomName, null, null, startTime, endTime, limit);
        
        // 提取指定列
        List<Map<String, Object>> result = new ArrayList<>();
        for (Mqttdata data : dataList) {
            Map<String, Object> rowMap = new HashMap<>();
            for (String column : columns) {
                switch (column.toLowerCase()) {
                    case "id":
                        rowMap.put("id", data.getId());
                        break;
                    case "device_id":
                        rowMap.put("deviceId", data.getDeviceId());
                        break;
                    case "datatime":
                        rowMap.put("dataTime", data.getDataTime());
                        break;
                    case "datatopic":
                        rowMap.put("dataTopic", data.getDataTopic());
                        break;
                    case "datavalue":
                        rowMap.put("dataValue", data.getDataValue());
                        break;
                    case "raw_payload":
                        rowMap.put("rawPayload", data.getRawPayload());
                        break;
                    case "message_type":
                        rowMap.put("messageType", data.getMessageType());
                        break;
                    case "room_name":
                        rowMap.put("roomName", data.getRoomName());
                        break;
                    case "temperature":
                        rowMap.put("temperature", data.getTemperature());
                        break;
                    case "humidity":
                        rowMap.put("humidity", data.getHumidity());
                        break;
                    case "is_human":
                        rowMap.put("isHuman", data.getIsHuman());
                        break;
                    case "liv_lit":
                        rowMap.put("livLit", data.getLivLit());
                        break;
                    case "kit_lit":
                        rowMap.put("kitLit", data.getKitLit());
                        break;
                    case "tol_lit":
                        rowMap.put("tolLit", data.getTolLit());
                        break;
                    case "sun":
                        rowMap.put("sun", data.getSun());
                        break;
                    case "adc_data":
                        rowMap.put("adcData", data.getAdcData());
                        break;
                    case "senser_light":
                        rowMap.put("senserLight", data.getSenserLight());
                        break;
                    case "fan_level":
                        rowMap.put("fanLevel", data.getFanLevel());
                        break;
                    case "alarm_bell":
                        rowMap.put("alarmBell", data.getAlarmBell());
                        break;
                    case "gas":
                        rowMap.put("gas", data.getGas());
                        break;
                    case "water_pump_level":
                        rowMap.put("waterPumpLevel", data.getWaterPumpLevel());
                        break;
                    case "senser_on":
                        rowMap.put("senserOn", data.getSenserOn());
                        break;
                    case "sensor_data":
                        rowMap.put("sensorData", data.getSensorData());
                        break;
                }
            }
            result.add(rowMap);
        }
        
        return result;
    }

}
