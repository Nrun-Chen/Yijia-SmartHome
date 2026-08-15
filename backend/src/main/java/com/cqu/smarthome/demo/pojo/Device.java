package com.cqu.smarthome.demo.pojo;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "device")
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "device_name")
    private String deviceName;

    @Column(name = "home_id")
    private Long homeId;

    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "device_type_id")
    private Long deviceTypeId;

    @Column(name = "device_mac")
    private String deviceMac;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    // 添加创建时间字段并设置默认值
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime = LocalDateTime.now();

    // 添加更新时间字段并设置默认值
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public Long getHomeId() {
        return homeId;
    }

    public void setHomeId(Long homeId) {
        this.homeId = homeId;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public Long getDeviceTypeId() {
        return deviceTypeId;
    }

    public void setDeviceTypeId(Long deviceTypeId) {
        this.deviceTypeId = deviceTypeId;
    }

    public String getDeviceMac() {
        return deviceMac;
    }

    public void setDeviceMac(String deviceMac) {
        this.deviceMac = deviceMac;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
    
    // 添加一个方法用于手动更新时间
    public void updateTime() {
        this.updateTime = LocalDateTime.now();
    }
}