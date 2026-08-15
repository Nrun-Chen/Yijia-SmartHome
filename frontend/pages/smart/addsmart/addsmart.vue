<template>
  <!-- 你的模板保持不变 -->
  <view>
    <!-- 文本输入框 -->
    <input v-model="inputText" class="input-box" placeholder="请输入场景名称" />

    <!-- 返回按钮 -->
    <view class="slot-container" @tap="goSlotPage">
      <image class="slot-icon" src="/static/icons/fanhui.png" mode="widthFix" />
    </view>

    <!-- 添加条件 / 任务 -->
    <text class="add-condition-text" @tap="showModal">添加条件</text>
    <text class="add-renwu" @tap="gotoforthmodal">添加任务</text>

    <!-- 条件矩形框 -->
    <view class="condition-box">
      <image v-if="iconstatus === 1" src=" " class="condition-icon2" />
      <image v-if="iconstatus === 2" src="/static/icons/weizhi.png" class="condition-icon2" />
      <image v-if="iconstatus === 3" src="/static/icons/shijian.png" class="condition-icon2" />
      <text class="status-text">{{ statusText }}</text>
      <text class="time-text">{{ timetext2 }}</text>
      <text class="time-text3">{{ timetext3 }}</text>
      <text class="time-text5">{{ timetext5 }}</text>
    </view>

    <!-- 任务矩形框 -->
    <view class="condition-box2">
      <image v-if="iconstatus2 === 1" src=" " class="condition-icon2" />
      <image v-if="iconstatus2 === 2" src="/static/icons/icon51.png" class="condition-icon2" />
      <image v-if="iconstatus2 === 3" src="/static/icons/icon52.png" class="condition-icon2" />
      <image v-if="iconstatus2 === 4" src="/static/icons/icon53.png" class="condition-icon2" />
      <image v-if="iconstatus2 === 5" src="/static/icons/icon54.png" class="condition-icon2" />
      <text class="text51">{{ Text51 }}</text>
    </view>

    <!-- 第二个浮层 -->
    <view v-if="showSecond" class="overlay2" @tap="closeSecondModal">
      <view class="modal2" @tap.stop>
        <view class="line2"></view>
        <view class="black-circle2"></view>
        <text class="modal-title2">位置变更</text>
        <image :src="imageStatus1 === 1 ? '/static/icons/lijia2.png' : '/static/icons/lijia1.png'" class="lijia" @tap="changeImageStatus1" />
        <image :src="imageStatus2 === 1 ? '/static/icons/guijia1.png' : '/static/icons/guijia2.png'" class="guijia" @tap="changeImageStatus2" />
        <image src="/static/icons/queding.png" class="queding" @tap="closeSecondModal2" />
      </view>
    </view>

    <!-- 第一个浮层 -->
    <view v-if="show" class="overlay" @tap="closeModal">
      <view class="modal" @tap.stop>
        <view class="line1"></view>
        <view class="black-circle"></view>
        <text class="modal-title">条件</text>
        <image src="/static/icons/richeng.png" class="image1" @tap="gotothirdmodal" />
        <image src="/static/icons/weizhibianhuan.png" class="image2" @tap="gotoSecondModal" />
      </view>
    </view>

    <!-- 第三个浮层 -->
    <view v-if="showthird" class="overlay3" @tap="closethirdmodal">
      <view class="modal3" @tap.stop>
        <view class="line3"></view>
        <view class="black-circle3"></view>
        <text class="modal-title3">日程安排</text>
        <text class="modal-title4">执行时间</text>
        <image src="/static/icons/queding.png" class="queding2" @tap="closethirdmodal2" />
        <text class="maohao">：</text>

        <view class="rect-group" @tap="toggleTimePicker">
          <text class="rect-text">{{ timeText }}</text>
          <image class="rect-icon" src="/static/icons/jiantou.png" />
        </view>

        <view class="rect-group2" @tap="toggleMinutePicker">
          <text class="rect-text2">{{ timeText11 }}</text>
          <image class="rect-icon2" src="/static/icons/jiantou.png" />
        </view>

        <view v-if="showTimePicker" class="time-picker-container">
          <scroll-view class="scroll-container" scroll-y scroll-with-animation>
            <view class="hour-list">
              <view class="hour-item" v-for="hour in hoursRange" :key="hour" @tap="onTimeSelect(hour)">
                <text class="hour-text">{{ hour }}</text>
              </view>
            </view>
          </scroll-view>
        </view>

        <view v-if="showMinutePicker" class="minute-picker-container">
          <scroll-view class="scroll-container" scroll-y scroll-with-animation>
            <view class="minute-list">
              <view class="minute-item" v-for="minute in minuteRange" :key="minute" @tap="onMinuteSelect(minute)">
                <text class="minute-text">{{ minute }}</text>
              </view>
            </view>
          </scroll-view>
        </view>
      </view>
    </view>

    <!-- 第四个浮层 -->
    <view v-if="showforth" class="overlay4" @tap="closeforthmodal">
      <view class="modal4" @tap.stop>
        <view class="line4"></view>
        <view class="black-circle4"></view>
        <text class="modal-title44">任务</text>
        <image src="/static/icons/yunxingshebei.png" class="image41" @tap="gotosixmodal" />
        <image src="/static/icons/tongzhi.png" class="image42" @tap="gotofifthmodal" />
      </view>
    </view>

    <!-- 第五个浮层 -->
    <view v-if="showfifth" class="overlay5" @tap="closefifthmodal">
      <view class="modal5" @tap.stop>
        <view class="line5"></view>
        <view class="black-circle5"></view>
        <text class="modal-title5">通知</text>
        <image :src="imageStatus51 === 1 ? '/static/icons/modal511.png' : '/static/icons/modal512.png'" class="modal51" @tap="changeImageStatus51" />
        <image :src="imageStatus52 === 1 ? '/static/icons/modal521.png' : '/static/icons/modal522.png'" class="modal52" @tap="changeImageStatus52" />
        <image :src="imageStatus53 === 1 ? '/static/icons/modal531.png' : '/static/icons/modal532.png'" class="modal53" @tap="changeImageStatus53" />
        <image src="/static/icons/queding.png" class="queding5" @tap="closefifthmodal2" />
      </view>
    </view>

    <!-- 第六个浮层：开启设备（空调风速 1/2/3 选择） -->
    <view v-if="showsix" class="overlay6" @tap="closesixmodal">
      <view class="modal6" @tap.stop>
        <view class="line6"></view>
        <view class="black-circle6"></view>
        <text class="modal-title6">开启设备</text>
        <image src="/static/icons/queding.png" class="queding6" @tap="closemodal6" />

        <!-- 可纵向滑动区域：x=19,y=89,w=328,h=249 -->
        <view class="dev-scroll">
          <scroll-view class="dev-scroll-view" scroll-y :show-scrollbar="false">
            <view class="dev-card" v-for="(dev, i) in devices" :key="i">
              <image class="dev-icon" :src="dev.icon" mode="widthFix" />
              <text class="dev-title">{{ dev.name }}</text>

              <!-- 仅空调显示风速 1/2/3 -->
              <view v-if="dev.type === 'ac'" class="ac-speed">
                <text class="ac-chip" :class="{ active: dev.on && dev.speed === 1 }" @tap.stop="setAcSpeed(i, 1)">1</text>
                <text class="ac-chip" :class="{ active: dev.on && dev.speed === 2 }" @tap.stop="setAcSpeed(i, 2)">2</text>
                <text class="ac-chip" :class="{ active: dev.on && dev.speed === 3 }" @tap.stop="setAcSpeed(i, 3)">3</text>
              </view>

              <!-- 右侧开关：仅做选择，不实时控制 -->
              <view class="dev-switch" :class="{ on: dev.on }" @tap.stop="toggleDevice(i)">
                <view class="dev-knob"></view>
              </view>
            </view>
          </scroll-view>
        </view>
      </view>
    </view>

    <!-- 普通文字与装饰 -->
    <text class="text-add-smart">创建智能场景</text>
    <text class="text-add-mingcheng">名称</text>
    <text class="text-add-tiaojian">条件</text>
    <text class="text-add-renwu">任务</text>

    <view class="line"></view>
    <view class="circle"></view>
    <view class="circle1"></view>
    <view class="circle2"></view>
    <view class="rectangle"></view>

    <!-- 提交 -->
    <view class="submit-btn" @tap="goToNextPage">
      <text class="submit-text">确认</text>
    </view>
  </view>
</template>

<script>
export default {
  computed: {
    statusText() {
      if (this.textStatus === 1) return '离家';
      if (this.textStatus === 2) return '归家';
      if (this.textStatus === 3) return '日程安排';
      return '';
    },
    timeText() { return this.selectedTime; },
    timetext2() { return this.textStatus === 3 ? this.selectedTime : ' '; },
    timetext3() { return this.textStatus === 3 ? '：' : ' '; },
    timeText11() { return this.selectedMinute; },
    timetext5() { return this.textStatus === 3 ? this.selectedMinute : ' '; },
    Text51() {
      if (this.iconstatus2 === 3) return ' 震动通知 ';
      if (this.iconstatus2 === 4) return ' 铃声通知 ';
      if (this.iconstatus2 === 5) return ' 静音通知 ';
      if (this.iconstatus2 === 2) return ' 开启设备 ';
      return ' ';
    }
  },

  data() {
    return {
      // ★ 根据登录态替换
      apiBase: 'http://localhost:8088',
      currentUserId: 3,  // 修正：使用正確的userId
      homeId: 1001,  // 修正：使用正確的homeId

      iconstatus2: 1,
      showMinutePicker: false,
      minuteRange: Array.from({ length: 60 }, (v, k) => `${k < 10 ? '0' : ''}${k}`),
      selectedMinute: '',
      showTimePicker: false,
      hoursRange: Array.from({ length: 24 }, (v, k) => `${k < 10 ? '0' : ''}${k}`),
      selectedTime: ' ',
      show: false,
      showSecond: false,
      showsix: false,
      inputText: '',
      imageStatus1: 1,
      imageStatus2: 1,
      iconstatus: 1,
      showthird: false,
      showforth: false,
      textStatus: 0, // 1离家 2归家 3时间
      showfifth: false,
      imageStatus51: 1,
      imageStatus52: 1,
      imageStatus53: 1,

      /* 第六个浮层的设备 */
      devices: [
        { id: 'd1',  icon: '/static/icons/deng.png',       name: '客厅灯',    on: true,  type: 'switch' },
        { id: 'd2',  icon: '/static/icons/deng.png',       name: '厨房灯',    on: false, type: 'switch' },
        { id: 'd3',  icon: '/static/icons/deng.png',       name: '卧室灯',    on: true,  type: 'switch' },
        { id: 'd4',  icon: '/static/icons/xiaoyedeng.png', name: '感应灯',    on: false, type: 'switch' },
        { id: 'ac1', icon: '/static/icons/kongtiao.png',   name: '空调',      on: true,  type: 'ac', speed: 1 },
        { id: 'd5',  icon: '/static/icons/jiaoshui.png',   name: '浇花器',    on: false, type: 'switch' },
      ],

      selectedTasks: []
    };
  },

  methods: {
    /* 通知类型单选 */
    changeImageStatus51() { this.imageStatus51 = this.imageStatus51 === 1 ? 2 : 1; this.imageStatus52 = 1; this.imageStatus53 = 1; },
    changeImageStatus52() { this.imageStatus52 = this.imageStatus52 === 1 ? 2 : 1; this.imageStatus51 = 1; this.imageStatus53 = 1; },
    changeImageStatus53() { this.imageStatus53 = this.imageStatus53 === 1 ? 2 : 1; this.imageStatus51 = 1; this.imageStatus52 = 1; },

    /* 时间选择 */
    toggleMinutePicker() { this.showMinutePicker = !this.showMinutePicker; },
    onMinuteSelect(minute) { this.selectedMinute = `${minute}`; this.showMinutePicker = false; },
    toggleTimePicker() { this.showTimePicker = !this.showTimePicker; },
    onTimeSelect(hour) { this.selectedTime = `${hour}`; this.showTimePicker = !this.showTimePicker; },

    /* 条件相关浮层 */
    showModal() { this.show = true; },
    closeModal() { this.show = false; },

    closeSecondModal() { this.showSecond = false; this.imageStatus2 = 1; this.imageStatus1 = 1; },
    closeSecondModal2() {
      this.showSecond = false; this.iconstatus = 2; this.show = false;
      if (this.imageStatus1 === 2) this.textStatus = 1; // 离家
      else if (this.imageStatus2 === 2) this.textStatus = 2; // 归家
      else this.textStatus = 0;
      this.imageStatus2 = 1; this.imageStatus1 = 1;
    },
    gotoSecondModal() { this.showSecond = true; },
    changeImageStatus1() { this.imageStatus1 = this.imageStatus1 === 1 ? 2 : 1; if (this.imageStatus1 === 2) this.imageStatus2 = 1; },
    changeImageStatus2() { this.imageStatus2 = this.imageStatus2 === 1 ? 2 : 1; if (this.imageStatus2 === 2) this.imageStatus1 = 1; },

    gotothirdmodal() { this.showthird = true; },
    closethirdmodal2() { this.showthird = false; this.show = false; this.iconstatus = 3; this.textStatus = 3; },
    closethirdmodal() { this.showthird = false; },

    /* 任务浮层 */
    gotoforthmodal() { this.showforth = true; },
    closeforthmodal() { this.showforth = false; },
    gotofifthmodal() { this.imageStatus53 = 1; this.imageStatus51 = 1; this.imageStatus52 = 1; this.showfifth = true; },
    closefifthmodal() { this.showfifth = false; },
    closefifthmodal2() {
      this.showfifth = false; this.showforth = false;
      if (this.imageStatus51 === 2) this.iconstatus2 = 3;      // 震动
      else if (this.imageStatus52 === 2) this.iconstatus2 = 4; // 铃声
      else if (this.imageStatus53 === 2) this.iconstatus2 = 5; // 静音
      else this.iconstatus2 = 1;
    },

    /* 第六个浮层：仅做“选择” */
    gotosixmodal() {
      // 打开时清空选择，避免上次残留
      this.devices.forEach(d => { d.on = false; if (d.type === 'ac') d.speed = d.speed || 1; });
      this.showsix = true;
    },
    closesixmodal() { this.showsix = false; },

    toggleDevice(idx) { this.devices[idx].on = !this.devices[idx].on; },

    /* 只有空调开机时才能改风速 */
    setAcSpeed(idx, n) {
      const d = this.devices[idx];
      if (!d || d.type !== 'ac') return;
      if (!d.on) { uni.showToast({ title: '请先开启空调', icon: 'none' }); return; }
      d.speed = n;
    },

    /* 设备选择确认 */
    closemodal6() {
      this.selectedTasks = this.devices
        .filter(d => d.on)
        .map(d => d.type === 'ac'
          ? { id: d.id, deviceName: d.name, type: 'ac', power: 1, fanSpeed: d.speed }
          : { id: d.id, deviceName: d.name, type: 'switch', power: 1 }
        );
      this.iconstatus2 = 2; // 任务概览显示“开启设备”
      this.showsix = false;
      this.showforth = false;
    },

    // ====== 提交到后端 ======
    composePayload() {
      // 条件
      let conditionType = null; // 0=离家 1=归家 2=时间
      let conditionTime = null; // 'HH:mm'
      if (this.textStatus === 1) conditionType = 0;
      if (this.textStatus === 2) conditionType = 1;
      if (this.textStatus === 3) {
        conditionType = 2;
        const h = (this.selectedTime || '').toString().padStart(2, '0');
        const m = (this.selectedMinute || '').toString().padStart(2, '0');
        conditionTime = `${h}:${m}`;
      }

      // 通知
      let notifyType = null; // 'vibrate' | 'ring' | 'silent'
      if (this.iconstatus2 === 3) notifyType = 'vibrate';
      if (this.iconstatus2 === 4) notifyType = 'ring';
      if (this.iconstatus2 === 5) notifyType = 'silent';

      // 设备指令映射
      const cmdMap = {
        d1: { field: 'liv_lit' },
        d2: { field: 'kit_lit' },
        d3: { field: 'tol_lit' },
        d4: { field: 'senser_lit' },
        ac1:{ field: 'fan_level' },
        d5: { field: 'water_pump_level' },
      };

      // 构建所有设备的任务，未选择的设备设置为0
      const allDeviceTasks = [];
      
      // 只有當用戶選擇"開啟設備"時才構建設備任務
      if (this.iconstatus2 === 2) {
        // 遍历所有设备，为每个设备创建任务
        this.devices.forEach(device => {
          const cmd = cmdMap[device.id];
          if (!cmd) return;
          
          let value = 0; // 默认为0（关闭状态）
          
          if (device.on) {
            // 如果设备被选中，根据设备类型设置值
            if (device.type === 'ac') {
              value = Number(device.speed || 1);
            } else {
              value = 1; // 开关设备为1（开启状态）
            }
          }
          
          allDeviceTasks.push({
            deviceId: device.id,
            deviceName: device.name,
            field: cmd.field,
            value: value
          });
        });
      }

      const payload = {
        homeId: this.homeId,
        userId: this.currentUserId,
        name: (this.inputText || '').trim(),
        conditionType,      // 0离家/1归家/2到点
        conditionTime,      // HH:mm（只有到点时有）
        notifyType,         // 可为空
        tasks: allDeviceTasks  // 数组，可为空
      };
      return payload;
    },

    // ★ 修改点：本地持久化为“单条聚合项”，标题用场景名；tasks 全量存着
    persistToLocal(dto) {
      // 1) 通知 → 横向卡片（localScenes）
      if (dto.notifyType) {
        const mapTypeToCN = { vibrate: '震动', ring: '铃声', silent: '静音' };
        const condText = dto.conditionType === 0 ? '离家' : dto.conditionType === 1 ? '归家' : '某个时间';
        const newScene = {
          id: `local-scene-${Date.now()}`,
          name: dto.name,
          type: mapTypeToCN[dto.notifyType] || '震动',
          condition: condText, // 添加條件字段
          enabled: true,  // 修改：默認狀態應該是開啟
          conditionTime: dto.conditionTime // 保存時間信息
        };
        try {
          const list = uni.getStorageSync('localScenes') || [];
          list.unshift(newScene);
          uni.setStorageSync('localScenes', list);
        } catch (_) {}
      }

      // 2) 开启设备 → 纵向列表只添加“一条”聚合项（标题=场景名称），并保留 tasks
      if (this.iconstatus2 === 2 && Array.isArray(dto.tasks) && dto.tasks.length > 0) {
        const condText = dto.conditionType === 0 ? '离家' : dto.conditionType === 1 ? '归家' : '某个时间';
        const newAction = {
          id: `local-act-${Date.now()}`,
          type: `scene_name:${dto.name}`, // 标记：标题即场景名
          condition: condText,
          enabled: true,
          tasks: dto.tasks,              // 保留所有设备开关，提交后端用
          conditionTime: dto.conditionTime // 保存时间信息
        };
        try {
          const list = uni.getStorageSync('localActions') || [];
          list.unshift(newAction);
          uni.setStorageSync('localActions', list);
        } catch (_) {}
      }
    },

    // 真實提交：只保存到本地，不保存到後端數據庫
    async submitToBackend() {
      const dto = this.composePayload();

      // 基本校驗
      if (!dto.name) { uni.showToast({ title: '請輸入場景名稱', icon: 'none' }); return false; }
      if (dto.conditionType === null) { uni.showToast({ title: '請選擇條件', icon: 'none' }); return false; }
      if (dto.conditionType === 2 && !dto.conditionTime) { uni.showToast({ title: '請選擇時間', icon: 'none' }); return false; }
      
      // 檢查是否至少選擇了一種任務類型（通知或設備）
      if (!dto.notifyType && (!dto.tasks || dto.tasks.length === 0)) {
        uni.showToast({ title: '請至少選擇一個任務（通知或設備）', icon: 'none' });
        return false;
      }

      try {
        // 只保存到本地，不保存到後端數據庫
        this.persistToLocal(dto);
        console.log('場景已保存到本地:', dto);
        console.log('場景類型分析:', {
          isNotification: !!dto.notifyType,
          isDeviceControl: !!(dto.tasks && dto.tasks.length > 0),
          notifyType: dto.notifyType,
          taskCount: dto.tasks ? dto.tasks.length : 0
        });

        uni.showToast({ title: '場景已保存到本地', icon: 'success' });
        return true;
      } catch(e) {
        console.error('保存場景失敗:', e);
        uni.showToast({ title: '保存失敗，請重試', icon: 'none' });
        return false;
      }
    },

    // 格式化设备命令 - 修正格式避免语法错误
    formatDeviceCommands(tasks) {
      if (!Array.isArray(tasks) || tasks.length === 0) {
        return '';
      }
      
      // 構建完整的設備命令，確保所有設備都有明確狀態（只包含需要的5個設備）
      const deviceCommands = {
        liv_lit: 0,
        kit_lit: 0,
        tol_lit: 0,
        fan_level: 0,
        water_pump_level: 0
      };
      
      // 根據任務設置設備狀態
      tasks.forEach(task => {
        if (task.field && task.value !== undefined) {
          deviceCommands[task.field] = task.value;
        }
      });
      
      // 格式化為後端要求的字符串格式 - 修正格式
      const commandStrings = Object.entries(deviceCommands)
        .map(([key, value]) => `${key}= ${value}`)
        .join(', ');
      
      console.log('格式化的設備命令:', commandStrings);
      return commandStrings;
    },

    // 格式化开始时间
    formatStartTime(conditionType, conditionTime) {
      // 如果是时间条件
      if (conditionType === 2 && conditionTime) {
        const today = new Date();
        const year = today.getFullYear();
        const month = String(today.getMonth() + 1).padStart(2, '0');
        const day = String(today.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}T${conditionTime}:00`;
      }
      // 默认返回今天的9:56
      const today = new Date();
      const year = today.getFullYear();
      const month = String(today.getMonth() + 1).padStart(2, '0');
      const day = String(today.getDate()).padStart(2, '0');
      return `${year}-${month}-${day}T09:56:00`;
    },

    // 點擊確認
    async goToNextPage() {
      const ok = await this.submitToBackend();
      if (!ok) { uni.showToast({ title: '提交失敗，請稍後重試', icon: 'none' }); return; }
      uni.showToast({ title: '已保存到本地', icon: 'success' });
      uni.switchTab({ url: '/pages/smart/smart' });
    },

    // 返回
    goSlotPage() { uni.switchTab({ url: '/pages/smart/smart' }); },
  }
};
</script>

<style>
/* 你的样式保持不变（此处为原样粘贴） */
@font-face { font-family: 'alibaba2'; src: url('@/static/ziti/Alibaba-PuHuiTi-Medium.ttf'); font-weight: 500; font-style: normal; }
@font-face { font-family: 'alibaba1'; src: url('@/static/ziti/Alibaba1.ttf'); font-weight: 400; font-style: normal; }

/* 返回按钮 */
.slot-container{ position:absolute; left:25px; top:79px; width:57px; height:57px; background-color:transparent; border:2px solid #D9D9D9; box-sizing:border-box; border-radius:15px; z-index:20; }
.slot-icon{ position:absolute; left:-2px; top:-2px; width:60px; height:60px; pointer-events:none; z-index:20; }

/* 标题与装饰 */
.text-add-smart{ font-family:'alibaba2',sans-serif; font-size:30px; color:#000; text-align:left; position:absolute; left:112px; top:155px; direction:ltr; }
.line{ position:absolute; left:87px; top:196px; width:235px; height:2px; background-color:#EA1763; }
.circle{ position:absolute; left:43px; top:230px; width:30px; height:30px; background-color:#000; border-radius:50%; }
.circle1{ position:absolute; left:43px; top:364px; width:30px; height:30px; background-color:#000; border-radius:50%; }
.circle2{ position:absolute; left:43px; top:506px; width:30px; height:30px; background-color:#000; border-radius:50%; }
.text-add-mingcheng{ font-family:'alibaba2',sans-serif; font-size:21px; color:#000; text-align:left; position:absolute; left:82px; top:230px; }
.text-add-tiaojian{ font-family:'alibaba2',sans-serif; font-size:21px; color:#000; text-align:left; position:absolute; left:82px; top:364px; }
.rectangle{ position:absolute; left:45px; top:277px; width:320px; height:62px; background-color:#F0F0F0; border-radius:35px; border:none; }
.input-box{ position:absolute; left:73px; top:274px; width:300px; height:62px; font-size:15px; font-family:'alibaba1',sans-serif; color:#898989; padding:5px; z-index:15; }
.submit-btn{ position:absolute; left:128px; top:729px; width:152px; height:59px; background-color:#EA1763; display:flex; justify-content:center; align-items:center; border-radius:27px; }
.submit-text{ position:absolute; left:50%; top:50%; transform:translate(-50%,-50%); font-size:18px; color:#fff; font-family:'alibaba2',sans-serif; z-index:100; }
.add-condition-text{ position:absolute; left:299px; top:369px; font-family:'alibaba1',sans-serif; font-size:15px; color:#EA1763; text-decoration:underline; cursor:pointer; }

/* 浮层 1 */
.overlay{ position:fixed; top:0; left:0; width:100%; height:100%; background-color:rgba(240,240,240,0.8); z-index:300; }
.modal{ position:absolute; bottom:0; left:20px; width:363px; height:427px; background:#fff; border-top-left-radius:35px; border-top-right-radius:35px; border-bottom-left-radius:0; border-bottom-right-radius:0; }
.line1{ position:absolute; left:129px; top:14px; width:87px; height:2px; background-color:#EA1763; }
.modal-title{ position:absolute; left:72px; top:43px; font-family:'alibaba2',sans-serif; font-size:20px; color:#000; }
.image1{ position:absolute; left:25px; top:91px; width:315px; height:80px; cursor:pointer; }
.image2{ position:absolute; left:25px; top:183px; width:315px; height:80px; cursor:pointer; }
.black-circle{ position:absolute; left:30px; top:41px; width:28px; height:28px; background:#000; border-radius:50%; }

/* 浮层 2/3/4/5/6 通用 */
.overlay2,.overlay3,.overlay4,.overlay5,.overlay6{ position:fixed; top:0; left:0; width:100%; height:100%; background-color:rgba(240,240,240,0.8); z-index:2000; }
.modal2,.modal3,.modal4,.modal5,.modal6{ position:absolute; bottom:0; left:20px; width:363px; height:427px; background:#fff; border-top-left-radius:35px; border-top-right-radius:35px; border-bottom-left-radius:0; border-bottom-right-radius:0; }
.line2,.line3,.line4,.line5,.line6{ position:absolute; left:129px; top:14px; width:87px; height:2px; background-color:#EA1763; }
.black-circle2,.black-circle3,.black-circle4,.black-circle5,.black-circle6{ position:absolute; left:34px; top:41px; width:34px; height:34px; background:#000; border-radius:50%; }
.modal-title2,.modal-title3,.modal-title5,.modal-title6{ position:absolute; left:75px; top:43px; font-family:'alibaba2',sans-serif; font-size:20px; color:#000; }

.lijia{ position:absolute; left:25px; top:91px; width:315px; height:75px; cursor:pointer; }
.guijia{ position:absolute; left:25px; top:183px; width:315px; height:75px; cursor:pointer; }
.queding,.queding2,.queding6{ position:absolute; left:105px; top:341px; width:135px; height:56px; cursor:pointer; }

.condition-box{ position:absolute; left:45px; top:424px; width:320px; height:72px; background:#F0F0F0; border-radius:35px; border:none; }
.condition-icon2{ position:absolute; left:25px; top:21px; width:26px; height:26px; }
.modal-title4{ position:absolute; left:45px; top:82px; font-family:'alibaba2',sans-serif; font-size:15px; color:#000; }
.status-text{ position:absolute; left:60px; top:25px; font-family:'alibaba2',sans-serif; font-size:15px; color:#000; }

.rect-group{ position:absolute; top:122px; left:34px; width:113px; height:64px; background:#f0f0f0; border-radius:20px; display:flex; justify-content:space-between; align-items:center; }
.rect-text{ position:absolute; left:42px; top:22px; font-family:'alibaba2',sans-serif; font-size:15px; color:#333; }
.rect-icon{ position:absolute; left:69px; top:22px; width:24px; height:24px; }
.time-text{ position:absolute; left:147px; top:26px; font-family:'alibaba2',sans-serif; font-size:14px; color:#333; }
.time-text3{ position:absolute; left:165px; top:26px; font-family:'alibaba2',sans-serif; font-size:14px; color:#333; }

.time-picker-container{ position:absolute; left:26px; top:190px; width:120px; height:120px; overflow:hidden; background:#fff; border-radius:25px; border:2px solid #CECECE; }
.scroll-container{ max-height:100%; overflow-y:scroll; }
.hour-list{ display:flex; flex-direction:column; align-items:center; justify-content:center; }
.hour-item{ padding:8px 0; text-align:center; width:100%; border-bottom:1px solid #ccc; }
.hour-text{ font-size:16px; color:#333; }
.maohao{ position:absolute; left:165px; top:140px; font-family:'alibaba2',sans-serif; font-size:17px; color:#333; }

.rect-group2{ position:absolute; top:122px; left:200px; width:113px; height:64px; background:#f0f0f0; border-radius:20px; display:flex; justify-content:space-between; align-items:center; }
.rect-text2{ position:absolute; left:42px; top:22px; font-family:'alibaba2',sans-serif; font-size:15px; color:#333; }
.rect-icon2{ position:absolute; left:69px; top:22px; width:24px; height:24px; }
.minute-picker-container{ position:absolute; top:190px; left:195px; width:120px; height:125px; background:#fff; overflow:hidden; border-radius:25px; border:2px solid #CECECE; }
.minute-list{ display:flex; flex-direction:column; }
.minute-item{ padding:10px; width:100%; text-align:center; border-bottom:1px solid #ccc; }
.show-minute-picker-btn{ font-size:18px; color:#EA1763; cursor:pointer; margin-bottom:10px; }
.time-text5{ position:absolute; left:175px; top:26px; font-family:'alibaba2',sans-serif; font-size:14px; color:#333; }

.text-add-renwu{ font-family:'alibaba2',sans-serif; font-size:21px; color:#000; text-align:left; position:absolute; left:82px; top:507px; }
.add-renwu{ position:absolute; left:299px; top:515px; font-family:'alibaba1',sans-serif; font-size:15px; color:#EA1763; text-decoration:underline; cursor:pointer; }
.modal-title44{ position:absolute; left:78px; top:45px; font-family:'alibaba2',sans-serif; font-size:20px; color:#000; }
.image41{ position:absolute; left:25px; top:91px; width:315px; height:80px; cursor:pointer; }
.image42{ position:absolute; left:25px; top:183px; width:315px; height:80px; cursor:pointer; }
.modal51{ position:absolute; left:25px; top:94px; width:312px; height:74px; cursor:pointer; }
.modal52{ position:absolute; left:25px; top:176px; width:312px; height:74px; cursor:pointer; }
.modal53{ position:absolute; left:25px; top:262px; width:312px; height:74px; cursor:pointer; }
.queding5{ position:absolute; left:113px; top:348px; width:135px; height:56px; cursor:pointer; }

.condition-box2{ position:absolute; left:45px; top:565px; width:320px; height:72px; background:#F0F0F0; border-radius:35px; border:none; }
.text51{ position:absolute; left:65px; top:24px; font-family:'alibaba2',sans-serif; font-size:15px; color:#000; }

/* ===== 第六个浮层：设备列表（可纵向滑动） ===== */
.dev-scroll{ position:absolute; left:19px; top:89px; width:328px; height:249px; overflow:hidden; }
.dev-scroll-view{ width:100%; height:100%; }
.dev-scroll-view::-webkit-scrollbar{ width:0; height:0; display:none; }

/* 单个卡片：312×74、#F0F0F0、圆角25 */
.dev-card{ position:relative; width:312px; height:74px; margin:0 auto 12px; background:#F0F0F0; border-radius:25px; box-sizing:border-box; }

/* 左侧图标 */
.dev-icon{ position:absolute; left:20px; top:25px; width:26px; height:26px; }

/* 中间主标题 */
.dev-title{ position:absolute; left:74px; top:23px; font-family:'alibaba2',sans-serif; font-size:16px; color:#000; }

/* 右侧开关（仅选择，不实时控制） */
.dev-switch{ position:absolute; right:18px; top:20px; width:62px; height:34px; border-radius:17px; background:#D9D9D9; box-sizing:border-box; padding:3px; }
.dev-switch .dev-knob{ width:28px; height:28px; border-radius:50%; background:#fff; transition:transform .2s ease; }
.dev-switch.on{ background:#EA1763; }
.dev-switch.on .dev-knob{ transform:translateX(28px); }

/* 空调风速 1/2/3 选择 */
.ac-speed{ position:absolute; right:92px; top:22px; display:flex; gap:8px; }
.ac-chip{ width:28px; height:28px; line-height:28px; text-align:center; border-radius:14px; background:#E6E6E6; font-family:'alibaba2',sans-serif; font-size:12px; color:#333; }
.ac-chip.active{ background:#EA1763; color:#fff; }
</style>
