package com.cqu.smarthome.demo.pojo.dto;


import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class RegisterRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 1, max = 10, message = "用户名长度必须在1-10个字符之间")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, message = "密码长度不能少于8个字符")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d!-/:-@\\[-`{-~]{8,}$",
            message = "密码至少包含字母和数字")
    private String password;
    
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入有效的手机号码")
    private String phone;
    
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "请输入有效的邮箱地址")
    private String email;
    
    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "验证码必须是6位数字")
    private String verificationCode;

    // getter和setter方法
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }
}