package com.cqu.smarthome.demo.service;

import com.cqu.smarthome.demo.pojo.User;
import java.util.Optional;

public interface IUserService {
    User add(User user);
    Optional<User> findById(Long id);
    Iterable<User> findAll();
    User update(User user);
    void deleteById(Long id);
}
