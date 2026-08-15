<template>
  <view class="tabbar" :style="{ ...safeStyle, '--icon-size': iconSize + 'px' }">
    <view
      v-for="(item,i) in tabs"
      :key="i"
      class="tab-item"
      :class="{ active: activeIndex===i }"
      @click="go(item,i)"
    >
      <image class="icon" :src="activeIndex===i ? item.active : item.icon" mode="widthFix" />
    </view>
  </view>
</template>

<script>
export default {
  name: 'CustomTabBar',
  props: {
    active: { type: Number, default: 0 },
    iconSize: { type: Number, default: 24 }
  },
  data(){
    return {
      activeIndex: this.active,
      tabs: [
        { path: '/pages/zhuye/zhuye', icon: '/static/tab/Home.png', active: '/static/tab/Home1.png' },
        { path: '/pages/index/index', icon: '/static/tab/wang.png', active: '/static/tab/wang1.png' },
        { path: '/pages/ai/ai',       icon: '/static/tab/Ai.png',   active: '/static/tab/Ai1.png' },
        { path: '/pages/mine/mine',   icon: '/static/tab/mine.png', active: '/static/tab/mine1.png' },
      ],
      safeStyle: {}
    }
  },
  methods:{
    go(item, i){
      this.activeIndex = i
      const url = item.path
      const pages = getCurrentPages()
      const cur = '/' + pages[pages.length - 1].route
      if (cur === url) return  // 已在当前页就不跳
  
      // 用 reLaunch 模拟 tab 切换：清栈+打开目标页，避免堆栈越来越深
      uni.reLaunch({ url })
    }
  },
  watch:{ active(v){ this.activeIndex = v } },
  methods:{
    go(item, i){
      this.activeIndex = i
      uni.switchTab({ url: item.path }).catch(()=>{
        uni.navigateTo({ url: item.path })
      })
    }
  }
}
</script>

<style scoped>
.tabbar{
  position: fixed; left:0; right:0; bottom:0;
  height: 60px;
  background: #FFFFFF;
  display: flex; justify-content: space-around; align-items: center;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.06);
  z-index: 1000;
}

.tab-item{
  width: 64rpx; height: 64rpx;
  display:flex; justify-content:center; align-items:center;
}

.icon{
  width: var(--icon-size, 20px);
  height: var(--icon-size, 20px);
}

.active .icon{ transform: scale(1.05); }
</style>
