package com.cqu.smarthome.demo.controller;

import com.cqu.smarthome.demo.pojo.Log;
import com.cqu.smarthome.demo.pojo.ResponseMessage;
import com.cqu.smarthome.demo.service.ILogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/logs")
public class LogController {
    
    @Autowired
    private ILogService logService;
    
    // POST - 添加日志
    @PostMapping
    public ResponseMessage<Log> addLog(@RequestBody Log log) {
        try {
            Log savedLog = logService.addLog(log);
            return ResponseMessage.success(savedLog);
        } catch (Exception e) {
            return new ResponseMessage<>(HttpStatus.BAD_REQUEST.value(), "添加日志失败: " + e.getMessage(), null);
        }
    }
    
    // GET - 根据ID获取日志
    @GetMapping("/{id}")
    public ResponseMessage<Log> getLogById(@PathVariable Long id) {
        Optional<Log> logOptional = logService.getLogById(id);
        if (logOptional.isPresent()) {
            return ResponseMessage.success(logOptional.get());
        } else {
            return new ResponseMessage<>(HttpStatus.NOT_FOUND.value(), "日志不存在", null);
        }
    }
    
    // GET - 根据用户ID获取日志
    @GetMapping("/user/{userId}")
    public ResponseMessage<List<Log>> getLogsByUserId(@PathVariable Long userId) {
        List<Log> logs = logService.getLogsByUserId(userId);
        return ResponseMessage.success(logs);
    }
    
    // GET - 根据时间范围获取日志
    @GetMapping("/time-range")
    public ResponseMessage<List<Log>> getLogsByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        List<Log> logs = logService.getLogsByTimeRange(startTime, endTime);
        return ResponseMessage.success(logs);
    }
    
    // GET - 根据用户ID和时间范围获取日志
    @GetMapping("/user/{userId}/time-range")
    public ResponseMessage<List<Log>> getLogsByUserIdAndTimeRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        List<Log> logs = logService.getLogsByUserIdAndTimeRange(userId, startTime, endTime);
        return ResponseMessage.success(logs);
    }
    
    // GET - 获取最新的日志
    @GetMapping("/latest")
    public ResponseMessage<List<Log>> getLatestLogs(@RequestParam(defaultValue = "100") int limit) {
        List<Log> logs = logService.getLatestLogs(limit);
        return ResponseMessage.success(logs);
    }
    
    // PUT - 更新日志
    @PutMapping
    public ResponseMessage<Log> updateLog(@RequestBody Log log) {
        try {
            Log updatedLog = logService.updateLog(log);
            if (updatedLog != null) {
                return ResponseMessage.success(updatedLog);
            } else {
                return new ResponseMessage<>(HttpStatus.NOT_FOUND.value(), "日志不存在", null);
            }
        } catch (Exception e) {
            return new ResponseMessage<>(HttpStatus.BAD_REQUEST.value(), "更新日志失败: " + e.getMessage(), null);
        }
    }
    
    // DELETE - 删除日志
    @DeleteMapping("/{id}")
    public ResponseMessage<String> deleteLog(@PathVariable Long id) {
        try {
            logService.deleteLog(id);
            return ResponseMessage.success("日志删除成功");
        } catch (Exception e) {
            return new ResponseMessage<>(HttpStatus.BAD_REQUEST.value(), "删除日志失败: " + e.getMessage(), null);
        }
    }
}