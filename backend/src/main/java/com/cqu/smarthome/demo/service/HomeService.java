package com.cqu.smarthome.demo.service;

import com.cqu.smarthome.demo.dao.HomeDao;
import com.cqu.smarthome.demo.dao.UserHomeDao;
import com.cqu.smarthome.demo.pojo.Home;
import com.cqu.smarthome.demo.pojo.UserHome;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class HomeService implements IHomeService {
    
    @Autowired
    private HomeDao homeDao;
    
    @Autowired
    private UserHomeDao userHomeDao;
    
    @Override
    @Transactional
    public Home createHome(Home home) {
        // 添加参数验证
        if (home == null) {
            throw new RuntimeException("家庭信息不能为空");
        }
        
        if (home.getName() == null || home.getName().trim().isEmpty()) {
            throw new RuntimeException("家庭名称不能为空");
        }
        
        // 已经不需要手动设置时间了，实体类默认值会处理
        home.setDeleted(false);
        return homeDao.save(home);
    }
    
    @Override
    @Transactional
    public boolean deleteHome(Long homeId, Long userId) {
        // 检查用户是否是房主
        if (!checkUserIsHost(userId, homeId)) {
            return false;
        }
        
        // 逻辑删除家庭
        Optional<Home> homeOptional = homeDao.findById(homeId);
        if (homeOptional.isPresent()) {
            Home home = homeOptional.get();
            home.setDeleted(true);
            homeDao.save(home);
            return true;
        }
        return false;
    }
    
    @Override
    @Transactional
    public Home updateHome(Home home, Long userId) {
        // 检查用户是否是房主或家庭成员
        if (!checkUserIsHost(userId, home.getId()) && !checkUserIsMember(userId, home.getId())) {
            return null;
        }
        
        // 检查家庭是否存在
        if (!homeDao.existsByIdAndIsDeletedFalse(home.getId())) {
            return null;
        }
        
        return homeDao.save(home);
    }
    
    @Override
    public List<Home> getHomesByUserId(Long userId) {
        List<Long> homeIds = userHomeDao.findByUserIdAndIsDeletedFalse(userId)
                .stream().map(UserHome::getHomeId).collect(Collectors.toList());
        return homeDao.findByIdInAndIsDeletedFalse(homeIds);
    }
    
    @Override
    public Home getHomeById(Long homeId) {
        // 更明确地处理Optional类型转换
        Optional<Home> homeOptional = homeDao.findByIdAndIsDeletedFalse(homeId);
        return homeOptional.orElse(null);
    }
    
    @Override
    public boolean checkUserIsHost(Long userId, Long homeId) {
        Optional<UserHome> userHomeOptional = userHomeDao.findByUserIdAndHomeIdAndIsDeletedFalse(userId, homeId);
        return userHomeOptional.isPresent() && userHomeOptional.get().getRole() == 0; // 0表示房主
    }
    
    @Override
    public boolean checkUserIsMember(Long userId, Long homeId) {
        Optional<UserHome> userHomeOptional = userHomeDao.findByUserIdAndHomeIdAndIsDeletedFalse(userId, homeId);
        return userHomeOptional.isPresent() && userHomeOptional.get().getRole() != 2; // 1表示家庭成员
    }
}