<template>
  <view class="page-container">
  <view class="container">
    <!-- 顶部按钮 -->
    <view class="top-btns">     
      <view class="btn" @click="goToSetting">
        <uni-icons type="gear-filled" size="30" color="#ffffff"></uni-icons> 
      </view>
    </view>

    <!-- 头像与信息 -->
    <view class="profile">
      <image 
          class="avatar" 
          :src="avatarUrl" 
          mode="aspectFill"
        ></image>
      <view class="info">
        <text class="name">{{ nickname }}</text>
        <!-- ★ 改为动态邮箱显示 -->
        <text class="email">{{ email || '暂无邮箱' }}</text>
      </view>
    </view>
	
	<!-- 快捷键部分 -->
    <view class="shortcut">
      <!-- 标题 -->
      <view class="shortcut-title">
        <text class="shortcut-text">快捷键</text>
      </view>
      <!-- 按钮组 -->
      <view class="shortcut-row">
        <view class="shortcut-card" @click="toggleDoorBell">
          <uni-icons type="locked-filled" size="25" :color="bellStatus ? '#EA1763' : '#5B17EA'"></uni-icons>
          <text class="shortcut-label">{{ bellStatus ? '关门' : '开门' }}</text>
        </view>
        <view class="shortcut-card" @click="disableAll">
          <uni-icons type="minus-filled" size="25" color="#EA8917"></uni-icons>
          <text class="shortcut-label">全部禁用</text>
        </view>
        <view class="shortcut-card" @click="turnOnAllLights">
          <image
            src="/static/light.png" 
            mode="widthFix" 
            style="width: 28px; height: 28px;tint-color: #00C5C5;" ></image>
          <text class="shortcut-label">一键开灯</text>
        </view>
      </view>
    </view>
		
    <!-- 通用部分 -->
    <view class="common">
      <!-- 上方黑色圆 + 文字 -->
      <view class="common-title">
        <view class="circle">
          <text class="circle-text">3</text>
        </view>
        <text class="common-text">通用</text>
      </view>

      <!-- 三个功能卡片 -->
	<view class="card" @click="goToFamily">
	  <uni-icons type="person-filled" size="28" color="#EA1763"></uni-icons>
	  <text class="card-text">家庭管理</text>
	  <view class="card-right">
		<uni-icons type="arrowright" size="25" color="#666"></uni-icons>
	  </view>
	</view>

	<view class="card" @click="goToNotification">
	  <image 
		src="/static/使用手册.png" 
		mode="widthFix" 
		style="width: 28px; height: 28px;" ></image>
	  <text class="card-text">使用手册</text>
	  <view class="card-right">
		<uni-icons type="arrowright" size="25" color="#666"></uni-icons>
	  </view>
	</view>

	<view class="card" @click="goToFAQ">
	  <uni-icons type="help-filled" size="32" color="#00C5C5"></uni-icons>
	  <text class="card-text">问题反馈</text>
	  <view class="card-right">
		<uni-icons type="arrowright" size="25" color="#666"></uni-icons>
	  </view>
	</view>
    </view>
	</view>
  </view>
</template>

<script>
import CustomTabBar from '@/components/CustomTabBar.vue'
import { controlDoorBell, controlLivingRoomLight, controlKitchenLight, controlBedroomLight, sendBackendPayload } from '@/libs/api/device'
export default {
  components: { CustomTabBar },
  name: "MinePage",
  data() {
      const cachedInfo = uni.getStorageSync('userInfo') || {}
      return {
        avatarUrl: uni.getStorageSync('userAvatar') || "/static/choose/4.jpg",
        nickname: uni.getStorageSync('userNickname') || "Yolo",
        email: uni.getStorageSync('userEmail') || cachedInfo.email || "",
        // 门铃状态，默认为0（未响铃）
        bellStatus: 0,
		userRole: null
      };
    },
  onShow() {
	    const savedAvatar = uni.getStorageSync("userAvatar");
		this.nickname = uni.getStorageSync('userNickname') || "Yolo";
	    if (savedAvatar) {
	      this.avatarUrl = savedAvatar;
	    }
        this.email =
          uni.getStorageSync('userEmail') ||
          (uni.getStorageSync('userInfo') || {}).email ||
          "";
		const userInfo = uni.getStorageSync('userInfo') || {}
		  const userId = userInfo.userId
		  const activeHomeId = uni.getStorageSync('activeHomeId')
		  const userHomes = uni.getStorageSync('user_home') || []
		
		  const record = userHomes.find(
		    r => String(r.userId) === String(userId) && String(r.homeId) === String(activeHomeId)
		  )
		  this.userRole = record ? Number(record.role) : null
	  },
  methods: {
	hasShortcutPermission() {
	    const role = Number(this.userRole)
	    return role === 0 || role === 1
	  },
	hasFamilyManagePermission() {
	    return Number(this.userRole) === 0
	  },
	
	toggleDoorBell() {
	    if (!this.hasShortcutPermission()) {
	      return uni.showToast({ title: '您没有该权限', icon: 'none' })
	    }
	    const newStatus = this.bellStatus ? 0 : 1;
	    controlDoorBell(newStatus)
	      .then(() => {
	        this.bellStatus = newStatus;
	        uni.showToast({
	          title: newStatus ? '已开门' : '已关门',
	          icon: 'success'
	        });
	      })
	      .catch(err => {
	        console.error('控制失败:', err);
	      });
	  },
	 // 全部禁用
  disableAll() {
    if (!this.hasShortcutPermission()) {
      return uni.showToast({ title: '您没有该权限', icon: 'none' })
    }

    // 禁用所有灯
    Promise.all([
      controlLivingRoomLight(0),
      controlKitchenLight(0),
      controlBedroomLight(0),
      // 禁用风扇
      sendBackendPayload({ code: 'fan_level', value: 0 }),
      // 禁用门铃
      controlDoorBell(0)
    ]).then(() => {
      uni.showToast({ title: '已全部禁用', icon: 'success' })
    }).catch(err => {
      console.error('禁用全部设备失败:', err);
      uni.showToast({ title: '禁用失败', icon: 'none' })
    })
  },
	
	  // 一键开灯
  turnOnAllLights() {
    if (!this.hasShortcutPermission()) {
      return uni.showToast({ title: '您没有该权限', icon: 'none' })
    }

    // 打开所有灯
    Promise.all([
      controlLivingRoomLight(1),
      controlKitchenLight(1),
      controlBedroomLight(1)
    ]).then(() => {
      uni.showToast({ title: '已全部开灯', icon: 'success' })
    }).catch(err => {
      console.error('打开所有灯失败:', err);
      uni.showToast({ title: '开灯失败', icon: 'none' })
    })
  },
    goToFamily() {
      uni.navigateTo({
        url: '/pages/family/family'
      })
    },
    goToNotification() {
          uni.navigateTo({
            url: '/pages/notification/notification'
          })
    },
    goToFAQ() {
      uni.navigateTo({
        url: '/pages/faq/faq'
      })
    },
	goToSetting() {
      uni.navigateTo({
        url: '/pages/setting/setting'
      })
    }
  }
}
</script>

<style>
@font-face {
  font-family: 'MyFont';
  src: url('/static/font/taibei1.ttf') format('truetype');
  font-weight: normal;
  font-style: normal;
}
.page-container {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
  background-color: #fff;
}


.container {
  width: 402px;
  height: 100%;
  background-color: #ffffff;
  position: relative;
}

/* 顶部按钮 */
.top-btns {
  position: absolute;
  top: 60rpx;
  right: 40rpx;
}

.btn {
  width: 120rpx;
  height: 120rpx;
  background-color: #101010;
  border-radius: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 头像与信息 */
.profile {
  position: absolute;
  top: 240rpx;
  left: 40rpx;
  display: flex;
  flex-direction: row;
  align-items: center;
}

.avatar {
  width: 200rpx;
  height: 200rpx;
  border-radius: 50%;
  background-color: #cccccc;
}

.info {
  margin-left: 40rpx;
  display: flex;
  flex-direction: column;
}

.name {
  font-family: 'MyFont';
  font-size: 60rpx;
  font-weight: bold;
  color: #000;
}

.email {
  font-size: 28rpx;
  color: #666;
  font-family: 'MyFont';
  margin-top: 10rpx;
}

/* 快捷键部分 */
.shortcut {
  position: absolute;
  top: 500rpx;
  left: 40rpx;
  right: 40rpx;
}

.shortcut-title {
  display: flex;
  align-items: center;
  margin-bottom: 30rpx;
  justify-content: flex-start;
  font-family: 'MyFont';
}

.shortcut-text {
  font-size: 40rpx;
  font-weight: bold;
  margin-left: 0;
  font-family: 'MyFont';
}

/* 快捷键行 */
.shortcut-row {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
}

.shortcut-card {
  width: 216rpx;
  height: 228rpx;
  border-radius: 25rpx;
  background-color: #f2f2f2;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.shortcut-label {
  margin-top: 20rpx;
  color: #101010;
  font-size: 28rpx;
  font-weight: bold;
  font-family: 'MyFont'; 
}

/* 通用部分 */
.common {
  position: absolute;
  top: 864rpx;
  left: 40rpx;
  right: 40rpx;
}

.common-title {
  display: flex;
  align-items: center;
  margin-bottom: 30rpx;
  font-family: 'MyFont';
}

.circle {
  width: 70rpx;
  height: 70rpx;
  border-radius: 50%;
  background-color: #101010;
  display: flex;
  align-items: center;
  justify-content: center;
}

.circle-text {
  color: #fff;
  font-size: 32rpx;
  font-weight: bold;
  font-family: 'MyFont'; 
}

.common-text {
  font-size: 40rpx;
  font-weight: bold;
  margin-left: 20rpx;
  font-family: 'MyFont';
}

/* 卡片样式 */
.card {
  width: 620rpx;
  height: 160rpx;
  background-color: #f2f2f2;
  border-radius: 40rpx;
  margin: 20rpx auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30rpx;
}

.card-text {
  flex: 1;
  font-size: 28rpx;
  font-weight: bold;
  margin-left: 20rpx;
  font-family: 'MyFont';
}
/* 让页面底部给 tabbar 腾出空间，避免被遮住 */
.container{
  padding-bottom: calc(env(safe-area-inset-bottom) + 140rpx);
}

/* 把tabbar 固定到底部，并确保在最上层 */
.tabbar-fixed{
  position: fixed;
  left: 0; right: 0; bottom: 0;
  z-index: 9999;
  background: transparent;
}
</style>