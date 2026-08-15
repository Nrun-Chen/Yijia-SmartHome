package com.cqu.smarthome.demo.service;

import com.cqu.smarthome.demo.dao.HomeDao;
import com.cqu.smarthome.demo.dao.UserHomeDao;
import com.cqu.smarthome.demo.pojo.Home;
import com.cqu.smarthome.demo.pojo.UserHome;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class PermissionService implements IPermissionService {
    
    @Autowired
    private UserHomeDao userHomeDao;
    
    @Autowired
    private HomeDao homeDao;
    
    // 预定义的权限列表
    private static final Map<String, String> PERMISSION_DEFINITIONS = new HashMap<String, String>() {
        {
            put("DEVICE_CONTROL", "设备控制权限");
            put("DEVICE_MANAGE", "设备管理权限");
            put("SCENE_MANAGE", "场景管理权限");
            put("HOME_MANAGE", "家庭管理权限");
            put("MEMBER_MANAGE", "成员管理权限");
            put("VISITOR_MANAGE", "访客管理权限");
        }
    };
    
    // 角色对应的默认权限
    private static final Map<Integer, List<String>> ROLE_PERMISSIONS = new HashMap<Integer, List<String>>() {
        {
            // 管理员角色（1）拥有所有权限
            put(1, Arrays.asList(
                "DEVICE_CONTROL", "DEVICE_MANAGE", "SCENE_MANAGE",
                "HOME_MANAGE", "MEMBER_MANAGE", "VISITOR_MANAGE"
            ));
            // 普通成员角色（2）拥有基本权限
            put(2, Arrays.asList(
                "DEVICE_CONTROL", "SCENE_MANAGE"
            ));
        }
    };
    
    @Override
    public boolean checkPermission(Long userId, Long homeId, String permissionCode) {
        // 验证家庭是否存在
        Optional<Home> homeOptional = homeDao.findByIdAndIsDeletedFalse(homeId);
        if (!homeOptional.isPresent()) {
            throw new RuntimeException("家庭不存在");
        }
        
        // 检查用户是否是家庭成员
        Optional<UserHome> userHomeOptional = userHomeDao.findByUserIdAndHomeIdAndIsDeletedFalse(userId, homeId);
        if (!userHomeOptional.isPresent()) {
            return false;
        }
        
        // 获取用户角色
        Integer role = userHomeOptional.get().getRole();
        
        // 检查角色是否有此权限
        List<String> rolePermissionList = ROLE_PERMISSIONS.get(role);
        if (rolePermissionList != null && rolePermissionList.contains(permissionCode)) {
            return true;
        }
        
        // 在实际应用中，这里可以从数据库中查询用户的自定义权限
        // 这里简化实现，仅基于角色判断
        return false;
    }

    @Override
    public List<String> getUserPermissions(Long userId, Long homeId) {
        // 验证家庭是否存在
        Optional<Home> homeOptional = homeDao.findByIdAndIsDeletedFalse(homeId);
        if (!homeOptional.isPresent()) {
            throw new RuntimeException("家庭不存在");
        }
        
        // 检查用户是否是家庭成员
        Optional<UserHome> userHomeOptional = userHomeDao.findByUserIdAndHomeIdAndIsDeletedFalse(userId, homeId);
        if (!userHomeOptional.isPresent()) {
            return new ArrayList<>();
        }
        
        // 获取用户角色对应的权限
        Integer role = userHomeOptional.get().getRole();
        List<String> rolePermissionList = ROLE_PERMISSIONS.get(role);
        
        // 在实际应用中，这里可以从数据库中查询用户的自定义权限并合并
        return rolePermissionList != null ? new ArrayList<>(rolePermissionList) : new ArrayList<>();
    }
    
    @Override
    @Transactional
    public boolean grantPermission(Long userId, Long homeId, String permissionCode, Long operatorId) {
        // 验证家庭是否存在
        Optional<Home> homeOptional = homeDao.findByIdAndIsDeletedFalse(homeId);
        if (!homeOptional.isPresent()) {
            throw new RuntimeException("家庭不存在");
        }
        
        // 检查操作人是否有权限（必须是管理员）
        Optional<UserHome> operatorUserHomeOptional = userHomeDao.findByUserIdAndHomeIdAndIsDeletedFalse(operatorId, homeId);
        if (!operatorUserHomeOptional.isPresent() || operatorUserHomeOptional.get().getRole() != 1) {
            throw new RuntimeException("只有管理员可以分配权限");
        }
        
        // 检查用户是否是家庭成员
        Optional<UserHome> userHomeOptional = userHomeDao.findByUserIdAndHomeIdAndIsDeletedFalse(userId, homeId);
        if (!userHomeOptional.isPresent()) {
            throw new RuntimeException("用户不是家庭成员");
        }
        
        // 检查权限是否存在
        if (!PERMISSION_DEFINITIONS.containsKey(permissionCode)) {
            throw new RuntimeException("权限不存在");
        }
        
        // 在实际应用中，这里应该将权限分配记录保存到数据库
        // 这里简化实现，仅记录日志
        System.out.println("用户 " + operatorId + " 为用户 " + userId + " 分配了权限: " + permissionCode + " 在家庭: " + homeId);
        
        return true;
    }
    
    @Override
    @Transactional
    public boolean revokePermission(Long userId, Long homeId, String permissionCode, Long operatorId) {
        // 验证家庭是否存在
        Optional<Home> homeOptional = homeDao.findByIdAndIsDeletedFalse(homeId);
        if (!homeOptional.isPresent()) {
            throw new RuntimeException("家庭不存在");
        }
        
        // 检查操作人是否有权限（必须是管理员）
        Optional<UserHome> operatorUserHomeOptional = userHomeDao.findByUserIdAndHomeIdAndIsDeletedFalse(operatorId, homeId);
        if (!operatorUserHomeOptional.isPresent() || operatorUserHomeOptional.get().getRole() != 1) {
            throw new RuntimeException("只有管理员可以撤销权限");
        }
        
        // 检查用户是否是家庭成员
        Optional<UserHome> userHomeOptional = userHomeDao.findByUserIdAndHomeIdAndIsDeletedFalse(userId, homeId);
        if (!userHomeOptional.isPresent()) {
            throw new RuntimeException("用户不是家庭成员");
        }
        
        // 不能撤销自己的权限
        if (userId.equals(operatorId)) {
            throw new RuntimeException("管理员不能撤销自己的权限");
        }
        
        // 检查权限是否存在
        if (!PERMISSION_DEFINITIONS.containsKey(permissionCode)) {
            throw new RuntimeException("权限不存在");
        }
        
        // 在实际应用中，这里应该从数据库中移除权限分配记录
        // 这里简化实现，仅记录日志
        System.out.println("用户 " + operatorId + " 撤销了用户 " + userId + " 的权限: " + permissionCode + " 在家庭: " + homeId);
        
        return true;
    }
    
    @Override
    public Map<String, String> getAllPermissions() {
        // 返回所有预定义的权限
        return new HashMap<>(PERMISSION_DEFINITIONS);
    }
}