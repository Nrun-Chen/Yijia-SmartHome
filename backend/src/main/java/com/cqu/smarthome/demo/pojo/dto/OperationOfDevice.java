package com.cqu.smarthome.demo.pojo.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.Map;

@Getter
@Setter
public class OperationOfDevice {
    private Long deviceId;
    private Long operationId;
    private Map<String, Object> params; // 添加params字段
    private String rawCommand; // 新增字段，用于存储格式如"liv_lit=0 kit_lit=1"的指令

    public OperationOfDevice() {
    }

    public OperationOfDevice(Long deviceId, Long operationId) {
        this.deviceId = deviceId;
        this.operationId = operationId;
    }

    // 手动添加getter方法，以确保代码能正常编译
    public Long getDeviceId() {
        return deviceId;
    }

    public Long getOperationId() {
        return operationId;
    }
    
    // 添加params的getter和setter方法
    public Map<String, Object> getParams() {
        return params;
    }
    
    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
    
    // 添加rawCommand的getter和setter方法
    public String getRawCommand() {
        return rawCommand;
    }
    
    public void setRawCommand(String rawCommand) {
        this.rawCommand = rawCommand;
    }
}
