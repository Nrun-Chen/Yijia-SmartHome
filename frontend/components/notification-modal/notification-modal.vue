<template>
  <view v-if="show" class="notification-overlay" @tap.stop>
    <view class="notification-modal" @tap.stop>
      <view class="notification-content">
        <!-- 通知类型图标 -->
        <view class="notification-icon-container">
          <image 
            class="notification-icon" 
            :src="getNotificationIcon(notifyType)" 
            mode="widthFix" 
          />
        </view>
        
        <!-- 场景名称 -->
        <text class="notification-title">{{ sceneName }}</text>
        
        <!-- 关闭按钮 -->
        <view class="notification-close" @tap="closeNotification">
          <text class="close-text">确定</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'NotificationModal',
  
  props: {
    show: {
      type: Boolean,
      default: false
    },
    sceneName: {
      type: String,
      default: ''
    },
    notifyType: {
      type: String,
      default: 'info'
    }
  },
  
  methods: {
    closeNotification() {
      this.$emit('close');
    },
    
    getNotificationIcon(type) {
      const iconMap = {
        'vibrate': '/static/icons/shake.svg',
        'ring': '/static/icons/ling.svg',
        'silent': '/static/icons/jingyin.svg',
        'success': '/static/icons/queding.png',
        'error': '/static/icons/warning.svg',
        'info': '/static/icons/gantan.svg'
      };
      
      return iconMap[type] || iconMap['info'];
    }
  }
}
</script>

<style scoped>
.notification-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
  animation: fadeIn 0.3s ease-out;
}

.notification-modal {
  width: 280px;
  background: #ffffff;  /* 改為白色背景 */
  border: 2px solid #e0e0e0;  /* 添加灰色描邊 */
  border-radius: 40px;
  padding: 30px 25px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.4);
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  animation: slideIn 0.4s ease-out;
  overflow: hidden;
}

.notification-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
}

.notification-icon-container {
  width: 60px;
  height: 60px;
  margin-bottom: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f5f5f5;  /* 改為淺灰色背景 */
  border-radius: 50%;
  border: 1px solid #e0e0e0;  /* 添加灰色邊框 */
}

.notification-icon {
  width: 35px;
  height: 35px;
  filter: brightness(0);  /* 改為黑色圖標 */
}

.notification-title {
  font-family: 'alibaba2', sans-serif;
  font-size: 20px;
  font-weight: bold;
  color: #333333;  /* 改為黑色文字 */
  margin-bottom: 25px;
  text-align: center;
  text-shadow: none;  /* 移除文字陰影 */
}

.notification-close {
  width: 100%;
  text-align: center;
  padding: 14px 20px;
  background: linear-gradient(45deg, #EA1763, #d4145a);
  border-radius: 40px;
  color: #fff;
  font-family: 'alibaba2', sans-serif;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  box-shadow: 0 6px 20px rgba(234, 23, 99, 0.5);
  transition: all 0.3s ease;
  border: none;
  outline: none;
}

.notification-close:active {
  transform: scale(0.95);
  box-shadow: 0 2px 8px rgba(234, 23, 99, 0.6);
}

/* 动画效果 */
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideIn {
  from { 
    opacity: 0;
    transform: translateY(-50px) scale(0.9);
  }
  to { 
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}
</style>
