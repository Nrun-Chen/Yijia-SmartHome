<template>
  <view class="page" @click="goHome">
    <!-- LOGO -->
    <image class="logo" src="/static/images/logo.png" mode="aspectFit" />

    <!-- 主标题文本（ZCOOL Xiaowei） -->
    <text class="title">欢迎使用逸家app</text>

    <!-- 提示文本 -->
    <text class="hint">点击空白部分</text>
  </view>
</template>

<script>
export default {
  methods: {
    goHome() {
      // 跳到 tabBar 的“主页”
      uni.switchTab({
        url: '/pages/zhuye/zhuye'
      });
    }
  }
}
</script>

<style>
/* 自定义字体：ZCOOL Xiaowei */
@font-face {
  font-family: 'ZCOOL Xiaowei';
  src: url('/static/fonts/ZhanKuXiaoLOGOTi-2.ttf') format('truetype');
  font-weight: normal;
  font-style: normal;
}

.page {
  position: relative;
  width: 100%;
  height: 100vh;
  background: #fff;
}

/* LOGO：居中、y=553、90×90、上滑入 */
.logo {
  position: absolute;
  top: 553px;
  left: 50%;
  width: 90px;
  height: 90px;
  transform: translate(-50%, 20px); /* 初始下移 20px，为上滑入做准备 */
  opacity: 0;
  animation: slideUpIn 600ms ease-out 100ms forwards;
}

/* 主文本：居中、y=668、#333333、23号、ZCOOL Xiaowei、下滑入 */
.title {
  position: absolute;
  top: 668px;
  left: 50%;
  transform: translate(-50%, -20px); /* 初始上移 20px，为下滑入做准备 */
  opacity: 0;
  color: #333333;
  font-size: 23px;
  font-family: 'ZCOOL Xiaowei', sans-serif;
  animation: slideDownIn 600ms ease-out 200ms forwards;
}

/* 提示文本：居中、y=754、默认字体、#333333、12号 */
.hint {
  position: absolute;
  top: 754px;
  left: 50%;
  transform: translateX(-50%);
  color: #333333;
  font-size: 12px;
}

/* 动效：上滑入 */
@keyframes slideUpIn {
  from {
    transform: translate(-50%, 20px);
    opacity: 0;
  }
  to {
    transform: translate(-50%, 0);
    opacity: 1;
  }
}

/* 动效：下滑入 */
@keyframes slideDownIn {
  from {
    transform: translate(-50%, -20px);
    opacity: 0;
  }
  to {
    transform: translate(-50%, 0);
    opacity: 1;
  }
}
</style>
