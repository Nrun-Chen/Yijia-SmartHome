package com.cqu.smarthome.demo.service;

import com.cqu.smarthome.demo.pojo.Log;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ILogService {
    // 添加日志
    Log addLog(Log log);
    
    // 根据ID获取日志
    Optional<Log> getLogById(Long id);
    
    // 根据用户ID获取日志
    List<Log> getLogsByUserId(Long userId);
    
    // 根据时间范围获取日志
    List<Log> getLogsByTimeRange(LocalDateTime startTime, LocalDateTime endTime);
    
    // 根据用户ID和时间范围获取日志
    List<Log> getLogsByUserIdAndTimeRange(Long userId, LocalDateTime startTime, LocalDateTime endTime);
    
    // 更新日志
    Log updateLog(Log log);
    
    // 删除日志
    void deleteLog(Long id);
    
    // 获取最新的日志列表
    List<Log> getLatestLogs(int limit);
}