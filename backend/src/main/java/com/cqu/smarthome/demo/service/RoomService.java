package com.cqu.smarthome.demo.service;

import com.cqu.smarthome.demo.dao.RoomDao;
import com.cqu.smarthome.demo.pojo.Room;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class RoomService implements IRoomService {
    
    @Autowired
    private RoomDao roomDao;
    
    @Autowired
    private IHomeService homeService;

    
    // 检查用户是否有房间操作权限
    private boolean checkUserPermission(Long userId, Long homeId) {
        return homeService.checkUserIsHost(userId, homeId) || homeService.checkUserIsMember(userId, homeId);
    }
    
    @Override
    @Transactional
    public Room createRoom(Room room, Long userId) {
        // 检查用户是否有权限创建房间
        if (!checkUserPermission(userId, room.getHomeId())) {
            return null;
        }
        
        // 检查房间名称是否已存在
        if (roomDao.existsByHomeIdAndNameAndIsDeletedFalse(room.getHomeId(), room.getName())) {
            throw new RuntimeException("该家庭中已存在同名房间");
        }
        
        room.setIsDeleted(false);
        room.setCreateTime(LocalDateTime.now());
        room.setUpdateTime(LocalDateTime.now());
        return roomDao.save(room);
    }
    
    @Override
    @Transactional
    public boolean deleteRoom(Long roomId, Long userId) {
        // 检查房间是否存在
        if (!roomDao.existsByIdAndIsDeletedFalse(roomId)) {
            return false;
        }
        
        Room room = roomDao.findByIdAndIsDeletedFalse(roomId).orElse(null);
        if (room == null) {
            return false;
        }
        
        // 检查用户是否有权限删除房间
        if (!checkUserPermission(userId, room.getHomeId())) {
            return false;
        }

        // 逻辑删除房间
        room.setIsDeleted(true);
        room.setUpdateTime(LocalDateTime.now());
        roomDao.save(room);
        return true;
    }
    
    @Override
    @Transactional
    public Room updateRoom(Room room, Long userId) {
        // 检查房间是否存在
        if (!roomDao.existsByIdAndIsDeletedFalse(room.getId())) {
            return null;
        }
        
        // 获取现有房间信息
        Room existingRoom = roomDao.findByIdAndIsDeletedFalse(room.getId()).orElse(null);
        if (existingRoom == null) {
            return null;
        }
        
        // 检查用户是否有权限更新房间
        if (!checkUserPermission(userId, room.getHomeId())) {
            return null;
        }
        
        // 如果修改了房间名称，检查新名称是否已存在
        if (!existingRoom.getName().equals(room.getName()) && 
            roomDao.existsByHomeIdAndNameAndIsDeletedFalse(room.getHomeId(), room.getName())) {
            throw new RuntimeException("该家庭中已存在同名房间");
        }
        
        // 保留创建时间，只更新其他字段
        room.setCreateTime(existingRoom.getCreateTime());
        room.setUpdateTime(LocalDateTime.now());
        return roomDao.save(room);
    }
    
    @Override
    public List<Room> getRoomsByHomeId(Long homeId) {
        return roomDao.findByHomeIdAndIsDeletedFalse(homeId);
    }
    
    @Override
    public Room getRoomById(Long roomId) {
        return roomDao.findByIdAndIsDeletedFalse(roomId).orElse(null);
    }
}