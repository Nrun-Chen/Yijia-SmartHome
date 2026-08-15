package com.cqu.smarthome.demo.controller;

import com.cqu.smarthome.demo.pojo.ResponseMessage;
import com.cqu.smarthome.demo.pojo.User;
import com.cqu.smarthome.demo.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController  // 接口方式返回对象，转换成json
@RequestMapping("/user")  // :8088/user/...
public class UserController {
    // Rest风格

    @Autowired
    IUserService userService;
    
    // POST - 创建用户
    @PostMapping
    public ResponseMessage<User> add(@RequestBody User user) {
        User userNew = userService.add(user);
        return ResponseMessage.success(userNew);
    }

    // GET - 根据ID查询用户
    @GetMapping("/{id}")
    public ResponseMessage<User> getById(@PathVariable Long id) {
        return userService.findById(id)
                .map(user -> ResponseMessage.success(user))
                .orElse(new ResponseMessage<>(HttpStatus.NOT_FOUND.value(), "用户不存在", null));
    }
    
    // GET - 查询所有用户
    @GetMapping
    public ResponseMessage<Iterable<User>> getAll() {
        Iterable<User> users = userService.findAll();
        return ResponseMessage.success(users);
    }

    // PUT - 更新用户
    @PutMapping
    public ResponseMessage<User> update(@RequestBody User user) {
        if (user.getUserId() == null) {
            return new ResponseMessage<>(HttpStatus.BAD_REQUEST.value(), "用户ID不能为空", null);
        }
        
        User updatedUser = userService.update(user);
        if (updatedUser != null) {
            return ResponseMessage.success(updatedUser);
        } else {
            return new ResponseMessage<>(HttpStatus.NOT_FOUND.value(), "用户不存在", null);
        }
    }

    // DELETE - 根据ID删除用户
    @DeleteMapping("/{id}")
    public ResponseMessage<String> delete(@PathVariable Long id) {
        userService.deleteById(id);
        return ResponseMessage.success("用户删除成功");
    }
}
