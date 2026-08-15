package com.cqu.smarthome.demo.controller;

import com.cqu.smarthome.demo.pojo.ResponseMessage;
import com.cqu.smarthome.demo.pojo.Scene;
import com.cqu.smarthome.demo.service.ISceneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scenes")
public class SceneController {

    @Autowired
    private ISceneService sceneService;

    /**
     * 创建新场景
     */
    @PostMapping
    public ResponseMessage<Scene> createScene(@RequestBody Scene scene) {
        try {
            // 这里暂时使用一个固定的userId，实际应该从用户会话或token中获取
            Long userId = 1L; // 示例用户ID
            Scene createdScene = sceneService.createScene(scene, userId);
            return new ResponseMessage<>(200, "场景创建成功", createdScene);
        } catch (RuntimeException e) {
            return new ResponseMessage<>(400, e.getMessage(), null);
        } catch (Exception e) {
            return new ResponseMessage<>(500, "系统异常，请稍后再试", null);
        }
    }

    /**
     * 更新场景
     */
    @PutMapping
    public ResponseMessage<Scene> updateScene(@RequestBody Scene scene) {
        try {
            // 这里暂时使用一个固定的userId，实际应该从用户会话或token中获取
            Long userId = 1L; // 示例用户ID
            Scene updatedScene = sceneService.updateScene(scene, userId);
            return new ResponseMessage<>(200, "场景更新成功", updatedScene);
        } catch (RuntimeException e) {
            return new ResponseMessage<>(400, e.getMessage(), null);
        } catch (Exception e) {
            return new ResponseMessage<>(500, "系统异常，请稍后再试", null);
        }
    }

    /**
     * 删除场景（逻辑删除）
     */
    @DeleteMapping("/{sceneId}")
    public ResponseMessage<Void> deleteScene(@PathVariable Long sceneId) {
        try {
            // 这里暂时使用一个固定的userId，实际应该从用户会话或token中获取
            Long userId = 1L; // 示例用户ID
            sceneService.deleteScene(sceneId, userId);
            return new ResponseMessage<>(200, "场景删除成功", null);
        } catch (RuntimeException e) {
            return new ResponseMessage<>(400, e.getMessage(), null);
        } catch (Exception e) {
            return new ResponseMessage<>(500, "系统异常，请稍后再试", null);
        }
    }

    /**
     * 根据ID获取场景详情
     */
    @GetMapping("/{sceneId}")
    public ResponseMessage<Scene> getSceneById(@PathVariable Long sceneId) {
        try {
            Scene scene = sceneService.getSceneById(sceneId);
            return new ResponseMessage<>(200, "查询成功", scene);
        } catch (RuntimeException e) {
            return new ResponseMessage<>(404, e.getMessage(), null);
        } catch (Exception e) {
            return new ResponseMessage<>(500, "系统异常，请稍后再试", null);
        }
    }

    /**
     * 根据家庭ID获取所有场景
     */
    @GetMapping("/home/{homeId}")
    public ResponseMessage<List<Scene>> getScenesByHomeId(@PathVariable Long homeId) {
        try {
            // 这里暂时使用一个固定的userId，实际应该从用户会话或token中获取
            Long userId = 1L; // 示例用户ID
            List<Scene> scenes = sceneService.getScenesByHomeId(homeId, userId);
            return new ResponseMessage<>(200, "查询成功", scenes);
        } catch (Exception e) {
            return new ResponseMessage<>(500, "系统异常，请稍后再试", null);
        }
    }

    /**
     * 激活场景
     */
    @PostMapping("/{sceneId}/activate")
    public ResponseMessage<Boolean> activateScene(@PathVariable Long sceneId) {
        try {
            // 这里暂时使用一个固定的userId，实际应该从用户会话或token中获取
            Long userId = 1L; // 示例用户ID
            sceneService.activateScene(sceneId, userId);
            return new ResponseMessage<>(200, "场景激活成功", true);
        } catch (RuntimeException e) {
            return new ResponseMessage<>(400, e.getMessage(), false);
        } catch (Exception e) {
            return new ResponseMessage<>(500, "系统异常，请稍后再试", false);
        }
    }

    /**
     * 获取家庭的所有启用场景
     */
    @GetMapping("/home/{homeId}/enabled")
    public ResponseMessage<List<Scene>> getEnabledScenesByHomeId(@PathVariable Long homeId) {
        try {
            List<Scene> enabledScenes = sceneService.getEnabledScenesByHomeId(homeId);
            return new ResponseMessage<>(200, "查询成功", enabledScenes);
        } catch (Exception e) {
            return new ResponseMessage<>(500, "系统异常，请稍后再试", null);
        }
    }
}