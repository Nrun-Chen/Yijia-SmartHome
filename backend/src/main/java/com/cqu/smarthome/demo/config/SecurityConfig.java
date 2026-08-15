package com.cqu.smarthome.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF，因为我们使用的是JWT
            .csrf().disable()
            // 设置无状态会话管理
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            // 配置URL权限
            .authorizeRequests()
            // 允许匿名访问认证相关接口
            .antMatchers("/api/auth/**").permitAll()
            // 允许匿名访问用户管理接口
            .antMatchers("/user/**").permitAll()
                // 允许匿名访问AI相关接口
                .antMatchers("/api/ai/**").permitAll()
                // 允许匿名访问静态资源
            .antMatchers("/", "/css/**", "/js/**", "/images/**").permitAll()
            // 其他所有请求都需要认证
            .anyRequest().permitAll()
            // 添加JWT过滤器
            .and()
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
            // 允许匿名访问的接口
        
        return http.build();
    }
}

