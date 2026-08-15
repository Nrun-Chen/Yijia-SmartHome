package com.cqu.smarthome.demo.pojo;

import javax.persistence.*;
import java.time.LocalDateTime;

@Table(name = "enter_request_record")
@Entity
public class EnterRequestRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "home_id")
    private Long homeId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "status")
    private Integer status;

    @Column(name = "record_time")
    private LocalDateTime recordTime;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false; // 添加默认值

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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(LocalDateTime recordTime) {
        this.recordTime = recordTime;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public enum Status {
        WAITING(0, "等待处理"),
        APPROVED(1, "已通过"),
        REJECTED(2, "被拒绝");

        private final Integer code;
        private final String description;

        Status(Integer code, String description) {
            this.code = code;
            this.description = description;
        }

        public Integer getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    @Override
    public String toString() {
        return "EnterRequestRecord{" +
                "id=" + id +
                ", homeId=" + homeId +
                ", userId=" + userId +
                ", status=" + status +
                ", recordTime=" + recordTime +
                ", isDeleted=" + isDeleted +
                '}';
    }
}
