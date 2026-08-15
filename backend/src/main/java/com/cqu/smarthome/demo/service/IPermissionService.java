package com.cqu.smarthome.demo.service;

import java.util.List;
import java.util.Map;

public interface IPermissionService {
    // 检查用户是否有权限执行操作
    boolean checkPermission(Long userId, Long homeId, String permissionCode);
    
    // 获取用户在家庭中的所有权限
    List<String> getUserPermissions(Long userId, Long homeId);
    
    // 为用户分配权限
    boolean grantPermission(Long userId, Long homeId, String permissionCode, Long operatorId);
    
    // 移除用户的权限
    boolean revokePermission(Long userId, Long homeId, String permissionCode, Long operatorId);
    
    // 获取所有可用的权限列表
    Map<String, String> getAllPermissions();
}