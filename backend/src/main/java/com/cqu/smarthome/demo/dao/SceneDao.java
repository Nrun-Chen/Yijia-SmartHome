package com.cqu.smarthome.demo.dao;

import com.cqu.smarthome.demo.pojo.Scene;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SceneDao extends JpaRepository<Scene, Long> {
    
    // 根据家庭ID查询场景
    List<Scene> findByHomeIdAndIsDeletedFalse(Long homeId);
    
    // 查询所有启用状态的场景
    List<Scene> findByStatusAndIsDeletedFalse(Integer status);
    
    // 根据激活类型查询启用状态的场景
    List<Scene> findByActivationTypeAndStatusAndIsDeletedFalse(Integer activationType, Integer status);
    
    // 查询所有未删除的场景
    List<Scene> findByIsDeletedFalse();
    
    // 根据家庭ID和名称查询场景（用于判断名称是否重复）
    boolean existsByNameAndHomeIdAndIsDeletedFalse(String name, Long homeId);
        /**
     * 根据家庭ID查询场景列表
     */
    List<Scene> findByHomeIdAndIsDeleted(Long homeId, Boolean isDeleted);

    /**
     * 根据家庭ID和启用状态查询场景列表
     */
    List<Scene> findByHomeIdAndStatusAndIsDeleted(Long homeId, Integer status, Boolean isDeleted);

    /**
     * 根据ID查询场景，同时检查是否被删除
     */
    Optional<Scene> findByIdAndIsDeleted(Long id, Boolean isDeleted);
}