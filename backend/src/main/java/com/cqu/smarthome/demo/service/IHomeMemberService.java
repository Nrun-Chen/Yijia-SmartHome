package com.cqu.smarthome.demo.service;

import com.cqu.smarthome.demo.pojo.UserHome;
import com.cqu.smarthome.demo.pojo.dto.ViewHomeUser;

import java.util.List;

public interface IHomeMemberService {
    // 添加家庭成员
    UserHome addHomeMember(Long homeId, Long userId, Integer role);
    
    // 移除家庭成员
    boolean removeHomeMember(Long homeId, Long userId, Long operatorId);
    
    // 更新家庭成员角色
    UserHome updateMemberRole(Long homeId, Long userId, Integer role, Long operatorId);
    
    // 获取家庭所有成员（原有方法 - 不修改）
    List<UserHome> getHomeMembers(Long homeId);
    
    // 获取家庭所有成员（包含用户详细信息的新方法）
    List<ViewHomeUser> getHomeMembersWithDetails(Long homeId);
    
    // 获取用户加入的所有家庭
    List<UserHome> getUserHomes(Long userId);
    
    // 判断用户是否是家庭成员
    boolean isHomeMember(Long homeId, Long userId);
    
    // 获取用户在家庭中的角色
    Integer getUserRoleInHome(Long homeId, Long userId);
}