<template>
  <view class="page-container">
    <view class="page">
      <!-- 顶部问候 -->
      <view class="topbar">
        <text class="hi">Hi! {{ displayName }}</text>
        <text class="caret" @click="toggleHomePicker"> ▾</text>
      </view>

      <!-- 三个统计卡 -->
      <view class="card left-card">
        <image class="icon" src="/static/icons/Humidity.svg" />
        <text class="value">{{ tempOutdoor }}</text>
        <text class="unit">%RH</text>
        <text class="label">湿度</text>
      </view>

      <view class="card center-card">
        <image class="icon" src="/static/icons/temperature.svg" />
        <text class="value">{{ tempIndoor }}</text>
        <text class="unit">°C</text>
        <text class="label">温度</text>
      </view>

      <view class="card right-card" @click="guardThen(() => JumpToSave())">
        <image class="icon" src="/static/icons/safety.svg" />
        <text class="value">{{ gasLevel }}</text>
        <text class="unit">ppm</text>
        <text class="label">煤气浓度</text>
      </view>

      <!-- 添加房间按钮（黑框） -->
      <view class="add-room-btn" @click="guardThen(() => addRoom())">
        <text class="add-room-text">添加房间</text>
      </view>

      <!-- 计数圆圈 + 标题 + 右侧黑色方框按钮（房间列表按钮） -->
      <view class="rooms-badge-fixed">{{ rooms.length }}</view>
      <text class="rooms-title-fixed">房间</text>
      <view class="rooms-list-btn" @click="goDeviceManager">
        <uni-icons type="list" size="22" color="#FFFFFF" />
      </view>

      <!-- 空状态 -->
      <view v-if="rooms.length === 0" class="rooms-empty-at-fixed">
        <image src="/static/icons/warningroom.svg" class="empty-icon" mode="aspectFit" />
        <text class="empty-title">还没添加房间</text>
        <text class="empty-tip">您还没有添加房间，请点击“添加房间”进行添加</text>
      </view>

      <!-- 房间列表（按当前家庭展示） -->
      <scroll-view v-else class="rooms-scroll-fixed" scroll-x enable-flex :show-scrollbar="false">
        <view
          v-for="(room, idx) in rooms"
          :key="room.id"
          class="room-item-small"
          @click="goRoom(room)"
          @longpress="guardThen(() => onLongPress(room, idx))"
          @touchstart="pressStart(room, idx)"
          @touchend="pressEnd"
          @touchcancel="pressEnd"
          @touchmove="pressEnd"
        >
          <view v-if="room.pinned" class="pin-ribbon">📌</view>
          <image :src="room.cover" class="room-cover" mode="aspectFill" />

          <!-- 底部覆盖矩形 -->
          <view class="room-bottom-rect"></view>

          <view class="room-info">
            <view class="room-name-block">
              <image :src="typeIcon[room.type] || typeIcon.default" class="room-type-icon-top" />
              <text class="room-name black center">{{ room.name }}</text>
            </view>

            <view class="room-stats">
              <view class="stat-group">
                <text class="stat-label">设备</text>
                <text class="stat-num">{{ room.devices || 0 }}</text>
              </view>
              <view class="stat-group">
                <text class="stat-label online">在线</text>
                <text class="stat-num online">{{ room.online || 0 }}</text>
              </view>
            </view>
          </view>
        </view>
      </scroll-view>

      <!-- 家庭选择浮层 -->
      <view v-if="showHomePicker" class="home-mask" @click.self="closeHomePicker">
        <view class="home-sheet" :style="{ height: homeSheetHeight + 'px' }">
          <view v-if="homes.length" class="home-row">
            <view
              v-for="home in homes"
              :key="home.id"
              class="home-chip"
              :class="{ active: String(home.id) === String(activeHomeId) }"
              @click="selectHome(home)"
            >
              <text class="home-chip-name">{{ home.name }}</text>
              <view v-if="String(home.id) === String(activeHomeId)" class="home-checked">
                <uni-icons type="checkmarkempty" size="16" color="#FFFFFF" />
              </view>
            </view>
          </view>

          <view v-if="homes.length" class="home-divider"></view>

          <view class="home-manage" @click="manageFamily">
            <text class="home-manage-text">管理家庭</text>
            <image src="/static/icons/chilun.svg" class="home-gear" />
          </view>
        </view>
      </view>

      <!-- 自定义删除确认弹窗 -->
      <view v-if="showDeleteConfirm" class="dlg-mask" @touchmove.stop @click.self="cancelDelete">
        <view class="dlg-panel">
          <image src="/static/icons/warning.svg" class="dlg-icon-img" mode="aspectFit" />
          <text class="dlg-title">删除房间</text>
          <text class="dlg-sub">你将要删除“{{ pendingRoom && pendingRoom.name }}”这个房间</text>
          <view class="dlg-actions">
            <view class="btn-cancel" @click="cancelDelete">否</view>
            <view class="btn-danger" @click="confirmDelete">是</view>
          </view>
        </view>
      </view>

      <!-- 自定义通知弹窗 -->
      <notification-modal
        :show="showNotification"
        :sceneName="notificationData.sceneName"
        :notifyType="notificationData.notifyType"
        @close="closeNotification"
      />
    </view>
  </view>
</template>

<script>
import CustomTabBar from '@/components/CustomTabBar.vue'
import NotificationModal from '@/components/notification-modal/notification-modal.vue'

import { getTemperatureData, getHumidityData, getGasData } from '@/libs/api/environmentApi.js'
import { getFamilyMembers } from '@/libs/api/family.js'

const GLOBAL_ROOMS_PREFIX = 'rooms:' // 每个家庭使用 rooms:<familyId> 作为 key

export default {
  components: { CustomTabBar, NotificationModal },
  data() {
    return {
      displayName: 'Yolo',
      tempOutdoor: '--',
      tempIndoor: '--',
      gasLevel: '--',
      powerUsage: '--',
      dataPollingTimer: null,

      // 加号浮窗
      showAddMenu: false,

      // 家庭浮层
      showHomePicker: false,
      homes: uni.getStorageSync('homes') || [],
      activeHomeId: uni.getStorageSync('activeHomeId') || '',

      // 家庭成员数据
      memberList: [],
      guestList: [],
      adminList: [],

      rooms: [],
      typeIcon: {
        bedroom : '/static/icons/bedroom.svg',
        living  : '/static/icons/livingroom.svg',
        study   : '/static/icons/studyroom.svg',
        kitchen : '/static/icons/kitchen.svg',
        store   : '/static/icons/storeroom.svg',
        bathroom: '/static/icons/bathroom.svg',
        default : '/static/icons/bedroom.svg'
      },

      // 删除弹窗
      showDeleteConfirm: false,
      pendingRoom: null,
      pendingIndex: -1,

      // 长按/点击判定
      pressTimer: null,
      isLongPress: false,

      // 通知相关
      showNotification: false,
      notificationData: {
        sceneName: '',
        notifyType: 'info'
      }
    }
  },

  onShow() {
    // 从本地存储获取用户名
    const localUserInfo = uni.getStorageSync('userInfo') || {}
    //console.log('localUserInfo', localUserInfo)
    this.displayName = localUserInfo.username || localUserInfo.nickname || 'Yolo'
    //console.log('this.nickname', localUserInfo.nickname)
    //console.log('this.username', localUserInfo.username)
    // 立即获取一次环境数据
    this.fetchEnvironmentData()
    
    // 设置定时器，每10秒获取一次数据
    if (!this.dataPollingTimer) {
      this.dataPollingTimer = setInterval(() => {
        this.fetchEnvironmentData()
      }, 1000)//10000
      console.log('环境数据轮询已启动，每1秒更新一次')
    }
    const u = uni.getStorageSync('userInfo') || {}
    this.displayName = u.username || u.nickname || this.displayName || 'Yolo'

    // 加载本地家庭列表，并确保每个家庭都有 id（string）
    const storedHomes = uni.getStorageSync('homes') || []
    this.homes = Array.isArray(storedHomes)
      ? storedHomes.map((h, idx) => ({
          id: h.id ? String(h.id) : String(idx + 1),
          ...h
        }))
      : []

    // 当前激活家庭兜底
    const storedActive = uni.getStorageSync('activeHomeId')
    if (storedActive) this.activeHomeId = String(storedActive)

    if (!this.activeHomeId && this.homes.length) {
      this.activeHomeId = this.homes[0].id
      uni.setStorageSync('activeHomeId', this.activeHomeId)
    }

    // 如果当前ID不在列表里：有家庭 → 回落到第一个；无家庭 → 清空
    const exists = this.homes.find(h => String(h.id) === String(this.activeHomeId))
    if (!exists) {
      if (this.homes.length) {
        this.activeHomeId = this.homes[0].id
        uni.setStorageSync('activeHomeId', this.activeHomeId)
      } else {
        this.activeHomeId = ''
        uni.removeStorageSync('activeHomeId')
      }
    }

    // 加载当前家庭的房间
    this.rooms = this.loadRoomsForActiveHome()

    // 合并从编辑页返回的新房间
    const newRoom = uni.getStorageSync('newRoom')
    if (newRoom) {
      if (!this.activeHomeId) {
        uni.removeStorageSync('newRoom')
      } else {
        if (!newRoom.id) newRoom.id = Date.now()
        if (!newRoom.type) newRoom.type = this.getTypeFromName(newRoom.name)
        const idx = this.rooms.findIndex(r => r.id === newRoom.id)
        if (idx > -1) this.$set(this.rooms, idx, { ...this.rooms[idx], ...newRoom })
        else this.rooms.push(newRoom)
        uni.removeStorageSync('newRoom')
        this.saveRoomsForActiveHome()
      }
    }
  },

  computed: {
    // 浮层高度：初始 70（仅管理家庭），每个家庭 +46
    homeSheetHeight() {
      const base = 70
      const per = 46
      return base + this.homes.length * per
    }
  },

  methods: {
    getCurrentUserRole() {
    try {
      const userInfo = uni.getStorageSync('userInfo') || {}
      const userId = userInfo.userId
      if (!userId || !this.activeHomeId) return null

      const userHomes = uni.getStorageSync('user_home') || [] 
      // user_home 结构: [{userId, homeId, role}, ...]
      const record = userHomes.find(
        r => String(r.userId) === String(userId) && String(r.homeId) === String(this.activeHomeId)
      )
      return record ? record.role : null
    } catch (e) {
      console.warn('获取用户角色失败', e)
      return null
    }
  },

    // 通知相关方法
    showCustomNotification(sceneName, notifyType) {
      this.notificationData.sceneName = sceneName;
      this.notificationData.notifyType = notifyType;
      this.showNotification = true;
    },

    closeNotification() {
      this.showNotification = false;
    },

  // 统一守卫：判断是否有家庭 & 是否管理员
  guardThen(fn) {
    if (!this.hasActiveHome()) {
      this.showAddMenu = false
      this.activeHomeId = ''
      uni.removeStorageSync('activeHomeId')
      uni.vibrateShort && uni.vibrateShort()
      uni.showToast({ title: '请先创建家庭', icon: 'none' })
      return
    }

    const role = this.getCurrentUserRole()
    if (role !== 0) {
      uni.showToast({ title: '您没有该权限', icon: 'none' })
      return
    }

    fn && fn()
  },

  /* —— 跳转们 —— */
  addRoom() {
    // 这里只负责跳转，不做权限判断，交给 guardThen
    uni.navigateTo({ url: `/pages/editRoom/editRoom?fid=${this.activeHomeId}` })
  },

  confirmDelete() {
    // 删除操作也需要管理员
    this.guardThen(() => {
      if (this.pendingIndex > -1) {
        this.rooms.splice(this.pendingIndex, 1)
        this.saveRoomsForActiveHome()
        uni.showToast({ title: '已删除', icon: 'none' })
      }
      this.cancelDelete()
    })
  },
    // 清除数据轮询定时器
    clearDataPollingTimer() {
      if (this.dataPollingTimer) {
        clearInterval(this.dataPollingTimer)
        this.dataPollingTimer = null
        console.log('环境数据轮询已停止')
      }
    },

    // 获取环境数据
    async fetchEnvironmentData() {
      try {
        
        const tempData = await getTemperatureData(1)
        const humiData = await getHumidityData(1)
        const gasData = await getGasData(1)
        //console.log('煤气数据:', gasData);
        // 处理温度数据
        if (tempData && tempData.length > 0) {
          const latestTemp = tempData[0]
          if (latestTemp.temperature !== undefined) {
            this.tempIndoor = latestTemp.temperature
            //console.log(`get到温度：${latestTemp.temperature}`)
          } else {
            this.tempIndoor = '--'
            console.log('温度数据格式不正确')
          }
        } else {
          this.tempIndoor = '--'
          console.log('没有获取到温度数据')
        }
        
        // 处理湿度数据
        if (humiData && humiData.length > 0) {
          const latestHumi = humiData[0]
          if (latestHumi.humidity !== undefined) {
            this.tempOutdoor = latestHumi.humidity
            //console.log(`get到湿度：${latestHumi.humidity}`)
          } else {
            this.tempOutdoor = '--'
            console.log('湿度数据格式不正确')
          }
        } else {
          this.tempOutdoor = '--'
          console.log('没有获取到湿度数据')
        }
        // 处理煤气数据
        //console.log('煤气数据原始内容:', gasData);
        if (gasData && gasData.length > 0) {
          const latestGas = gasData[0]
          if (latestGas.gas !== undefined) {
            this.gasLevel = latestGas.gas
            //console.log(`get到煤气浓度：${latestGas.gas}`)
          } else {
            this.gasLevel = '--'
            console.log('煤气数据格式不正确')
          }
        } else {
          this.gasLevel = '--'
          console.log('没有获取到煤气数据')
        }
      } catch (error) {
        console.error('获取环境数据失败:', error)
        // 出错时也显示--
        this.tempIndoor = '--'
        this.tempOutdoor = '--'
        this.gasLevel = '--'
      }
    },



    /* —— 家庭相关 —— */
    hasActiveHome() {
      return !!(this.activeHomeId && this.homes && this.homes.some(h => String(h.id) === String(this.activeHomeId)))
    },
    getRoomsKey(fid) {
      return GLOBAL_ROOMS_PREFIX + (fid || this.activeHomeId || 'none')
    },
    loadRoomsForActiveHome() {
      try {
        if (!this.activeHomeId) return []
        const raw = uni.getStorageSync(this.getRoomsKey(this.activeHomeId))
        return Array.isArray(raw) ? raw : []
      } catch {
        return []
      }
    },
    saveRoomsForActiveHome() {
      try {
        if (!this.activeHomeId) return
        const key = `rooms:${this.activeHomeId}`

        // 1) 写回该家庭的房间桶
        uni.setStorageSync(key, this.rooms)

        // 2) 同步更新 homes 里的该家庭“房间个数”
        const homes = uni.getStorageSync('homes') || []
        const idx = homes.findIndex(h => String(h.id) === String(this.activeHomeId))
        if (idx > -1) {
          const count = Array.isArray(this.rooms) ? this.rooms.length : 0
          homes[idx] = { ...homes[idx], rooms: count, roomCount: count }
          uni.setStorageSync('homes', homes)
        }
      } catch (e) {}
    },

    /* —— 统一守卫 —— */
    guardThen(fn) {
      if (!this.hasActiveHome()) {
        this.showAddMenu = false
        // 清掉可能的脏ID
        this.activeHomeId = ''
        uni.removeStorageSync('activeHomeId')
        // 反馈
        uni.vibrateShort && uni.vibrateShort()
        uni.showToast({ title: '请先创建家庭', icon: 'none' })
        return
      }
      fn && fn()
    },

    /* —— 跳转们 —— */
    addRoom() {
      if (!this.hasActiveHome()) {
        uni.showToast({ title: '请先创建家庭', icon: 'none' })
        return
      }
      uni.navigateTo({ url: `/pages/editRoom/editRoom?fid=${this.activeHomeId}` })
    },
    JumpToSave() {
      uni.navigateTo({ url: `/pages/safe/safe` })
    },
    goDeviceManager() {
      if (!this.hasActiveHome()) {
        uni.showToast({ title: '请先创建家庭', icon: 'none' })
        return
      }
      // 带上当前家庭ID，设备管理页按 fid 读取 rooms:<fid>
      uni.navigateTo({
        url: `/pages/devicemanager/devicemanager?fid=${this.activeHomeId}`
      })
    },

    // —— 房间项交互 —— 
    goRoom(room) {
      if (this.isLongPress) { this.isLongPress = false; return }
      if (!room || !room.id) {
        return uni.showToast({ title: '房间数据异常', icon: 'none' })
      }
      uni.navigateTo({ url: `/pages/room/room?id=${room.id}&fid=${this.activeHomeId}` })
    },

    // 名称推断类型
    getTypeFromName(name = '') {
      const n = String(name)
      if (/(卧|卧室|bedroom)/i.test(n)) return 'bedroom'
      if (/(客|客厅|living)/i.test(n)) return 'living'
      if (/(书|书房|study)/i.test(n)) return 'study'
      if (/(厨|厨房|kitchen)/i.test(n)) return 'kitchen'
      if (/(储|储藏|储物|杂|库|store|storage)/i.test(n)) return 'store'
      if (/(卫|卫生间|浴室|洗手间|bath(room)?|toilet|wc)/i.test(n)) return 'bathroom'
      return 'default'
    },

    // —— 长按删除：设置标记，避免与点击冲突 —— 
    onLongPress(room, idx) {
      this.isLongPress = true
      this.pendingRoom = room
      this.pendingIndex = idx
      this.showDeleteConfirm = true
    },
    pressStart(room, idx) {
      this.pressEnd()
      this.pressTimer = setTimeout(() => { this.onLongPress(room, idx) }, 500)
    },
    pressEnd() {
      if (this.pressTimer) { clearTimeout(this.pressTimer); this.pressTimer = null }
    },

    cancelDelete() { this.showDeleteConfirm = false; this.pendingRoom = null; this.pendingIndex = -1 },
    confirmDelete() {
      if (this.pendingIndex > -1) {
        this.rooms.splice(this.pendingIndex, 1)
        this.saveRoomsForActiveHome()
        uni.showToast({ title: '已删除', icon: 'none' })
      }
      this.cancelDelete()
    },

    // 家庭浮层
    toggleHomePicker() { this.showHomePicker = !this.showHomePicker },
    closeHomePicker()  { this.showHomePicker = false },

    selectHome(home) {
      const oldActive = this.activeHomeId
      this.activeHomeId = String(home.id)
      uni.setStorageSync('activeHomeId', this.activeHomeId)
      console.log('切换家庭', '旧active:', oldActive, '新active:', this.activeHomeId)
      console.log(`获取了${this.activeHomeId}的家庭用户`)

      // 调用获取家庭成员信息接口
      try {
        getFamilyMembers(this.activeHomeId)
          .then(res => {
            // 假设res.data包含members、guests和admins数组
            const newMemberList = res.data?.members || [];
            const newGuestList = res.data?.guests || [];
            const newAdminList = res.data?.admins || [];

            // 检查是否需要更新本地存储
            const oldMemberList = uni.getStorageSync(`members_${this.activeHomeId}`) || [];
            const oldGuestList = uni.getStorageSync(`guests_${this.activeHomeId}`) || [];
            const oldAdminList = uni.getStorageSync(`admins_${this.activeHomeId}`) || [];

            // 只有当数据发生变化时才更新本地存储
            if (JSON.stringify(newMemberList) !== JSON.stringify(oldMemberList)) {
              this.memberList = newMemberList;
              uni.setStorageSync(`members_${this.activeHomeId}`, newMemberList);
              console.log(`家庭${this.activeHomeId}成员列表已更新`);
            } else {
              this.memberList = oldMemberList;
              console.log(`家庭${this.activeHomeId}成员列表无变化，未更新`);
            }

            if (JSON.stringify(newGuestList) !== JSON.stringify(oldGuestList)) {
              this.guestList = newGuestList;
              uni.setStorageSync(`guests_${this.activeHomeId}`, newGuestList);
              console.log(`家庭${this.activeHomeId}访客列表已更新`);
            } else {
              this.guestList = oldGuestList;
              console.log(`家庭${this.activeHomeId}访客列表无变化，未更新`);
            }

            if (JSON.stringify(newAdminList) !== JSON.stringify(oldAdminList)) {
              this.adminList = newAdminList;
              uni.setStorageSync(`admins_${this.activeHomeId}`, newAdminList);
              console.log(`家庭${this.activeHomeId}管理员列表已更新`);
            } else {
              this.adminList = oldAdminList;
              console.log(`家庭${this.activeHomeId}管理员列表无变化，未更新`);
            }
          })
          .catch(error => {
            console.error('获取家庭成员信息失败:', error);
            uni.showToast({ title: '获取家庭成员信息失败', icon: 'none' });
          });
      } catch (error) {
        console.error('调用获取家庭成员信息接口异常:', error);
        uni.showToast({ title: '系统错误，请重试', icon: 'none' });
      }

      this.rooms = this.loadRoomsForActiveHome()
      this.closeHomePicker()
    },

    manageFamily() { uni.navigateTo({ url: '/pages/homeManage/homeManage' }) },
  },

  // 页面隐藏时清除定时器
  onHide() {
    this.clearDataPollingTimer()
  },

  // 组件销毁时清除定时器
  beforeDestroy() {
    this.clearDataPollingTimer()
  },

  watch: {
    // 当前家庭的房间变化 → 保存到该家庭对应的 key
    rooms: { deep: true, handler() { this.saveRoomsForActiveHome() } },
  }
}
</script>

<style scoped>
.page-container {
  position: fixed; /* 固定在屏幕，不随滚动移动 */
  top: 0;
  left: 0;
  right: 0;
  bottom: 0; /* 覆盖全屏 */
  overflow: hidden; /* 彻底禁止容器内滚动 */
  background-color: #fff; /* 继承页面背景色 */
}

/* —— 你的样式原样保留 —— */
.page { min-height: 100vh; background-color: #fff; position: relative; }
.topbar { position: absolute; top: 106px; left: 50%; transform: translateX(-50%); display: flex; align-items: center; }
.hi { font-size: 22px; font-weight: 700; color: #333; }
.caret { font-size: 18px; margin-left: 4px; }

.card{ position: absolute; top: 168px; width: 109px; height: 140px; border-radius: 20px; display: flex; flex-direction: column; align-items: center; justify-content: flex-start; color: #fff; padding-top: 15px; line-height: 1; gap: 12px; font-family:'Taipei Sans TC',sans-serif; }
.left-card { left: 22px; background: #5B17EA; }
.center-card { left: 50%; transform: translateX(-50%); background: #EA1763; }
.right-card { left: 272px; background: #EA8917; }
.icon { width: 25px; height: 25px; filter: brightness(0) invert(1); }
.card .value { font-size: 28px; line-height: 28px; font-weight: 700; font-variant-numeric: tabular-nums; }
.card .unit  { font-size: 12px; line-height: 12px; }
.card .label { font-size: 12px; line-height: 12px; }

.add-room-btn { position: absolute; top: 347px; left: 50%; transform: translateX(-50%); width: 359px; height: 49px; border-radius: 15px; background: #000; display: flex; justify-content: center; align-items: center; }
.add-room-text { font-size: 15px; color: #fff; font-weight: bold; }

.rooms-badge-fixed { position: absolute; left: 19px; top: 410px; width: 40px; height: 40px; border-radius: 20px; background: #000; color: #fff; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 16px; }
.rooms-title-fixed { position: absolute; left: 75px; top: 416px; font-size: 20px; font-weight: 700; color: #333; }

/* 右侧黑色方框按钮（房间列表按钮） */
.rooms-list-btn{
  position: absolute;
  top: 410px;
  left: calc(50% + 140px); /* 与你的第二份计算方式一致 */
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: #000;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6px 18px rgba(0,0,0,.08);
  z-index: 3; /* 确保在滚动区之上 */
}

/* 空状态使用弹性布局，避免溢出 */
.rooms-empty-at-fixed {
  position: absolute;
  left: 0;
  right: 0;
  top: 460px; /* 与房间列表顶部对齐 */
  bottom: calc(env(safe-area-inset-bottom) + 140rpx);
  display: flex;
  flex-direction: column;
  justify-content: center;
  text-align: center;
}
.empty-icon { width: 75px; height: 75px; margin: 0 auto 12px; }
.empty-title { font-size: 30px; font-weight: bold; color: #000; margin-bottom: 8px; }
.empty-tip { font-size: 12px; color: #808080; white-space: nowrap; }

.rooms-scroll-fixed{
  position: absolute;
  top: 460px;
  left: 16px;
  right: 0;
  bottom: calc(env(safe-area-inset-bottom) + 140rpx);
  white-space: nowrap;
  padding-right: 16px;
  z-index: 1;
  overflow-y: hidden;
}
.room-item-small { position: relative; width: 163px; height: 316px; border-radius: 22px; overflow: hidden; display: inline-flex; margin-right: 16px; background: #f5f5f5; box-shadow: 0 6px 18px rgba(0,0,0,.08); }
.room-cover { width: 100%; height: 100%; }
.pin-ribbon { position: absolute; left: 0; top: 0; background: #C53261; color: #fff; padding: 8px 10px; border-bottom-right-radius: 14px; font-size: 14px; z-index: 2; }

.room-bottom-rect{ position: absolute; left: 0; bottom: 0; width: 100%; height: 124px; background: rgba(231,232,232,0.92); border-radius: 0 0 22px 22px; z-index: 1; }
.room-info{ position: absolute; left: 0; right: 0; bottom: 0; padding: 12px; background: transparent; z-index: 2; }
.room-name-block{ display: flex; flex-direction: column; align-items: center; gap: 10px; margin-bottom: 8px; width: 100%; }
.room-type-icon-top{ width: 30px; height: 30px; }
.room-name.black{ color: #000; font-family:'Taipei Sans TC',sans-serif; }
.room-name.center{ text-align: center; }

.room-info .room-stats{ display: flex; justify-content: space-between; align-items: center; font-size: 12px; opacity: .95; padding: 0 2px; }
.room-info .stat-group{ display: flex; align-items: center; column-gap: 6px; }
.room-info .stat-label{ color: #898989; }
.room-info .stat-num{ color: #000; }
.room-info .online{ color: #FF3B7A; font-weight: 700; }

.dlg-mask { position: fixed; left: 0; top: 0; right: 0; bottom: 0; background: rgba(0,0,0,.35); display: flex; align-items: center; justify-content: center; z-index: 999; }
.dlg-panel { width: 296px; background: #fff; border-radius: 18px; box-shadow: 0 10px 30px rgba(0,0,0,.18); padding: 22px 18px 20px; text-align: center; }
.dlg-icon-img { width: 56px; height: 56px; margin: 0 auto 12px; }
.dlg-title { display: block; font-size: 18px; font-weight: 800; color: #222; margin-bottom: 8px; }
.dlg-sub { display: block; font-size: 13px; color: #666; line-height: 1.6; margin-bottom: 20px; }
.dlg-actions { display: flex; justify-content: space-between; gap: 14px; }
.btn-cancel, .btn-danger { flex: 1; height: 40px; line-height: 40px; border-radius: 22px; text-align: center; font-size: 15px; font-weight: 500; }
.btn-cancel { background: #F6F6F8; color: #5E6573; }
.btn-danger { background: #E2545C; color: #fff; }

.home-mask{ position: fixed; inset: 0; background: rgba(0,0,0,.35); z-index: 998; }
.home-sheet{ position: absolute; top: 136px; left: 50%; transform: translateX(-50%); width: 227px; background: #fff; border-radius: 26px; box-shadow: 0 12px 36px rgba(0,0,0,.18); padding: 12px 14px; }
.home-row{ display: flex; flex-wrap: wrap; column-gap: 12px; row-gap: 8px; padding: 6px 4px 8px; }
.home-chip{ display: inline-flex; align-items: center; gap: 8px; height: 36px; padding: 0 14px; border-radius: 18px; background: #F5F7F7; color: #000; }
.home-chip.active{ background: #DDF3F0; }
.home-chip-name{ font-size: 15px; color:#000; }
.home-checked{ width: 22px; height: 22px; border-radius: 11px; background:#58D0C1; display:flex; align-items:center; justify-content:center; }
.home-divider{ height:1px; background:#E9E9E9; margin:8px 0 10px; }
.home-manage{ display:flex; align-items:center; width:100%; padding:6px 4px; }
.home-manage-text{ font-family:'Alibaba',sans-serif; font-size:13px; color:#000; }
.home-gear{ width:18px; height:18px; margin-left:auto; display:block; }
</style>
