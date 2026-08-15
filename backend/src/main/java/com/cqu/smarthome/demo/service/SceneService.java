package com.cqu.smarthome.demo.service;

import com.cqu.smarthome.demo.dao.SceneDao;
import com.cqu.smarthome.demo.pojo.Scene;
import com.cqu.smarthome.demo.pojo.ActivationType;
import com.cqu.smarthome.demo.pojo.Mqttdata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 在类顶部添加必要的import
import org.springframework.scheduling.annotation.Scheduled;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SceneService implements ISceneService {

    @Autowired
    private SceneDao sceneDao;

    @Autowired
    private MqttDataService mqttDataService;

    @Autowired
    private MqttMessageSender mqttMessageSender;

    @Override
    public Scene createScene(Scene scene, Long userId) {
        // 创建时间和更新时间自动设置
        return sceneDao.save(scene);
    }

    @Override
    public Scene getSceneById(Long id) {
        Optional<Scene> sceneOptional = sceneDao.findByIdAndIsDeleted(id, false);
        return sceneOptional.orElse(null);
    }

    @Override
    public List<Scene> getScenesByHomeId(Long homeId, Long userId) {
        return sceneDao.findByHomeIdAndIsDeleted(homeId, false);
    }

    @Override
    public Scene updateScene(Scene scene, Long userId) {
        // 确保场景存在
        Scene existingScene = getSceneById(scene.getId());
        if (existingScene == null) {
            throw new RuntimeException("场景不存在");
        }
        // 检查权限
        if (!checkUserPermission(userId, scene.getId())) {
            throw new RuntimeException("无权限操作此场景");
        }
        // 更新场景信息
        existingScene.setName(scene.getName());
        existingScene.setDescription(scene.getDescription());
        existingScene.setDeviceCommands(scene.getDeviceCommands());
        existingScene.setActivationType(scene.getActivationType());
        existingScene.setStatus(scene.getStatus());
        existingScene.setStartTime(scene.getStartTime());
        existingScene.setEndTime(scene.getEndTime());
        existingScene.setHumanDetectThreshold(scene.getHumanDetectThreshold());
        
        return sceneDao.save(existingScene);
    }

    @Override
    @Transactional
    public void deleteScene(Long id, Long userId) {
        // 检查权限
        if (!checkUserPermission(userId, id)) {
            throw new RuntimeException("无权限操作此场景");
        }
        
        Scene scene = getSceneById(id);
        if (scene != null) {
            scene.setIsDeleted(true);
            sceneDao.save(scene);
        }
    }

    @Override
    public Scene toggleSceneStatus(Long id, Integer status, Long userId) {
        // 检查权限
        if (!checkUserPermission(userId, id)) {
            throw new RuntimeException("无权限操作此场景");
        }
        
        Scene scene = getSceneById(id);
        if (scene != null) {
            scene.setStatus(status);
            return sceneDao.save(scene);
        }
        return null;
    }

    @Override
    @Transactional
    public void activateScene(Long id, Long userId) {
        // 检查权限
        if (!checkUserPermission(userId, id)) {
            throw new RuntimeException("无权限操作此场景");
        }
        
        Scene scene = getSceneById(id);
    
        // 检查场景是否启用
        if (scene == null || scene.getStatus() != 1) {
            throw new RuntimeException("场景不存在或未启用");
        }
    
        // 根据激活类型判断是否满足激活条件
        boolean shouldActivate = false;
        ActivationType activationType = scene.getActivationTypeEnum();

        if (activationType == null) {
            // 默认无条件激活
            shouldActivate = true;
        } else if (activationType == ActivationType.TIME_BASED) {
            // 定时激活 - 检查当前时间是否晚于或等于设定时间
            if (scene.getStartTime() != null) {
                LocalDateTime now = LocalDateTime.now();
                
                // 只比较时间部分（小时和分钟）
                LocalTime nowTime = now.toLocalTime();
                LocalTime startTime = scene.getStartTime().toLocalTime();
                
                shouldActivate = nowTime.isAfter(startTime) || nowTime.equals(startTime);
            }
        } else if (activationType == ActivationType.HUMAN_DETECTED) {
            // 基于人体感应激活 - 保持不变
            // 获取最新的MQTT数据，检查是否有人体感应
            Mqttdata latestMqttData = mqttDataService.getLatestMqttDataByDeviceId(scene.getHomeId());
            if (latestMqttData != null && latestMqttData.getIsHuman() != null) {
                shouldActivate = "1".equals(latestMqttData.getIsHuman());
            }
        }
    
        // 如果满足激活条件，则执行场景
        if (shouldActivate) {
            // 发送设备指令
            String commands = scene.getDeviceCommands();
            System.out.println("准备发送MQTT消息，场景ID: " + scene.getId() + ", 指令: " + commands);
            
            try {
                // 使用正确的方法重载发送场景消息
                mqttMessageSender.sendSceneMessage(scene.getId(), "activate", commands);
                System.out.println("MQTT消息发送成功");
            } catch (Exception e) {
                System.err.println("MQTT消息发送失败: " + e.getMessage());
                e.printStackTrace();
            }
            
            // 更新场景的激活状态
            scene.setIsActive(true);
            sceneDao.save(scene);
        } else {
            System.out.println("不满足场景激活条件，场景ID: " + scene.getId());
        }
    }

    @Override
    public boolean checkSceneNameExists(String name, Long homeId) {
        return sceneDao.existsByNameAndHomeIdAndIsDeletedFalse(name, homeId);
    }

    @Override
    public boolean checkUserPermission(Long userId, Long sceneId) {
        Scene scene = getSceneById(sceneId);
        // 这里简化处理，实际应该检查用户是否拥有该家庭的权限
        return scene != null;
    }

    @Override
    public void executeSceneActivation(Scene scene) {
        // 检查场景是否启用
        if (scene != null && scene.getStatus() == 1) {
            // 发送设备指令
            String commands = scene.getDeviceCommands();
            System.out.println("准备执行场景激活，场景ID: " + scene.getId() + ", 指令: " + commands);
            
            try {
                mqttMessageSender.sendSceneMessage(scene.getId(), "execute", commands);
                System.out.println("场景激活MQTT消息发送成功");
            } catch (Exception e) {
                System.err.println("场景激活MQTT消息发送失败: " + e.getMessage());
                e.printStackTrace();
            }
            
            // 更新场景的激活状态
            scene.setIsActive(true);
            sceneDao.save(scene);
        } else {
            System.out.println("场景未启用，不执行激活: " + (scene != null ? scene.getId() : "null"));
        }
    }
    
    // 这个方法需要添加到ISceneService接口中
    public List<Scene> getEnabledScenesByHomeId(Long homeId) {
        return sceneDao.findByHomeIdAndStatusAndIsDeleted(homeId, 1, false);
    }
    
    /**
     * 定时任务：每分钟检查一次所有启用的定时激活场景
     * 这个方法会自动运行，不需要手动调用
     */
    @Scheduled(cron = "0 * * * * ?") // 每分钟的第0秒执行一次
    public void checkAndActivateScheduledScenes() {
        try {
            // 获取所有启用的场景
            List<Scene> enabledScenes = sceneDao.findByStatusAndIsDeletedFalse(1);
            
            if (enabledScenes != null && !enabledScenes.isEmpty()) {
                LocalDateTime now = LocalDateTime.now();
                LocalTime nowTime = now.toLocalTime();
                
                for (Scene scene : enabledScenes) {
                    // 只处理定时激活类型的场景
                    if (scene.getActivationType() != null && scene.getActivationType() == 0) { // 0表示定时激活
                        if (scene.getStartTime() != null) {
                            LocalTime startTime = scene.getStartTime().toLocalTime();
                            
                            // 检查当前时间是否晚于或等于设定时间
                            if (nowTime.isAfter(startTime) || nowTime.equals(startTime)) {
                                // 检查今天是否已经激活过，避免重复激活
                                LocalDateTime lastActivatedTime = scene.getLastActivatedTime();
                                if (lastActivatedTime == null || 
                                    !lastActivatedTime.toLocalDate().isEqual(now.toLocalDate())) {
                                    
                                    System.out.println("定时任务：时间匹配，准备激活场景: " + scene.getId() + ", " + scene.getName());
                                    executeSceneActivation(scene);
                                    
                                    // 更新最后激活时间
                                    scene.setLastActivatedTime(now);
                                    sceneDao.save(scene);
                                }
                            }
                        } else {
                            executeSceneActivation(scene);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("定时任务检查场景激活失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 响应人体感应事件
     * 这个方法应该在MQTT消息接收的地方被调用
     * @param homeId 家庭ID
     * @param isHuman 人体感应状态 ("1"表示检测到人，其他表示未检测到)
     */
    public void handleHumanDetectionEvent(Long homeId, String isHuman) {
        try {
            // 获取该家庭下所有启用的场景，并在服务层进行过滤
            List<Scene> enabledScenes = sceneDao.findByHomeIdAndStatusAndIsDeleted(homeId, 1, false);
            List<Scene> enabledHumanScenes = enabledScenes.stream()
                    .filter(scene -> scene.getActivationType() != null && scene.getActivationType() == 1) // 1表示人体感应激活
                    .collect(Collectors.toList());
            
            if (enabledHumanScenes != null && !enabledHumanScenes.isEmpty() && "1".equals(isHuman)) {
                for (Scene scene : enabledHumanScenes) {
                    System.out.println("人体感应：检测到人，准备激活场景: " + scene.getId() + ", " + scene.getName());
                    executeSceneActivation(scene);
                }
            }
        } catch (Exception e) {
            System.err.println("处理人体感应事件失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // 移除不必要的方法，直接在handleHumanDetectionEvent中实现相同功能
    }
