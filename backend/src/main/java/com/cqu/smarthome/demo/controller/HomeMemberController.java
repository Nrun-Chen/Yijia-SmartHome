package com.cqu.smarthome.demo.controller;

import com.cqu.smarthome.demo.pojo.ResponseMessage;
import com.cqu.smarthome.demo.pojo.UserHome;
import com.cqu.smarthome.demo.pojo.dto.ViewHomeUser;
import com.cqu.smarthome.demo.service.IHomeMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/home-member")
public class HomeMemberController {

    @Autowired
    private IHomeMemberService homeMemberService;

    // POST - 添加家庭成员
    @PostMapping
    public ResponseMessage<UserHome> addHomeMember(
            @RequestParam Long homeId,
            @RequestParam Long userId,
            @RequestParam Integer role) {
        try {
            UserHome userHome = homeMemberService.addHomeMember(homeId, userId, role);
            return ResponseMessage.success(userHome);
        } catch (RuntimeException e) {
            return new ResponseMessage<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null);
        }
    }

    // DELETE - 移除家庭成员
    @DeleteMapping
    public ResponseMessage<String> removeHomeMember(
            @RequestParam Long homeId,
            @RequestParam Long userId,
            @RequestParam Long operatorId) {
        try {
            boolean removed = homeMemberService.removeHomeMember(homeId, userId, operatorId);
            if (removed) {
                return ResponseMessage.success("家庭成员移除成功");
            } else {
                return new ResponseMessage<>(HttpStatus.BAD_REQUEST.value(), "移除失败", null);
            }
        } catch (RuntimeException e) {
            return new ResponseMessage<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null);
        }
    }

    // PUT - 更新家庭成员角色
    @PutMapping
    public ResponseMessage<UserHome> updateMemberRole(
            @RequestParam Long homeId,
            @RequestParam Long userId,
            @RequestParam Integer role,
            @RequestParam Long operatorId) {
        try {
            UserHome userHome = homeMemberService.updateMemberRole(homeId, userId, role, operatorId);
            return ResponseMessage.success(userHome);
        } catch (RuntimeException e) {
            return new ResponseMessage<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null);
        }
    }

    // GET - 获取家庭所有成员
    @GetMapping("/home/{homeId}")
    public ResponseMessage<List<UserHome>> getHomeMembers(@PathVariable Long homeId) {
        try {
            List<UserHome> members = homeMemberService.getHomeMembers(homeId);
            return ResponseMessage.success(members);
        } catch (RuntimeException e) {
            return new ResponseMessage<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null);
        }
    }
    
    // 新增接口 - 获取包含详细信息的家庭成员列表
    @GetMapping("/home/{homeId}/details")
    public ResponseMessage<List<ViewHomeUser>> getHomeMembersWithDetails(@PathVariable Long homeId) {
        try {
            List<ViewHomeUser> members = homeMemberService.getHomeMembersWithDetails(homeId);
            return ResponseMessage.success(members);
        } catch (RuntimeException e) {
            return new ResponseMessage<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null);
        }
    }

    // GET - 获取用户加入的所有家庭
    @GetMapping("/user/{userId}")
    public ResponseMessage<List<UserHome>> getUserHomes(@PathVariable Long userId) {
        try {
            List<UserHome> userHomes = homeMemberService.getUserHomes(userId);
            return ResponseMessage.success(userHomes);
        } catch (RuntimeException e) {
            return new ResponseMessage<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null);
        }
    }

    // GET - 判断用户是否是家庭成员
    @GetMapping("/check")
    public ResponseMessage<Boolean> isHomeMember(
            @RequestParam Long homeId,
            @RequestParam Long userId) {
        boolean isMember = homeMemberService.isHomeMember(homeId, userId);
        return ResponseMessage.success(isMember);
    }

    // GET - 获取用户在家庭中的角色
    @GetMapping("/role")
    public ResponseMessage<Integer> getUserRoleInHome(
            @RequestParam Long homeId,
            @RequestParam Long userId) {
        Integer role = homeMemberService.getUserRoleInHome(homeId, userId);
        if (role != null) {
            return ResponseMessage.success(role);
        } else {
            return new ResponseMessage<>(HttpStatus.NOT_FOUND.value(), "用户不是该家庭成员", null);
        }
    }
}