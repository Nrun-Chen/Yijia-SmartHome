package com.cqu.smarthome.demo.dao;

import com.cqu.smarthome.demo.pojo.EnterRequestRecord;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnterRequestRecordDao extends CrudRepository<EnterRequestRecord, Long> {
    // 根据用户ID查找所有请求
    List<EnterRequestRecord> findByUserIdAndIsDeletedFalse(Long userId);
    
    // 根据家庭ID查找所有请求
    List<EnterRequestRecord> findByHomeIdAndIsDeletedFalse(Long homeId);
}