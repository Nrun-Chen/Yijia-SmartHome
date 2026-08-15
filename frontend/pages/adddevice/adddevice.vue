<template>
  <view class="page-container">
    <view class="page">
      <!-- 返回 -->
      <view class="btn-box back-btn" @tap="goHome">
        <uni-icons type="back" size="22" color="#101010" />
      </view>

      <!-- 扫码 -->
      <view class="btn-scan" @tap="goScan" aria-label="scan">
        <uni-icons type="scan" size="22" color="#FFFFFF" />
      </view>

      <text class="title-text">添加设备</text>
      <text class="subtitle-text">附近设备自动扫描</text>
      <image class="device-img" src="/static/images/device.png" />

      <text class="manual-text">手动添加设备</text>

      <!-- 卡片网格（直接给当前房间添加） -->
      <view class="card-grid">
        <view class="card" @tap="startAdd('ac')">
          <image class="card-icon" src="/static/icons/kongtiao.svg" />
          <text class="card-text">空调</text>
        </view>
        <view class="card" @tap="startAdd('light')">
          <image class="card-icon" src="/static/icons/deng.svg" />
          <text class="card-text">灯</text>
        </view>
        <view class="card" @tap="startAdd('temp')">
          <image class="card-icon" src="/static/icons/wendu.svg" />
          <text class="card-text">温度传感器</text>
        </view>
        <view class="card" @tap="startAdd('humi')">
          <image class="card-icon" src="/static/icons/shidu.svg" />
          <text class="card-text">湿度传感器</text>
        </view>
        <view class="card" @tap="startAdd('smoke')">
          <image class="card-icon" src="/static/icons/yanwu.svg" />
          <text class="card-text">烟雾报警器</text>
        </view>
        <view class="card" @tap="startAdd('water')">
          <image class="card-icon" src="/static/room/hua.svg" />
          <text class="card-text">智能浇花器</text>
        </view>
      </view>

      <!-- 弹窗 -->
      <view v-if="popup.show" class="popup-mask" @tap="closePopup">
        <!-- 注意：阻止事件冒泡到遮罩 -->
        <view class="popup-box" @tap.stop @click.stop>
          <text class="popup-title">添加设备</text>

          <view>
            <text class="popup-label">设备名称</text>
            <input v-model="popup.name" class="popup-input" :placeholder="popup.placeholder" />

            <view class="popup-btns">
              <button class="btn cancel"
                      hover-class="none"
                      @tap.stop.prevent="closePopup"
                      @click.stop.prevent="closePopup">
                取消
              </button>

              <button class="btn confirm"
                      hover-class="none"
                      :disabled="!canConfirm"
                      @tap.stop.prevent="onConfirmTap"
                      @click.stop.prevent="onConfirmTap">
                确认添加
              </button>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
const PREFIX = 'rooms:'

// UI 元信息
const DEVICE_META = {
  ac:    { label: '空调',       icon: '/static/room/ac.svg',    type: 'aircon' },
  light: { label: '灯',         icon: '/static/room/light.svg', type: 'light'  },
  temp:  { label: '温度传感器', icon: '/static/room/wendu.svg', type: 'sensor' },
  humi:  { label: '湿度传感器', icon: '/static/icons/shidu.svg', type: 'shidu' },
  smoke: { label: '烟雾报警器', icon: '/static/room/yanwu.svg', type: 'yanwu' },
  bell:  { label: '门铃',       icon: '/static/room/ling.svg',  type: 'bell'   },
  water: { label: '智能浇花器', icon: '/static/room/hua1.svg', type: 'hua'  }
}

// 后端绑定码
const LIGHT_CODE_BY_ROOM = { living: 'liv_lit', kitchen: 'kit_lit', bathroom: 'tol_lit' }
const FAN_CODE = 'fan_level'

export default {
  data() {
    return {
      fid: '',
      roomId: '',
      popup: { show: false, type: '', name: '', placeholder: '' }
    }
  },
  computed: {
    canConfirm () {
      return typeof this.popup.name === 'string' && this.popup.name.trim().length > 0
    }
  },
  onLoad (opts) {
    this.fid    = String(opts?.fid || uni.getStorageSync('activeHomeId') || '')
    this.roomId = String(opts?.roomId || opts?.id || '')
    if (!this.fid || !this.roomId) {
      uni.showToast({ title: '缺少房间信息，无法添加设备', icon: 'none' })
      setTimeout(() => this.goHome(), 600)
    }
  },
  methods: {
    goHome(){ uni.reLaunch({ url: '/pages/zhuye/zhuye' }) },
    goScan(){ uni.navigateTo({ url: '/pages/erweima/erweima' }) },
    storageKey(){ return this.fid ? (PREFIX + this.fid) : 'rooms' },
	backToRoom() {
		const url = `/pages/room/room?id=${this.roomId}&fid=${this.fid}`
		const pages = getCurrentPages && getCurrentPages()
		if (pages && pages.length >= 2) {
		  const prev = pages[pages.length - 2]
		  const route = prev && (prev.route || prev.$page?.fullPath || '')
		  // 如果就是从房间页进来的，直接返回
		  if (route && route.indexOf('pages/room/room') !== -1) {
			// 可选：返回前手动刷新一下上一页数据（即使不调，上页 onShow 也会刷新）
			try {
			  if (prev.$vm && typeof prev.$vm.fetchRoomDetails === 'function') {
				prev.$vm.fetchRoomDetails()
			  }
			} catch (e) {}
			uni.navigateBack({ delta: 1 })
			return
		  }
		}
		// 不是从房间页来：直接跳转到该房间
		uni.reLaunch({ url })
	},
    // 进入添加流程
    startAdd(kind){
      if (!this.fid || !this.roomId) { uni.showToast({ title:'缺少房间信息', icon:'none' }); return }
      const meta = DEVICE_META[kind] || DEVICE_META.light
      // 直接替换对象，保证双向绑定
      this.popup = { show: true, type: kind, name: meta.label, placeholder: `例如：${meta.label}` }
    },

    closePopup(){ this.popup.show = false },

    // 点击确认的统一入口（先给一个必达的提示，便于判断是否触发）
    onConfirmTap(){
      if (!this.canConfirm) { uni.showToast({ title:'请输入设备名称', icon:'none' }); return }
      uni.showToast({ title:'正在添加…', icon:'none', duration: 800 })  // 保底提示
      this.confirmAdd()
    },

    // 兜底获取 rooms 与房间下标
    getRoomRecord(){
      const keyA = this.storageKey()
      let key = keyA
      let rooms = uni.getStorageSync(keyA) || []
      const findIdx = arr => (arr || []).findIndex(r => String(r.id) === String(this.roomId))
      let idx = findIdx(rooms)
      // 兜底：有些老数据在全局 'rooms'
      if (idx === -1 && keyA !== 'rooms') {
        const roomsB = uni.getStorageSync('rooms') || []
        const idxB = findIdx(roomsB)
        if (idxB > -1) { key = 'rooms'; rooms = roomsB; idx = idxB }
      }
      return { key, rooms, idx }
    },

    // 真正写入
    confirmAdd() {
        const friendlyName = (this.popup.name || '').trim()
        const { key, rooms, idx } = this.getRoomRecord()
        if (idx === -1) {
          uni.showToast({ title: '房间不存在或参数丢失', icon: 'none' })
          this.popup.show = false
          return
        }
    
        const room = rooms[idx]
        const roomType = room.type || 'default'
    
        let list =
          Array.isArray(room.devicesList) ? room.devicesList :
          Array.isArray(room.deviceList)  ? room.deviceList  :
          Array.isArray(room.devicesArr)  ? room.devicesArr  :
          (Array.isArray(room.devices) && typeof room.devices[0] === 'object' ? room.devices : [])
    
        const meta = DEVICE_META[this.popup.type] || DEVICE_META.light
        let backend = {}
        if (meta.type === 'light') {
          backend = { code: ( { living:'liv_lit', kitchen:'kit_lit', bathroom:'tol_lit' } )[roomType] || '', caps: ['on','off','level'] }
        } else if (meta.type === 'aircon') {
          backend = { fanCode: 'fan_level', caps: ['fanLevel'] }
        } else {
          backend = { code: '', caps: [] }
        }
    
        const newDevice = {
          id: `d-${Date.now()}-${Math.floor(Math.random()*1e6)}`,
          name: friendlyName,
          type: meta.type,
          icon: meta.icon,
          status: false,
          level: 1,
          backend
        }
    
        const next = [...list, newDevice]
        rooms[idx] = { ...room, devicesList: next, devices: next.length, online: next.filter(x => x.status).length }
        uni.setStorageSync(key, rooms)
    
        this.popup.show = false
        uni.showToast({ title: '添加成功', icon: 'none' })
        setTimeout(() => this.backToRoom(), 250)
      }
  }
}
</script>

<style scoped>
.page-container{position:fixed;inset:0;overflow:hidden;background:#fff}
.page{width:100%;height:100%;position:relative;background:#fff;overflow:hidden}
.btn-box{width:45px;height:45px;border-radius:15px;background:#fff;border:1px solid #D9D9D9;display:flex;align-items:center;justify-content:center;position:absolute}
.back-btn{left:38px;top:79px}
.btn-scan{position:absolute;left:333px;top:79px;width:45px;height:45px;border-radius:15px;background:#000;display:flex;align-items:center;justify-content:center}

.title-text{position:absolute;top:150px;left:50%;transform:translateX(-50%);font-family:"Taipei Sans TC";font-size:30px;font-weight:400;color:#000}
.subtitle-text{position:absolute;top:186px;left:50%;transform:translateX(-50%);font-family:"Alibaba";font-size:12px;color:#878787}
.device-img{position:absolute;top:225px;left:50%;transform:translateX(-50%);width:267px;height:267px}
.manual-text{position:absolute;top:520px;left:38px;font-family:"Alibaba";font-size:17px;font-weight:bold;color:#000}

.card-grid{position:absolute;top:560px;left:21px;width:369px;display:grid;grid-template-columns:179px 179px;column-gap:11px;row-gap:20px}
.card{width:179px;height:90px;border-radius:25px;background:#F0F0F0;display:flex;flex-direction:column;align-items:center;justify-content:center}
.card-icon{width:32px;height:32px;margin-bottom:6px}
.card-text{font-family:"Alibaba";font-size:16px;font-weight:bold;color:#000}

/* 弹窗层级明确：mask(1000) / box(1001) */
.popup-mask{position:fixed;inset:0;background:rgba(0,0,0,.45);display:flex;align-items:center;justify-content:center;z-index:1000}
.popup-box{width:620rpx;background:#fff;border-radius:24rpx;padding:40rpx 30rpx;box-sizing:border-box;z-index:1001;position:relative}
.popup-title{display:block;font-size:32rpx;font-weight:700;text-align:center;color:#111;margin-bottom:24rpx}
.popup-label{display:block;font-size:26rpx;color:#666;margin-bottom:16rpx}
.popup-input{width:100%;height:80rpx;border:1px solid #ddd;border-radius:16rpx;padding:0 20rpx;font-size:28rpx;box-sizing:border-box;margin:10rpx 0 24rpx}
.popup-btns{display:flex;justify-content:space-between;margin-top:8rpx}
.btn{flex:1;height:80rpx;display:flex;align-items:center;justify-content:center;font-size:28rpx;font-weight:bold;border-radius:16rpx;margin:0 10rpx;border:0}
.btn.cancel{background:#f0f0f0;color:#333}
.btn.confirm{background:#EA1763;color:#fff}
.btn.confirm:disabled{opacity:.5}
</style>
