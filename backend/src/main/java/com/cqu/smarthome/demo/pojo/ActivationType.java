package com.cqu.smarthome.demo.pojo;

public enum ActivationType {
    TIME_BASED(0, "定时激活", "基于设置的开始时间自动激活场景"),
    HUMAN_DETECTED(1, "人体感应激活", "基于MQTT数据中的is_human字段检测到有人时激活场景");

    private final Integer code;
    private final String name;
    private final String description;

    ActivationType(Integer code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public static ActivationType getByCode(Integer code) {
        for (ActivationType type : ActivationType.values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}