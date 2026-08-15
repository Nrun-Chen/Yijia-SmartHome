package com.cqu.smarthome.demo.dao;

import com.cqu.smarthome.demo.pojo.Log;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LogDao extends CrudRepository<Log, Long> {
    
    // 根据用户ID查询日志
    List<Log> findByUserId(Long userId);
    
    // 根据创建时间范围查询日志
    List<Log> findByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    // 根据用户ID和创建时间范围查询日志
    List<Log> findByUserIdAndCreateTimeBetween(Long userId, LocalDateTime startTime, LocalDateTime endTime);
    
    // 根据用户ID查询最新的N条日志
    List<Log> findTop100ByUserIdOrderByCreateTimeDesc(Long userId);
}