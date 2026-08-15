package com.cqu.smarthome.demo.dao;

import com.cqu.smarthome.demo.pojo.Device;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceDao extends CrudRepository<Device, Long> {
    // 根据家庭ID查询设备列表
    List<Device> findByHomeIdAndIsDeletedFalse(Long homeId);
    
    // 根据房间ID查询设备列表
    List<Device> findByRoomIdAndIsDeletedFalse(Long roomId);
    
    // 根据设备MAC地址查询设备
    Device findByDeviceMacAndIsDeletedFalse(String deviceMac);
    
    // 根据设备类型ID查询设备列表
    List<Device> findByDeviceTypeIdAndIsDeletedFalse(Long deviceTypeId);
    
    // 检查指定家庭和房间中是否存在指定名称的设备
    boolean existsByHomeIdAndRoomIdAndDeviceNameAndIsDeletedFalse(Long homeId, Long roomId, String deviceName);
    
    // 检查指定MAC地址的设备是否存在（除了指定ID的设备）
    boolean existsByDeviceMacAndIdNotAndIsDeletedFalse(String deviceMac, Long id);
}