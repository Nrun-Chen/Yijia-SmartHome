package com.cqu.smarthome.demo.pojo;

import javax.persistence.*;
import java.io.Serializable;

@Table(name = "role_default_permission")
@Entity
public class RolePermission implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "role")
    private Integer role;

    @Column(name = "device_id")
    private Long deviceId;

    @Column(name = "operation_id")
    private Long operationId;

    @Column(name = "has_permission")
    private Boolean hasPermission;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
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

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

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

        public Integer getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    @Override
    public String toString() {
        return "RoleDefaultPermission{" +
                "id=" + id +
                ", role=" + role +
                ", deviceId=" + deviceId +
                ", operationId=" + operationId +
                ", hasPermission=" + hasPermission +
                ", isDeleted=" + isDeleted +
                '}';
    }
}
