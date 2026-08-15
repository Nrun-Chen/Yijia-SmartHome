package com.cqu.smarthome.demo.pojo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SceneDeviceView {
    private Long deviceId;
    private Long deviceTypeId;
    private String deviceName;

    public SceneDeviceView(Long deviceId, Long deviceTypeId, String deviceName) {
        this.deviceId = deviceId;
        this.deviceTypeId = deviceTypeId;
        this.deviceName = deviceName;
    }
}