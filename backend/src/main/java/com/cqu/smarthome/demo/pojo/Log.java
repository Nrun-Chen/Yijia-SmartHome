package com.cqu.smarthome.demo.pojo;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Log")
public class Log {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Column(name = "log")
    private String log;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    // 添加自动设置创建时间的方法
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }

    // getter和setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "Log{" +
                "id=" + id +
                ", log='" + log + '\'' +
                ", userId=" + userId +
                ", createTime=" + createTime +
                '}';
    }
}
