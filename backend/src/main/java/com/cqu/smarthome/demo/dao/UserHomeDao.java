package com.cqu.smarthome.demo.dao;

import com.cqu.smarthome.demo.pojo.UserHome;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserHomeDao extends CrudRepository<UserHome, Long> {
    // 根据用户ID和家庭ID查找关联
    Optional<UserHome> findByUserIdAndHomeIdAndIsDeletedFalse(Long userId, Long homeId);
    
    // 根据用户ID查找其所有家庭关联
    List<UserHome> findByUserIdAndIsDeletedFalse(Long userId);
    
    // 根据家庭ID查找所有成员
    List<UserHome> findByHomeIdAndIsDeletedFalse(Long homeId);
    
    // 根据家庭ID和角色查找成员
    List<UserHome> findByHomeIdAndRoleAndIsDeletedFalse(Long homeId, Integer role);
    
    // 检查用户是否在某个家庭中
    boolean existsByUserIdAndHomeIdAndIsDeletedFalse(Long userId, Long homeId);
    
    // 根据家庭ID和用户ID删除关联
    void deleteByHomeIdAndUserId(Long homeId, Long userId);
}