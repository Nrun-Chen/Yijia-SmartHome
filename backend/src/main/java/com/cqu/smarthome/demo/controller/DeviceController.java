package com.cqu.smarthome.demo.controller;

import com.cqu.smarthome.demo.pojo.Device;
import com.cqu.smarthome.demo.pojo.ResponseMessage;
import com.cqu.smarthome.demo.service.IDeviceService;
import com.cqu.smarthome.demo.dao.DeviceDao;
import com.cqu.smarthome.demo.service.IHomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/device")
public class DeviceController {

    @Autowired
    private IDeviceService deviceService;

    @Autowired
    private DeviceDao deviceDao;

    @Autowired
    private IHomeService homeService;

    // POST - 创建设备
    @PostMapping
    public ResponseMessage<Device> createDevice(@RequestBody Device device, @RequestParam Long userId) {
        if (device.getHomeId() == null || device.getRoomId() == null) {
            return new ResponseMessage<>(HttpStatus.BAD_REQUEST.value(), "家庭ID和房间ID不能为空", null);
        }
        
        if (device.getDeviceName() == null || device.getDeviceName().trim().isEmpty()) {
            return new ResponseMessage<>(HttpStatus.BAD_REQUEST.value(), "设备名称不能为空", null);
        }
        
        if (device.getDeviceMac() == null || device.getDeviceMac().trim().isEmpty()) {
            return new ResponseMessage<>(HttpStatus.BAD_REQUEST.value(), "设备MAC地址不能为空", null);
        }

        try {
            Device createdDevice = deviceService.createDevice(device, userId);
            return ResponseMessage.success(createdDevice);
        } catch (RuntimeException e) {
            return new ResponseMessage<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null);
        } catch (Exception e) {
            return new ResponseMessage<>(HttpStatus.FORBIDDEN.value(), "无权限创建设备", null);
        }
    }

    // PUT - 更新设备
    @PutMapping("/{deviceId}")
    public ResponseMessage<Device> updateDevice(
            @PathVariable Long deviceId, 
            @RequestBody Device device, 
            @RequestParam Long userId) {
        device.setId(deviceId);
        
        try {
            Device updatedDevice = deviceService.updateDevice(device, userId);
            if (updatedDevice != null) {
                return ResponseMessage.success(updatedDevice);
            } else {
                return new ResponseMessage<>(HttpStatus.NOT_FOUND.value(), "设备不存在", null);
            }
        } catch (RuntimeException e) {
            return new ResponseMessage<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null);
        } catch (Exception e) {
            return new ResponseMessage<>(HttpStatus.FORBIDDEN.value(), "无权限更新设备", null);
        }
    }

    // DELETE - 删除设备
    @DeleteMapping("/{deviceId}")
    public ResponseMessage<Boolean> deleteDevice(@PathVariable Long deviceId, @RequestParam Long userId) {
        try {
            boolean deleted = deviceService.deleteDevice(deviceId, userId);
            if (deleted) {
                return ResponseMessage.success(true);
            } else {
                return new ResponseMessage<>(HttpStatus.NOT_FOUND.value(), "设备不存在", false);
            }
        } catch (Exception e) {
            return new ResponseMessage<>(HttpStatus.FORBIDDEN.value(), "无权限删除设备", false);
        }
    }

    // GET - 根据ID获取设备
    @GetMapping("/{deviceId}")
    public ResponseMessage<Device> getDeviceById(@PathVariable Long deviceId, @RequestParam Long userId) {
        Device device = deviceService.getDeviceById(deviceId, userId);
        if (device != null) {
            return ResponseMessage.success(device);
        } else {
            return new ResponseMessage<>(HttpStatus.NOT_FOUND.value(), "设备不存在", null);
        }
    }

    // GET - 获取家庭的所有设备
    @GetMapping("/home/{homeId}")
    public ResponseMessage<List<Device>> getDevicesByHomeId(@PathVariable Long homeId, @RequestParam Long userId) {
        List<Device> devices = deviceService.getDevicesByHomeId(homeId, userId);
        return ResponseMessage.success(devices);
    }

    // GET - 获取房间的所有设备
    @GetMapping("/room/{roomId}")
    public ResponseMessage<List<Device>> getDevicesByRoomId(@PathVariable Long roomId, @RequestParam Long userId) {
        List<Device> devices = deviceService.getDevicesByRoomId(roomId, userId);
        return ResponseMessage.success(devices);
    }

    // GET - 根据MAC地址获取设备
    @GetMapping("/mac/{deviceMac}")
    public ResponseMessage<Device> getDeviceByMac(@PathVariable String deviceMac, @RequestParam Long userId) {
        Device device = deviceDao.findByDeviceMacAndIsDeletedFalse(deviceMac);
        if (device != null) {
            // 检查用户权限
            if (homeService.checkUserIsHost(userId, device.getHomeId()) || homeService.checkUserIsMember(userId, device.getHomeId())) {
                return ResponseMessage.success(device);
            } else {
                return new ResponseMessage<>(HttpStatus.FORBIDDEN.value(), "无权限查看该设备", null);
            }
        } else {
            return new ResponseMessage<>(HttpStatus.NOT_FOUND.value(), "未找到该MAC地址的设备", null);
        }
    }
}