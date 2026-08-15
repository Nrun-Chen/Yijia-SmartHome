package com.cqu.smarthome.demo.controller;

import com.cqu.smarthome.demo.pojo.Home;
import com.cqu.smarthome.demo.pojo.ResponseMessage;
import com.cqu.smarthome.demo.service.HomeMemberService;
import com.cqu.smarthome.demo.service.IHomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/home")
public class HomeController {

    @Autowired
    private IHomeService homeService;
    
    // 通过Spring依赖注入HomeMemberService
    @Autowired
    private HomeMemberService homeMemberService;

    // POST - 创建家庭
    @PostMapping
    public ResponseMessage<Home> createHome(@RequestBody Home home) {
        try {
            // 从SecurityContext获取当前认证的用户ID
            String userIdStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Long userId = Long.parseLong(userIdStr);
            
            // 创建家庭
            Home createdHome = homeService.createHome(home);
            
            // 创建成功后，建立用户与家庭的关系
            if (createdHome != null) {
                // 使用注入的homeMemberService添加用户到家庭中，并设置为房主角色(0)
                homeMemberService.addHomeMember(createdHome.getId(), userId, 0);
                
                return ResponseMessage.success(createdHome);
            } else {
                return new ResponseMessage<>(HttpStatus.BAD_REQUEST.value(), "创建家庭失败", null);
            }
        } catch (RuntimeException e) {
            // 处理业务异常
            return new ResponseMessage<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null);
        } catch (Exception e) {
            // 处理其他异常
            return new ResponseMessage<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "创建家庭时发生错误: " + e.getMessage(), null);
        }
    }

    // DELETE - 删除家庭
    @DeleteMapping("/{homeId}")
    public ResponseMessage<String> deleteHome(@PathVariable Long homeId, @RequestParam Long userId) {
        boolean deleted = homeService.deleteHome(homeId, userId);
        if (deleted) {
            return ResponseMessage.success("家庭删除成功");
        } else {
            return new ResponseMessage<>(HttpStatus.FORBIDDEN.value(), "无权限删除该家庭", null);
        }
    }

    // PUT - 更新家庭
    @PutMapping
    public ResponseMessage<Home> updateHome(@RequestBody Home home, @RequestParam Long userId) {
        if (home.getId() == null) {
            return new ResponseMessage<>(HttpStatus.BAD_REQUEST.value(), "家庭ID不能为空", null);
        }

        Home updatedHome = homeService.updateHome(home, userId);
        if (updatedHome != null) {
            return ResponseMessage.success(updatedHome);
        } else {
            return new ResponseMessage<>(HttpStatus.FORBIDDEN.value(), "无权限更新该家庭", null);
        }
    }

    // GET - 获取用户的所有家庭
    @GetMapping("/user/{userId}")
    public ResponseMessage<List<Home>> getHomesByUserId(@PathVariable Long userId) {
        List<Home> homes = homeService.getHomesByUserId(userId);
        return ResponseMessage.success(homes);
    }

    // GET - 根据ID获取家庭
    @GetMapping("/{homeId}")
    public ResponseMessage<Home> getHomeById(@PathVariable Long homeId) {
        Home home = homeService.getHomeById(homeId);
        if (home != null) {
            return ResponseMessage.success(home);
        } else {
            return new ResponseMessage<>(HttpStatus.NOT_FOUND.value(), "家庭不存在", null);
        }
    }

    // GET - 检查用户是否是房主
    @GetMapping("/check/host")
    public ResponseMessage<Boolean> checkUserIsHost(@RequestParam Long userId, @RequestParam Long homeId) {
        boolean isHost = homeService.checkUserIsHost(userId, homeId);
        return ResponseMessage.success(isHost);
    }

    // GET - 检查用户是否是家庭成员
    @GetMapping("/check/member")
    public ResponseMessage<Boolean> checkUserIsMember(@RequestParam Long userId, @RequestParam Long homeId) {
        boolean isMember = homeService.checkUserIsMember(userId, homeId);
        return ResponseMessage.success(isMember);
    }
}