package com.cqu.smarthome.demo.pojo;

import javax.persistence.*;
import java.time.LocalDateTime;

@Table(name = "user_custom")
@Entity

public class UserCustom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "home_id")
    private Long homeId;

    @Column(name = "device_id")
    private Long deviceId;

    @Column(name = "operation_id")
    private Long operationId;

    @Column(name = "has_permission")
    private Boolean hasPermission;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getHomeId() {
        return homeId;
    }

    public void setHomeId(Long homeId) {
        this.homeId = homeId;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public Long getOperationId() {
        return operationId;
    }

    public void setOperationId(Long operationId) {
        this.operationId = operationId;
    }

    public Boolean getHasPermission() {
        return hasPermission;
    }

    public void setHasPermission(Boolean hasPermission) {
        this.hasPermission = hasPermission;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    @Override
    public String toString() {
        return "UserCustomPermission{" +
                "id=" + id +
                ", userId=" + userId +
                ", homeId=" + homeId +
                ", deviceId=" + deviceId +
                ", operationId=" + operationId +
                ", hasPermission=" + hasPermission +
                ", endTime=" + endTime +
                ", isDeleted=" + isDeleted +
                '}';
    }
}
