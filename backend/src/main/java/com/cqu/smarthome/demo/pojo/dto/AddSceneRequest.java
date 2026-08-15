package com.cqu.smarthome.demo.pojo.dto;

// 修改AddSceneRequest类，添加triggerType和roomName字段
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class AddSceneRequest {
    private String name;
    private String description;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<OperationOfDevice> deviceOperation;
    // 添加triggerType字段：0-定时触发，1-人员检测触发（有人），2-人员检测触发（没人），3-双重触发（定时+人员检测）
    private Integer triggerType = 0;
    // 添加roomName字段，用于人员检测触发场景
    private String roomName;

    public AddSceneRequest(String name, String description, Integer status, LocalDateTime startTime, LocalDateTime endTime, List<OperationOfDevice> deviceOperation) {
        this.name = name;
        this.description = description;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.deviceOperation = deviceOperation;
    }

    public String getName() {
        return name;  // 添加返回语句
    }

    // 添加缺失的getter方法
    public String getDescription() {
        return description;
    }

    public Integer getStatus() {
        return status;
    }

    public List<OperationOfDevice> getDeviceOperation() {
        return deviceOperation;
    }
    
    // 添加缺失的startTime和endTime的getter方法
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public Integer getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(Integer triggerType) {
        this.triggerType = triggerType;
    }
    
    public String getRoomName() {
        return roomName;
    }
    
    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }
}
