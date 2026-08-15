package com.cqu.smarthome.demo.pojo;

import javax.persistence.*;
import java.time.LocalDateTime;

@Table(name = "room")
@Entity
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "home_id")
    private Long homeId;

    @Column(name = "name")
    private String name;

    @Column(name = "is_deleted")
    private Boolean isDeleted;
    
    // 添加创建时间字段
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    // 添加更新时间字段
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getHomeId() {
        return homeId;
    }

    public void setHomeId(Long homeId) {
        this.homeId = homeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    // 添加createTime和updateTime的getter/setter方法
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

    @Override
    public String toString() {
        return "Room{" +
                "id=" + id +
                ", homeId=" + homeId +
                ", name='" + name + '\'' +
                ", isDeleted=" + isDeleted +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}