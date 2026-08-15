package com.cqu.smarthome.demo.service;

import com.cqu.smarthome.demo.dao.UserDao;
import com.cqu.smarthome.demo.pojo.JWTUtils;
import com.cqu.smarthome.demo.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
public class AuthService implements IAuthService {
    @Autowired
    private UserDao userDao;

    @Autowired
    private JWTUtils jwtUtils;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private VerificationCodeService verificationCodeService;
    
    // 添加PasswordEncoder自动注入
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public boolean checkPhone(String phone) {
        return userDao.existsByPhoneAndIsDeletedFalse(phone);
    }

    @Override
    public boolean checkPassword(String phone, String password) {
    Optional<User> userOptional = userDao.findByPhoneAndIsDeletedFalse(phone);
    return userOptional.isPresent() && passwordEncoder.matches(password, userOptional.get().getPassword());
    }

    @Override
    @Transactional
    public void createUser(User user) {
        // 使用passwordEncoder进行密码加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setDeleted(false);
        userDao.save(user);
    }

    @Override
    public String getToken(String userId) {
        return jwtUtils.generateToken(userId);
    }

    @Override
    public Long getUserIdByPhone(String phone) {
        return userDao.findByPhoneAndIsDeletedFalse(phone)
                .map(User::getUserId)
                .orElse(null);
    }

    @Override
    public User getUserByPhone(String phone) {
        return userDao.findByPhoneAndIsDeletedFalse(phone).orElse(null);
    }
    
    @Override
    public boolean checkEmail(String email) {
        return userDao.existsByEmailAndIsDeletedFalse(email);
    }
    
    @Override
    public boolean checkPasswordByEmail(String email, String password) {
        Optional<User> userOptional = userDao.findByEmailAndIsDeletedFalse(email);
        return userOptional.isPresent() && passwordEncoder.matches(password, userOptional.get().getPassword());
    }
    
    @Override
    public Long getUserIdByEmail(String email) {
        return userDao.findByEmailAndIsDeletedFalse(email)
                .map(User::getUserId)
                .orElse(null);
    }
    
    @Override
    public boolean checkUsername(String username) {
        return userDao.existsByUsernameAndIsDeletedFalse(username);
    }
    
    @Override
    public boolean sendVerificationCode(String email) {
        // 检查邮箱是否存在
        if (!checkEmail(email)) {
            return false;
        }
        
        try {
            // 生成验证码
            String code = verificationCodeService.generateCode(email);
            // 发送验证码
            emailService.sendVerificationCode(email, code);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean resetPasswordByCode(String email, String code, String newPassword) {
        // 验证验证码
        if (!verificationCodeService.verifyCode(email, code)) {
            return false;
        }
        
        // 查找用户并更新密码
        Optional<User> userOptional = userDao.findByEmailAndIsDeletedFalse(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // 使用passwordEncoder进行密码加密
            user.setPassword(passwordEncoder.encode(newPassword));
            userDao.save(user);
            return true;
        }
        
        return false;
    }
}
