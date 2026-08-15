<template>
  <view class="container">
    <!-- 最高层透明热区：点哪都能触发 -->
    <view class="hitbox" @tap="goToLogon"></view>

    <!-- 图片：上滑入 -->
    <image class="logo-image" src="/static/images/logo.png" mode="widthFix" />

    <!-- 标题：右滑入 -->
    <text class="title-text">逸家</text>

    <!-- slogan：左滑入 -->
    <text class="slogan">More wisdom, more comfort</text>

    <view class="decor">
      <image class="lamp"     :src="icons.lamp"      mode="widthFix" />
      <image class="ac"       :src="icons.ac"        mode="widthFix" />
      <image class="tv"       :src="icons.tv"        mode="widthFix" />
      <image class="socket"   :src="icons.socket"    mode="widthFix" />
      <image class="sofa"     :src="icons.sofa"      mode="widthFix" />
      <image class="dryer"    :src="icons.hairdryer" mode="widthFix" />
      <image class="headset"  :src="icons.headset"   mode="widthFix" />
      <image class="house"    :src="icons.house"     mode="widthFix" />
      <image class="cupboard" :src="icons.cupboard"  mode="widthFix" />
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      jumping: false, // 防止连点
      icons: {
        lamp: '/static/icons/Lamp-2.svg',
        ac: '/static/icons/ac.svg',
        tv: '/static/icons/tv.svg',
        socket: '/static/icons/socket.svg',
        sofa: '/static/icons/sofa.svg',
        hairdryer: '/static/icons/hairdryer.svg',
        headset: '/static/icons/Headset-2.svg',
        house: '/static/icons/house.svg',
        cupboard: '/static/icons/cupboard.svg'
      }
    }
  },
  methods: {
    goToLogon() {
      if (this.jumping) return;
      this.jumping = true;

      // 如果不想让用户返回启动页，可改为：uni.redirectTo 或 uni.reLaunch
      uni.reLaunch({
        url: '/pages/logon/logon', // 按你的实际路径修改
        complete: () => { this.jumping = false; }
      });
    }
  }
}
</script>

<style scoped>
.container {
  position: relative;
  height: 100vh;
  background: #fff;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 顶层透明热区，覆盖整页并接管点击 */
.hitbox{
  position: absolute;
  inset: 0;
  z-index: 9999;
  /* 不要 pointer-events:none，这个元素需要吃点击 */
}

/* 这些只是展示，不吃点击，确保不会挡住热区 */
.logo-image,.title-text,.slogan,.decor{
  pointer-events: none;
}

/* 动画：图片上滑入 */
@keyframes slideUp {
  from {
    transform: translateY(100px);
    opacity: 0;
    visibility: hidden;
  }
  to {
    transform: translateY(0);
    opacity: 1;
    visibility: visible;
  }
}

/* 动画：标题右滑入 */
@keyframes slideRight {
  from { transform: translateX(100px); opacity: 0; }
  to   { transform: translateX(0);     opacity: 1; }
}

/* 动画：slogan 左滑入 */
@keyframes slideLeft {
  from { transform: translateX(-100px); opacity: 0; }
  to   { transform: translateX(0);      opacity: 1; }
}

/* 图片动画 */
.logo-image {
  position: absolute;
  left: 164px;
  top: 251px;
  width: 75px;
  height: 75px;
  animation: slideUp 1s ease-out forwards;
}

/* 标题动画 */
.title-text {
  position: absolute;
  left: 173px;
  top: 344px;
  font-family: 'ZCOOL Xiaowei', sans-serif;
  font-size: 28px;
  font-weight: 400;
  color: #101010;
  animation: slideRight 1s ease-out forwards;
}

/* slogan动画 */
.slogan {
  position: absolute;
  left: 94px;
  top: 405px;
  font-size: 16px;
  font-family: 'Inter', sans-serif;
  color: #101010;
  opacity: 0.9;
  animation: slideLeft 1s ease-out forwards;
}

/* 装饰层（不挡点击） */
.decor {
  position: absolute;
  inset: 0;
  pointer-events: none;  /* 保证内部装饰不拦截点击 */
  filter: grayscale(100%);
  opacity: .55;
}

/* 图标定位 */
.lamp { left: 45px; top: 520px; width: 80px; height: 80px; position:absolute; }
.ac { left: 142px; top: 600px; width: 56px; height: 56px; position:absolute; }
.tv { left: 205px; top: 560px; width: 32px; height: 32px; position:absolute; }
.socket { left: 279px; top: 520px; width: 69px; height: 69px; position:absolute; }
.sofa { left: 67px; top: 656px; width: 75px; height: 75px; position:absolute; }
.dryer { left: 253px; top: 609px; width: 34px; height: 34px; position:absolute; }
.headset { left: 221px; top: 661px; width: 32px; height: 32px; position:absolute; }
.house { left: 299px; top: 637px; width: 188px; height: 188px; position:absolute; }
.cupboard { left: 158px; top: 712px; width: 47px; height: 47px; position:absolute; }
</style>
