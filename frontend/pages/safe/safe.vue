<template>
  <view class="page-container">
  <view class="container">
    <!-- 返回按钮 -->
    <view class="back-card" @click="goZhuye">
      <uni-icons type="arrowleft" size="25" color="#666"></uni-icons>
    </view>

    <!-- 顶部安全监测标题 -->
    <view class="safety-title">安全监测</view>
    
    <!-- 多层圆形容器 -->
    <view class="circle-container">
      <!-- 外层 -->
      <view class="outer-circle"></view>
      <!-- 中层（动态样式：根据浓度切换颜色） -->
      <view 
        class="middle-circle"
        :style="{
          backgroundColor: middleCircleBg,
          borderColor: middleCircleBorder
        }"
      >
        <!-- 内部文字：顶部GAS + 中间煤气值 + 底部状态（正常/预警/超标） -->
        <view class="gas-text">GAS</view>
        <view class="gas-value">
          <text class="value-num">{{ gasValue || '000' }}</text>
          <text class="value-unit">ppm</text>
        </view>
        <view class="gas-desc">{{ statusText }}</view>
      </view>
      <view class="circle-bottom-text">煤气检测</view>
    </view>

    <!-- 分级预警弹窗（350*250） -->
    <uni-popup 
      ref="alertPopup" 
      type="center"
      :mask-click="false"  
    >
      <view class="alert-popup">
        <!-- 75px圆形警告图标（上部居中） -->
        <view class="alert-icon-wrap">
          <image 
            src="/static/警告.png"
            mode="widthFix" 
            class="alert-icon"
            alt="警告图标"
          ></image>
        </view>
        <!-- 24px警告文字 -->
        <view class="alert-text">{{ alertContent }}</view>
        <!-- 确认按钮 -->
        <button class="alert-confirm-btn" @click="$refs.alertPopup.close()">
          知道了
        </button>
      </view>
    </uni-popup>
  </view>
  </view>
</template>

<script>
// 导入煤气数据API
import { getGasData } from '@/libs/api/environmentApi.js';
export default {
  data() {
      return {
        gasValue: '',                     // 显示的 ppm 值（来自主页）
        middleCircleBg: 'rgba(0, 197, 197, 0.7)',
        middleCircleBorder: '#0F94A0',
        statusText: '正常',
        alertContent: '',
        lastAlertLevel: 0,               // 0=无预警，1=预警，2=超标
        _gasListener: null               // 事件句柄
      }
    },
  watch: {
      // ppm 一变就走分级逻辑
      gasValue(newVal) {
        this.updateGasStatus(Number(newVal))
      }
    },
  onLoad() {
    // 实际项目中调用接口获取实时数据（示例）
    this.getGasData();
    // 模拟实时数据更新（每3秒刷新一次，实际项目删除此定时器，用接口轮询/websocket）
    setInterval(() => this.getGasData(), 1000);
  },
  onShow() {
      // 1) 先用缓存值立即渲染一次
      const cached = uni.getStorageSync('latestGasPPM')
      if (cached !== undefined && cached !== '') {
        this.gasValue = String(cached)   // 触发 watch
      }
  
      // 2) 监听主页广播
      this._gasListener = (val) => { this.gasValue = String(val) }
      uni.$on('gas:update', this._gasListener)
    },
  
    onHide() {
      // 解绑监听，防止重复注册
      if (this._gasListener) {
        uni.$off('gas:update', this._gasListener)
        this._gasListener = null
      }
    },
    beforeDestroy() { // vue2
      if (this._gasListener) {
        uni.$off('gas:update', this._gasListener)
        this._gasListener = null
      }
    },

  methods: {
      goZhuye() {
        uni.reLaunch({ url: '/pages/zhuye/zhuye' })
      },

      // 获取煤气数据
      async getGasData() {
        try {
          // 从API获取最新的一条煤气数据
          const gasData = await getGasData(1);
          if (gasData && gasData.length > 0) {
            const latestGas = gasData[0];
            if (latestGas.gas !== undefined) {
              this.gasValue = String(latestGas.gas);
              // 保存到本地存储
              uni.setStorageSync('latestGasPPM', latestGas.gas);
            } else {
              console.log('煤气数据格式不正确');
              // 尝试从本地存储获取
              const cached = uni.getStorageSync('latestGasPPM');
              if (cached !== undefined && cached !== '') {
                this.gasValue = String(cached);
              }
            }
          } else {
            console.log('没有获取到煤气数据');
            // 尝试从本地存储获取
            const cached = uni.getStorageSync('latestGasPPM');
            if (cached !== undefined && cached !== '') {
              this.gasValue = String(cached);
            }
          }
        } catch (error) {
          console.error('获取煤气数据失败:', error);
          // 尝试从本地存储获取
          const cached = uni.getStorageSync('latestGasPPM');
          if (cached !== undefined && cached !== '') {
            this.gasValue = String(cached);
          }
        }
      },

      // —— 分级预警逻辑：保持你的实现 —— 
      updateGasStatus(value) {
        if (isNaN(value) || value === 0) {
          this.resetDefaultStatus()
          return
        }
        if (value >= 600) {
          this.middleCircleBg = 'rgba(234, 23, 99, 0.7)'
          this.middleCircleBorder = '#EA1763'
          this.statusText = '超标'
          this.alertContent = '煤气泄露！请打开门窗通风！并关闭气源！'
          this.openAlertPopup(2)
        } else if (value >= 550) {
          this.middleCircleBg = 'rgba(252, 202, 0, 0.7)'
          this.middleCircleBorder = '#F9A400'
          this.statusText = '预警'
          this.alertContent = '煤气含量达到预警标准！请注意检查！'
          this.openAlertPopup(1)
        } else {
          this.resetDefaultStatus()
        }
      },
  
      resetDefaultStatus() {
        this.middleCircleBg = 'rgba(0, 197, 197, 0.7)'
        this.middleCircleBorder = '#0F94A0'
        this.statusText = '正常'
        this.lastAlertLevel = 0
      },
  
      openAlertPopup(level) {
        if (this.lastAlertLevel !== level) {
          this.$refs.alertPopup && this.$refs.alertPopup.open()
          this.lastAlertLevel = level
        }
      }
    }
}
</script>

<style>
.page-container {
  position: fixed; /* 固定在屏幕，不随滚动移动 */
  top: 0;
  left: 0;
  right: 0;
  bottom: 0; /* 覆盖全屏 */
  overflow: hidden; /* 彻底禁止容器内滚动 */
  background-color: #fff; /* 继承页面背景色 */
}
/* 引入自定义字体 */
@font-face {
  font-family: 'MyFont';
  src: url('/static/font/taibei1.ttf') format('truetype');
  font-weight: normal;
  font-style: normal;
}

/* 全局样式重置与基础设置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.container {
  width: 402px;   
  height: 874px;  
  background-color: #FFFFFF;
  position: relative;
  margin: 0 auto; 
  font-family: 'MyFont'; 
  overflow: hidden; 
}

/* 返回按钮样式 */
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

/* 顶部安全监测标题 */
.safety-title {
  position: absolute;
  top: 22%; 
  left: 50%;
  transform: translateX(-50%); 
  font-size: 34px; 
  color: #333333;
  font-weight: 500;
  letter-spacing: 1px;
}

/* 多层圆形容器 */
.circle-container {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 350px; 
  height: 350px;
  position: relative;
}

/* 外层圆 */
.outer-circle {
  width: 100%;
  height: 100%;
  border-radius: 50%; 
  background-color: #FFFFFF;
  border: 2px solid #E5E5E5; 
  position: relative;
  z-index: 1;
}

/* 中层圆 */
.middle-circle {
  width: 308px;
  height: 308px;
  border-radius: 50%;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 2;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #FFFFFF;
  padding: 20px 0;
  transition: all 0.5s ease; /* 样式变化添加过渡，更流畅 */
  border-width: 5px; 
  border-style: solid;
}

/* 顶部GAS文字 */
.gas-text {
  font-size: 28px;
  margin-bottom: 15px;
  letter-spacing: 2px;
  opacity: 0.9;
}

/* 中间煤气值区域 */
.gas-value {
  display: flex;
  align-items: baseline;
  justify-content: center;
  margin-bottom: 20px;
}

/* 煤气值数字 */
.value-num {
  font-size: 90px;
  font-weight: bold;
  line-height: 1;
}

/* ppm单位 */
.value-unit {
  font-size: 32px;
  margin-left: 8px;
  opacity: 0.8;
}

/* 底部状态文字（正常/预警/超标） */
.gas-desc {
  font-size: 24px;
  opacity: 0.9;
  letter-spacing: 1px;
}

/* 煤气监测文字 */
.circle-bottom-text {
  font-size: 20px;
  color: #333333;
  letter-spacing: 1px;
  opacity: 0.9;
  position: absolute; 
  top: calc(100% + 25px); 
  left: 50%; 
  transform: translateX(-50%);
  white-space: nowrap;
}

/* 弹窗样式（350*250） */
.alert-popup {
  width: 350px;
  height: 250px;
  background-color: #FFFFFF;
  border-radius: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-around;
  padding: 20px;
  box-sizing: border-box;
}

/* 弹窗图标容器（75px圆形） */
.alert-icon-wrap {
  width: 75px;
  height: 75px;
  border-radius: 50%;
  background-color: #FFF0F0; /* 图标背景色，可根据需求调整 */
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 弹窗图标（75px大小） */
.alert-icon {
  width: 45px;
  height: 45px;
}

/* 弹窗文字（24px） */
.alert-text {
  font-size: 24px;
  color: #333333;
  text-align: center;
  line-height: 1.5;
  font-family: 'MyFont';
}

/* 弹窗确认按钮 */
.alert-confirm-btn {
  width: 120px;
  height: 45px;
  background-color: #000000;
  color: #FFFFFF;
  font-size: 20px;
  border-radius: 25px;
  border: none;
  font-family: 'MyFont';
}

/* 清除按钮默认样式 */
.alert-confirm-btn::after {
  border: none;
}
</style>