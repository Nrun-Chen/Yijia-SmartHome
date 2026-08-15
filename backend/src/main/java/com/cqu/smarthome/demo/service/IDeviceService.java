package com.cqu.smarthome.demo.service;

import com.cqu.smarthome.demo.pojo.Device;
import java.util.List;

public interface IDeviceService {
    // 创建设备
    Device createDevice(Device device, Long userId);
    
    // 删除设备
    boolean deleteDevice(Long deviceId, Long userId);
    
    // 更新设备
    Device updateDevice(Device device, Long userId);
    
    // 根据设备ID获取设备
    Device getDeviceById(Long deviceId, Long userId);
    
    // 根据家庭ID获取设备列表
    List<Device> getDevicesByHomeId(Long homeId, Long userId);
    
    // 根据房间ID获取设备列表
    List<Device> getDevicesByRoomId(Long roomId, Long userId);
    
    // 检查设备名称是否重复
    boolean checkDeviceNameExists(Long homeId, Long roomId, String deviceName, Long excludeDeviceId);
    
    // 检查设备MAC地址是否重复
    boolean checkDeviceMacExists(String deviceMac, Long excludeDeviceId);
}