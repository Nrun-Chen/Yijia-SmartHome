package com.cqu.smarthome.demo.service.impl;

import com.cqu.smarthome.demo.dao.MqttdataDao;
import com.cqu.smarthome.demo.pojo.Mqttdata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MqttDataServiceImpl {
    
    @Autowired
    private MqttdataDao mqttdataDao;
    
    public List<Mqttdata> getLatestMqttData(int limit) {
        List<Mqttdata> allData = mqttdataDao.findAllByOrderByDataTimeDesc();
        // 修复Java 8兼容问题，替换toList()方法
        if (allData.size() <= limit) {
            return new ArrayList<>(allData);
        }
        return new ArrayList<>(allData.subList(0, limit));
    }
    
    public Mqttdata getLatestMqttDataByDeviceId(Long deviceId) {
        return mqttdataDao.findTopByDeviceIdOrderByDataTimeDesc(deviceId);
    }
    
    public boolean hasLatestHumanDetection() {
        List<Mqttdata> latestData = getLatestMqttData(1);
        if (latestData.isEmpty()) {
            return false;
        }
        Mqttdata data = latestData.get(0);
        return "1".equals(data.getIsHuman());
    }
}