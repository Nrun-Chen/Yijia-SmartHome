package com.cqu.smarthome.demo.pojo;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Table(name = "user_home")
@Entity
public class UserHome implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "home_id")
    private Long homeId;

    @Column(name = "role")
    private Integer role;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    // 添加缺失的时间字段
    @Column(name = "join_time")
    private LocalDateTime joinTime;

    @Column(name = "leave_time")
    private LocalDateTime leaveTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    // 添加角色枚举
    public enum Role {
        HOST(0, "房主"),
        MEMBER(1, "家庭成员"),
        GUEST(2, "访客");

        private final Integer code;
        private final String description;

        Role(Integer code, String description) {
            this.code = code;
            this.description = description;
        }

        public static String getByCode(Integer code) {
            for (Role role : Role.values()) {
                if (role.code.equals(code)) {
                    return role.description;
                }
            }
            return "未知角色";
        }
    }

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

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    // 添加缺失的getter/setter方法
    public LocalDateTime getJoinTime() {
        return joinTime;
    }

    public void setJoinTime(LocalDateTime joinTime) {
        this.joinTime = joinTime;
    }

    public LocalDateTime getLeaveTime() {
        return leaveTime;
    }

    public void setLeaveTime(LocalDateTime leaveTime) {
        this.leaveTime = leaveTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
    
    @Override
    public String toString() {
        return "UserHome{" +
                "id=" + id +
                ", userId=" + userId +
                ", homeId=" + homeId +
                ", role=" + role +
                ", isDeleted=" + isDeleted +
                ", joinTime=" + joinTime +
                ", leaveTime=" + leaveTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
