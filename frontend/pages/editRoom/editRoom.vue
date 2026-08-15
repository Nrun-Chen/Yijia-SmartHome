<template>
  <view class="page">
    <!-- 返回 -->
    <view class="btn-box back-btn" @tap="safeBack">
      <uni-icons type="back" size="22" color="#101010" />
    </view>
    <!-- 保存 -->
    <view class="btn-box save-btn" @tap="saveRoom">
      <uni-icons type="checkmarkempty" size="22" color="#FFFFFF" />
    </view>

    <!-- 标题 -->
    <text class="page-title">房间编辑</text>

    <!-- 字段区 -->
    <view class="fields">
      <!-- 房间类型 -->
      <picker mode="selector" :range="typeOptions" :value="typeIndex" @change="onTypeChange">
        <view class="field">
          <text class="field-label">房间类型</text>
          <text class="field-value">{{ typeLabel }}</text>
          <uni-icons class="field-arrow" type="arrowright" size="17" color="#000000" />
        </view>
      </picker>

      <!-- 房间壁纸 -->
      <view class="field" @tap="chooseWallpaper">
        <text class="field-label">房间壁纸</text>
        <uni-icons class="field-arrow" type="arrowright" size="17" color="#000000" />
      </view>

      <!-- 壁纸预览 -->
      <view class="wallpaper-preview" v-if="form.wallpaper">
        <image :src="form.wallpaper" class="preview-img" mode="aspectFill" />
      </view>
    </view>
  </view>
</template>

<script>
const GLOBAL_ROOMS_PREFIX = 'rooms:'

// 导入房间相关API
const { addRoom } = require('@/libs/api/room');


// 每种房间类型的默认设备（注意：保存时会再补充唯一 id）
const ROOM_DEFAULT_DEVICES = {
  bedroom: [
    { name: '空调',       type:'aircon', icon: '/static/room/ac.svg',     status: false },
    { name: '灯',       type:'light',  icon: '/static/room/yedeng.svg', status: false },
    { name: '烟雾报警器', type:'sensor', icon: '/static/room/yanwu.svg',  status: false }
  ],
  living: [
    { name: '灯',     type:'light',  icon: '/static/room/light.svg',  status: false },
    { name: '空调',       type:'aircon', icon: '/static/room/ac.svg',     status: false },
    { name: '智能浇花器',       type:'water',   icon: '/static/room/water.png',   status: false },
    { name: '烟雾报警器', type:'sensor', icon: '/static/room/yanwu.svg',  status: false }
  ],
  study: [
    { name: '灯',       type:'light',  icon: '/static/room/light.svg',  status: false },
    { name: '空调',       type:'aircon', icon: '/static/room/ac.svg',     status: false },
    { name: '烟雾报警器', type:'sensor', icon: '/static/room/yanwu.svg',  status: false }
  ],
  kitchen: [
    { name: '灯',       type:'light',  icon: '/static/room/light.svg',  status: false },
    { name: '烟雾报警器', type:'sensor', icon: '/static/room/yanwu.svg',  status: false }
  ],
  bathroom: [
    { name: '灯',       type:'light',  icon: '/static/room/yedeng.svg', status: false },
    { name: '排气扇',   type:'fan',    icon: '/static/room/music.svg',  status: false }
  ],
  store: [
    { name: '仓库灯',       type:'light',  icon: '/static/room/light.svg',  status: false },
    { name: '温湿度传感器', type:'sensor', icon: '/static/room/wenshi.svg', status: false }
  ],
  default: [
    { name: '灯', type:'light', icon: '/static/room/light.svg', status: false }
  ]
}

export default {
  data() {
    return {
      fid: '',
      form: {
        id: null,
        wallpaper: '',
        name: '' // 自动 = 类型中文
      },
      typeOptions: ['卧室','厨房','储藏室','卫生间','书房','客厅','其他'],
      typeMap: {
        '卧室':'bedroom','厨房':'kitchen','储藏室':'store',
        '卫生间':'bathroom','书房':'study','客厅':'living','其他':'default'
      },
      typeIndex: 0
    }
  },
  computed: {
    typeLabel(){ return this.typeOptions[this.typeIndex] || '其他' },
    selectedTypeKey(){ return this.typeMap[this.typeLabel] || 'default' }
  },
  onLoad(query){
    this.fid = query?.fid || uni.getStorageSync('activeHomeId') || ''
    this.form.name = this.typeOptions[this.typeIndex]
  },
  onShow(){
    const url = uni.getStorageSync('pickedWallpaper')
    if (url) {
      this.form.wallpaper = url
      uni.removeStorageSync('pickedWallpaper')
    }
  },
  methods:{
    onTypeChange(e){
      const idx = Number(e.detail?.value)
      if (Number.isFinite(idx)) {
        this.typeIndex = idx
        this.form.name = this.typeOptions[idx]
      }
    },
    safeBack(){
      const pages = getCurrentPages && getCurrentPages()
      if (pages && pages.length > 1) uni.navigateBack({ delta: 1 })
      else uni.reLaunch({ url: '/pages/zhuye/zhuye' })
    },
    chooseWallpaper(){
      uni.navigateTo({
        url: '/pages/wallpaper/wallpaper',
        events: { onPick: ({ url }) => { this.form.wallpaper = url } },
        success: (res) => {
          res.eventChannel && res.eventChannel.emit('init', { wallpaper: this.form.wallpaper })
        }
      })
    },

    // 生成全局唯一 id
    uid(prefix='r'){ return `${prefix}_${Date.now()}_${Math.floor(Math.random()*1e6)}` },

    // 房间类型映射到API要求的name格式
    getRoomApiName(typeLabel) {
      const nameMap = {
        '客厅': 'liv',
        '厨房': 'kit',
        '卫生间': 'tol',
        '卧室': 'bed'
      };
      return nameMap[typeLabel] || 'other-' + Math.floor(Math.random() * 1000);
    },

    saveRoom(){
      if (!this.fid) {
        uni.showToast({ title:'请先创建家庭', icon:'none' })
        return
      }
      if (!this.form.wallpaper) return uni.showToast({ title:'请选择壁纸', icon:'none' })

      const type = this.selectedTypeKey
      const roomId = this.form.id || this.uid('room')

      // 生成默认设备 + 唯一 id
      const tpl = ROOM_DEFAULT_DEVICES[type] || ROOM_DEFAULT_DEVICES.default
      const now = Date.now()
      const devicesList = tpl.map((d, i) => ({ id: `${roomId}_${i}_${now}`, ...d }))

      const room = {
        id: roomId,
        name: this.typeLabel,     // 中文名称：卧室/厨房/…
        type,                     // 英文 key：bedroom/…
        cover: this.form.wallpaper,
        pinned: false,
        devicesList,
        devices: devicesList.length,
        online: devicesList.filter(x => !!x.status).length
      }

      // 读取桶，保证数组
      const key = GLOBAL_ROOMS_PREFIX + this.fid
      let list = []
      try { const raw = uni.getStorageSync(key); list = Array.isArray(raw) ? raw : [] } catch(e){ list = [] }

      // 去重后写入（优先按 id，其次按 name）
      const byId   = list.findIndex(r => String(r.id) === String(room.id))
      const byName = list.findIndex(r => r.name === room.name)
      if (byId > -1)      list.splice(byId, 1, { ...list[byId], ...room })
      else if (byName>-1) list.splice(byName, 1, { ...list[byName], ...room })
      else                list.push(room)

      try { uni.setStorageSync(key, list) } catch(e){}

      // 给主页的 onShow 合并使用
      try { uni.setStorageSync('newRoom', room) } catch(e){}

      // 调用新增房间API
      const userId = uni.getStorageSync('userInfo')?.id || '4'; // 默认userId为4
      const homeId = this.fid;
      const apiName = this.getRoomApiName(this.typeLabel);
      const roomData = {
        name: apiName,
        homeId: homeId,
        description: this.typeLabel
      };

      addRoom(userId, roomData)
        .then(() => {
          uni.showToast({ title:'房间创建成功', icon:'success' });
          this.safeBack();
        })
        .catch(error => {
          console.error('房间创建失败:', error);
          uni.showToast({ title:'房间创建失败，请重试', icon:'none' });
        });
    }
  }
}
</script>

<style scoped>
.page{ background:#fff; min-height:100vh; position:relative; }

/* 顶部按钮 */
.btn-box{
  width:45px; height:45px; border-radius:15px;
  background:#FFFFFF; border:1px solid #D9D9D9;
  display:flex; align-items:center; justify-content:center;
  position:absolute; z-index:1000; cursor:pointer;
}
.back-btn{ left:38px; top:79px; }
.save-btn{ right:38px; top:79px; background:#EA1763; border:none; }

/* 标题 */
.page-title{
  position:absolute; left:45px; top:165px;
  font-family:'Taipei Sans TC',sans-serif; font-size:27px; font-weight:500; color:#000;
  z-index:10;
}

/* 字段区 */
.fields{
  position:absolute; left:0; right:0; top:230px;
  padding:0 28px;
}

/* 字段块 */
.field{
  height:60px; border-radius:15px; background:#F0F0F0;
  display:flex; align-items:center; padding:0 12px;
  position:relative; margin-bottom:18px;
}
.field-label{ font-size:15px; color:#000; }

/* 值靠右对齐，紧挨箭头 */
.field-value{
  position:absolute;
  right:34px; /* 箭头右边距12 + 箭头宽17 + 间隔5 */
  top:50%; transform:translateY(-50%);
  font-size:14px; color:#000; opacity:.9;
  white-space:nowrap; overflow:hidden; text-overflow:ellipsis;
}
.field-arrow{ position:absolute; right:12px; top:50%; transform:translateY(-50%); }

/* 壁纸预览 */
.wallpaper-preview{
  width:100%; height:144px; border-radius:20px; overflow:hidden;
  background:#EDEDED; margin-top:10px;
}
.preview-img{ width:100%; height:100%; display:block; }
</style>
