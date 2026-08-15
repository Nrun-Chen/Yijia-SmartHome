package com.cqu.smarthome.demo.pojo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceCategorySortResponse {
    private Long id;

    private Long categoryId;

    private String name;

    private String status;

    private String RoomName;

    private String homeName;

    private Integer dataValue;

    public DeviceCategorySortResponse(Long id, Long categoryId, String name, String status, String roomName, String homeName, Integer dataValue) {
        this.id = id;
        this.categoryId = categoryId;
        this.name = name;
        this.status = status;
        this.RoomName = roomName;
        this.homeName = homeName;
        this.dataValue = dataValue;
    }
}
