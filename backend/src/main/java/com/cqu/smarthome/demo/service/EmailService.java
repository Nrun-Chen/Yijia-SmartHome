package com.cqu.smarthome.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail; // 直接从配置文件获取发件人邮箱
    
    public void sendVerificationCode(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("逸家 - 邮箱验证码");
        message.setText("您的验证码是：" + code + "，有效期为5分钟，请尽快使用。");
        
        mailSender.send(message);
    }
    
    // 添加一个测试方法，方便直接测试邮件发送功能
    public void sendTestEmail(String toEmail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("逸家 - 邮件测试");
        message.setText("这是一封测试邮件，说明您的邮箱服务配置正常！");
        
        mailSender.send(message);
    }
}