<template>
  <view class="container">
    <!-- 返回按钮：固定定位 -->
    <view class="back-card" @click="goMine">
      <uni-icons type="arrowleft" size="25" color="#666"></uni-icons>
    </view>

    <!-- 长图区域 -->
    <view class="long-img-container">
      <image 
        class="long-img"
        :src="longImgUrl"
        mode="widthFix"
        @load="handleImgLoad"
      ></image>
    </view>
  </view>
</template>

<script>
export default {
  name: "family",
  data() {
    return {
      longImgUrl: "/static/images/用户手册.png"
    };
  },
  methods: {
    goMine() {
      uni.navigateBack({
        delta: 1,
        fail: () => {
          uni.redirectTo({ url: "/pages/mine/mine" });
        }
      });
    },
    handleImgLoad() {
      console.log("长图加载完成");
    }
  }
};
</script>

<style scoped>
@font-face {
  font-family: 'MyFont';
  src: url('/static/font/taibei1.ttf') format('truetype');
  font-weight: normal;
  font-style: normal;
}

.container {
  width: 100%;
  min-height: 100vh;
  background-color: #ffffff;
  padding: 30rpx 0;
  box-sizing: border-box;
  overflow-x: hidden;
}

.back-card {
  position: fixed;
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
  z-index: 99;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.long-img-container {
  width: 100%;
  padding: 0 20rpx;
  box-sizing: border-box;
  margin-top: 180rpx;
}

.long-img {
  width: 100%;
  border-radius: 30rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
  background-color: #f5f5f5;
}
</style>
