package com.cqu.smarthome.demo.pojo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MyHomeRole {
    private Long homeId;
    private String name;
    private Integer role;

    public MyHomeRole(Long homeId, String name, Integer role) {
        this.homeId = homeId;
        this.name = name;
        this.role = role;
    }
}
