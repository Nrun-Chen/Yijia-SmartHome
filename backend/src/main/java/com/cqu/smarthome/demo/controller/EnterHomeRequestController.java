package com.cqu.smarthome.demo.controller;

import com.cqu.smarthome.demo.pojo.EnterRequestRecord;
import com.cqu.smarthome.demo.pojo.GuestRecord;
import com.cqu.smarthome.demo.pojo.ResponseMessage;
import com.cqu.smarthome.demo.service.IEnterHomeRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/home/request")
public class EnterHomeRequestController {

    @Autowired
    private IEnterHomeRequestService enterHomeRequestService;

    // 提交进入家庭请求
    @PostMapping
    public ResponseMessage<EnterRequestRecord> submitEnterRequest(
            @RequestParam Long homeId,
            @RequestParam Long userId,
            @RequestParam(required = false) String reason) {
        EnterRequestRecord record = enterHomeRequestService.submitEnterRequest(homeId, userId, reason);
        return ResponseMessage.success(record);
    }

    // 审批进入家庭请求
    @PutMapping("/{requestId}/approve")
    public ResponseMessage<EnterRequestRecord> approveEnterRequest(
            @PathVariable Long requestId,
            @RequestParam Long approverId,
            @RequestParam Integer status,
            @RequestParam(required = false) String remark) {
        try {
            EnterRequestRecord record = enterHomeRequestService.approveEnterRequest(requestId, approverId, status, remark);
            return ResponseMessage.success(record);
        } catch (RuntimeException e) {
            return new ResponseMessage<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null);
        }
    }

    // 获取家庭的进入请求列表
    @GetMapping("/home/{homeId}")
    public ResponseMessage<List<EnterRequestRecord>> getHomeEnterRequests(
            @PathVariable Long homeId,
            @RequestParam(required = false) Integer status) {
        List<EnterRequestRecord> requests = enterHomeRequestService.getHomeEnterRequests(homeId, status);
        return ResponseMessage.success(requests);
    }

    // 获取用户的进入请求记录
    @GetMapping("/user/{userId}")
    public ResponseMessage<List<EnterRequestRecord>> getUserEnterRequests(@PathVariable Long userId) {
        List<EnterRequestRecord> requests = enterHomeRequestService.getUserEnterRequests(userId);
        return ResponseMessage.success(requests);
    }

    // 创建访客记录
    @PostMapping("/guest-record")
    public ResponseMessage<GuestRecord> createGuestRecord(
            @RequestParam Long homeId,
            @RequestParam Long userId,
            @RequestParam Integer recordType,
            @RequestParam(required = false) String remark) {
        GuestRecord record = enterHomeRequestService.createGuestRecord(homeId, userId, recordType, remark);
        return ResponseMessage.success(record);
    }
}