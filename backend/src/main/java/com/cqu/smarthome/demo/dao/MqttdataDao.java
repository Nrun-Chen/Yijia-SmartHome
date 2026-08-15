package com.cqu.smarthome.demo.dao;

import com.cqu.smarthome.demo.pojo.Mqttdata;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MqttdataDao extends CrudRepository<Mqttdata, Long> {
    // 根据设备ID查找所有数据记录
    List<Mqttdata> findByDeviceId(Long deviceId);
    
    // 根据房间名称查找数据记录
    List<Mqttdata> findByRoomName(String roomName);
    
    // 根据房间名称和时间范围查找数据记录
    List<Mqttdata> findByRoomNameAndDataTimeBetween(String roomName, LocalDateTime startTime, LocalDateTime endTime);
    
    // 根据消息类型查找数据记录
    List<Mqttdata> findByMessageType(String messageType);
    
    // 根据设备ID和时间范围查找数据记录
    List<Mqttdata> findByDeviceIdAndDataTimeBetween(Long deviceId, LocalDateTime startTime, LocalDateTime endTime);
    
    // 根据设备ID和时间范围查找数据记录并按时间降序排序
    List<Mqttdata> findByDeviceIdAndDataTimeBetweenOrderByDataTimeDesc(Long deviceId, LocalDateTime startTime, LocalDateTime endTime);
    
    // 根据主题查找数据记录
    List<Mqttdata> findByDataTopic(String dataTopic);
    
    // 根据设备ID和主题查找数据记录
    List<Mqttdata> findByDeviceIdAndDataTopic(Long deviceId, String dataTopic);
    
    // 根据设备ID和主题查找数据记录并按时间降序排序
    List<Mqttdata> findByDeviceIdAndDataTopicOrderByDataTimeDesc(Long deviceId, String dataTopic);
    
    // 在现有接口中添加以下方法
    // 查找所有记录并按时间降序排序
    List<Mqttdata> findAllByOrderByDataTimeDesc();
    
    // 根据设备ID和主题以及时间范围查找数据记录
    List<Mqttdata> findByDeviceIdAndDataTopicAndDataTimeBetween(Long deviceId, String dataTopic, LocalDateTime startTime, LocalDateTime endTime);
    
    // 获取设备的最新MQTT数据（按时间降序排序取第一条）
    Mqttdata findTopByDeviceIdOrderByDataTimeDesc(Long deviceId);
}