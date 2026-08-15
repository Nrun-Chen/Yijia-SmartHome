package com.cqu.smarthome.demo.service;

import com.cqu.smarthome.demo.dao.LogDao;
import com.cqu.smarthome.demo.pojo.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class LogService implements ILogService {
    
    @Autowired
    private LogDao logDao;
    
    @Override
    public Log addLog(Log log) {
        return logDao.save(log);
    }
    
    @Override
    public Optional<Log> getLogById(Long id) {
        return logDao.findById(id);
    }
    
    @Override
    public List<Log> getLogsByUserId(Long userId) {
        return logDao.findByUserId(userId);
    }
    
    @Override
    public List<Log> getLogsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return logDao.findByCreateTimeBetween(startTime, endTime);
    }
    
    @Override
    public List<Log> getLogsByUserIdAndTimeRange(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        return logDao.findByUserIdAndCreateTimeBetween(userId, startTime, endTime);
    }
    
    @Override
    public Log updateLog(Log log) {
        // 检查日志是否存在
        if (!logDao.existsById(log.getId())) {
            return null; // 或者抛出异常
        }
        return logDao.save(log);
    }
    
    @Override
    public void deleteLog(Long id) {
        logDao.deleteById(id);
    }
    
    @Override
    public List<Log> getLatestLogs(int limit) {
        // 将所有日志转换为List并按创建时间倒序排序，然后取前limit条
        List<Log> allLogs = StreamSupport.stream(logDao.findAll().spliterator(), false)
                .collect(Collectors.toList());
        
        return allLogs.stream()
                .sorted((log1, log2) -> log2.getCreateTime().compareTo(log1.getCreateTime()))
                .limit(limit)
                .collect(Collectors.toList());
    }
}