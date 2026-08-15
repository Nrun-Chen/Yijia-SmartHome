package com.cqu.smarthome.demo.service;

import com.cqu.smarthome.demo.dao.UserDao;
import com.cqu.smarthome.demo.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService implements IUserService {

    @Autowired
    UserDao userDao;

    @Override
    public User add(User user) {
        // 有id修改，没有新增
        return userDao.save(user);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userDao.findById(id);
    }

    @Override
    public Iterable<User> findAll() {
        return userDao.findAll();
    }

    @Override
    public User update(User user) {
        // 检查用户是否存在
        if (user.getUserId() == null || !userDao.existsById(user.getUserId())) {
            return null;
        }
        
        // 查询数据库中现有的用户信息
        User existingUser = userDao.findById(user.getUserId()).orElse(null);
        if (existingUser == null) {
            return null;
        }
        
        // 只更新非null的字段，保留其他字段的原值
        if (user.getUsername() != null) {
            existingUser.setUsername(user.getUsername());
        }
        if (user.getPhone() != null) {
            existingUser.setPhone(user.getPhone());
        }
        if (user.getEmail() != null) {
            existingUser.setEmail(user.getEmail());
        }
        if (user.getPassword() != null) {
            existingUser.setPassword(user.getPassword());
        }
        if (user.getDeleted() != null) {
            existingUser.setDeleted(user.getDeleted());
        }
        
        // 保存更新后的用户信息
        return userDao.save(existingUser);
    }

    @Override
    public void deleteById(Long id) {
        userDao.deleteById(id);
    }
}
