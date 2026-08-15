<template>
  <!-- 页面容器：背景图全屏显示 -->
  <view class="page-container">
  <view
    class="page"
    :style="{ backgroundImage: `url(${roomCover})`, backgroundSize: 'cover', backgroundPosition: 'center' }"
  >
    <!-- 返回按钮 -->
    <view class="back-card" @click="safeBack">
      <uni-icons type="arrowleft" size="25" color="#666"></uni-icons>
    </view>

    <!-- 右上角：加号方框 + 菜单 -->
    <view class="add-anchor" @click.stop>
      <view class="add-btn" @click.stop="toggleAddMenu">＋</view>

      <!-- 遮罩：点击关闭 -->
      <view v-if="showAddMenu" class="add-mask" @click="closeAddMenu"></view>

      <!-- 浮窗菜单：只保留“添加设备” -->
      <view v-if="showAddMenu" class="add-menu" @click.stop>
        <view class="add-item" @click="guardThen(() => goAddDevice())">添加设备</view>
      </view>
    </view>

    <!-- 半透明内容容器 -->
    <view class="transparent-container" v-if="ready">
      <!-- 房间信息 -->
      <view class="room-info">
        <text class="room-name">{{ roomTypeText }}</text>
      </view>

      <!-- 设备统计 -->
      <view class="stats-container">
        <view class="stat-item">
          <text class="stat-label">设备总数</text>
          <text class="stat-value">{{ devicesCount }}</text>
        </view>
        <view class="stat-item">
          <text class="stat-label">在线设备</text>
          <text class="stat-value online">{{ onlineCount }}</text>
        </view>
      </view>

      <!-- 设备网格（支持左滑删除） -->
      <view v-if="devices.length" class="devices-grid">
        <view
          v-for="device in devices"
          :key="device.id"
          class="device-swipe"
        >
          <!-- 右侧删除按钮（底层，卡片滑开时露出） -->
          <view class="delete-box" @tap.stop="deleteDevice(device)">删除</view>
      
          <!-- 顶层卡片：只在卡片上接管滑动手势 -->
          <view
            class="device-card"
            :style="{
              transform: `translateX(${device._dx || 0}px)`,
              transition: device._dragging ? 'none' : 'transform .18s ease'
            }"
            @touchstart.stop="onCardTouchStart($event, device)"
            @touchmove.stop.prevent="onCardTouchMove($event, device)"
            @touchend.stop="onCardTouchEnd($event, device)"
            @touchcancel.stop="onCardTouchEnd($event, device)"
            @tap="onCardClick(device)"
          >
            <image :src="device.icon" class="card-icon" mode="aspectFit" />
            <text class="card-name">{{ device.name }}</text>
            <text class="card-sub" v-if="device.sub">{{ device.sub }}</text>
      
            <!-- 迷你开关 -->
            <view class="tiny-switch" :class="{ active: device.status }" @tap.stop="toggleDevice(device)">
              <view class="tiny-knob"></view>
            </view>
          </view>
        </view>
      </view>
    </view> <!-- /.transparent-container -->

    <!-- ============ 浮层与遮罩：与 transparent-container 同级 ============ -->

    <!-- 遮罩 -->
    <view
      v-if="showAcPanel || showLightPanel"
      class="ac-mask"
      :style="{ opacity: (1 - sheetOffset/(sheetMaxOffset||1)).toFixed(2) }"
      @click="closeAnyPanel"
      @touchmove.stop.prevent
    ></view>

    <!-- 空调面板 -->
    <view
      v-if="showAcPanel"
      class="ac-panel"
      :style="{
        transform: `translate3d(0, ${sheetOffset}px, 0)`,
        transition: dragging ? 'none' : 'transform .22s ease'
      }"
    >
      <!-- 顶部把手 -->
      <view
        class="ac-drag"
        :catch-move="true"
        @touchstart.stop.prevent="onSheetStart"
        @touchmove.stop.prevent="onSheetMove"
        @touchend.stop.prevent="onSheetEnd"
        @touchcancel.stop.prevent="onSheetCancel"
      >
        <view class="ac-topline"></view>
      </view>

      <!-- 标题 + 右上角可滑动开关 -->
      <view class="ac-header">
        <text class="ac-title">空调</text>
        <view
          class="ac-switch"
          :class="{ on: acOn }"
          @click.stop="acToggle"
          @touchstart.stop="onSwitchStart"
          @touchmove.stop="onSwitchMove"
          @touchend.stop="onSwitchEnd"
          @touchcancel.stop="onSwitchEnd"
        >
          <view class="ac-switch-knob" :style="{ left: (3 + swPos * swRange) + 'px' }"></view>
        </view>
      </view>

      <!-- 圆盘 -->
      <view
        class="ac-dial"
        ref="dial"
        @touchstart.stop.prevent="onDialStart"
        @touchmove.stop.prevent="onDialMove"
        @touchend.stop.prevent="onDialEnd"
        @touchcancel.stop.prevent="onDialEnd"
      >
        <svg :width="dialSize" :height="dialSize">
          <g :transform="`translate(${dialCenter}, ${dialCenter})`">
            <circle :r="dialOuterR" stroke="#EFEFEF" stroke-width="24" fill="none" />
            <circle
              :r="dialOuterR"
              stroke="#7A3FF0"
              stroke-width="24"
              fill="none"
              stroke-linecap="round"
              :stroke-dasharray="circumference"
              :stroke-dashoffset="progressOffset"
              transform="rotate(-90)"
            />
            <g v-for="i in tickCount" :key="i" :transform="`rotate(${(i-1)*tickStep})`">
              <line
                :x1="0" :y1="-(dialInnerR)"
                :x2="0" :y2="-(dialInnerR - tickLen(i))"
                :stroke="tickColor(i)"
                :stroke-width="tickWidth(i)"
                stroke-linecap="round"
              />
            </g>
          </g>
        </svg>
        <view class="dial-center">
          <view class="dial-percent">{{ percent }}</view>
          <view class="dial-unit">℃</view>
        </view>
      </view>

      <!-- +/- -->
      <view class="ac-stepper">
        <view class="ac-round-btn" @click.stop="acDec">－</view>
        <view class="ac-round-btn" @click.stop="acInc">＋</view>
      </view>

      <!-- 模式 -->
      <view class="ac-modes">
        <view class="ac-mode" :class="{ active: acMode==='cool' }" @click.stop="setAcMode('cool')">❄ 冷风</view>
        <view class="ac-mode" :class="{ active: acMode==='heat' }" @click.stop="setAcMode('heat')">🌡 制热</view>
        <view class="ac-mode" :class="{ active: acMode==='fan' }"  @click.stop="setAcMode('fan')">💨 排风</view>
      </view>
    </view> <!-- /.ac-panel -->

    <!-- 灯控面板 -->
    <view
      v-if="showLightPanel"
      class="ac-panel light-panel"
      :style="{
        transform: `translate3d(0, ${sheetOffset}px, 0)`,
        transition: dragging ? 'none' : 'transform .22s ease'
      }"
    >
      <!-- 顶部把手 -->
      <view
        class="ac-drag"
        :catch-move="true"
        @touchstart.stop.prevent="onSheetStart"
        @touchmove.stop.prevent="onSheetMove"
        @touchend.stop.prevent="onSheetEnd"
        @touchcancel.stop.prevent="onSheetCancel"
      >
        <view class="ac-topline"></view>
      </view>

      <!-- 标题 + 右上角开关 -->
      <view class="ac-header">
        <text class="ac-title">{{ lightTitle }}</text>
        <view
          class="ac-switch"
          :class="{ on: lightOn }"
          @click.stop="lightToggle"
          @touchstart.stop="onSwitchStart"
          @touchmove.stop="onSwitchMove"
          @touchend.stop="onLightSwitchEnd"
          @touchcancel.stop="onLightSwitchEnd"
        >
          <view class="ac-switch-knob" :style="{ left: (3 + swPos * swRange) + 'px' }"></view>
        </view>
      </view>

      <!-- 三档 -->
      <view class="ac-modes">
        <view class="ac-mode" :class="{ active: lightLevel===1 }" @click.stop="setLightMode(1)">一档</view>
        <view class="ac-mode" :class="{ active: lightLevel===2 }" @click.stop="setLightMode(2)">二档</view>
        <view class="ac-mode" :class="{ active: lightLevel===3 }" @click.stop="setLightMode(3)">三档</view>
      </view>
    </view>
  </view>
  </view> <!-- /.page -->
</template>

<script>
// 导入设备控制API
import { sendLightCmd, sendFanLevelCmd, sendBackendPayload } from '@/libs/api/device';

const TYPE_MAP = {
  bedroom: '卧室',
  living: '客厅',
  study: '书房',
  kitchen: '厨房',
  store: '储物间',
  bathroom: '卫生间',
  default: '其他'
}

// —— 房间类型 -> 后端灯码 —— //
const LIGHT_CODE_BY_ROOM = {
  living:   'liv_lit', // 客厅
  kitchen:  'kit_lit', // 厨房
  bathroom: 'tol_lit',  // 卫生间
  bedroom:  'tol_lit'  // 卧室(使用厕所灯接口)
}
// 排气扇代码（厕所排气扇）
const EXHAUST_FAN_CODE = 'fan_level'
const FAN_CODE = 'fan_level' // 风扇/空调档位统一

export default {
  data() {
    return {
      ready: false,
      roomId: '',
      fid: '',
      roomType: '',
      roomTypeText: '',
      roomCover: '/static/images/1.jpg',
      devicesCount: 0,
      onlineCount: 0,
      devices: [],

      // 空调浮层
      showAcPanel: false,
      acOn: true,
      acTemp: 19,
      acMode: 'cool',
      acTimer: 12,
      acHumidity: 40,
      acDeviceId: '',

      // 面板拖拽
      sheetOffset: 0,
      sheetMaxOffset: 320,
      dragStartY: 0,
      dragStartOffset: 0,
      dragging: false,

      // 开关拖动
      swDragging: false,
      swPos: 1,
      swRange: 20,

      // 圆盘参数
      dialSize: 300,
      dialCenter: 150,
      dialOuterR: 120,
      dialInnerR: 95,
      dialStartDeg: 270,
      dialDragging: false,
      tempMin: 16,
      tempMax: 30,

      tickCount: 60,
      _dialRect: null,

      // 灯控
      showLightPanel: false,
      lightDeviceId: '',
      lightTitle: '灯光',
      lightOn: true,
      lightLevel: 1,

      showAddMenu: false,

      // —— 滑动删除相关 —— //
      swipeMax: 72  // 删除按钮宽度（px）
    }
  },

  watch: {
    percent(newP){
      this.applyAutoMode(newP)
      this.publishAcState && this.publishAcState()
    },
    acOn(newVal){
      this.swPos = newVal ? 1 : 0
      const idx = this.devices.findIndex(d => String(d.id) === String(this.acDeviceId))
      if (idx > -1) {
        this.$set(this.devices[idx], 'status', !!newVal)
        this.saveRoomDevices()
        this.updateOnlineCount()
      }
    }
  },

  computed: {
    dialProgress(){
      const span = (this.tempMax - this.tempMin) || 1
      const raw  = (this.acTemp - this.tempMin) / span
      const p    = Number.isFinite(raw) ? raw : 0
      return Math.min(1, Math.max(0, p))
    },
    circumference(){ return 2 * Math.PI * this.dialOuterR },
    progressOffset(){ return this.circumference * (1 - this.dialProgress) },
    tickStep(){ return 360 / this.tickCount },
    percent(){
      const v = Math.round(this.dialProgress * 100)
      return Number.isFinite(v) ? Math.max(0, Math.min(100, v)) : 0
    },
  },

  onLoad(options) {
    this.roomId = String(options.id || '')
    this.fid    = String(options.fid || uni.getStorageSync('activeHomeId') || '')
    this.fetchRoomDetails()
  },

  onShow() {
    if (this.roomId) this.fetchRoomDetails()
  },

  methods: {
    /* ========= 基础与导航 ========= */
    safeBack() {
      const pages = getCurrentPages()
      if (pages && pages.length > 1) uni.navigateBack()
      else uni.reLaunch({ url: '/pages/zhuye/zhuye' })
    },

    toggleAddMenu(){ this.showAddMenu = !this.showAddMenu },
    closeAddMenu(){ this.showAddMenu = false },
    guardThen(fn){
      if(!this.fid){
        this.showAddMenu = false
        uni.showToast({ title: '请先创建家庭', icon: 'none' })
        return
      }
      fn && fn()
    },
    goAddDevice(){
      this.showAddMenu = false
      uni.navigateTo({ url: `/pages/adddevice/adddevice?fid=${this.fid}&roomId=${this.roomId}` })
    },

    /* ========= 数据加载 ========= */
    fetchRoomDetails() {
      const room = this.getRoomById(this.roomId)
      if (!room) {
        this.roomType     = 'default'
        this.roomTypeText = TYPE_MAP.default
        this.roomCover    = '/static/images/1.jpg'
        this.devices      = []
        this.updateOnlineCount()
        this.ready = true
        return
      }
      this.roomType     = room.type || 'default'
      this.roomTypeText = TYPE_MAP[this.roomType] || TYPE_MAP.default
      this.roomCover    = room.cover || '/static/images/1.jpg'
    
      const listFromRoom =
        Array.isArray(room.devicesList) ? room.devicesList :
        Array.isArray(room.deviceList)  ? room.deviceList  :
        Array.isArray(room.devicesArr)  ? room.devicesArr  :
        (Array.isArray(room.devices) && typeof room.devices[0]==='object' ? room.devices : [])
    
      // ✅ 归一化：把各种来源字段统一到 item.status（布尔）
      this.devices = (listFromRoom || []).map(d => {
        const status =
          (typeof d.status !== 'undefined' ? !!d.status :
          (typeof d.on     !== 'undefined' ? !!d.on :
          (typeof d.power  !== 'undefined' ? Number(d.power)===1 :
          (typeof d.switch !== 'undefined' ? !!d.switch :
          (typeof d.state  === 'string'    ? d.state.toUpperCase()==='ON' :
          (typeof d.statusText==='string'  ? d.statusText.toLowerCase()==='on' : false))))))
    
        return { ...d, status }
      })
    
      this.updateOnlineCount()
      this.ready = true
    },

    getRoomById(id) {
      if (!id) return null
      if (this.fid) {
        const k = `rooms:${this.fid}`
        const list = uni.getStorageSync(k) || []
        return list.find(x => String(x.id) === String(id)) || null
      }
      const list = uni.getStorageSync('rooms') || []
      return list.find(x => String(x.id) === String(id)) || null
    },

    saveRoomDevices(){
      this.updateOnlineCount()
    
      // 把设备写回时，同时写 status 和 on（兼容别的页面/老数据）
      const listForStore = this.devices.map(d => ({
        ...d,
        status: !!d.status,
        on:      !!d.status
      }))
    
      const patch = {
        devicesList: listForStore,
        devices: listForStore.length,
        online:  listForStore.filter(x => x.status === true).length
      }
    
      if (this.fid) {
        const key  = `rooms:${this.fid}`
        const list = uni.getStorageSync(key) || []
        const i = list.findIndex(r => String(r.id) === String(this.roomId))
        if (i > -1) {
          list[i] = { ...list[i], ...patch }
          uni.setStorageSync(key, list)
        }
      } else {
        const list = uni.getStorageSync('rooms') || []
        const i = list.findIndex(r => String(r.id) === String(this.roomId))
        if (i > -1) {
          list[i] = { ...list[i], ...patch }
          uni.setStorageSync('rooms', list)
        }
      }
    
      // ✅ 广播：让首页/设备管理页面时刷新
      uni.$emit('rooms-updated', { fid: this.fid, rid: String(this.roomId) })
    },

    updateOnlineCount() {
      this.devicesCount = this.devices.length
      this.onlineCount  = this.devices.filter(d => d.status).length
    },

    /* ========= 设备进入控制 ========= */
    isAc(device){
      return device && ((device.type === 'aircon') || (device.name === '空调'))
    },
    isLight(device){
      if (!device) return false
      const n = String(device.name || '').toLowerCase()
      const t = String(device.type || '').toLowerCase()
      return t === 'light' || n.includes('灯') || n.includes('light') || n.includes('lamp')
    },

    // 判断是否为排气扇或烟雾报警器
    isExhaustFan(device){
      if (!device) return false;
      const n = String(device.name || '').toLowerCase();
      const t = String(device.type || '').toLowerCase();
      return t === 'exhaust' || n.includes('排气扇') || n.includes('exhaust') || 
             n.includes('烟雾') || n.includes('报警器') || n.includes('烟感');
    },

    onCardClick(device){
      // 若当前卡片处于“展开删除”的状态，不触发打开控制面板
      if ((device._dx || 0) < -10 || device._open) return
      this.controlDevice(device)
    },

    controlDevice(device) {
      if (this.isAc(device)) {
        this.acDeviceId = device.id || ''
        this.acOn = !!device.status
        this.swPos = this.acOn ? 1 : 0
        this.showAcPanel = true
        this.$nextTick(async () => {
          const sys = uni.getSystemInfoSync()
          const wh  = sys.safeArea?.height || sys.windowHeight || 640
          this.sheetMaxOffset = Math.min(Math.round(wh * 0.40), 360)
          this.sheetOffset = 0
          await this.calcDialRect()
        })
        return
      }

      if (this.isLight(device)) {
        this.lightDeviceId = device.id || ''
        this.lightTitle    = device.name || '灯光'
        this.lightOn       = !!device.status
        this.lightLevel    = Number(device.level || device.speed || 1)
        this.swPos         = this.lightOn ? 1 : 0

        // 运行时兜底：没有 backend.code 的老设备，按房间类型补齐
        this.ensureLightBackend(device)

        this.showLightPanel = true
        this.$nextTick(() => {
          const sys = uni.getSystemInfoSync()
          const wh  = sys.safeArea?.height || sys.windowHeight || 640
          this.sheetMaxOffset = Math.min(Math.round(wh * 0.40), 360)
          this.sheetOffset = 0
        })
        return
      }

      uni.showToast({ title: `打开设备：${device.name}`, icon: 'none' })
    },

    toggleDevice(device) {
      device.status = !device.status
      this.updateOnlineCount()

      // ★ 灯：点卡片上的小开关也下发 on/off
      if (this.isLight(device)) {
        this.ensureLightBackend(device)
        console.log('发送灯控命令:', { device: device.name, action: device.status ? 'on' : 'off' });
        this.sendLightCmd(device, { action: device.status ? 'on' : 'off' })
          .then(res => {
            console.log('灯控命令发送成功:', res);
          })
          .catch(err => {
            console.error('灯控命令发送失败:', err);
            uni.showToast({ title: '灯控失败', icon: 'none' });
          })
      } else if (this.isExhaustFan(device)) {
        // 排气扇或烟雾报警器：类似灯的控制逻辑，1表示开，0表示关
        this.ensureExhaustFanBackend(device)
        const value = device.status ? 1 : 0
        console.log('发送排气扇/烟雾报警器控制命令:', { device: device.name, value: value });
        this.sendBackendPayload({ code: EXHAUST_FAN_CODE, value: value, device })
          .then(res => {
            console.log('排气扇/烟雾报警器控制成功:', res);
          })
          .catch(err => {
            console.error('排气扇/烟雾报警器控制失败:', err);
            uni.showToast({ title: '设备控制失败', icon: 'none' });
          })
      } else if (this.isAc(device)) {
        // 空调：直接更新状态并控制设备
        console.log('发送空调控制命令:', { device: device.name, status: device.status });
        this.updateFanLevelByAcStatus();
      } else if (device.type === 'fan' || String(device.name).includes('风扇')) {
        // 风扇：调用独立控制方法
        this.toggleFan(device);
      }

      // 同步 AC 面板开关
      if (this.showAcPanel && String(device.id) === String(this.acDeviceId) && this.isAc(device)) {
        this.acOn = device.status
        this.swPos = this.acOn ? 1 : 0
      }
      this.saveRoomDevices()
    },

    /* ========= 灯控：开关 / 三档 ========= */
    lightToggle(){
      const next = !this.lightOn
      this.lightOn = next
      const idx = this.devices.findIndex(d => String(d.id) === String(this.lightDeviceId))
      if (idx > -1) {
        const dev = this.devices[idx]
        this.$set(dev, 'status', !!next)
        this.ensureLightBackend(dev)
        console.log('发送灯控命令:', { device: dev.name, action: next ? 'on' : 'off' });
        this.sendLightCmd(dev, { action: next ? 'on' : 'off' })
          .then(res => {
            console.log('灯控命令发送成功:', res);
          })
          .catch(err => {
            console.error('灯控命令发送失败:', err);
            uni.showToast({ title: '灯控失败', icon: 'none' });
          })
        this.saveRoomDevices()
        this.updateOnlineCount()
      }
    },

    setLightMode(level){
      const lv = [1,2,3].includes(level) ? level : 1
      this.lightLevel = lv
      if (!this.lightOn) this.lightOn = true

      const idx = this.devices.findIndex(d => String(d.id) === String(this.lightDeviceId))
      if (idx > -1) {
        const dev = this.devices[idx]
        this.$set(dev, 'level', lv)
        this.$set(dev, 'status', true)
        this.ensureLightBackend(dev)
        // ★ 三档下发：level=1/2/3
        console.log('发送灯控命令:', { device: dev.name, action: 'level', level: lv });
        this.sendLightCmd(dev, { action:'level', level: lv })
          .then(res => {
            console.log('灯控命令发送成功:', res);
          })
          .catch(err => {
            console.error('灯控命令发送失败:', err);
            uni.showToast({ title: '灯控失败', icon: 'none' });
          })
        this.saveRoomDevices()
        this.updateOnlineCount()
      }
    },

    onLightSwitchEnd(){
      if(!this.swDragging) return
      this.swDragging = false
      const nextOn = this.swPos >= 0.5
      this.lightOn = nextOn
      this.swPos = nextOn ? 1 : 0
      const idx = this.devices.findIndex(d => String(d.id) === String(this.lightDeviceId))
      if (idx > -1) {
        const dev = this.devices[idx]
        dev.status = this.lightOn
        this.ensureLightBackend(dev)
        console.log('发送灯控命令:', { device: dev.name, action: this.lightOn ? 'on' : 'off' });
        this.sendLightCmd(dev, { action: this.lightOn ? 'on' : 'off' })
          .then(res => {
            console.log('灯控命令发送成功:', res);
          })
          .catch(err => {
            console.error('灯控命令发送失败:', err);
            uni.showToast({ title: '灯控失败', icon: 'none' });
          })
        this.saveRoomDevices()
        this.updateOnlineCount()
      }
    },

    /* ========= 与后端打通（已迁移到device.js） ========= */
    lightCodeForRoom(roomType){
      return LIGHT_CODE_BY_ROOM[roomType] || ''
    },
    ensureLightBackend(device){
      if (!device || device.type !== 'light') return
      const code = device?.backend?.code || this.lightCodeForRoom(this.roomType)
      if (!device.backend) device.backend = {}
      if (!device.backend.code && code) {
        device.backend.code = code
        device.backend.caps = ['on','off','level']
        this.saveRoomDevices()
      }
    },

    // 风扇独立控制方法
    toggleFan(device){
      this.ensureFanBackend(device)
      const nextLevel = device.status ? 0 : 1  // 关了就开一档，开着就关
      device.level = nextLevel
      device.status = nextLevel > 0

      console.log('发送风扇控制命令:', { device: device.name, level: nextLevel });
      // 处理风扇开关：0表示关闭，1-3表示档位
      if (nextLevel === 0) {
        console.log('关闭风扇');
        // 直接调用sendBackendPayload发送0值来关闭风扇
        this.sendBackendPayload({ code: FAN_CODE, value: 0, device })
          .then(res => {
            console.log('风扇关闭成功:', res);
          })
          .catch(err => {
            console.error('风扇关闭失败:', err);
            uni.showToast({ title: '风扇关闭失败', icon: 'none' });
          })
      } else {
        // 发送正常档位命令
        this.sendFanLevelCmd(device, nextLevel)
          .then(res => {
            console.log('风扇控制成功:', res);
          })
          .catch(err => {
            console.error('风扇控制失败:', err);
            uni.showToast({ title: '风扇控制失败', icon: 'none' });
          })
      }

      this.saveRoomDevices()
      this.updateOnlineCount()
    },

    ensureFanBackend(device){
      if (!device || device.type !== 'fan') return
      if (!device.backend) device.backend = {};
      if (!device.backend.code) {
        device.backend.code = FAN_CODE // 'fan_level'
        device.backend.caps = ['on','off','level']
        this.saveRoomDevices()
      }
    },

    // 确保排气扇后端信息
    ensureExhaustFanBackend(device){
      if (!device || !this.isExhaustFan(device)) return
      if (!device.backend) device.backend = {};
      if (!device.backend.code) {
        device.backend.code = EXHAUST_FAN_CODE // 'exh_level'
        device.backend.caps = ['on','off']
        this.saveRoomDevices()
      }
    },

    // 导入的设备控制方法
    sendLightCmd,
    sendFanLevelCmd,
    sendBackendPayload,

    // 根据空调状态更新风扇档位
    updateFanLevelByAcStatus(shouldLog = true){
      // 找到风扇设备
      const fanDevice = this.devices.find(d => d.type === 'fan' || String(d.name).includes('空调'));
      console.log('找到的风扇设备:', fanDevice);
      if (!fanDevice) return;

      let fanLevel = 0;
      // 使用设备实际状态而不是acOn变量
      const deviceStatus = fanDevice.status;
      if (deviceStatus) {
        if (this.acMode === 'cool') fanLevel = 1;
        else if (this.acMode === 'heat') fanLevel = 2;
        else if (this.acMode === 'fan') fanLevel = 3;
      }else{
        fanLevel = 0
      }

      // 调用风扇档位控制API
      console.log('发送风扇档位控制命令:', { device: fanDevice.name, level: fanLevel });
      // 发送正确的值：0表示关闭，1-3表示档位
      if (fanLevel === 0) {
        console.log('关闭风扇');
        // 发送0值来关闭风扇
        this.sendBackendPayload({ code: FAN_CODE, value: 0, device: fanDevice, shouldLog })
          .then(res => {
            console.log('风扇关闭成功:', res);
          })
          .catch(err => {
            console.error('风扇关闭失败:', err);
            uni.showToast({ title: '风扇关闭失败', icon: 'none' });
          })
      } else {
        // 发送档位值来开启风扇
        this.sendBackendPayload({ code: FAN_CODE, value: fanLevel, device: fanDevice, shouldLog })
          .then(res => {
            console.log('风扇控制成功:', res);
          })
          .catch(err => {
            console.error('风扇控制失败:', err);
            uni.showToast({ title: '风扇控制失败', icon: 'none' });
          })
      }
      // 更新本地设备状态
      fanDevice.level = fanLevel;
      fanDevice.status = fanLevel > 0;
      this.saveRoomDevices();
    },

    /* ========= 面板交互（通用） ========= */
    closeAnyPanel(){
      this.showAcPanel = false
      this.showLightPanel = false
      this.sheetOffset = 0
    },

    /* ========= 空调浮层：拖拽/开关/圆盘 ========= */
    onSheetStart(e){
      const t = this._getTouch(e); if(!t) return
      this.dragging = true
      this.dragStartY = t.y
      this.dragStartOffset = this.sheetOffset
    },
    onSheetMove(e){
      if(!this.dragging) return
      const t = this._getTouch(e); if(!t) return
      let next = this.dragStartOffset + (t.y - this.dragStartY)
      if (next < 0) next = 0
      if (next > this.sheetMaxOffset) next = this.sheetMaxOffset
      this.sheetOffset = next
    },
    _snapOrClose(){
      const max = this.sheetMaxOffset
      const y   = this.sheetOffset
      if (y > max * 0.70){
        this.showAcPanel = false
        this.showLightPanel = false
        this.sheetOffset = 0
      } else if (y > max * 0.45){
        this.sheetOffset = max
      } else {
        this.sheetOffset = 0
      }
      this.$nextTick(() => this.calcDialRect())
    },
    onSheetEnd(){ this.dragging=false; this._snapOrClose() },
    onSheetCancel(){ this.dragging=false; this._snapOrClose() },

    onSwitchStart(e){
      const t = this._getTouch(e); if(!t) return
      this.swDragging = true
      this._swStartX = t.x
      this._swStartPos = this.swPos
    },
    onSwitchMove(e){
      if(!this.swDragging) return
      const t = this._getTouch(e); if(!t) return
      const dx = t.x - this._swStartX
      let next = this._swStartPos + dx / this.swRange
      if (next < 0) next = 0
      if (next > 1) next = 1
      this.swPos = next
    },
    onSwitchEnd(){
      if(!this.swDragging) return
      this.swDragging = false
      const nextOn = this.swPos >= 0.5
      this.acOn = nextOn
      this.swPos = nextOn ? 1 : 0
      const idx = this.devices.findIndex(d => String(d.id) === String(this.acDeviceId))
      if (idx > -1) {
        this.devices[idx].status = this.acOn
        this.saveRoomDevices()
      }
      // 更新风扇档位（不记录日志，避免重复）
      this.updateFanLevelByAcStatus(false);
    },

    onDialStart(e){ this.dialDragging = true; this._updateTempByTouch(e) },
    onDialMove(e){ if (!this.dialDragging) return; this._updateTempByTouch(e) },
    onDialEnd(){ this.dialDragging = false },

    calcDialRect(){
      return new Promise(resolve => {
        const q = uni.createSelectorQuery().in(this)
        q.select('.ac-dial').boundingClientRect(res => {
          if (res) this._dialRect = { left: res.left, top: res.top, width: res.width, height: res.height }
          resolve(res)
        }).exec()
      })
    },
    _getTouch(e){
      const t = (e.touches && e.touches[0]) || (e.changedTouches && e.changedTouches[0]) || e
      const x = t.clientX != null ? t.clientX : (t.pageX != null ? t.pageX : (t.x || 0))
      const y = t.clientY != null ? t.clientY : (t.pageY != null ? t.pageY : (t.y || 0))
      return { x, y }
    },
    _updateTempByTouch(e){
      if (!this._dialRect) return
      const t = (e.touches && e.touches[0]) || (e.changedTouches && e.changedTouches[0]) || e
      const pageX = t.clientX ?? t.pageX ?? t.x
      const pageY = t.clientY ?? t.pageY ?? t.y

      const { left, top } = this._dialRect
      const cx = left + this.dialCenter
      const cy = top  + this.dialCenter

      let deg = Math.atan2(pageY - cy, pageX - cx) * 180 / Math.PI
      if (deg < 0) deg += 360

      const diffCW  = (deg - this.dialStartDeg + 360) % 360
      const p       = diffCW / 360
      const temp    = this.tempMin + p * (this.tempMax - this.tempMin)

      this.acTemp = Math.round(temp)
    },

    tickLen(i){ return (i-1) % 5 === 0 ? 14 : 8 },
    tickWidth(i){ return (i-1) % 5 === 0 ? 3 : 2 },
    tickColor(i){ return '#DADADA' },

    applyAutoMode(p){
      this.acOn = p > 0
      if      (p <= 22) this.acMode = 'cool'
      else if (p <= 25) this.acMode = 'fan'
      else              this.acMode = 'heat'
    },
    targetPercentByMode(mode){
      if (mode === 'cool') return 22
      if (mode === 'fan')  return 24
      if (mode === 'heat') return 26
      return 0
    },
    setPercent(p){
      const n = Number(p)
      const pct = Number.isFinite(n) ? Math.round(n) : 0
      const cl = Math.max(0, Math.min(100, pct))
      const span = (this.tempMax - this.tempMin) || 1
      this.acTemp = this.tempMin + (cl / 100) * span
    },
    setAcMode(mode){
      console.log('设置空调模式:', mode)
      this.acMode = mode
      this.setPercent(this.targetPercentByMode(mode))
      if (!this.acOn) this.acOn = true
      // 更新风扇档位（不记录日志，避免重复）
      console.log('更新风扇档位')
      this.updateFanLevelByAcStatus(false);
    },
    acToggle(){
      const next = !this.acOn
      this.acOn = next
      if (!next) this.setPercent(0)
      else this.setPercent(this.targetPercentByMode(this.acMode || 'cool') || 22)
      
      // 更新设备状态
      const idx = this.devices.findIndex(d => String(d.id) === String(this.acDeviceId))
      if (idx > -1) {
        this.devices[idx].status = this.acOn
        this.saveRoomDevices()
      }
      
      // 更新风扇档位（不记录日志，避免重复）
      this.updateFanLevelByAcStatus(false);
    },
    // 调整 1%（避免重复定义）
    acInc(){ this.setPercent(this.percent + 1); if (!this.acOn) this.acOn = true },
    acDec(){ this.setPercent(this.percent - 1); if (!this.acOn) this.acOn = true },

    /* ========= 左滑删除：手势处理 ========= */
    closeAllSwipes(exceptId){
      // 合上其它卡片
      this.devices.forEach(d => {
        if (String(d.id) === String(exceptId)) return
        if (d._dx || d._open || d._dragging) {
          this.$set(d, '_dx', 0)
          this.$set(d, '_open', false)
          this.$set(d, '_dragging', false)
        }
      })
    },
    onCardTouchStart(e, device){
      const { x } = this._getTouch(e) || {}
      if (!device) return
      this.closeAllSwipes(device.id)
      this.$set(device, '_dragging', true)
      this.$set(device, '_startX', x || 0)
      this.$set(device, '_startDx', device._dx || 0)
    },
    onCardTouchMove(e, device){
      const t = this._getTouch(e); if(!t || !device) return
      const dx = (device._startDx || 0) + (t.x - (device._startX || 0))
      // 只允许向左，范围 [-swipeMax, 0]
      const clamped = Math.max(-this.swipeMax, Math.min(0, dx))
      this.$set(device, '_dx', clamped)
    },
    onCardTouchEnd(e, device){
      if (!device) return
      const final = (device._dx || 0) <= -(this.swipeMax / 2) ? -this.swipeMax : 0
      this.$set(device, '_dx', final)
      this.$set(device, '_open', final < 0)
      this.$set(device, '_dragging', false)
    },
    deleteDevice(device){
      const idx = this.devices.findIndex(d => String(d.id) === String(device.id))
      if (idx > -1) {
        this.devices.splice(idx, 1)
        this.saveRoomDevices()
        this.updateOnlineCount()
        uni.showToast({ title:'已删除', icon:'none' })
      }
    }
  }
}
</script>

<style scoped>
.page-container {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  overflow: hidden; background-color: #fff;
}
.page{min-height:100vh;background-repeat:no-repeat;position:relative;padding:0;margin:0}
.back-card{position:absolute;top:30px;left:20px;width:50px;height:50px;background:#fff;border-radius:13px;display:flex;align-items:center;justify-content:center;box-shadow:0 2px 8px rgba(0,0,0,.1);z-index:100}
.edit-container{position:absolute;top:30px;right:20px;width:50px;height:50px;background:#000;border-radius:13px;display:flex;align-items:center;justify-content:center;box-shadow:0 2px 8px rgba(0,0,0,.2);z-index:100}
.transparent-container{padding:32px 16px 16px;background:#F1F1F1;height:534px;width:100%;box-sizing:border-box;position:absolute;bottom:0;left:0;background-image:linear-gradient(to top,#F1F1F1 70%,transparent 100%);border-top-left-radius:20px;border-top-right-radius:20px;overflow-y:auto}
.room-info{text-align:center;margin-bottom:24px}
.room-name{font-size:24px;font-weight:bold;color:#333}
.stats-container{display:flex;align-items:center;padding:16px;background:#fff;border-radius:12px;box-shadow:0 2px 8px rgba(0,0,0,.05);margin-bottom:16px;gap:0;flex-wrap:nowrap}
.stat-item{flex:0 0 50%;min-width:0}
.stats-container .stat-item:nth-child(1){text-align:left}
.stats-container .stat-item:nth-child(2){text-align:right}
.stat-label{font-size:14px;color:#888}
.stat-value{font-size:24px;font-weight:bold;color:#333;display:block;margin-top:4px}
.stat-value.online{color:#FF3B7A}

/* 网格 + 滑动容器 */
.devices-grid{
  display:grid;
  grid-template-columns:repeat(2,160px);
  column-gap:10px;
  row-gap:10px;
  padding:0 17px;
  box-sizing:border-box;
  justify-content:start;
}

.device-swipe{
  position:relative;
  width:160px;
  height:120px;            /* ← 与卡片高度一致 */
  border-radius:15px;
  overflow:hidden;         /* 关键：滑开不越界 */
}
.delete-box{
  position:absolute;
  top:0; right:0;
  width:72px;              /* 与 js 里的 swipeMax 对应 */
  height:100%;
  background:#FF3B7A;
  color:#fff;
  font-size:14px;
  font-weight:700;
  display:flex;
  align-items:center;
  justify-content:center;
  z-index:0;
}

.device-card{
  position:relative;
  z-index:1;
  width:160px;
  height:120px;            /* ← 与 device-swipe 一样 */
  background:#fff;
  border-radius:15px;
  box-shadow:0 2px 8px rgba(0,0,0,.06);
  display:flex;
  flex-direction:column;
  align-items:center;
  padding-top:20px;
  overflow:hidden;
}
.card-icon{ width:25px; height:25px; }
.card-name{ margin-top:15px; font-size:14px; font-weight:600; color:#222; }
.card-sub{ margin-top:6px; font-size:12px; color:#8A8A8A; }

.tiny-switch{
  width:37px; height:20px; border-radius:10px;
  background:#D9D9D9; position:relative; margin-top:14px;
  transition:background-color .2s;
}
.tiny-switch.active{ background:#EA1763; }
.tiny-knob{
  width:16px; height:16px; border-radius:50%;
  background:#fff; position:absolute; top:2px; left:2px;
  transition:left .2s;
}
.tiny-switch.active .tiny-knob{ left:19px; }
/* 遮罩 + 面板 */
.ac-mask{ position: fixed; inset: 0; background: rgba(0,0,0,.25); z-index: 999; transition: opacity .22s; }
.ac-panel{ position: fixed; left: 0; right: 0; bottom: 0; height: 60vh; max-height: 560px; background: #fff; border-top-left-radius: 20px; border-top-right-radius: 20px; box-shadow: 0 -8px 24px rgba(0,0,0,.08); padding: 16px 18px 24px; z-index: 1000; }
.ac-topline{ width: 56px; height: 4px; background:#EA1763; border-radius: 4px; margin: 6px auto 10px; }
.ac-header{ display:flex; align-items:center; justify-content:space-between }
.ac-title{ font-size:20px; font-weight:800; color:#222 }
.ac-switch{ width:44px; height:24px; border-radius:12px; background:#D9D9D9; position:relative; transition:.2s; touch-action:none; user-select:none; }
.ac-switch.on{ background:#EA1763 }
.ac-switch-knob{ position:absolute; top:3px; left:3px; width:18px; height:18px; background:#fff; border-radius:50%; transition:left .2s }
.ac-switch.on .ac-switch-knob{ left:23px }

.ac-dial{ position: relative; display:flex; justify-content:center; margin:16px 0 8px }
.ac-temp{ position:relative; font-size:48px; font-weight:800; color:#111 }
.ac-unit{ position:relative; margin-left:6px; font-size:14px; color:#888 }
.dial-center{ position: absolute; left:50%; top:50%; transform: translate(-50%, -50%); display:flex; flex-direction:column; align-items:center; pointer-events:none; z-index:1; }
.dial-percent{ font-size:56px; font-weight:800; color:#111; line-height:1; }
.dial-unit{ margin-top:6px; font-size:12px; color:#888; line-height:1; }

.ac-stepper{ display:flex; justify-content:space-between; margin: 8px 28px 0 }
.ac-round-btn{ width:54px; height:54px; border-radius:18px; background:#000; color:#fff; display:flex; align-items:center; justify-content:center; font-size:24px; font-weight:700 }

.ac-modes{ display:flex; gap:10px; margin:16px 0 8px }
.ac-mode{ flex:1; height:62px; border-radius:16px; background:#F4F4F4; display:flex; align-items:center; justify-content:center; font-size:16px; font-weight:700; color:#333 }
.ac-mode.active{ background:#7A3FF0; color:#fff }

.ac-stats{ display:flex; gap:12px; margin-top:10px }
.ac-stat{ flex:1; height:74px; background:#F6F6F6; border-radius:16px; display:flex; align-items:center; justify-content:center; gap:6px }
.ac-stat-k{ font-size:14px; color:#9a9a9a }
.ac-stat-v{ font-size:22px; font-weight:800; color:#111 }
.ac-stat-s{ font-size:12px; color:#9a9a9a }

/* 灯控面板：更矮一些 */
.light-panel{ height: 20vh; max-height: 420px; }

/* 右上角加号 */
.add-anchor{ position:absolute; top:30px; right:20px; width:45px; height:45px; z-index:1200; }
.add-btn{ width:45px; height:45px; border-radius:13px; background:#000; color:#fff; font-size:22px; line-height:45px; text-align:center; box-shadow:0 2px 8px rgba(0,0,0,.2); }
.add-mask{ position:fixed; inset:0; background:rgba(0,0,0,.25); z-index:1190; }
.add-menu{ position:absolute; top:54px; right:0; width:160px; background:#fff; border-radius:18px; box-shadow:0 12px 36px rgba(0,0,0,.18); overflow:hidden; z-index:1210; padding:8px 0; font-family:"Alibaba","PingFang SC",Arial,sans-serif; font-size:13px; }
.add-item{ padding:12px 16px; color:#000; }
.add-item:active{ background:#F6F6F8; }
</style>
