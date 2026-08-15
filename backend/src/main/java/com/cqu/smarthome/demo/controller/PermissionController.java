package com.cqu.smarthome.demo.controller;

import com.cqu.smarthome.demo.pojo.ResponseMessage;
import com.cqu.smarthome.demo.service.IPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/permission")
public class PermissionController {

    @Autowired
    private IPermissionService permissionService;

    // 检查用户是否有权限执行操作
    @GetMapping("/check")
    public ResponseMessage<Boolean> checkPermission(
            @RequestParam Long userId,
            @RequestParam Long homeId,
            @RequestParam String permissionCode) {
        boolean hasPermission = permissionService.checkPermission(userId, homeId, permissionCode);
        return ResponseMessage.success(hasPermission);
    }

    // 获取用户在家庭中的所有权限
    @GetMapping("/user/{userId}/home/{homeId}")
    public ResponseMessage<List<String>> getUserPermissions(
            @PathVariable Long userId,
            @PathVariable Long homeId) {
        List<String> permissions = permissionService.getUserPermissions(userId, homeId);
        return ResponseMessage.success(permissions);
    }

    // 为用户分配权限
    @PostMapping
    public ResponseMessage<Boolean> grantPermission(
            @RequestParam Long userId,
            @RequestParam Long homeId,
            @RequestParam String permissionCode,
            @RequestParam Long operatorId) {
        boolean result = permissionService.grantPermission(userId, homeId, permissionCode, operatorId);
        if (result) {
            return ResponseMessage.success(true);
        } else {
            return new ResponseMessage<>(HttpStatus.BAD_REQUEST.value(), "分配权限失败，操作人权限不足", false);
        }
    }

    // 移除用户的权限
    @DeleteMapping
    public ResponseMessage<Boolean> revokePermission(
            @RequestParam Long userId,
            @RequestParam Long homeId,
            @RequestParam String permissionCode,
            @RequestParam Long operatorId) {
        boolean result = permissionService.revokePermission(userId, homeId, permissionCode, operatorId);
        if (result) {
            return ResponseMessage.success(true);
        } else {
            return new ResponseMessage<>(HttpStatus.BAD_REQUEST.value(), "移除权限失败，操作人权限不足", false);
        }
    }

    // 获取所有可用的权限列表
    @GetMapping
    public ResponseMessage<Map<String, String>> getAllPermissions() {
        Map<String, String> permissions = permissionService.getAllPermissions();
        return ResponseMessage.success(permissions);
    }
}