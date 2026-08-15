<template>
  <view class="page">
    <!-- 顶部栏 -->
    <view class="topbar">
      <view class="back-btn" @click="goBack">
        <uni-icons type="back" size="22" color="#101010" />
      </view>
      <text class="title">扫描二维码</text>
      <view class="right-space" />
    </view>

    <!-- 相机预览（App、H5可用；需要相机权限） -->
    <camera class="camera" mode="scanCode" device-position="back" flash="off"></camera>

    <!-- 提示文案（半透明圆角胶囊） -->
    <view class="hint">
      你可以使用扫描二维码来接受家庭邀请并添加智能设备
    </view>

    <!-- 扫描区域（圆角矩形 + 半透明遮罩 + 边框 + 动画扫描线） -->
    <view class="scan-wrap">
      <!-- 外层用于定位 -->
      <view class="scan-box">
        <!-- 扫描线 -->
        <view class="scan-line" />
      </view>

      <!-- 四周遮罩（挖空中间） -->
      <view class="mask">
        <view class="hole" />
      </view>
    </view>

    <!-- 底部白色安全区（可选，用于和系统手势区域区分） -->
    <view class="safe-bottom" />
  </view>
</template>

<script>
export default {
  methods: {
    goBack() {
      uni.navigateBack({ delta: 1 })
    },
    // 如果你不想用 <camera mode="scanCode"> 自带的能力，也可改为按钮触发：
    // startScan() { uni.scanCode({ onlyFromCamera: true, success: res => { console.log(res) } }) }
  }
}
</script>

<style scoped>
/* 以 750 设计稿为例，rpx 自适配各种机型 */
.page {
  width: 100vw;
  height: 100vh;
  background: #000; /* 背景先用黑色衬托相机画面 */
  position: relative;
  overflow: hidden;
}

/* 顶部栏 */
.topbar {
  position: absolute;
  top: env(safe-area-inset-top);
  left: 0;
  right: 0;
  height: 96rpx;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  z-index: 5;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.05);
}
.back-btn, .right-space {
  width: 96rpx;
  height: 96rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
.title {
  font-family: "Alibaba", "PingFang SC", Arial, sans-serif;
  font-size: 34rpx;
  font-weight: 700;
  color: #000;
}

/* 相机预览充满全屏（顶栏之下） */
.camera {
  position: absolute;
  top: calc(env(safe-area-inset-top) + 96rpx);
  left: 0;
  right: 0;
  bottom: 0;
  width: 100vw;
  height: calc(100vh - env(safe-area-inset-top) - 96rpx);
  z-index: 1;
  background: #000;
}

/* 半透明提示胶囊：放在扫描框上方 */
.hint {
  position: absolute;
  z-index: 6;
  left: 60rpx;
  right: 60rpx;
  top: calc(env(safe-area-inset-top) + 160rpx);
  padding: 18rpx 24rpx;
  border-radius: 24rpx;
  color: #fff;
  font-size: 24rpx;
  line-height: 1.5;
  background: rgba(0,0,0,0.35);
  text-align: left;
}

/* 扫描区域布局 */
.scan-wrap {
  position: absolute;
  z-index: 5;
  /* 你截图中扫描框大致在页面中偏上，给个居中偏上的位置 */
  top: calc(env(safe-area-inset-top) + 240rpx);
  left: 0;
  right: 0;
  bottom: 0;
}

/* 扫描框本体：圆角矩形 + 边框（淡淡的） */
.scan-box {
  width: 560rpx;          /* 扫描框宽 */
  height: 420rpx;         /* 扫描框高（不是正方形，贴合你截图） */
  margin: 0 auto;
  border-radius: 32rpx;
  border: 2rpx solid rgba(255,255,255,0.35);
  position: relative;
  overflow: hidden;        /* 让扫描线在内部裁切 */
  background: rgba(255,255,255,0.08); /* 轻微的白雾感，模拟你的效果 */
  backdrop-filter: blur(2px);
}

/* 动画扫描线：粉色细线，左右留白，往下循环 */
.scan-line {
  position: absolute;
  left: 24rpx;
  right: 24rpx;
  height: 6rpx;
  background: #ff4b72; /* 你截图接近粉红色，可按需替换 */
  border-radius: 3rpx;
  animation: scanMove 1.8s linear infinite;
  /* 发光一点点 */
  box-shadow: 0 0 12rpx rgba(255,75,114,0.8);
}
@keyframes scanMove {
  0%   { top: 60rpx; opacity: 0 }
  10%  { opacity: 1 }
  90%  { opacity: 1 }
  100% { top: 360rpx; opacity: 0 }
}

/* 遮罩（挖孔）：灰黑半透明 + 中间留一个圆角矩形孔位 */
.mask {
  position: fixed;
  inset: 0;              /* top/left/right/bottom = 0 */
  z-index: 4;
  pointer-events: none;  /* 不挡触摸 */
}
/* 利用多层渐变+mask/clip-path 兼容性不一，这里用四块遮罩围住 */
.mask::before,
.mask::after {
  content: "";
  position: absolute;
  left: 0; right: 0;
  height: 9999rpx; /* 足够大 */
  background: rgba(0,0,0,0.45);
}
.mask::before { top: 0; }
/* 下方遮罩靠近底部白色安全区，仍旧覆盖相机 */
.mask::after  { bottom: 0; }

.hole {
  position: absolute;
  width: 560rpx;
  height: 420rpx;
  left: 50%;
  transform: translateX(-50%);
  top: calc(env(safe-area-inset-top) + 240rpx);
  border-radius: 32rpx;
  /* 用投影把四周“压暗”，看起来像周围都有遮罩 */
  box-shadow: 0 0 0 9999rpx rgba(0,0,0,0.45);
}

/* 底部安全区 */
.safe-bottom {
  position: absolute;
  left: 0; right: 0; bottom: 0;
  height: calc( env(safe-area-inset-bottom) + 24rpx );
  background: #fff; /* 可换成透明 */
  z-index: 3;
}
</style>
