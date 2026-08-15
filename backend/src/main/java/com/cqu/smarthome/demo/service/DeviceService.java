package com.cqu.smarthome.demo.service;

import com.cqu.smarthome.demo.dao.DeviceDao;
import com.cqu.smarthome.demo.pojo.Device;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DeviceService implements IDeviceService {
    
    @Autowired
    private DeviceDao deviceDao;
    
    @Autowired
    private IHomeService homeService;
    
    // 检查用户是否有设备操作权限（房主或家庭成员可以操作，访客不行）
    private boolean checkUserPermission(Long userId, Long homeId) {
        if (userId == null || homeId == null) {
            return false;
        }
        return homeService.checkUserIsHost(userId, homeId) || homeService.checkUserIsMember(userId, homeId);
    }
    
    @Override
    @Transactional
    public Device createDevice(Device device, Long userId) {
        // 参数验证
        if (device == null) {
            throw new RuntimeException("设备信息不能为空");
        }
        
        if (userId == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        
        if (device.getDeviceName() == null || device.getDeviceName().trim().isEmpty()) {
            throw new RuntimeException("设备名称不能为空");
        }
        
        if (device.getHomeId() == null) {
            throw new RuntimeException("设备必须归属家庭");
        }
        
        if (device.getRoomId() == null) {
            throw new RuntimeException("设备必须归属房间");
        }
        
        if (device.getDeviceMac() == null || device.getDeviceMac().trim().isEmpty()) {
            throw new RuntimeException("设备MAC地址不能为空");
        }
        
        // 检查用户权限
        if (!checkUserPermission(userId, device.getHomeId())) {
            throw new RuntimeException("您没有权限创建设备");
        }
        
        // 检查设备名称是否重复
        if (checkDeviceNameExists(device.getHomeId(), device.getRoomId(), device.getDeviceName(), null)) {
            throw new RuntimeException("该房间中已存在同名设备");
        }
        
        // 检查设备MAC地址是否重复
        if (checkDeviceMacExists(device.getDeviceMac(), null)) {
            throw new RuntimeException("该MAC地址的设备已存在");
        }
        
        // 设置设备状态
        device.setDeleted(false);
        device.setCreateTime(LocalDateTime.now());
        device.setUpdateTime(LocalDateTime.now());
        
        return deviceDao.save(device);
    }
    
    @Override
    @Transactional
    public boolean deleteDevice(Long deviceId, Long userId) {
        // 参数验证
        if (deviceId == null || userId == null) {
            return false;
        }
        
        // 获取设备信息
        Optional<Device> deviceOptional = deviceDao.findById(deviceId);
        if (!deviceOptional.isPresent() || deviceOptional.get().getDeleted()) {
            return false;
        }
        
        Device device = deviceOptional.get();
        
        // 检查用户权限
        if (!checkUserPermission(userId, device.getHomeId())) {
            return false;
        }
        
        // 逻辑删除设备
        device.setDeleted(true);
        device.setUpdateTime(LocalDateTime.now());
        deviceDao.save(device);
        return true;
    }
    
    @Override
    @Transactional
    public Device updateDevice(Device device, Long userId) {
        // 参数验证
        if (device == null || device.getId() == null || userId == null) {
            throw new RuntimeException("设备信息、设备ID和用户ID不能为空");
        }
        
        if (device.getDeviceName() == null || device.getDeviceName().trim().isEmpty()) {
            throw new RuntimeException("设备名称不能为空");
        }
        
        if (device.getHomeId() == null) {
            throw new RuntimeException("设备必须归属家庭");
        }
        
        if (device.getRoomId() == null) {
            throw new RuntimeException("设备必须归属房间");
        }
        
        // 获取设备信息
        Optional<Device> deviceOptional = deviceDao.findById(device.getId());
        if (!deviceOptional.isPresent() || deviceOptional.get().getDeleted()) {
            return null;
        }
        
        Device existingDevice = deviceOptional.get();
        
        // 检查用户权限
        if (!checkUserPermission(userId, existingDevice.getHomeId())) {
            return null;
        }
        
        // 检查设备名称是否重复（排除当前设备）
        if (!existingDevice.getDeviceName().equals(device.getDeviceName())) {
            if (checkDeviceNameExists(device.getHomeId(), device.getRoomId(), device.getDeviceName(), device.getId())) {
                throw new RuntimeException("该房间中已存在同名设备");
            }
        }
        
        // 检查设备MAC地址是否重复（排除当前设备）
        if (device.getDeviceMac() != null && !device.getDeviceMac().trim().isEmpty()) {
            if (!device.getDeviceMac().equals(existingDevice.getDeviceMac()) && 
                checkDeviceMacExists(device.getDeviceMac(), device.getId())) {
                throw new RuntimeException("该MAC地址的设备已存在");
            }
        }
        
        // 更新设备信息
        device.setCreateTime(existingDevice.getCreateTime());
        device.setUpdateTime(LocalDateTime.now());
        device.setDeleted(existingDevice.getDeleted());

        return deviceDao.save(device);
    }
    
    @Override
    public Device getDeviceById(Long deviceId, Long userId) {
        // 参数验证
        if (deviceId == null || userId == null) {
            return null;
        }
        
        // 获取设备信息
        Optional<Device> deviceOptional = deviceDao.findById(deviceId);
        if (!deviceOptional.isPresent() || deviceOptional.get().getDeleted()) {
            return null;
        }
        
        Device device = deviceOptional.get();
        
        // 检查用户权限
        if (!checkUserPermission(userId, device.getHomeId())) {
            return null;
        }
        
        return device;
    }
    
    @Override
    public List<Device> getDevicesByHomeId(Long homeId, Long userId) {
        // 参数验证
        if (homeId == null || userId == null) {
            return null;
        }
        
        // 检查用户权限
        if (!checkUserPermission(userId, homeId)) {
            return null;
        }
        
        return deviceDao.findByHomeIdAndIsDeletedFalse(homeId);
    }
    
    @Override
    public List<Device> getDevicesByRoomId(Long roomId, Long userId) {
        // 参数验证
        if (roomId == null || userId == null) {
            return null;
        }
        
        // 获取房间中的任意设备以检查用户权限
        List<Device> devices = deviceDao.findByRoomIdAndIsDeletedFalse(roomId);
        if (devices.isEmpty()) {
            return devices;
        }
        
        // 使用第一个设备的家庭ID检查权限
        Long homeId = devices.get(0).getHomeId();
        if (!checkUserPermission(userId, homeId)) {
            return null;
        }
        
        return devices;
    }
    
    @Override
    public boolean checkDeviceNameExists(Long homeId, Long roomId, String deviceName, Long excludeDeviceId) {
        if (homeId == null || roomId == null || deviceName == null || deviceName.trim().isEmpty()) {
            return false;
        }
        
        if (excludeDeviceId == null) {
            return deviceDao.existsByHomeIdAndRoomIdAndDeviceNameAndIsDeletedFalse(homeId, roomId, deviceName);
        }

        // 查询房间中的所有设备
        List<Device> devices = deviceDao.findByRoomIdAndIsDeletedFalse(roomId);
        for (Device device : devices) {
            if (device.getHomeId().equals(homeId) && !device.getId().equals(excludeDeviceId) && device.getDeviceName().equals(deviceName)) {
                return true;
            }
        }

        return false;
    }
    
    @Override
    public boolean checkDeviceMacExists(String deviceMac, Long excludeDeviceId) {
        if (deviceMac == null || deviceMac.trim().isEmpty()) {
            return false;
        }
        
        if (excludeDeviceId == null) {
            Device device = deviceDao.findByDeviceMacAndIsDeletedFalse(deviceMac);
            return device != null;
        }
        
        return deviceDao.existsByDeviceMacAndIdNotAndIsDeletedFalse(deviceMac, excludeDeviceId);
    }
}