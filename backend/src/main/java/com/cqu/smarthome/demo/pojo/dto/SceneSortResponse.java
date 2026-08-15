package com.cqu.smarthome.demo.pojo.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class SceneSortResponse {
    private Long id;

    private Long homeId;

    private String name;

    private String homeName;

    private String description;

    private Integer status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    public SceneSortResponse(Long id, Long homeId, String name, String homeName, String description, Integer status, LocalDateTime startTime, LocalDateTime endTime) {
        this.id = id;
        this.homeId = homeId;
        this.name = name;
        this.homeName = homeName;
        this.description = description;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
