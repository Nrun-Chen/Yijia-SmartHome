package com.cqu.smarthome.demo.service;

import com.cqu.smarthome.demo.pojo.Home;
import java.util.List;

public interface IHomeService {
    Home createHome(Home home);
    boolean deleteHome(Long homeId, Long userId);
    Home updateHome(Home home, Long userId);
    List<Home> getHomesByUserId(Long userId);
    Home getHomeById(Long homeId);
    boolean checkUserIsHost(Long userId, Long homeId);
    boolean checkUserIsMember(Long userId, Long homeId);
}