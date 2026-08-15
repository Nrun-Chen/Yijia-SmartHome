package com.cqu.smarthome.demo.service;

import com.cqu.smarthome.demo.pojo.Scene;

import java.util.List;

public interface ISceneService {
    
    // 创建场景
    Scene createScene(Scene scene, Long userId);
    
    // 根据ID获取场景
    Scene getSceneById(Long id);
    
    // 根据家庭ID获取所有场景
    List<Scene> getScenesByHomeId(Long homeId, Long userId);
    
    // 更新场景
    Scene updateScene(Scene scene, Long userId);
    
    // 删除场景
    void deleteScene(Long id, Long userId);
    
    // 启用/禁用场景
    Scene toggleSceneStatus(Long id, Integer status, Long userId);
    
    // 手动激活场景
    void activateScene(Long id, Long userId);
    
    // 检查场景名称是否存在
    boolean checkSceneNameExists(String name, Long homeId);
    
    // 检查用户是否有权限操作场景
    boolean checkUserPermission(Long userId, Long sceneId);
    
    // 执行场景激活逻辑（内部方法，用于定时和感应触发）
    void executeSceneActivation(Scene scene);
    
    // 获取家庭的所有启用场景
    List<Scene> getEnabledScenesByHomeId(Long homeId);
}