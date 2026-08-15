package com.cqu.smarthome.demo.pojo;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "scene")
public class Scene {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "home_id")
    private Long homeId;

    @Column(name = "description")
    private String description;

    // 操作设备指令，以JSON格式存储
    @Column(name = "device_commands", columnDefinition = "TEXT")
    private String deviceCommands;

    // 激活类型，使用枚举的code值
    @Column(name = "activation_type")
    private Integer activationType;

    // 启用状态：0-禁用，1-启用
    @Column(name = "status")
    private Integer status = 0;

    // 激活状态：0-未激活，1-已激活
    @Column(name = "is_active")
    private Boolean isActive = false;

    // 开始时间，用于定时激活类型
    @Column(name = "start_time")
    private LocalDateTime startTime;

    // 结束时间，可选
    @Column(name = "end_time")
    private LocalDateTime endTime;

    // 人体感应检测阈值（可选）
    @Column(name = "human_detect_threshold")
    private Integer humanDetectThreshold = 1;

    // 添加创建时间字段并设置默认值
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime = LocalDateTime.now();

    // 添加更新时间字段并设置默认值
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime = LocalDateTime.now();

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;
    private LocalDateTime lastActivatedTime;

    // 添加getter和setter方法
    public LocalDateTime getLastActivatedTime() {
        return lastActivatedTime;
    }

    public void setLastActivatedTime(LocalDateTime lastActivatedTime) {
        this.lastActivatedTime = lastActivatedTime;
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.updateTime = LocalDateTime.now();
    }

    public Long getHomeId() {
        return homeId;
    }

    public void setHomeId(Long homeId) {
        this.homeId = homeId;
        this.updateTime = LocalDateTime.now();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.updateTime = LocalDateTime.now();
    }

    public String getDeviceCommands() {
        return deviceCommands;
    }

    public void setDeviceCommands(String deviceCommands) {
        this.deviceCommands = deviceCommands;
        this.updateTime = LocalDateTime.now();
    }

    public Integer getActivationType() {
        return activationType;
    }

    public void setActivationType(Integer activationType) {
        this.activationType = activationType;
        this.updateTime = LocalDateTime.now();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
        this.updateTime = LocalDateTime.now();
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
        this.updateTime = LocalDateTime.now();
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
        this.updateTime = LocalDateTime.now();
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
        this.updateTime = LocalDateTime.now();
    }

    public Integer getHumanDetectThreshold() {
        return humanDetectThreshold;
    }

    public void setHumanDetectThreshold(Integer humanDetectThreshold) {
        this.humanDetectThreshold = humanDetectThreshold;
        this.updateTime = LocalDateTime.now();
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

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean deleted) {
        isDeleted = deleted;
        this.updateTime = LocalDateTime.now();
    }

    // 获取激活类型的枚举对象
    @Transient
    public ActivationType getActivationTypeEnum() {
        return ActivationType.getByCode(this.activationType);
    }

    @Override
    public String toString() {
        return "Scene{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", homeId=" + homeId +
                ", activationType=" + activationType +
                ", status=" + status +
                ", isActive=" + isActive +
                ", startTime=" + startTime +
                '}';
    }
}