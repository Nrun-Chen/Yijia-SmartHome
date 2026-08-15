package com.cqu.smarthome.demo.pojo.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class UpdateHomeRequest {
    @NotBlank(message = "家庭名称不能为空")
    @Size(max = 50, message = "家庭名称不能超过50个字符")
    private String name;
    
    @NotBlank(message = "家庭地址不能为空")
    @Size(max = 200, message = "家庭地址不能超过200个字符")
    private String address;
    
    // getter and setter
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
}