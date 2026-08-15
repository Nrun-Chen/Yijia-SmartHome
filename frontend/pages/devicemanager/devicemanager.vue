<template>
  <view class="container">
    <!-- 返回按钮（原样保留） -->
    <view class="back-card" @click="goZhuye">
      <uni-icons type="arrowleft" size="25" color="#666"></uni-icons>
    </view>

    <!-- ===== 房间分组（每个房间一个分组） ===== -->
    <view
      v-for="(grp, gIdx) in groups"
      :key="grp.roomId"
      style="width:100%;display:flex;flex-direction:column;align-items:center;"
    >
      <!-- 分组标题：房间名 + 设备数 -->
      <view class="room-header">
        <text class="room-title">{{ grp.roomName || '未命名房间' }}</text>
        <text class="room-count">（{{ grp.devices.length }}）</text>
      </view>

      <!-- 该房间的设备列表（沿用你的灰色卡片样式） -->
      <view v-for="(item, index) in grp.devices" :key="item.id || index" class="device-wrapper">
        <!-- 操作区 -->
        <view class="card-actions" :style="{ width: actionsW + 'px' }">
          <view class="btn-rename" :style="{ width: actionW + 'px' }" @click.stop="renameDevice(gIdx, index)">重命名</view>
          <view class="btn-delete" :style="{ width: actionW + 'px' }" @click.stop="deleteDevice(gIdx, index)">删除</view>
        </view>

        <!-- 可滑动内容轨道 -->
        <view
          class="card-track"
          :style="{ transform: 'translateX(' + (-item.slideX) + 'px)' }"
          @touchstart="onTouchStart(gIdx, index, $event)"
          @touchmove="onTouchMove(gIdx, index, $event)"
          @touchend="onTouchEnd(gIdx, index)"
          @touchcancel="onTouchEnd(gIdx, index)"
        >
          <view class="device-card">
            <image :src="item.icon" class="device-icon" mode="aspectFit"></image>
            <text class="device-name">{{ item.name }}</text>
			<text class="power-pill" :class="{ on: item.on === true }">
			    {{ item.on ? 'ON' : 'OFF' }}
			  </text>
          </view>
        </view>
      </view>

      <!-- 若该房间暂时没有设备，也保留分组标题（可选空态文案） -->
      <view v-if="grp.devices.length===0" style="width:374px;text-align:left;color:#999;font-size:12px;margin:4px 0 16px;">
        暂无设备
      </view>
    </view>

    <!-- 全局空态 -->
    <view v-if="groups.length===0" style="margin-top:120px;color:#999;">
      暂无房间或设备
    </view>

    <!-- 重命名弹窗（样式保持你的） -->
    <view v-if="renamePopup.show" class="popup-mask">
      <view class="popup-box">
        <text class="popup-title">重命名设备</text>
        <input v-model="renamePopup.value" class="popup-input" placeholder="请输入新名称" />
        <view class="popup-btns">
          <view class="btn cancel" @click="cancelRename">取消</view>
          <view class="btn confirm" @click="confirmRename">确定</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
const PREFIX = 'rooms:'

export default {
  name: "Device_Control",
  data() {
    return {
      fid: '',                 // 当前家庭ID（用于读写 rooms:<fid>）
      groups: [],              // [{ roomId, roomName, devices:[{id,name,icon,slideX}]}]
      // 以下为你原来的交互状态
      actionW: 96,
      startX: 0,
      swiping: false,
      activeG: -1,             // 正在滑动的分组索引
      activeI: -1,             // 正在滑动的设备索引
      renamePopup: { show:false, gIdx:null, index:null, value:"" }
    };
  },

  onLoad(query){
    this.fid = String(query.fid || uni.getStorageSync('activeHomeId') || '')
    this.loadAllGroups()
  
    // ✅ 新增：实时监听别处更新
    uni.$on('rooms-updated', ({ fid }) => {
      if (!fid || String(fid) === String(this.fid)) {
        this.loadAllGroups()
      }
    })
  },
  onUnload(){
    // ✅ 新增：离开页面时移除监听
    uni.$off('rooms-updated')
  },

  methods: {
    // —— 返回按钮（保持你的实现） ——
    goZhuye() { uni.reLaunch({ url: '/pages/zhuye/zhuye' }) },

    // ===== 读取所有房间并组装分组 =====
    loadAllGroups(){
      const key = this.fid ? (PREFIX + this.fid) : 'rooms'
      const rooms = uni.getStorageSync(key) || []
      // 将每个房间映射为一个分组；兼容多种设备字段
      const groups = rooms.map(r => {
        const raw =
          Array.isArray(r.devicesList) ? r.devicesList :
          (Array.isArray(r.deviceList) ? r.deviceList :
          (Array.isArray(r.devicesArr) ? r.devicesArr :
          (Array.isArray(r.devices) && typeof r.devices[0]==='object' ? r.devices : [])))
        return {
          roomId: String(r.id),
          roomName: r.name || r.roomName || '',
          devices: (raw || []).map(d => {
            // 兼容多来源字段：on / power(1|0) / status('on'|'off') / state('ON'|'OFF') / switch(true|false)
            // 兼容多来源字段：status(boolean) / on / power(1|0) / status('on'|'off') / state('ON'|'OFF') / switch(true|false) / statusText('on')
            const on =
              (typeof d.status === 'boolean' ? d.status : undefined) ??
              d.on ??
              (typeof d.power  !== 'undefined' ? Number(d.power) === 1 : undefined) ??
              (typeof d.switch !== 'undefined' ? !!d.switch : undefined) ??
              (typeof d.status === 'string'    ? d.status.toLowerCase() === 'on' : undefined) ??
              (typeof d.state  === 'string'    ? d.state.toUpperCase()  === 'ON' : undefined) ??
              (typeof d.statusText === 'string'? d.statusText.toLowerCase() === 'on' : undefined) ??
              false

          
            return {
              id: d.id ?? (r.id + '-' + (d.name||'') + '-' + Math.random()),
              name: d.name,
              icon: d.icon,
              on: !!on,           // ✅ 带上开关状态
              slideX: 0
            }
          })

        }
      })
      this.groups = groups
    },

    // ===== 将某个分组（某个房间）的设备写回到 rooms 存储 =====
    persistGroup(gIdx){
      const key = this.fid ? (PREFIX + this.fid) : 'rooms'
      const rooms = uni.getStorageSync(key) || []
      const grp = this.groups[gIdx]
      const i = rooms.findIndex(r => String(r.id) === grp.roomId)
      if (i !== -1){
        const list = grp.devices.map(({id,name,icon,on}) => ({id,name,icon,on: !!on}))
    
        rooms[i] = {
          ...rooms[i],
          devicesList: list,           // ✅ 数组保留 on
          devices: list.length         // （如首页只读数量，不受影响）
          // 如需记录 online 数可在这里计算：online: list.filter(x=>x.on).length
        }
        uni.setStorageSync(key, rooms)
      }
      uni.$emit('rooms-updated', { fid: this.fid })
    },

    // ===== 手势滑动（分组 + 索引） =====
    onTouchStart(gIdx, idx, e) {
      const t = e.touches[0];
      this.startX = t.clientX;
      this.swiping = true;
      this.activeG = gIdx;
      this.activeI = idx;
    },
    onTouchMove(gIdx, idx, e) {
      if (!this.swiping) return;
      const t = e.touches[0];
      let delta = this.startX - t.clientX;
      if (delta < 0) delta = 0;
      if (delta > this.actionsW) delta = this.actionsW;
      this.$set(this.groups[gIdx].devices, idx, {
        ...this.groups[gIdx].devices[idx],
        slideX: delta
      });
    },
    onTouchEnd(gIdx, idx) {
      this.swiping = false;
      const opened = this.groups[gIdx].devices[idx].slideX > this.actionsW / 2 ? this.actionsW : 0;
      this.$set(this.groups[gIdx].devices, idx, {
        ...this.groups[gIdx].devices[idx],
        slideX: opened
      });
      this.activeG = -1;
      this.activeI = -1;
    },

    // ===== 操作：重命名 / 删除 =====
    renameDevice(gIdx, idx){
      this.renamePopup.show  = true
      this.renamePopup.gIdx  = gIdx
      this.renamePopup.index = idx
      this.renamePopup.value = this.groups[gIdx].devices[idx].name
    },
    confirmRename(){
      const val = this.renamePopup.value.trim()
      if (val){
        const { gIdx, index } = this.renamePopup
        this.groups[gIdx].devices[index].name = val
        this.persistGroup(gIdx)
        uni.showToast({ title: '修改成功', icon: 'none' })
      }
      this.renamePopup.show = false
    },
    cancelRename(){ this.renamePopup.show = false },

    deleteDevice(gIdx, idx){
      this.groups[gIdx].devices.splice(idx, 1)
      this.persistGroup(gIdx)
      uni.showToast({ title:'设备已删除', icon:'none' })
    }
  },

  computed: {
    actionsW(){ return this.actionW * 2; }
  }
}
</script>

<style>
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
  position: relative;
  overflow: hidden;
  /* 顶部留白，用于避开返回按钮 */
  padding-top: 160px;
  box-sizing: border-box;
  /* 水平居中卡片 */
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

/* 返回按钮 —— 原样 */
.back-card {
  position: fixed;
  left: 37px;
  top: 69px;
  width: 55px;
  height: 55px;
  border-radius: 20px;
  background: #FFFFFF;
  border: 1px solid #D9D9D9;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
}

/* 分组标题（新增，和卡片同宽对齐） */
.room-header {
  width: 374px;
  margin: 0px 0 26px;   /* 原来是 8px 0 6px → 上方 +20px，下方 +20px */
  display: flex;
  align-items: center;

  margin-left: 50px;     /* 整体往右挪 50px */
}
.room-title{
  font-size: 17px;
  color:#000;
  font-weight: 600;
}
.room-count{
  font-size: 12px;
  color:#999;
  margin-left: 6px;
}

/* ============ 设备卡片（374×73，圆角25，#F0F0F0） ============ */
.device-wrapper {
  position: relative;
  width: 350px;
  height: 73px;
  border-radius: 25px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,.08);
  margin-bottom: 10px;
}

/* 操作区固定在右侧 */
.card-actions {
  position: absolute;
  right: 0; top: 0;
  height: 100%;
  display: flex;
  z-index: 1;
}
.btn-rename,
.btn-delete {
  height: 100%;
  display: flex; align-items: center; justify-content: center;
  font-size: 14px; font-weight: bold; color: #fff;
}
.btn-rename { background: #007aff; }
.btn-delete { background: #ff3b30; border-radius: 0 25px 25px 0; }

/* 滑动轨道 */
.card-track {
  position: absolute;
  inset: 0;
  z-index: 2;
  transition: transform .18s ease;
  will-change: transform;
}

/* 内容卡片 */
.device-card {
  width: 100%;
  height: 100%;
  background-color: #F0F0F0;
  border-radius: 25px;
  display: flex;
  align-items: center;
  padding-left: 20px; /* 图标离左边 20px */
  gap: 20px;          /* 图标与文字间距 20px */
}

/* 图标 24×24 */
.device-icon {
  width: 24px;
  height: 24px;
  border-radius: 6px;
}

/* 文字：台北黑体 */
.device-name {
  font-size: 16px;
  color: #333;
  font-family: 'MyFont';
}

/* 弹窗（原样） */
.popup-mask {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 99;
}
.popup-box {
  width: 560rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 40rpx 30rpx;
  box-sizing: border-box;
}
.popup-title {
  font-size: 32rpx;
  font-weight: bold;
  text-align: center;
  margin-bottom: 30rpx;
  color: #333;
  font-family: 'MyFont';
}
.popup-input {
  width: 100%;
  height: 80rpx;
  border: 1px solid #ddd;
  border-radius: 16rpx;
  padding: 0 20rpx;
  font-size: 28rpx;
  margin-bottom: 40rpx;
  box-sizing: border-box;
  font-family: 'MyFont';
}
.popup-btns {
  display: flex;
  justify-content: space-between;
}
.btn {
  flex: 1;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: bold;
  border-radius: 16rpx;
  margin: 0 10rpx;
}
.btn.cancel { background: #f0f0f0; color: #333; }
.btn.confirm { background: #EA1763; color: #fff; }
/* ✅ 开关状态胶囊 */
.power-pill{
  margin-left: auto;             /* 顶到右边 */
  margin-right: 30px;
  padding: 2px 10px;
  font-size: 12px;
  font-weight: 700;
  border-radius: 999px;
  background: #E5E5EA;           /* OFF 灰底 */
  color: #3A3A3C;
}
.power-pill.on{
  background: #EA1763;           /* ON 绿底 */
  color: #fff;
}

</style>
