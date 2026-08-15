package com.cqu.smarthome.demo.pojo;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Mqtt_data")
public class Mqttdata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Column(name = "device_id")
    private Long deviceId;

    @Column(name = "datatime")
    private LocalDateTime dataTime;

    @Column(name = "datatopic")
    private String dataTopic;

    @Column(name = "datavalue")
    private Integer dataValue;
    
    // 新增字段：存储原始JSON消息内容
    @Column(name = "raw_payload", columnDefinition = "TEXT")
    private String rawPayload;
    
    // 新增字段：消息类型
    @Column(name = "message_type")
    private String messageType;
    
    // 新增字段：房间名称（用于p_bigroom这样的主题）
    @Column(name = "room_name")
    private String roomName;
    
    // 新增字段：温度值
    @Column(name = "temperature")
    private Double temperature;
    
    // 新增字段：湿度值
    @Column(name = "humidity")
    private Double humidity;
    
    // 新增字段：其他传感器数据（JSON格式存储）
    @Column(name = "sensor_data", columnDefinition = "TEXT")
    private String sensorData;
    
    // 新增特定传感器字段
    @Column(name = "liv_lit")
    private Integer livLit;  // 客厅灯光状态
    
    @Column(name = "kit_lit")
    private Integer kitLit;  // 厨房灯光状态
    
    @Column(name = "tol_lit")
    private Integer tolLit;  // 走廊灯光状态
    
    @Column(name = "is_human")
    private Integer isHuman;  // 人体感应
    
    @Column(name = "sun")
    private Integer sun;  // 阳光传感器
    
    @Column(name = "adc_data")
    private Integer adcData;  // ADC数据
    
    @Column(name = "senser_light")
    private Integer senserLight;  // 光线传感器
    
    @Column(name = "fan_level")
    private Integer fanLevel;  // 风扇级别
    
    @Column(name = "alarm_bell")
    private Integer alarmBell;  // 警报铃状态
    
    @Column(name = "gas")
    private String gas;  // 气体浓度（带单位）
    
    // 水泵水位
    @Column(name = "water_pump_level")
    private Integer waterPumpLevel;

        // 在类的字段定义部分添加
    private Integer senserOn;

    // 在getter和setter方法部分添加
    public Integer getSenserOn() {
        return senserOn;
    }

    public void setSenserOn(Integer senserOn) {
        this.senserOn = senserOn;
    }
        
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public LocalDateTime getDataTime() {
        return dataTime;
    }

    public void setDataTime(LocalDateTime dataTime) {
        this.dataTime = dataTime;
    }

    public String getDataTopic() {
        return dataTopic;
    }

    public void setDataTopic(String dataTopic) {
        this.dataTopic = dataTopic;
    }

    public Integer getDataValue() {
        return dataValue;
    }

    public void setDataValue(Integer dataValue) {
        this.dataValue = dataValue;
    }

    public String getRawPayload() {
        return rawPayload;
    }
    
    public void setRawPayload(String rawPayload) {
        this.rawPayload = rawPayload;
    }
    
    public String getMessageType() {
        return messageType;
    }
    
    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
    
    public String getRoomName() {
        return roomName;
    }
    
    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }
    
    public Double getTemperature() {
        return temperature;
    }
    
    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }
    
    public Double getHumidity() {
        return humidity;
    }
    
    public void setHumidity(Double humidity) {
        this.humidity = humidity;
    }
    
    public String getSensorData() {
        return sensorData;
    }
    
    public void setSensorData(String sensorData) {
        this.sensorData = sensorData;
    }
    
    // 为新增字段添加getter和setter方法
    public Integer getLivLit() {
        return livLit;
    }
    
    public void setLivLit(Integer livLit) {
        this.livLit = livLit;
    }
    
    public Integer getKitLit() {
        return kitLit;
    }
    
    public void setKitLit(Integer kitLit) {
        this.kitLit = kitLit;
    }
    
    public Integer getTolLit() {
        return tolLit;
    }
    
    public void setTolLit(Integer tolLit) {
        this.tolLit = tolLit;
    }
    
    public Integer getIsHuman() {
        return isHuman;
    }
    
    public void setIsHuman(Integer isHuman) {
        this.isHuman = isHuman;
    }
    
    public Integer getSun() {
        return sun;
    }
    
    public void setSun(Integer sun) {
        this.sun = sun;
    }
    
    public Integer getAdcData() {
        return adcData;
    }
    
    public void setAdcData(Integer adcData) {
        this.adcData = adcData;
    }
    
    public Integer getSenserLight() {
        return senserLight;
    }
    
    public void setSenserLight(Integer senserLight) {
        this.senserLight = senserLight;
    }
    
    public Integer getFanLevel() {
        return fanLevel;
    }
    
    public void setFanLevel(Integer fanLevel) {
        this.fanLevel = fanLevel;
    }
    
    public Integer getAlarmBell() {
        return alarmBell;
    }
    
    public void setAlarmBell(Integer alarmBell) {
        this.alarmBell = alarmBell;
    }
    
    public String getGas() {
        return gas;
    }
    
    public void setGas(String gas) {
        this.gas = gas;
    }
    
    // 为水泵水位添加getter和setter方法
    public Integer getWaterPumpLevel() {
        return waterPumpLevel;
    }
    
    public void setWaterPumpLevel(Integer waterPumpLevel) {
        this.waterPumpLevel = waterPumpLevel;
    }
    
    // 更新toString方法，包含新增字段
    @Override
    public String toString() {
        return "Mqttdata{" +
                "id=" + id +
                ", deviceId=" + deviceId +
                ", dataTime=" + dataTime +
                ", dataTopic='" + dataTopic + '\'' +
                ", dataValue=" + dataValue +
                ", rawPayload='" + rawPayload + '\'' +
                ", messageType='" + messageType + '\'' +
                ", roomName='" + roomName + '\'' +
                ", temperature=" + temperature +
                ", humidity=" + humidity +
                ", livLit=" + livLit +
                ", kitLit=" + kitLit +
                ", tolLit=" + tolLit +
                ", isHuman=" + isHuman +
                ", sun=" + sun +
                ", adcData=" + adcData +
                ", senserLight=" + senserLight +
                ", fanLevel=" + fanLevel +
                ", alarmBell=" + alarmBell +
                ", gas='" + gas + '\'' +
                ", waterPumpLevel=" + waterPumpLevel +
                ", senserOn=" + senserOn +
                ", sensorData='" + sensorData + '\'' +
                '}';
    }

}
