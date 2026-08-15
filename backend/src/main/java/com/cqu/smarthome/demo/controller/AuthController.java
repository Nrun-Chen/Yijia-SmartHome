package com.cqu.smarthome.demo.controller;

import com.cqu.smarthome.demo.pojo.User;
import com.cqu.smarthome.demo.pojo.dto.LoginRequest;
import com.cqu.smarthome.demo.pojo.dto.RegisterRequest;
import com.cqu.smarthome.demo.service.EmailService;
import com.cqu.smarthome.demo.service.IAuthService;
import com.cqu.smarthome.demo.service.VerificationCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private IAuthService authService;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private VerificationCodeService verificationCodeService;
    
    // 用户注册接口
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        // 检查用户名是否已存在
        if (authService.checkUsername(registerRequest.getUsername())) {
            return ResponseEntity.badRequest().body("用户名已被使用");
        }
        
        // 检查手机号是否已注册
        if (authService.checkPhone(registerRequest.getPhone())) {
            return ResponseEntity.badRequest().body("手机号已被注册");
        }
        
        // 检查邮箱是否已注册
        if (authService.checkEmail(registerRequest.getEmail())) {
            return ResponseEntity.badRequest().body("邮箱已被注册");
        }
        
        // 验证验证码
        if (!verificationCodeService.verifyCode(registerRequest.getEmail(), registerRequest.getVerificationCode())) {
            return ResponseEntity.badRequest().body("验证码错误或已过期");
        }
        
        // 创建用户
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(registerRequest.getPassword());
        user.setPhone(registerRequest.getPhone());
        user.setEmail(registerRequest.getEmail());
        
        authService.createUser(user);
        return ResponseEntity.ok("注册成功");
    }

    // 用户登录接口
    // 修改用户登录接口，支持JSON请求体
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        String identifier = loginRequest.getIdentifier();
        String password = loginRequest.getPassword();
        
        // 验证标识符格式，判断是手机号还是邮箱
        boolean isPhone = identifier.matches("^1[3-9]\\d{9}$");
        boolean isEmail = identifier.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
        
        if (!isPhone && !isEmail) {
            return ResponseEntity.badRequest().body("请输入有效的手机号或邮箱");
        }
        
        // 根据标识符类型进行登录验证
        boolean isAuthenticated;
        Long userId = null;
        
        if (isPhone) {
            isAuthenticated = authService.checkPassword(identifier, password);
            userId = authService.getUserIdByPhone(identifier);
        } else {
            isAuthenticated = authService.checkPasswordByEmail(identifier, password);
            userId = authService.getUserIdByEmail(identifier);
        }
        
        if (!isAuthenticated) {
            return ResponseEntity.badRequest().body("用户名或密码错误");
        }
        
        String token = authService.getToken(userId.toString());
        return ResponseEntity.ok(token);
    }

    // 获取用户信息接口
    @GetMapping("/user")
    public ResponseEntity<?> getUserByPhone(@RequestParam String phone) {
        User user = authService.getUserByPhone(phone);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        // 隐藏密码等敏感信息
        user.setPassword(null);
        return ResponseEntity.ok(user);
    }
    
    // 注册时发送验证码接口
    @PostMapping("/register/send-code")
    public ResponseEntity<?> sendRegisterVerificationCode(@RequestParam String email) {
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body("邮箱不能为空");
        }
        
        // 检查邮箱格式
        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            return ResponseEntity.badRequest().body("请输入有效的邮箱地址");
        }
        
        // 检查邮箱是否已注册
        if (authService.checkEmail(email)) {
            return ResponseEntity.badRequest().body("该邮箱已被注册");
        }
        
        try {
            // 生成验证码
            String code = verificationCodeService.generateCode(email);
            // 发送验证码
            emailService.sendVerificationCode(email, code);
            return ResponseEntity.ok("验证码已发送，请注意查收");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("发送验证码失败，请稍后重试");
        }
    }
    
    // 新增：重置密码接口
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestParam String email,
            @RequestParam String code,
            @RequestParam String newPassword) {
        
        if (authService.resetPasswordByCode(email, code, newPassword)) {
            return ResponseEntity.ok("密码重置成功");
        } else {
            return ResponseEntity.badRequest().body("验证码无效或已过期");
        }
    }
    
    // 新增：重置密码时发送验证码接口
    @PostMapping("/reset-password/send-code")
    public ResponseEntity<?> sendResetPasswordVerificationCode(@RequestParam String email) {
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body("邮箱不能为空");
        }
        
        // 检查邮箱格式
        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            return ResponseEntity.badRequest().body("请输入有效的邮箱地址");
        }
        
        // 检查邮箱是否已注册（重置密码需要邮箱已存在）
        if (!authService.checkEmail(email)) {
            return ResponseEntity.badRequest().body("该邮箱未注册");
        }
        
        try {
            // 生成验证码
            String code = verificationCodeService.generateCode(email);
            // 发送验证码
            emailService.sendVerificationCode(email, code);
            return ResponseEntity.ok("重置密码验证码已发送，请注意查收");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("发送验证码失败，请稍后重试");
        }
    }

    // 新增：测试邮件发送接口（用于调试）
    @GetMapping("/test-email")
    public ResponseEntity<?> testEmail(@RequestParam String email) {
        try {
            emailService.sendTestEmail(email);
            return ResponseEntity.ok("测试邮件已发送，请查收");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("测试邮件发送失败：" + e.getMessage());
        }
    }
}
