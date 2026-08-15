<template>
  <view class="page">
    <!-- 左上角返回按钮 -->
    <view class="btn-back" @click="goHome">
      <uni-icons type="back" :size="25" color="#101010" />
    </view>

    <!-- 右上角加号按钮（只控制浮窗） -->
    <view class="btn-add" @click="toggleMenu">
      <uni-icons type="plusempty" :size="25" color="#FFFFFF" />
    </view>

    <!-- 顶部标题 -->
    <text class="title">{{ titleText }}</text>

    <!-- 家庭数量圆形 -->
    <view class="family-count"><text class="count-text">{{ familyCount }}</text></view>

    <!-- 家庭列表文本 -->
    <text class="family-title">家庭列表</text>

    <!-- 家庭卡片们 -->
    <view
      v-for="(home, idx) in homes"
      :key="home.id"
      class="home-card"
      :style="{ top: firstTop + idx * (cardH + gapY) + 'px', width: cardW + 'px', height: cardH + 'px' }"
    >
      <!-- 右侧操作区（静止不动，内容左滑时露出） -->
      <view class="card-actions" :style="{ width: actionsW + 'px' }">
        <view class="btn-invite" :style="{ width: actionW + 'px' }" @click.stop="invite(home)">邀请</view>
        <view class="btn-delete" :style="{ width: actionW + 'px' }" @click.stop="deleteHome(idx, home)">删除</view>
      </view>

      <!-- 可滑动内容轨道 -->
      <view
        class="card-track"
        :style="{ transform: 'translateX(' + (-home.slideX) + 'px)' }"
        @touchstart="onTouchStart(idx, $event)"
        @touchmove="onTouchMove(idx, $event)"
        @touchend="onTouchEnd(idx)"
        @touchcancel="onTouchEnd(idx)"
        @click="editHome(home)"
      >
        <!-- 上：图片块（ditu 在上） -->
        <view class="home-card-top"></view>

        <!-- 下：灰色矩形块（高 50，上圆角 0、下圆角 18），内含两行文字 -->
        <view class="home-card-footer">
          <text class="footer-title">{{ (home.name || userDisplayName) + '的家' }}</text>
          <view class="footer-meta">
            <view class="meta-group">
              <text class="meta-num">{{ getRoomCount(home) }}</text>
              <text class="meta-label">个房间</text>
            </view>
            <view class="meta-group">
              <text class="meta-num">{{ getMemberCount(home) }}</text>
              <text class="meta-label">名成员</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 占位：让页面可以滚动到最后一张卡片 -->
    <view class="extender" :style="{ height: listHeight + 'px' }"></view>

    <!-- 半透明遮罩（点击关闭） -->
    <view v-if="showMenu" class="mask" @click="showMenu = false"></view>

    <!-- 浮窗菜单 -->
    <view v-if="showMenu" class="add-menu">
      <view class="menu-item" @click="onAddHome">添加新家庭</view>
      <view class="divider"></view>
      <view class="menu-item" @click="onScanInvite">扫描邀请</view>
    </view>
  </view>
</template>

<script>
import { deleteFamily } from '@/libs/api/family';
const GLOBAL_ROOMS_PREFIX = 'rooms:' // 每个家庭的房间桶：rooms:<homeId>

export default {
  data() {
    return {
      userName: '',
      familyCount: 0,
      showMenu: false,

      // 列表与卡片参数
      homes: [],
      firstTop: 268,
      gapY: 30,
      cardW: 320,
      cardH: 169,

      // 滑动相关
      actionW: 96, // 单个操作按钮宽
      startX: 0,
      swiping: false
    }
  },
  computed: {
    titleText() {
      const u = uni.getStorageSync('userInfo') || {}
      const name = this.userName || u.nickname || u.username || uni.getStorageSync('userName') || 'Yolo'
      return `${name}的家庭`
    },
    userDisplayName() {
      const u = uni.getStorageSync('userInfo') || {}
      return this.userName || u.nickname || u.username || uni.getStorageSync('userName') || 'Yolo'
    },
    // 操作区总宽
    actionsW() { return this.actionW * 2 },
    listHeight() {
      const n = this.homes.length
      if (n === 0) return 360
      const lastBottom = this.firstTop + (n - 1) * (this.cardH + this.gapY) + this.cardH + 80
      return Math.max(lastBottom, 360)
    }
  },
  

  onShow() {
  // 1) 读取 homes
  let storedHomes = uni.getStorageSync('homes') || [];

  // 2) 容错：强制补全必要字段
  storedHomes = storedHomes.map(h => ({
    id: String(h.id || Date.now()),   // 没 id 就给个时间戳
    name: h.name || this.userDisplayName + '的家',
    address: h.address || '',
    rooms: typeof h.rooms === 'number' ? h.rooms : 0,
    roomCount: typeof h.roomCount === 'number' ? h.roomCount : 0,
    members: typeof h.members === 'number' ? h.members : 0,
    createdAt: h.createdAt || Date.now(),
    slideX: 0
  }))

  // 3) 覆盖回存储（保证数据结构统一）
  uni.setStorageSync('homes', storedHomes)

  // 4) 同步 rooms 数量
  this.syncHomeRoomsFromBuckets()

  // 5) 赋值到页面
  this.homes = storedHomes
  this.familyCount = storedHomes.length
},

  methods: {
    goHome() { uni.reLaunch({ url: '/pages/zhuye/zhuye' }) },
    toggleMenu() { this.showMenu = !this.showMenu },

    onAddHome() {
      this.showMenu = false
      uni.navigateTo({ url: '/pages/addhome/addhome' })
    },
    onScanInvite() {
      this.showMenu = false
      uni.showToast({ title: '扫描邀请开发中', icon: 'none' })
    },

    /* —— 显示数值 —— */
    getRoomCount(home) {
      if (typeof home?.rooms === 'number') return home.rooms
      if (typeof home?.roomCount === 'number') return home.roomCount
      // 兜底：直接数桶
      try {
        const list = uni.getStorageSync(GLOBAL_ROOMS_PREFIX + home.id) || []
        return Array.isArray(list) ? list.length : 0
      } catch { return 0 }
    },
    getMemberCount(home) {
      return typeof home?.members === 'number' ? home.members : (typeof home?.membersCount === 'number' ? home.membersCount : 0)
    },

    /* —— 同步 homes 的 rooms/roomCount 与桶长度 —— */
    syncHomeRoomsFromBuckets() {
      try {
        const homes = uni.getStorageSync('homes') || []
        let changed = false
        homes.forEach((h, i) => {
          const list = uni.getStorageSync(GLOBAL_ROOMS_PREFIX + h.id) || []
          const count = Array.isArray(list) ? list.length : 0
          if (h.rooms !== count || h.roomCount !== count) {
            homes[i] = {
              ...h,
              name: h.name, // 家庭名称，来自创建时用户输入
              address: h.address, // 家庭地址，来自创建时用户输入
              rooms: count, // 房间数量，同步自rooms:<homeId>本地存储桶
              roomCount: count, // 房间数量副本，与rooms保持一致
              members: typeof h.members === 'number' ? h.members : 0, // 家庭成员数量，来自服务器
              createdAt: h.createdAt, // 创建时间戳，创建家庭时生成
              slideX: h.slideX // 卡片滑动位置，UI交互临时状态，不存储到本地
            }
            changed = true
          }
        })
        if (changed) uni.setStorageSync('homes', homes)
      } catch (e) {}
    },

    /* —— 滑动手势 —— */
    onTouchStart(idx, e) {
      const t = (e.touches && e.touches[0]) || (e.changedTouches && e.changedTouches[0]) || e
      this.startX = t.clientX || t.pageX || 0
      this.swiping = true
    },
    onTouchMove(idx, e) {
      if (!this.swiping) return
      const t = (e.touches && e.touches[0]) || (e.changedTouches && e.changedTouches[0]) || e
      const x = t.clientX || t.pageX || 0
      let delta = this.startX - x // 左滑为正
      if (delta < 0) delta = 0
      if (delta > this.actionsW) delta = this.actionsW
      this.$set(this.homes[idx], 'slideX', delta)
    },
    onTouchEnd(idx) {
      this.swiping = false
      const opened = this.homes[idx].slideX > this.actionsW / 2 ? this.actionsW : 0
      this.$set(this.homes[idx], 'slideX', opened)
    },

    /* —— 编辑家庭按钮 —— */
    editHome(home) {
      // 当用户点击家庭卡片时，编辑该家庭
      if (home.slideX === 0) { // 只有当卡片未滑动时才响应点击
        uni.setStorageSync('activeHomeId', home.id)
        // 提示用户已进入编辑
        uni.showToast({ title: '编辑 ' + home.name, icon: 'none' })
        // 导航到编辑家庭页面
        uni.navigateTo({ url: '/pages/edithome/edithome?fid=' + home.id })
      }
    },
    invite(home) {
      uni.showToast({ title: `邀请：${this.userDisplayName}`, icon: 'none' })
      // 可跳转：uni.navigateTo({ url: '/pages/invite/invite?homeId=' + home.id })
    },
    deleteHome(idx, home) {
      // 获取userId
      const userInfo = uni.getStorageSync('userInfo') || {};
      const userId = userInfo.userId || '-1'; // 默认userId为3，根据需求
      console.log('home.id:', home.id);
      console.log('userId:', userId);
      if (userId === '-1') {
        uni.showToast({ title: '未查询到用户id', icon: 'none' })
        return
      }
      // 调用删除家庭API
      deleteFamily(home.id, userId)
      
        .then(() => {
          // 1) 删除本地 homes 中该项
          const next = [...this.homes]
          next.splice(idx, 1)
          this.homes = next
          this.familyCount = next.length

          // 2) 写回 homes（去掉 slideX）
          const saved = next.map(({ slideX, ...rest }) => rest)
          uni.setStorageSync('homes', saved)

          // 3) 如果删除的是活跃家庭，清除活跃家庭ID
          const activeHomeId = uni.getStorageSync('activeHomeId')
          if (activeHomeId === home.id) {
            uni.removeStorageSync('activeHomeId')
          }

          // 4) 同时删除该家庭的房间桶（保持干净）
          try { uni.removeStorageSync(GLOBAL_ROOMS_PREFIX + home.id) } catch (e) {}

          uni.showToast({ title: '删除成功', icon: 'success' })
        })
        .catch(error => {
          console.error('删除家庭失败:', error);
          uni.showToast({ title: '删除失败，请重试', icon: 'none' })
        })
    }
  }
}
</script>

<style>
.page { width: 100vw; min-height: 100vh; background-color: #ffffff; position: relative; }

/* 左上角返回矩形 */
.btn-back {
  position: fixed; left: 37px; top: 69px; width: 55px; height: 55px;
  border-radius: 15px; background: #FFFFFF; border: 1px solid #D9D9D9;
  display: flex; align-items: center; justify-content: center; z-index: 5;
}

/* 右上角加号矩形（只开菜单） */
.btn-add {
  position: fixed; left: 316px; top: 69px; width: 55px; height: 55px;
  border-radius: 20px; background: #000; display: flex; align-items: center; justify-content: center; z-index: 6;
}

/* 顶部标题 */
.title {
  position: fixed; top: 143px; left: 50%; transform: translateX(-50%);
  font-family: "Taipei Sans TC", "PingFang SC", "Segoe UI", Arial, sans-serif;
  font-size: 27px; font-weight: 600; color: #000;
}

/* 数字圆 */
.family-count {
  position: fixed; left: 49px; top: 199px; width: 40px; height: 40px;
  border-radius: 50%; background: #000; display: flex; align-items: center; justify-content: center;
}
.count-text { font-size: 16px; color: #fff; font-weight: 600; }

/* 文案：家庭列表 */
.family-title {
  position: fixed; left: 101px; top: 208px;
  font-family: "Alibaba", "PingFang SC", Arial, sans-serif; font-size: 17px; color: #000; font-weight: 600;
}

/* 家庭卡片容器 */
.home-card{
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  width: 320px;
  height: 169px;
  border-radius: 18px;
  overflow: hidden; /* 关键：隐藏溢出，露出右侧操作 */
  box-shadow: 0 10px 24px rgba(0,0,0,.08);
}

/* 右侧操作区：固定在卡片内部右侧 */
.card-actions{
  position: absolute;
  right: 0; top: 0;
  height: 100%;
  display: flex;
  z-index: 1;
}
.btn-invite,
.btn-delete{
  height: 100%;
  display: flex; align-items: center; justify-content: center;
  font-size: 18px; font-weight: 700; color: #fff;
}
/* 颜色与圆角：邀请四角 0；删除右侧两个圆角 18（与卡片一致），左侧 0 */
.btn-invite{ background: #FA9D3B; border-radius: 0; }
.btn-delete{ background: #FA5051; border-radius: 0 18px 18px 0; }

/* 可滑动内容轨道：在上层，向左平移露出操作区 */
.card-track{
  position: absolute;
  inset: 0;
  z-index: 2;
  transition: transform .18s ease; /* 松手后回弹/就位动画 */
  will-change: transform;
}

/* 上半部分：ditu */
.home-card-top{
  position: absolute;
  left: 0; top: 0;
  width: 100%;
  height: calc(100% - 50px);
  background-image: url('/static/images/ditu.png');
  background-size: cover;
  background-position: center;
  border-radius: 18px 18px 0 0;
}

/* 下半部分：灰色矩形 + 文字 */
.home-card-footer{
  position: absolute;
  left: 0; bottom: 0;
  width: 100%;
  height: 50px;
  background: #E5E5E5;
  border-radius: 0 0 18px 18px;
}

/* 第一行：用户名的家，x=69 */
.footer-title{
  position: absolute;
  left: 69px;
  top: 6px;
  font-family: "Alibaba","PingFang SC",Arial,sans-serif;
  font-size: 15px;
  color: #000000;
}

/* 第二行：两组横排，左对齐，组间距约 20px */
.footer-meta{
  position: absolute;
  left: 69px;
  bottom: 6px;
  display: flex;
  align-items: baseline;
  column-gap: 20px;
}
.meta-group{ display: inline-flex; align-items: baseline; column-gap: 4px; }
.meta-num{ font-family: "Alibaba","PingFang SC",Arial,sans-serif; font-size: 13px; color: #000000; }
.meta-label{ font-family: "Alibaba","PingFang SC",Arial,sans-serif; font-size: 13px; color: #A3A3A3; }

/* 占位撑高 */
.extender{ width: 1px; }

/* —— 浮窗 —— */
.mask { position: fixed; left: 0; top: 0; width: 100vw; height: 100vh; background: rgba(0,0,0,0.08); z-index: 8; }
.add-menu {
  position: fixed; top: 132px; right: 20px; width: 180px; height: 110px;
  background: #fff; border-radius: 18px; box-shadow: 0 8px 24px rgba(0,0,0,0.12); z-index: 9; overflow: hidden;
}
.menu-item{ height: 52px; display: flex; align-items: center; justify-content: center; font-family: "Alibaba","PingFang SC",Arial,sans-serif; font-size: 15px; color: #000; }
.divider{ height:1px; background:#E6E6E6; margin:0 16px; }
</style>
