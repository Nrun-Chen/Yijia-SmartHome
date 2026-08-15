package com.cqu.smarthome.demo.service;

import com.cqu.smarthome.demo.dao.EnterRequestRecordDao;
import com.cqu.smarthome.demo.dao.GuestRecordDao;
import com.cqu.smarthome.demo.dao.HomeDao;
import com.cqu.smarthome.demo.dao.UserHomeDao;
import com.cqu.smarthome.demo.pojo.EnterRequestRecord;
import com.cqu.smarthome.demo.pojo.GuestRecord;
import com.cqu.smarthome.demo.pojo.Home;
import com.cqu.smarthome.demo.pojo.UserHome;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EnterHomeRequestService implements IEnterHomeRequestService {
    
    @Autowired
    private EnterRequestRecordDao enterRequestRecordDao;
    
    @Autowired
    private GuestRecordDao guestRecordDao;
    
    @Autowired
    private HomeDao homeDao;
    
    @Autowired
    private UserHomeDao userHomeDao;
    
    @Override
    @Transactional
    public EnterRequestRecord submitEnterRequest(Long homeId, Long userId, String reason) {
        // 验证家庭是否存在
        Optional<Home> homeOptional = homeDao.findByIdAndIsDeletedFalse(homeId);
        if (!homeOptional.isPresent()) {
            throw new RuntimeException("家庭不存在");
        }
        
        // 检查用户是否已经是家庭成员
        Optional<UserHome> userHomeOptional = userHomeDao.findByUserIdAndHomeIdAndIsDeletedFalse(userId, homeId);
        if (userHomeOptional.isPresent()) {
            throw new RuntimeException("用户已经是该家庭成员");
        }
        
        // 检查是否有未处理的请求
        List<EnterRequestRecord> allRequests = enterRequestRecordDao.findByHomeIdAndIsDeletedFalse(homeId);
        boolean hasPendingRequest = false;
        for (EnterRequestRecord req : allRequests) {
            if (req.getUserId().equals(userId) && req.getStatus() == 1) {
                hasPendingRequest = true;
                break;
            }
        }
        
        if (hasPendingRequest) {
            throw new RuntimeException("已有未处理的进入请求");
        }
        
        // 创建进入请求记录
        EnterRequestRecord requestRecord = new EnterRequestRecord();
        requestRecord.setHomeId(homeId);
        requestRecord.setUserId(userId);
        requestRecord.setStatus(1); // 1：待审批
        requestRecord.setRecordTime(LocalDateTime.now());
        requestRecord.setIsDeleted(false);
        
        EnterRequestRecord savedRecord = enterRequestRecordDao.save(requestRecord);
        
        // 发送通知给家庭管理员
        System.out.println("发送进入请求通知到家庭：" + homeId);
        
        return savedRecord;
    }
    
    @Override
    @Transactional
    public EnterRequestRecord approveEnterRequest(Long requestId, Long approverId, Integer status, String remark) {
        // 验证请求是否存在
        Optional<EnterRequestRecord> requestOptional = ((Optional<EnterRequestRecord>) enterRequestRecordDao.findById(requestId));
        if (!requestOptional.isPresent() || requestOptional.get().getIsDeleted()) {
            throw new RuntimeException("请求记录不存在");
        }
        
        EnterRequestRecord requestRecord = requestOptional.get();
        
        // 验证请求是否已处理
        if (requestRecord.getStatus() != 1) {
            throw new RuntimeException("请求已处理");
        }
        
        // 验证审批人是否有权限
        Long homeId = requestRecord.getHomeId();
        if (!hasPermission(approverId, homeId)) {
            throw new RuntimeException("审批人没有权限");
        }
        
        // 更新请求状态
        requestRecord.setStatus(status);
        // 注意：EnterRequestRecord实体类中没有approvalTime字段，所以移除这行
        
        EnterRequestRecord updatedRecord = enterRequestRecordDao.save(requestRecord);
        
        // 如果同意进入，可以在这里添加用户到家庭成员
        if (status == 2) { // 2：已同意
            // 创建访客记录
            createGuestRecord(homeId, requestRecord.getUserId(), 0, "通过进入请求");
            
            // 发送通知给申请人
            System.out.println("发送进入请求同意通知给用户：" + requestRecord.getUserId());
        } else if (status == 3) { // 3：已拒绝
            // 发送通知给申请人
            System.out.println("发送进入请求拒绝通知给用户：" + requestRecord.getUserId());
        }
        
        return updatedRecord;
    }

    @Override
    public List<EnterRequestRecord> getHomeEnterRequests(Long homeId, Integer status) {
        List<EnterRequestRecord> allRequests = enterRequestRecordDao.findByHomeIdAndIsDeletedFalse(homeId);
        if (status != null) {
            // 使用collect(Collectors.toList())替代toList()
            return allRequests.stream()
                    .filter(req -> req.getStatus().equals(status))
                    .collect(Collectors.toList());
        }
        return allRequests;
    }

    @Override
    public List<EnterRequestRecord> getUserEnterRequests(Long userId) {
        return enterRequestRecordDao.findByUserIdAndIsDeletedFalse(userId);
    }
    
    @Override
    @Transactional
    public GuestRecord createGuestRecord(Long homeId, Long userId, Integer recordType, String remark) {
        // 验证家庭是否存在
        Optional<Home> homeOptional = homeDao.findByIdAndIsDeletedFalse(homeId);
        if (!homeOptional.isPresent()) {
            throw new RuntimeException("家庭不存在");
        }
        
        // 创建访客记录
        GuestRecord guestRecord = new GuestRecord();
        guestRecord.setHomeId(homeId);
        guestRecord.setUserId(userId);
        guestRecord.setRecordType(recordType);
        guestRecord.setRecordTime(LocalDateTime.now());
        guestRecord.setIsDeleted(false);
        
        return guestRecordDao.save(guestRecord);
    }
    
    /**
     * 检查用户是否有权限审批家庭请求
     */
    private boolean hasPermission(Long userId, Long homeId) {
        // 使用isPresent()检查Optional对象
        return userHomeDao.findByUserIdAndHomeIdAndIsDeletedFalse(userId, homeId).isPresent();
    }
}