<template>
  <view class="page">
    <!-- 返回按钮 -->
    <view class="back-btn" @tap="goBack">
      <uni-icons type="back" size="22" color="#101010" />
    </view>

    <!-- 壁纸列表 -->
    <view class="wallpaper-list">
      <image
        v-for="(item, i) in wallpapers"
        :key="i"
        :src="item"
        class="wallpaper-item"
        mode="aspectFill"
        :class="{ active: selected === item }"
        :data-url="item"
        :draggable="false"
        @tap="pick(item)"
      />
    </view>

    <!-- 确认按钮 -->
    <view class="confirm-btn" @tap="confirm">确定</view>
  </view>
</template>

<!-- wallpaper.vue（片段） -->
<script>
export default {
  data() {
    return {
      wallpapers: [
        '/static/images/1.jpg',
        '/static/images/2.jpg',
        '/static/images/3.jpg',
        '/static/images/4.jpg',
        '/static/images/5.jpg',
        '/static/images/6.jpg',
      ],
      selected: '',
      ec: null
    }
  },
  onLoad() {
    try {
      this.ec = this.getOpenerEventChannel && this.getOpenerEventChannel()
      // ✅ 监听来自父页的 init 事件，做“预选中”
      if (this.ec && this.ec.on) {
        this.ec.on('init', ({ wallpaper }) => {
          if (wallpaper) this.selected = wallpaper
        })
      }
    } catch (e) {
      this.ec = null
    }
  },
  methods: {
    pick(item) {
      this.selected = item
    },
    goBack() {
      uni.navigateBack()
    },
    confirm() {
      if (!this.selected) {
        return uni.showToast({ title: '请选择一张壁纸', icon: 'none' })
      }
      // 主路：事件回传
      if (this.ec && this.ec.emit) {
        this.ec.emit('onPick', { url: this.selected })
      }
      // 兜底：无论如何都写一份本地存储，父页 onShow 会读取并清理
      try { uni.setStorageSync('pickedWallpaper', this.selected) } catch(e){}

      uni.navigateBack()
    }
  }
}
</script>


<style scoped>
.page {
  min-height: 100vh;
  background: #fff;
  position: relative;
}
.back-btn {
  position: fixed;
  left: 38px;
  top: 79px;
  width: 45px;
  height: 45px;
  border-radius: 15px;
  background: #fff;
  border: 1px solid #D9D9D9;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
  pointer-events: auto;
}
.wallpaper-list {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-around;
  margin-top: 140px;
}
.wallpaper-item {
  width: 40%;
  height: 150px;
  margin: 10px 0;
  border-radius: 15px;
  overflow: hidden;
  border: 2px solid transparent;
  /* 确保可点 */
  pointer-events: auto;
}
.wallpaper-item.active {
  border-color: #EA1763;
}
.confirm-btn {
  margin: 20px auto 40px;
  width: 80%;
  height: 45px;
  border-radius: 15px;
  background: #EA1763;
  color: #fff;
  text-align: center;
  line-height: 45px;
  font-weight: 700;
}
</style>
