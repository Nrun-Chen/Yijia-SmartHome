package com.cqu.smarthome.demo.controller;

import com.cqu.smarthome.demo.pojo.ResponseMessage;
import com.cqu.smarthome.demo.pojo.Room;
import com.cqu.smarthome.demo.service.IRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/room")
public class RoomController {

    @Autowired
    private IRoomService roomService;

    // POST - 创建房间
    @PostMapping
    public ResponseMessage<Room> createRoom(@RequestBody Room room, @RequestParam Long userId) {
        if (room.getHomeId() == null) {
            return new ResponseMessage<>(HttpStatus.BAD_REQUEST.value(), "家庭ID不能为空", null);
        }

        Room createdRoom = roomService.createRoom(room, userId);
        if (createdRoom != null) {
            return ResponseMessage.success(createdRoom);
        } else {
            return new ResponseMessage<>(HttpStatus.FORBIDDEN.value(), "无权限创建房间", null);
        }
    }

    // DELETE - 删除房间
    @DeleteMapping("/{roomId}")
    public ResponseMessage<String> deleteRoom(@PathVariable Long roomId, @RequestParam Long userId) {
        boolean deleted = roomService.deleteRoom(roomId, userId);
        if (deleted) {
            return ResponseMessage.success("房间删除成功");
        } else {
            return new ResponseMessage<>(HttpStatus.FORBIDDEN.value(), "无权限删除房间", null);
        }
    }

    // PUT - 更新房间
    @PutMapping
    public ResponseMessage<Room> updateRoom(@RequestBody Room room, @RequestParam Long userId) {
        if (room.getId() == null || room.getHomeId() == null) {
            return new ResponseMessage<>(HttpStatus.BAD_REQUEST.value(), "房间ID和家庭ID不能为空", null);
        }

        Room updatedRoom = roomService.updateRoom(room, userId);
        if (updatedRoom != null) {
            return ResponseMessage.success(updatedRoom);
        } else {
            return new ResponseMessage<>(HttpStatus.FORBIDDEN.value(), "无权限更新房间或房间不存在", null);
        }
    }

    // GET - 获取家庭的所有房间
    @GetMapping("/home/{homeId}")
    public ResponseMessage<List<Room>> getRoomsByHomeId(@PathVariable Long homeId) {
        List<Room> rooms = roomService.getRoomsByHomeId(homeId);
        return ResponseMessage.success(rooms);
    }

    // GET - 根据ID获取房间
    @GetMapping("/{roomId}")
    public ResponseMessage<Room> getRoomById(@PathVariable Long roomId) {
        Room room = roomService.getRoomById(roomId);
        if (room != null) {
            return ResponseMessage.success(room);
        } else {
            return new ResponseMessage<>(HttpStatus.NOT_FOUND.value(), "房间不存在", null);
        }
    }
}