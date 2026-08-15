package com.cqu.smarthome.demo.dao;

import com.cqu.smarthome.demo.pojo.Room;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomDao extends CrudRepository<Room, Long> {
    // 根据家庭ID查找所有房间
    List<Room> findByHomeIdAndIsDeletedFalse(Long homeId);
    
    // 根据家庭ID和房间名称查找
    Optional<Room> findByHomeIdAndNameAndIsDeletedFalse(Long homeId, String name);
    
    // 检查房间名称在家庭中是否已存在
    boolean existsByHomeIdAndNameAndIsDeletedFalse(Long homeId, String name);
    
    // 批量删除家庭中的所有房间
    void deleteByHomeId(Long homeId);
    
    // 添加：根据ID查找未删除的房间
    Optional<Room> findByIdAndIsDeletedFalse(Long id);
    
    // 添加：根据ID检查未删除的房间是否存在
    boolean existsByIdAndIsDeletedFalse(Long id);
}