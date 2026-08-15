package com.cqu.smarthome.demo.dao;

import com.cqu.smarthome.demo.pojo.GuestRecord;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GuestRecordDao extends CrudRepository<GuestRecord, Long> {
    // 根据用户ID查找所有访客记录
    List<GuestRecord> findByUserIdAndIsDeletedFalse(Long userId);
    
    // 根据家庭ID查找所有访客记录
    List<GuestRecord> findByHomeIdAndIsDeletedFalse(Long homeId);
}