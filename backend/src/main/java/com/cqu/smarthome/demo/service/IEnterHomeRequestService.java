package com.cqu.smarthome.demo.service;

import com.cqu.smarthome.demo.pojo.EnterRequestRecord;
import com.cqu.smarthome.demo.pojo.GuestRecord;

import java.util.List;

public interface IEnterHomeRequestService {
    // 提交进入家庭请求
    EnterRequestRecord submitEnterRequest(Long homeId, Long userId, String reason);
    
    // 审批进入家庭请求
    EnterRequestRecord approveEnterRequest(Long requestId, Long approverId, Integer status, String remark);
    
    // 获取家庭的进入请求列表
    List<EnterRequestRecord> getHomeEnterRequests(Long homeId, Integer status);
    
    // 获取用户的进入请求记录
    List<EnterRequestRecord> getUserEnterRequests(Long userId);
    
    // 创建访客记录
    GuestRecord createGuestRecord(Long homeId, Long userId, Integer recordType, String remark);
}