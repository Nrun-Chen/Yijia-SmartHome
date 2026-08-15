<template>
  <view class="container">
    <!-- 顶部矩形图片 -->
    <view class="header-image-container">
      <image 
        class="header-image" 
        src="/static/images/1.jpg" 
        mode="widthFix" 
        alt="顶部背景图"
      ></image>
    </view>
    
    <!-- 返回按钮 -->
    <view class="back-card" @click="goMine">
      <uni-icons type="arrowleft" size="25" color="#666"></uni-icons>
    </view>
	
	<!-- 标题 -->
	<view class="title-wrapper">
	  <view class="personal-setting-text">个人信息</view>
	  <view class="title-red-line"></view>
	</view>

    <!-- 信息列表区域 -->
    <view class="info-list">
      <!-- 头像 -->
      <view class="info-card" @click="openAvatarPicker">
        <text class="info-label">头像</text>
        <view class="avatar-wrapper">
          <image :src="avatarUrl" class="avatar"></image>
          <uni-icons type="arrowright" size="20" color="#999"></uni-icons>
        </view>
      </view>

      <!-- 昵称 -->
      <view class="info-card" @click="handleNickname">
        <text class="info-label">昵称</text>
        <view class="nickname-wrapper">
          <text class="nickname-text">{{ nickname }}</text>
          <uni-icons type="arrowright" size="20" color="#999"></uni-icons>
        </view>
      </view>

      <!-- ID -->
      <view class="info-card">
        <text class="info-label">ID</text>
        <text class="id-text">{{ userId }}</text>
      </view>

      <!-- 性别 -->
      <view class="info-card">
        <text class="info-label">性别</text>
        <picker class="gender-picker" :range="genderRange" @change="handleGenderChange" :value="genderIndex">
          <text class="gender-text">{{ genderRange[genderIndex] }}</text>
        </picker>
      </view>

      <!-- 退出登录：点击打开弹窗（不再直接用showModal） -->
      <view class="logout-card" @click="openLogoutPopup">
        <text class="logout-text">退出登录</text>
      </view>
    </view>

    <!-- 头像选择弹窗 -->
    <uni-popup ref="avatarPicker" type="center">
      <view class="popup-avatar-wrapper">
        <view class="popup-close-icon" @click="closeAvatarPicker">
          <uni-icons type="close" size="28rpx" color="#999"></uni-icons>
        </view>
        <view 
          v-for="(imageUrl, index) in avatarImageList" 
          :key="index" 
          class="popup-avatar-item" 
          @click="selectAvatar(imageUrl)"
        >
          <image :src="imageUrl" mode="aspectFill" class="grid-avatar"></image>
        </view>
      </view>
    </uni-popup>
	
	<!-- 修改昵称弹窗 -->
	<uni-popup ref="nicknamePopup" type="center">
	  <view class="popup-wrapper">
	    <view class="popup-title">修改昵称</view>
	    <view class="popup-input-group">
	      <view class="popup-input-item">
	        <input class="popup-input" placeholder="请输入新昵称" v-model="editNickname" maxlength="10"/>
	      </view>
	    </view>
	    <view class="popup-btn-group">
	      <button class="popup-cancel-btn" @click="$refs.nicknamePopup.close()">取消</button>
		  <button class="popup-confirm-btn" @click="confirmNickname">确定</button>
	    </view>
	  </view>
	</uni-popup>

    <!-- 新增：退出登录弹窗（与修改昵称弹窗样式一致） -->
    <uni-popup ref="logoutPopup" type="center">
      <view class="popup-wrapper">
        <!-- 弹窗标题（对应“修改昵称”的标题） -->
        <view class="popup-title">退出登录</view>
        <!-- 弹窗内容（替换输入框，改为提示文本） -->
        <view class="popup-content">
          <text class="content-text">确定要退出登录吗？</text>
        </view>
        <!-- 按钮组（与修改昵称弹窗的按钮样式一致） -->
        <view class="popup-btn-group">
          <button class="popup-cancel-btn" @click="$refs.logoutPopup.close()">取消</button>
          <button class="popup-confirm-btn" @click="confirmLogout">确定</button>
        </view>
      </view>
    </uni-popup>

  </view>
</template>

<script>
const { updateUsername } = require('../../libs/api/auth');
export default {
  data() {
    const userInfo = uni.getStorageSync('userInfo') || {}
    console.log(userInfo.username)
    return {
      avatarUrl: uni.getStorageSync('userAvatar') || "/static/choose/4.jpg",
      nickname: userInfo.username || "Yolo",
      editNickname: "",
      userId: userInfo.userId || "",
      genderRange: ["男", "女", "外星人"],
      genderIndex: uni.getStorageSync('userGenderIndex') || 0,
      avatarImageList: [
        "/static/choose/1.jpg", "/static/choose/2.jpg", 
        "/static/choose/3.jpg", "/static/choose/4.jpg",
        "/static/choose/5.jpg", "/static/choose/6.jpg",
        "/static/choose/7.jpg", "/static/choose/8.jpg"
      ]
    };
  },
  onShow() {
    // 每次页面显示时更新用户信息
    const userInfo = uni.getStorageSync('userInfo') || {}
    this.nickname = userInfo.username || "Yolo"
    this.userId = userInfo.userId || ""
  },
  methods: {
    goMine() {
      uni.navigateBack();
    },
    openAvatarPicker() {
      this.$refs.avatarPicker.open();
    },
    selectAvatar(imageUrl) {
      this.avatarUrl = imageUrl;
      uni.setStorageSync('userAvatar', imageUrl); 
      this.$refs.avatarPicker.close();
      uni.showToast({ title: "头像更新成功", icon: "success", duration: 1500 });
    },
    closeAvatarPicker() {
      this.$refs.avatarPicker.close();
    },
    handleGenderChange(e) {
      this.genderIndex = e.detail.value;
      uni.setStorageSync('userGenderIndex', this.genderIndex);
    },

    // 打开昵称弹窗
    handleNickname() {
      this.editNickname = this.nickname;
      this.$refs.nicknamePopup.open();
    },
    // 确认修改昵称
    confirmNickname() {
      if (!this.editNickname.trim()) {
        uni.showToast({ title: "昵称不能为空", icon: "none" });
        return;
      }
      if (this.editNickname.trim().length > 10) {
        uni.showToast({ title: "昵称不能超过10个字", icon: "none" });
        return;
      }

      updateUsername(this.editNickname.trim())
        .then(() => {
          this.nickname = this.editNickname.trim();
          this.$refs.nicknamePopup.close();
          uni.showToast({ title: "昵称修改成功", icon: "success" });
        })
        .catch(error => {
          console.error('修改昵称失败:', error);
          uni.showToast({ title: error.message || "修改昵称失败", icon: "none" });
        });
    },

    // 新增：打开退出登录弹窗（对应打开昵称弹窗的逻辑）
    openLogoutPopup() {
      this.$refs.logoutPopup.open();
    },
    // 新增：确认退出登录（核心逻辑）
    confirmLogout() {
      try {
        // 1. 清除用户相关数据
        uni.removeStorageSync('userAvatar');
        uni.removeStorageSync('userName');
        uni.removeStorageSync('userGenderIndex');
        uni.removeStorageSync('token');
        uni.removeStorageSync('userInfo');
        uni.removeStorageSync('userEmail');
        uni.removeStorageSync('userNickname');
        
        // 2. 清除家庭相关数据
        const activeHomeId = uni.getStorageSync('activeHomeId');
        if (activeHomeId) {
          // 清除家庭成员、访客和管理员数据
          uni.removeStorageSync(`members_${activeHomeId}`);
          uni.removeStorageSync(`guests_${activeHomeId}`);
          uni.removeStorageSync(`admins_${activeHomeId}`);
          // 清除活跃家庭ID
          uni.removeStorageSync('activeHomeId');
          uni.removeStorageSync('homes');
        }
        
        // 3. 跳转到登录页面
        uni.redirectTo({
          url: '/pages/logon/logon',
          fail: (err) => {
            console.log('跳转登录页失败：', err);
            uni.showToast({ title: '跳转失败，请检查页面路径', icon: 'none' });
          }
        });

        // 4. 关闭弹窗 + 提示成功
        this.$refs.logoutPopup.close();
        uni.showToast({ title: "退出登录成功", icon: "success" });
      } catch (error) {
        console.error('清除本地数据失败:', error);
        uni.showToast({ title: "清除数据失败", icon: "none" });
      }
    }
  }
};
</script>

<style>
@font-face {
  font-family: 'MyFont';
  src: url('/static/font/taibei1.ttf') format('truetype');
  font-weight: normal;
  font-style: normal;
}

.container {
  width: 402px;   
  height: 874px;  
  background-color: #F0F0F0;
  position: relative;
  margin: 0 auto; 
  font-family: 'MyFont'; 
  overflow: hidden; 
}

/* 顶部矩形图片容器 */
.header-image-container {
  width: 100%;
  height: 494rpx;
  overflow: hidden;
}

.header-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 返回按钮 */
.back-card {
  position: absolute;
  top: 98rpx;
  left: 38rpx;
  width: 120rpx;
  height: 120rpx;
  background-color: #ffffff;
  border: 2rpx solid #d9d9d9;
  border-radius: 45rpx;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 10;
}

/* 标题区域 */
.title-wrapper {
  position: absolute;
  left: 0;
  right: 0;
  top: 510rpx;
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
}

.personal-setting-text {
  font-size: 56rpx;
  font-weight: bold;
  color: #101010;
  font-family: 'MyFont'; 
}

.title-red-line {
  width: 250rpx;
  height: 6rpx;
  background-color: #ea1763;
}

/* 信息列表容器 */
.info-list {
  position: absolute;
  top: 680rpx;
  left: 0;
  right: 0;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
  align-items: center; 
  padding: 0 20rpx;
}

/* 信息卡片 */
.info-card {
  max-width: 650rpx; 
  width: 100%;
  height: 160rpx;
  background-color: #ffffff;
  border-radius: 30rpx;
  border: 2rpx solid #f0f0f0;
  padding: 0 40rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-sizing: border-box;
}

/* 标签样式 */
.info-label {
  font-size: 34rpx;
  color: #333333;
  font-family: 'MyFont'; 
}

/* 头像容器 */
.avatar-wrapper {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

/* 头像样式 */
.avatar {
  width: 90rpx;
  height: 90rpx;
  border-radius: 50%;
  border: 1rpx solid #eee;
}

/* 昵称/性别文本容器 */
.nickname-wrapper, .gender-picker {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

/* 文本样式 */
.nickname-text, .id-text, .gender-text {
  font-size: 34rpx;
  color: #333333;
  font-family: 'MyFont'; 
}

/* 退出登录按钮 */
.logout-card {
  max-width: 650rpx;
  width: 100%;
  height: 160rpx;
  background-color: #000000;
  border-radius: 30rpx;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 10rpx;
}

.logout-text {
  font-size: 36rpx;
  color: #ffffff; 
  font-weight: 500;
  font-family: 'MyFont'; 
}

/* 弹窗通用样式（复用修改昵称弹窗的容器样式） */
.popup-wrapper {
  width: 600rpx;
  padding: 40rpx;
  background-color: #ffffff;
  border-radius: 40rpx;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  gap: 40rpx; /* 标题、内容、按钮组之间的间距 */
}

/* 弹窗标题（与修改昵称弹窗一致） */
.popup-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  text-align: center;
  font-family: 'MyFont'; 
}

/* 新增：退出登录弹窗的内容样式（替换输入框） */
.popup-content {
  display: flex;
  justify-content: center;
  padding: 10rpx 0;
}
.content-text {
  font-size: 28rpx;
  color: #666;
  font-family: 'MyFont'; 
}

/* 弹窗输入组（仅昵称弹窗使用，保留） */
.popup-input-group {
  display: flex;
  flex-direction: column;
  gap: 25rpx;
  margin-bottom: 10rpx;
}
.popup-input-item {
  width: 100%;
}
.popup-input {
  width: 100%;
  height: 80rpx;
  padding: 0 20rpx;
  border: 2rpx solid #eee;
  border-radius: 40rpx;
  font-size: 28rpx;
  box-sizing: border-box;
  font-family: 'MyFont'; 
}
.popup-input::placeholder {
  color: #999;
  font-size: 26rpx;
  font-family: 'MyFont'; 
}

/* 弹窗按钮组（复用，昵称/退出弹窗通用） */
.popup-btn-group {
  display: flex;
  gap: 20rpx;
}
.popup-confirm-btn, .popup-cancel-btn {
  flex: 1;
  height: 90rpx;
  line-height: 90rpx;
  font-size: 32rpx;
  border-radius: 40rpx;
  border: none;
  padding: 0;
  font-family: 'MyFont'; 
}
.popup-confirm-btn {
  background-color: #101010;
  color: #ffffff;
}
.popup-cancel-btn {
  background-color: #f5f5f5;
  color: #333;
}
.popup-confirm-btn::after, .popup-cancel-btn::after {
  border: none;
}

/* 头像选择弹窗样式（保留） */
.popup-avatar-wrapper {
  width: 600rpx;
  min-height: 400rpx; 
  background: #fff;
  border-radius: 20rpx;
  padding: 50rpx 30rpx 30rpx; 
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  position: relative; 
  box-sizing: border-box; 
}
.popup-close-icon {
  position: absolute;
  top: 20rpx; 
  right: 20rpx; 
  width: 60rpx;
  height: 60rpx;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 10;
}
.popup-avatar-item {
  width: 22%; 
  height: 140rpx;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 20rpx; 
}
.grid-avatar {
  width: 120rpx;
  height: 120rpx; 
  border-radius: 50%; 
  border: 2rpx solid #f0f0f0;
  object-fit: cover; 
  overflow: hidden;
  opacity: 1; 
}
</style>
