<template>
<view class="page-container">
  <view class="container">
    <!-- 返回按钮 -->
    <view class="back-card" @click="goMine">
      <uni-icons type="arrowleft" size="25" color="#666"></uni-icons>
    </view>
    <text class="top-text">问题反馈</text>
    <!-- 图片 -->
    <view class="image-container">
      <image 
        class="header-image" 
        src="/static/images/logo.png" 
        mode="widthFix"
      ></image>
    </view>
    <text class="bottom-text">扫码进群反馈问题，前端、后端、硬件各种专员24小时在线为您排忧解惑！</text>
  </view>
  </view>
</template>

<script>
export default {
  name: "family",
  methods: {
    goMine() {
      uni.navigateBack({
        url: '/pages/mine/mine' 
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
  background-color: #fff;
  position: relative;
  overflow: hidden; 
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

/* 上方文字 */
.top-text {
  position: absolute;
  top: 15%; 
  left: 50%;
  transform: translateX(-50%); 
  font-size: 56rpx;
  font-weight: bold;
  color: #101010;
}

/* 图片容器 */
.image-container {
  position: absolute;
  top: 50%; /* 垂直居中基准点 */
  left: 50%;
  transform: translate(-50%, -50%); /* 整体居中 */
  width: 500rpx; /* 图片宽度 */
  height: auto;
}

/* 图片样式 */
.header-image {
  width: 100%;
  height: auto;
  object-fit: contain;
  border-radius: 20rpx;
}

/* 下方文字 */
.bottom-text {
  position: absolute;
  top: 90%; 
  left: 50%;
  transform: translateX(-50%); 
  font-size: 28rpx;
  color: #666666;
  text-align: center;
  width: 80%; 
}
</style>
