package com.cqu.smarthome.demo.service;

import com.cqu.smarthome.demo.pojo.Room;
import java.util.List;

public interface IRoomService {
    Room createRoom(Room room, Long userId);
    boolean deleteRoom(Long roomId, Long userId);
    Room updateRoom(Room room, Long userId);
    List<Room> getRoomsByHomeId(Long homeId);
    Room getRoomById(Long roomId);
}