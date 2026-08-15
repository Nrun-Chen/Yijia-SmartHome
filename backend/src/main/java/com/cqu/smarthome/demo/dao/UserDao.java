package com.cqu.smarthome.demo.dao;

import com.cqu.smarthome.demo.pojo.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDao extends CrudRepository<User,Long> {
    // 根据手机号查询密码
    Optional<String> findPasswordByPhoneAndIsDeletedFalse(String phone);

    // 检查手机号是否存在
    boolean existsByPhoneAndIsDeletedFalse(String phone);

    // 根据手机号查询用户
    Optional<User> findByPhoneAndIsDeletedFalse(String phone);

    // 根据ID查询用户（已排除逻辑删除）
    Optional<User> findByUserIdAndIsDeletedFalse(Long userId); 

    // 根据ID列表查询用户
    List<User> findByUserIdInAndIsDeletedFalse(List<Long> userId);
    
    // 新增：检查邮箱是否存在
    boolean existsByEmailAndIsDeletedFalse(String email);
    
    // 新增：根据邮箱查询用户
    Optional<User> findByEmailAndIsDeletedFalse(String email);
    
    // 新增：检查用户名是否已存在
    boolean existsByUsernameAndIsDeletedFalse(String username);
}
