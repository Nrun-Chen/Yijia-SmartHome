package com.cqu.smarthome.demo.dao;

import com.cqu.smarthome.demo.pojo.Home;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HomeDao extends CrudRepository<Home, Long> {
    // 根据家庭名称和用户ID查找家庭
    Optional<Home> findByNameAndIsDeletedFalse(String name);
    
    // 检查家庭名称是否已存在
    boolean existsByNameAndIsDeletedFalse(String name);
    
    // 添加：检查ID是否存在且未删除
    boolean existsByIdAndIsDeletedFalse(Long id);

    // 添加：根据ID查找未删除的家庭
    Optional<Home> findByIdAndIsDeletedFalse(Long id);
    
    // 添加：根据ID列表查找未删除的家庭
    List<Home> findByIdInAndIsDeletedFalse(List<Long> ids);
}