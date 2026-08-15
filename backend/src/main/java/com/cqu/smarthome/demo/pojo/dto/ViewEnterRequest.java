package com.cqu.smarthome.demo.pojo.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ViewEnterRequest {
    private Long requestId;
    private Long userId;
    private String userName;
    private Integer status;
    private LocalDateTime recordTime;
}
