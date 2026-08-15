<template>
  <view class="page-container">
    <!-- 左上角测试按钮 -->
    <view class="test-btn" @tap="printLocalData">
      <text class="test-text">测试</text>
    </view>
    <!-- 测试获取家庭信息按钮 -->
    <view class="test-btn get-family-btn" @tap="getLatestFamilyData">
      <text class="test-text">获取家庭信息</text>
    </view>
    
    <!-- 清空本地数据按钮 -->
    <view class="test-btn clear-data-btn" @tap="clearLocalData">
      <text class="test-text">清空本地数据</text>
    </view>
  </view>
</template>

<script>
// 导入获取家庭信息API
const { getUserFamily } = require('@/libs/api/family');
export default {
  data() {
    return {};
  },
  methods: {
    /** 获取最新的家庭信息 */
    async getLatestFamilyData() {
    try {
      const userInfo = uni.getStorageSync('userInfo') || {};
      const userId = userInfo.id || userInfo.userId || '';

      if (!userId) {
        console.error('未找到用户ID，无法获取家庭信息');
        uni.showToast({ title: '未找到用户ID', icon: 'none' });
        return;
      }

      uni.showLoading({ title: '获取中...' });
      const familyData = await getUserFamily(userId);
      uni.hideLoading();

      if (!Array.isArray(familyData)) {
        console.error('家庭数据格式不正确:', familyData);
        uni.showToast({ title: '数据格式不正确', icon: 'none' });
        return;
      }

      // ✅ 格式化每个家庭对象，统一结构
      const normalizedHomes = familyData.map(h => ({
        id: String(h.id || h.familyId || Date.now()), // 强制字符串ID
        name: h.name || h.familyName || '未命名家庭',
        address: h.address || '',
        rooms: typeof h.rooms === 'number' ? h.rooms : (h.roomCount || 0),
        roomCount: typeof h.roomCount === 'number' ? h.roomCount : (h.rooms || 0),
        members: typeof h.members === 'number' ? h.members : (h.memberCount || 0),
        createdAt: h.createdAt || Date.now()
      }));

      // ✅ 更新本地存储
      uni.setStorageSync('homes', normalizedHomes);
      console.log('家庭信息已更新:', normalizedHomes);

      if (normalizedHomes.length > 0) {
        uni.showToast({ title: '获取成功，共 ' + normalizedHomes.length + ' 个家庭', icon: 'none' });
      } else {
        uni.showToast({ title: '未加入任何家庭', icon: 'none' });
      }
    } catch (error) {
      uni.hideLoading();
      console.error('获取家庭信息失败:', error);
      uni.showToast({ title: '获取失败', icon: 'none' });
    }
  },

    
    /** 清空本地数据 */
    clearLocalData() {
      try {
        // 显示确认对话框
        uni.showModal({
          title: '确认清空',
          content: '确定要清空所有本地数据吗？此操作不可撤销。',
          success: (res) => {
            if (res.confirm) {
              // 清除本地存储的关键数据
              uni.removeStorageSync('homes');
              uni.removeStorageSync('activeHomeId');
              
              // 清除房间相关数据
              const prefix = 'rooms:';
              const keys = uni.getStorageInfoSync().keys;
              keys.forEach(key => {
                if (key.startsWith(prefix)) {
                  uni.removeStorageSync(key);
                }
              });

              console.log('已清空所有本地数据');
              uni.showToast({ title: '本地数据已清空', icon: 'success' });
            }
          }
        });
      } catch (error) {
        console.error('清空本地数据失败:', error);
        uni.showToast({ title: '操作失败', icon: 'none' });
      }
    },

    /** 点击测试按钮：打印本地存储的关键数据 */
    printLocalData() {
      try {
        // 1. 获取本地存储的各数据（不存在时返回默认值，避免undefined）
        const localUser = uni.getStorageSync('userInfo') || '未存储用户数据（userInfo）';
        const localToken = uni.getStorageSync('token') || '未存储token';
        const localHomes = uni.getStorageSync('homes') || '未存储家庭数据（homes）';
        

        // 2. 格式化打印（带缩进，方便查看）
        console.log('================ 本地存储数据 =================');
        console.log('1. 用户数据（userInfo）：');
        console.log(
          typeof localUser === 'object' 
            ? JSON.stringify(localUser, null, 2)  // 对象/数组格式化
            : localUser                            // 字符串直接打印
        );

        console.log('\n2. 认证Token（token）：');
        console.log(localToken);
        console.log('\n3. 家庭数据（homes）：');
        console.log(
          typeof localHomes === 'object' 
            ? JSON.stringify(localHomes, null, 2) 
            : localHomes
        );
        console.log('===============================================');

        // 可选：给用户页面提示
        uni.showToast({ title: '已打印数据到控制台', icon: 'none', duration: 1500 });
      } catch (error) {
        // 捕获存储读取异常（如存储满、权限问题）
        console.error('读取本地存储失败：', error);
        uni.showToast({ title: '读取数据失败', icon: 'none' });
      }
    }
  }
};
</script>

<style scoped>
/* 页面容器：确保按钮定位相对于页面 */
.page-container {
  position: relative;
  min-height: 100vh; /* 占满屏幕高度，避免按钮超出可视区 */
  background: #fff; /* 可选：设置页面背景色 */
}

/* 测试按钮：左上角小方块 */
.test-btn {
  /* 定位：左上角 */
  position: absolute;
  left: 20px;    /* 距离左侧20px */
  top: 20px;     /* 距离顶部20px（若有导航栏，可调整为60px+） */
  /* 方块样式 */
  width: 60px;   /* 方块宽度 */
  height: 36px;  /* 方块高度 */
  background: #007AFF; /* 蓝色背景（显眼，可自定义） */
  border-radius: 4px;  /* 轻微圆角（可选） */
  /* 文字居中 */
  display: flex;
  align-items: center;
  justify-content: center;
  /* 点击反馈（可选） */
  cursor: pointer;
  transition: background 0.2s; /* 背景色过渡效果 */
}

/* 获取家庭信息按钮样式 */
.get-family-btn {
  left: 90px;  /* 位于第一个按钮右侧 */
  width: 120px; /* 更宽的按钮以容纳文字 */
  background: #00CC66; /* 绿色背景以区分 */
}

/* 清空本地数据按钮样式 */
.clear-data-btn {
  left: 220px;  /* 位于获取家庭信息按钮右侧 */
  width: 120px; /* 更宽的按钮以容纳文字 */
  background: #FF3B30; /* 红色背景以表示警告 */
}

/* 按钮 hover 效果（可选，增强交互） */
.test-btn:active {
  background: #0066CC; /* 点击时加深背景色 */
}

/* 测试文字样式 */
.test-text {
  font-size: 14px;
  color: #fff; /* 白色文字，与蓝色背景对比 */
  font-weight: 500;
}
</style>