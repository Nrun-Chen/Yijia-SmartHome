package com.cqu.smarthome.demo.service;

import com.cqu.smarthome.demo.dao.HomeDao;
import com.cqu.smarthome.demo.dao.UserDao;
import com.cqu.smarthome.demo.dao.UserHomeDao;
import com.cqu.smarthome.demo.pojo.Home;
import com.cqu.smarthome.demo.pojo.User;
import com.cqu.smarthome.demo.pojo.UserHome;
import com.cqu.smarthome.demo.pojo.dto.ViewHomeUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class HomeMemberService implements IHomeMemberService {
    
    @Autowired
    private UserHomeDao userHomeDao;
    
    @Autowired
    private HomeDao homeDao;
    
    @Autowired
    private UserDao userDao;
    
    @Override
    @Transactional
    public UserHome addHomeMember(Long homeId, Long userId, Integer role) {
        // 验证家庭是否存在
        Optional<Home> homeOptional = homeDao.findByIdAndIsDeletedFalse(homeId);
        if (!homeOptional.isPresent()) {
            throw new RuntimeException("家庭不存在");
        }
        
        // 验证用户是否存在
        Optional<User> userOptional = userDao.findByUserIdAndIsDeletedFalse(userId);
        if (!userOptional.isPresent()) {
            throw new RuntimeException("用户不存在");
        }
        
        // 检查用户是否已经是家庭成员
        if (isHomeMember(homeId, userId)) {
            throw new RuntimeException("用户已经是家庭成员");
        }
        
        // 创建成员关系
        UserHome userHome = new UserHome();
        userHome.setHomeId(homeId);
        userHome.setUserId(userId);
        userHome.setRole(role);
        userHome.setJoinTime(LocalDateTime.now());
        userHome.setIsDeleted(false);
        
        // 添加返回语句，返回保存后的对象
        return userHomeDao.save(userHome);
    }
    
    @Override
    @Transactional
    public boolean removeHomeMember(Long homeId, Long userId, Long operatorId) {
        // 验证家庭是否存在
        Optional<Home> homeOptional = homeDao.findByIdAndIsDeletedFalse(homeId);
        if (!homeOptional.isPresent()) {
            throw new RuntimeException("家庭不存在");
        }
        
        // 验证操作人员用户是否存在 - 新增检查
        Optional<User> operatorOptional = userDao.findByUserIdAndIsDeletedFalse(operatorId);
        if (!operatorOptional.isPresent()) {
            throw new RuntimeException("操作人员不存在");
        }
        
        // 检查操作人是否有权限 - 只有房主(角色0)可以移除成员
        Integer operatorRole = getUserRoleInHome(homeId, operatorId);
        if (operatorRole == null || operatorRole != 0) { // 0：房主
            throw new RuntimeException("只有房主可以移除成员");
        }
        
        // 不能移除自己
        if (userId.equals(operatorId)) {
            throw new RuntimeException("房主不能移除自己");
        }
        
        // 检查目标用户是否是家庭成员
        Optional<UserHome> userHomeOptional = userHomeDao.findByUserIdAndHomeIdAndIsDeletedFalse(userId, homeId);
        if (!userHomeOptional.isPresent()) {
            throw new RuntimeException("用户不是家庭成员");
        }
        
        // 检查目标用户是否是房主 - 房主不能移除其他房主
        UserHome targetUserHome = userHomeOptional.get();
        if (targetUserHome.getRole() != null && targetUserHome.getRole() == 0) {
            throw new RuntimeException("房主不能移除其他房主");
        }
        
        // 逻辑删除成员关系
        targetUserHome.setIsDeleted(true);
        targetUserHome.setLeaveTime(LocalDateTime.now());
        userHomeDao.save(targetUserHome);
        
        return true;
    }
    
    @Override
    @Transactional
    public UserHome updateMemberRole(Long homeId, Long userId, Integer role, Long operatorId) {
        // 验证家庭是否存在
        Optional<Home> homeOptional = homeDao.findByIdAndIsDeletedFalse(homeId);
        if (!homeOptional.isPresent()) {
            throw new RuntimeException("家庭不存在");
        }
        
        // 验证操作人员用户是否存在 - 新增检查
        Optional<User> operatorOptional = userDao.findByUserIdAndIsDeletedFalse(operatorId);
        if (!operatorOptional.isPresent()) {
            throw new RuntimeException("操作人员不存在");
        }
        
        // 检查操作人是否有权限 - 只有房主(角色0)可以修改角色
        Integer operatorRole = getUserRoleInHome(homeId, operatorId);
        if (operatorRole == null || operatorRole != 0) { // 0：房主
            throw new RuntimeException("只有房主可以修改角色");
        }
        
        // 不能修改自己的角色
        if (userId.equals(operatorId)) {
            throw new RuntimeException("房主不能修改自己的角色");
        }
        
        // 检查用户是否是家庭成员
        Optional<UserHome> userHomeOptional = userHomeDao.findByUserIdAndHomeIdAndIsDeletedFalse(userId, homeId);
        if (!userHomeOptional.isPresent()) {
            throw new RuntimeException("用户不是家庭成员");
        }
        
        // 不能将其他用户设置为房主
        if (role != null && role == 0) {
            throw new RuntimeException("不能将其他用户设置为房主");
        }
        
        // 更新角色
        UserHome userHome = userHomeOptional.get();
        userHome.setRole(role);
        userHome.setUpdateTime(LocalDateTime.now());
        
        return userHomeDao.save(userHome);
    }

    @Override
    public List<UserHome> getHomeMembers(Long homeId) {
        // 验证家庭是否存在
        Optional<Home> homeOptional = homeDao.findByIdAndIsDeletedFalse(homeId);
        if (!homeOptional.isPresent()) {
            throw new RuntimeException("家庭不存在");
        }
        
        return userHomeDao.findByHomeIdAndIsDeletedFalse(homeId);
    }
    
    // 新实现的方法 - 获取包含用户详细信息的家庭成员列表
    @Override
    public List<ViewHomeUser> getHomeMembersWithDetails(Long homeId) {
        // 验证家庭是否存在
        Optional<Home> homeOptional = homeDao.findByIdAndIsDeletedFalse(homeId);
        if (!homeOptional.isPresent()) {
            throw new RuntimeException("家庭不存在");
        }
        
        // 获取家庭中的成员关系
        List<UserHome> userHomeList = userHomeDao.findByHomeIdAndIsDeletedFalse(homeId);
        
        // 转换为包含用户详细信息的列表
        List<ViewHomeUser> members = new ArrayList<>();
        for (UserHome userHome : userHomeList) {
            Optional<User> userOptional = userDao.findByUserIdAndIsDeletedFalse(userHome.getUserId());
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                members.add(new ViewHomeUser(
                        user.getUserId(),
                        user.getUsername(),
                        user.getPhone(),
                        userHome.getRole()
                ));
            }
        }
        
        return members;
    }

    @Override
    @Transactional
    public List<UserHome> getUserHomes(Long userId) {
        // 验证用户是否存在
        // 修改方法名为正确的findByUserIdAndIsDeletedFalse
        Optional<User> userOptional = userDao.findByUserIdAndIsDeletedFalse(userId);
        if (!userOptional.isPresent()) {
            throw new RuntimeException("用户不存在");
        }
        
        return userHomeDao.findByUserIdAndIsDeletedFalse(userId);
    }

    @Override
    public boolean isHomeMember(Long homeId, Long userId) {
        Optional<UserHome> userHomeOptional = userHomeDao.findByUserIdAndHomeIdAndIsDeletedFalse(userId, homeId);
        return userHomeOptional.isPresent();
    }
    
    @Override
    public Integer getUserRoleInHome(Long homeId, Long userId) {
        Optional<UserHome> userHomeOptional = userHomeDao.findByUserIdAndHomeIdAndIsDeletedFalse(userId, homeId);
        return userHomeOptional.isPresent() ? userHomeOptional.get().getRole() : null;
    }
}