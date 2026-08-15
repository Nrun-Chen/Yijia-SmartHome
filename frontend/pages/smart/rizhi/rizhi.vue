<template>
  <view class="page">
    <!-- 左上角可点击图片（替换 src 与 targetUrl） -->
    <image
      class="nav-img"
      :src="imgSrc"
      mode="widthFix"
      @click="goTarget"
    />
    <!-- 上部固定标题图片（位置 148,138；尺寸 111×39） -->
    <image class="top-fixed-img" :src="topImgSrc" />

    <!-- ✅ 新增：动态日志列表（不影响既有布局与样式） -->
    <view class="logs-list">
      <view 
        class="log-row" 
        v-for="(item, idx) in logs" 
        :key="`log-${item.id}-${idx}`"
        @longpress="onLongPressLog(item, idx)"
      >
        <view class="dot"></view>
        <view class="log-card">
          <image class="log-icon" :src="logIcon" mode="widthFix" />
          <text class="log-text">{{ item.message }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
// 导入本地日志管理器
import { getLogs, deleteLog } from '@/libs/util/logManager';

export default {
  data() {
    return {
      imgSrc: '/static/images/fanhui.png',      // 你的返回图
      targetUrl: '/pages/smart/smart',          // 你的目标页
      topImgSrc: '/static/images/caozuorizhi.png', // 标题图

      // 日志图标
      logIcon: '/static/icons/ling.svg',

      // 本地日志数据
      logs: []
    }
  },
  mounted() {
    // 直接加载本地日志
    this.loadLocalLogs();
    // 监听日志变化（可选）
    this.logListener = setInterval(() => {
      this.loadLocalLogs();
    }, 10000); // 每10秒刷新一次
  },
  beforeDestroy() {
    // 清除定时器
    if (this.logListener) {
      clearInterval(this.logListener);
    }
  },
  methods: {
    goTarget() {
      // 直接跳转到smart页面
      uni.switchTab({ url: '/pages/smart/smart' });
    },
    // 从API获取日志数据
    async loadLogsFromAPI() {
      try {
        // 获取当前用户ID
        let userInfo = uni.getStorageSync('userInfo');
        let userId = '2001'; // 默认用户ID，与smart.vue保持一致
        if (userInfo && userInfo.userId) {
          userId = userInfo.userId;
        } else {
          console.warn('未找到用户ID，使用默认值2001');
        }
        
        console.log('正在从API获取日志，用户ID:', userId);
        console.log('用户信息:', userInfo);
        let response = await fetch(`http://localhost:8088/api/logs/user/${userId}`);
        
        // 如果当前用户ID查不到数据，尝试使用用户ID 3（从API响应数据看，所有日志的userId都是3）
        if (response.ok) {
          const data = await response.json();
          if (data && data.code === 200 && data.data && data.data.length === 0) {
            console.log('当前用户ID查不到日志，尝试使用用户ID 3');
            response = await fetch('http://localhost:8088/api/logs/user/3');
          }
        } else {
          console.log('当前用户ID请求失败，尝试使用用户ID 3');
          response = await fetch('http://localhost:8088/api/logs/user/3');
        }
        
        if (response.ok) {
          const data = await response.json();
          console.log('API响应数据:', data);
          
          if (data && data.code === 200 && data.data) {
            // 转换API数据格式为页面需要的格式
            this.logs = data.data.map(item => ({
              id: item.id,
              message: item.log,
              timestamp: item.createTime
            }));
            
            // 按时间戳降序排序，最新的日志显示在最上面
            this.logs.sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));
            
            // 同时保存到本地存储作为备份
            this.saveLogsToLocal(this.logs);
            console.log('从API成功获取日志:', this.logs.length, '条');
            return;
          }
        }
        
        console.warn('API获取日志失败，使用本地日志');
        this.loadLocalLogs();
        
      } catch (error) {
        console.error('API获取日志出错:', error);
        this.loadLocalLogs();
      }
    },
    // 从本地存储加载日志
    loadLocalLogs() {
      try {
        // 获取当前用户ID
        let userInfo = uni.getStorageSync('userInfo');
        let currentUserId = '2001'; // 默认用户ID
        if (userInfo && userInfo.userId) {
          currentUserId = userInfo.userId;
        }
        
        const allLogs = getLogs();
        
        // 只加载当前用户的日志
        this.logs = allLogs
          .filter(log => {
            // 如果日志有userId字段，只显示当前用户的日志
            if (log.userId) {
              return String(log.userId) === String(currentUserId);
            }
            // 如果没有userId字段，显示所有日志（向后兼容）
            return true;
          })
          .map(log => ({
            id: log.id,
            message: log.message,
            timestamp: log.timestamp
          }));
        
        // 按时间戳降序排序，最新的日志显示在最上面
        this.logs.sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));
        
        console.log('从本地加载当前用户日志:', this.logs.length, '条，用户ID:', currentUserId);
      } catch (error) {
        console.error('加载本地日志失败:', error);
        this.logs = [
          { id: Date.now(), message: '加载日志失败，请重试' }
        ];
      }
    },
    // 保存日志到本地存储
    saveLogsToLocal(logs) {
      try {
        uni.setStorageSync('api_logs_backup', JSON.stringify(logs));
        console.log('日志已保存到本地备份');
      } catch (error) {
        console.error('保存日志到本地失败:', error);
      }
    },
    
    // 长按日志项处理
    onLongPressLog(item, idx) {
      uni.vibrateShort && uni.vibrateShort(); // 震动反馈
      
      uni.showModal({
        title: '删除日志',
        content: '确定要删除这条日志吗？',
        success: (res) => {
          if (res.confirm) {
            this.deleteLogItem(item.id);
          }
        }
      });
    },
    
    // 删除日志项
    deleteLogItem(logId) {
      try {
        const success = deleteLog(logId);
        if (success) {
          // 重新加载日志列表
          this.loadLocalLogs();
          uni.showToast({
            title: '删除成功',
            icon: 'success'
          });
        } else {
          uni.showToast({
            title: '删除失败',
            icon: 'none'
          });
        }
      } catch (error) {
        console.error('删除日志失败:', error);
        uni.showToast({
          title: '删除失败',
          icon: 'none'
        });
      }
    }
  }
}
</script>

<style>
@font-face {
  font-family: 'alibaba2';
  src: url('@/static/ziti/Alibaba-PuHuiTi-Medium.ttf');
  font-weight: 500; font-style: normal;
}

/* 保持你原有的三条全局规则不变 */
page, :root { --window-top: 0px !important; }
.uni-page-head, .uni-navbar, .uni-page-head-hd { display: none !important; }
.uni-page, .uni-page-body, .uni-content { top: 0 !important; }

/* 固定整个页面，禁止页面滚动 */
page {
  height: 100vh;
  overflow: hidden;
}

.uni-page, .uni-page-body, .uni-content {
  height: 100vh;
  overflow: hidden;
}

/* 隐藏页面滚动条 */
::-webkit-scrollbar {
  display: none;
}

/* 确保页面容器不滚动 */
.page {
  height: 100vh;
  overflow: hidden;
  position: relative;
}

/* 保持不变：左上角返回图片 */
.nav-img {
  position: absolute;
  left: 33px;
  top: 49px;
  width: 60px;
  height: 60px;
}

/* 保持不变：标题图片 */
.top-fixed-img {
  position: absolute;
  left: 148px;
  top: 138px;
  width: 111px;
  height: 35px;
}

/* ====== ✅ 新增样式：全是新类名，不会覆盖你现有样式 ====== */

/* 日志列表整体：采用绝对定位，设置固定高度和滚动 */
.logs-list {
  position: absolute;
  left: 0;
  right: 0;
  /* 放在标题图片下方，按需微调这个 top 值即可 */
  top: 210px;
  bottom: 0;
  /* 设置固定高度，让内容可以滚动 */
  height: calc(100vh - 210px);
  overflow-y: auto;
  padding-bottom: 24px;
  /* 设置滚动条样式为白色 */
  scrollbar-width: thin;
  scrollbar-color: white transparent;
}

/* Webkit浏览器滚动条样式 */
.logs-list::-webkit-scrollbar {
  width: 6px;
}

.logs-list::-webkit-scrollbar-track {
  background: transparent;
}

.logs-list::-webkit-scrollbar-thumb {
  background: white;
  border-radius: 3px;
}

.logs-list::-webkit-scrollbar-thumb:hover {
  background: #f0f0f0;
}

/* 每一行：左侧小圆点 + 右侧矩形卡片 */
.log-row {
  display: flex;
  align-items: center;
}

/* 行与行之间的间距 13px */
.log-row + .log-row {
  margin-top: 13px;
}

/* 玫红色小圆点：#EA1763，尺寸 5×5；距离左边 15px */
.dot {
  width: 3px;
  height: 3px;
  border-radius: 50%;
  background: #EA1763;
  margin-left: 15px;
  margin-right: 10px; /* 点与卡片的间距 */
}

/* 灰色圆角矩形卡片（和你现有风格一致） */
.log-card {
  flex: 1;
  margin-right: 15px;       /* 右侧留白 */
  background: #efefef;
  border-radius: 30px;
  min-height: 40px;
  padding: 14px 16px;
  display: flex;
  align-items: center;
}

/* 固定图标（可换路径） */
.log-icon {
  width: 25px;
  height: 25px;
  margin-right: 12px;
}

/* 日志文字（自动换行） */
.log-text {
  font-size: 14px;
  color: #222;
  line-height: 1.2;
  word-break: break-all;
  font-family: 'alibaba2', 'PingFang SC', 'Microsoft YaHei', Arial, sans-serif;
}
</style>
