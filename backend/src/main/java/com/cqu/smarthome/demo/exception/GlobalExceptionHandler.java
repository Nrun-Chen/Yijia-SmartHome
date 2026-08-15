package com.cqu.smarthome.demo.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    // 处理参数校验异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException e) {
        logger.error("参数校验异常：{}", e.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", HttpStatus.BAD_REQUEST.value());
        
        // 获取具体的错误信息
        StringBuilder errorMessage = new StringBuilder();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errorMessage.append(error.getDefaultMessage()).append("; ");
        }
        
        response.put("message", errorMessage.toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    // 处理其他所有异常
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleException(Exception e, HttpServletRequest request) {
        logger.error("发生未捕获异常：{}", e.getMessage(), e);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("message", "服务器内部错误：" + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    // 新增处理RuntimeException的方法，更精确地捕获业务异常
    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        logger.error("业务异常：{}", e.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", HttpStatus.BAD_REQUEST.value());
        response.put("message", e.getMessage());
        response.put("data", null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}