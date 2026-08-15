package com.cqu.smarthome.demo.pojo;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "home")
public class Home {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "home_id")
    private Long id;

    @Column(name = "home_name")
    private String name;

    @Column(name = "home_address")
    private String address;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
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

    // 重写setUpdateTime方法，确保更新时自动更新时间
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
    
    // 添加一个方法用于手动更新时间
    public void updateTime() {
        this.updateTime = LocalDateTime.now();
    }

    
}
