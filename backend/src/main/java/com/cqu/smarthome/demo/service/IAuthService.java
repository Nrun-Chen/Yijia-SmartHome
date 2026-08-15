package com.cqu.smarthome.demo.service;

import com.cqu.smarthome.demo.pojo.User;

public interface IAuthService {
    boolean checkPhone(String phone);

    boolean checkPassword(String username, String password);

    void createUser(User user);

    String getToken(String userId);

    Long getUserIdByPhone(String phone);

    User getUserByPhone(String phone);
    
    // 新增方法：通过邮箱发送验证码
    boolean sendVerificationCode(String email);
    
    // 新增方法：通过验证码重置密码
    boolean resetPasswordByCode(String email, String code, String newPassword);
    
    // 新增方法：检查邮箱是否存在
    boolean checkEmail(String email);
    
    // 添加通过邮箱验证密码的方法
    boolean checkPasswordByEmail(String email, String password);
    
    // 添加通过邮箱获取用户ID的方法
    Long getUserIdByEmail(String email);
    
    // 新增：检查用户名是否已存在
    boolean checkUsername(String username);
}
