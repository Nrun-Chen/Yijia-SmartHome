<template> 
  <view class="page-container">
	<!-- 仅当 role === 2 时，整页禁止使用 -->
	<view v-if="blocked" class="forbid-mask" @touchmove.stop @click.stop>
	  <view class="forbid-panel">暂无使用该页面的权利</view>
	</view>
    <view class="container">
      <!-- 自定义导航栏 -->
      <view class="custom-navbar">
        <view class="status-icons"></view>
      </view>
   
      <!-- 左上图片框（可更换） -->
      <image class="user-image" :src="userImage" />
       
      <image
        class="add-card-img"
        src="/static/images/add1111.png"
        mode="widthFix"
        @tap="gotoaddsmart"
      />

      <!-- 页面内其它固定元素 -->
      <view class="zuhe2" @tap="goLogPage">
        <view class="rectangle-1"></view>
        <image src="/static/icons/zhineng4.png" class="zaixian" mode="widthFix" />
        <view class="text5">操作日志</view>
      </view>
      <view class="black-circle"></view>
      <view class="smart-scene-label">智能场景</view>

      <!-- 页面普通文字 -->
      <view class="page-title">智能场景</view>
      <view class="text2">根据一些条件自动运行任务</view>
      
      <!-- 空状态提示 -->
      <view v-if="cards.length === 0 && actions.length === 0">
        <image class="empty-icon" src="/static/icons/gantan.svg" mode="widthFix" />
        <text class="empty-title">还没添加智能场景</text>
        <text class="empty-title2">您还没有添加智能场景，请您点击"添加智能场景"进行添加</text>
      </view>

      <!-- 横向滑动区：卡片列表 -->
      <scroll-view
        class="scroll-area"
        scroll-x
        :scroll-y="false"
        :show-scrollbar="false"
        v-show="cards.length > 0"
      >
        <view class="scroll-content">
          <view
            class="card-item"
            v-for="(card, idx) in cards"
            :key="card.id"
            @longpress="onDeleteScene(card, idx)"
          >
            <view class="card-bg" :style="{ backgroundColor: card.bgColor }"></view>
            <image class="card-icon" :src="card.icon" mode="widthFix" />
            <!-- 背景色匹配的图片开关 -->
            <image
              class="card-icon2"
              :src="switchSrc(card)"
              mode="widthFix"
              @tap.stop="toggleScene(card)"
            />
            <text class="card-title" :style="{ color: card.titleColor }">{{ card.title || receivedText }}</text>
            <text class="card-subtitle" :style="{ color: card.subtitleColor }">1项   任务</text>
          </view>
        </view>
      </scroll-view>

      <!-- 用户名矩形 -->
      <view class="name-pill">
        <text class="name-pill-text">{{ userName }}</text>
      </view>

      <!-- 纵向动作列表（上下滑动） -->
      <scroll-view
        class="actions-list"
        :class="{ 'actions-list-no-cards': cards.length === 0 }"
        scroll-y
        :scroll-x="false"
        :show-scrollbar="false"
        v-show="actions.length > 0"
      >
        <view
          class="action-card"
          v-for="(item, idx) in actions"
          :key="item.id || idx"
          @longpress="onDeleteAction(item, idx)"
        >
          <view class="action-top">
            <image class="icon-left" :src="iconForCondition(item.conditionType)" mode="widthFix" />
            <!-- 固定在右上角的图片开关（已无 icon-arrow 与 icon-right） -->
            <image
              class="action-switch"
              :src="actionSwitchSrc(item)"
              mode="widthFix"
              @tap.stop="toggleAction(item, idx)"
            />
          </view>

          <view class="action-middle">
            <text class="action-title">{{ titleForAction(item.type) }}</text>
            <image class="icon-chevron" src="/static/icons/jiantou.png" mode="widthFix" />
          </view>

          <text class="action-subtitle">1项  任务</text>
        </view>
      </scroll-view>

    </view>

    <!-- 智能场景通知弹窗 -->
    <view v-if="showNotification" class="notification-overlay">
      <view class="notification-modal" @tap.stop>
        <view class="notification-content">
          <!-- 通知类型图标 -->
          <view class="notification-icon-container">
            <image 
              class="notification-icon" 
              :src="getNotificationIcon(currentNotification.notifyType)" 
              mode="widthFix" 
            />
          </view>
          
          <!-- 场景名称 -->
          <text class="notification-title">{{ currentNotification.sceneName }}</text>
          
          <!-- 关闭按钮 -->
          <view class="notification-close" @tap="closeNotification">
            <text class="close-text">确定</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'SmartScene',
  
  data() {
    return {
	  userRole: null,     // 0=房主 1=成员 2=访客
	  blocked: false,
      defaultAvatar: '/static/images/logo.png',
      userImage: '/static/images/logo.png',
      currentUserId: 'USER_ID_PLACEHOLDER',
      userName: (uni.getStorageSync('userInfo') && uni.getStorageSync('userInfo').username) || '用户名',
      
      // MQTT開關狀態
      mqttEnabled: true,
      
      /* 按矩形背景色分别配置开/关图片（横向卡片用） */
      switchImgs: {
        '#EEE7FD': { on: '/static/icons/kaiguan11.png',  off: '/static/icons/kaiguan22.png'  },
        '#FDE7EF': { on: '/static/icons/kaiguan21.png',  off: '/static/icons/kaiguan222.png' },
        '#FDF3E7': { on: '/static/icons/kaiguan31.png',  off: '/static/icons/kaiguan32.png'  },
      },

      /* 纵向动作卡片通用的图片开关（占位） */
      actionSwitchOnImg: '/static/icons/kaiguan.png',
      actionSwitchOffImg: '/static/icons/kaiguan111.png',

      receivedText: '',
      templates: [
        { title: '1',   subtitle: '1项   任务', bgColor: '#EEE7FD', icon: '/static/icons/shake.svg',   icon2: '/static/icons/shenglue.svg', titleColor: '#000000', subtitleColor: '#6E6E6E' },
        { title: '买票', subtitle: '1项   任务', bgColor: '#FDE7EF', icon: '/static/icons/jingyin.svg', icon2: '/static/icons/shenglue.svg', titleColor: '#000000', subtitleColor: '#6E6E6E' },
        { title: '休息时间', subtitle: '1项   任务', bgColor: '#FDF3E7', icon: '/static/icons/ling.svg',  icon2: '/static/icons/shenglue.svg', titleColor: '#000000', subtitleColor: '#6E6E6E' },
      ],

      cards: [],
      nextTplIdx: 0,

      actions: [],

      // 通知相关
      showNotification: false,
      currentNotification: {
        sceneName: '',
        executeTime: '',
        success: false,
        notifyType: 'info' // 'info', 'success', 'error'
      }
    };
  },

  computed: {
    // 移除currentTime computed屬性，因為不再需要顯示時間
  },

  methods: {
	computeRole() {
	      try {
	        const userInfo = uni.getStorageSync('userInfo') || {};
	        const userId = userInfo.userId;
	        const activeHomeId = uni.getStorageSync('activeHomeId');
	        const userHomes = uni.getStorageSync('user_home') || [];
	        const rec = userHomes.find(
	          r => String(r.userId) === String(userId) && String(r.homeId) === String(activeHomeId)
	        );
	        this.userRole = rec ? Number(rec.role) : null;
	        this.blocked = this.userRole === 2; // 访客 => 禁用本页
	      } catch (e) {
	        this.userRole = null;
	        this.blocked = false; // 出错时不拦截
	      }
	    },
    startPolling() {
      // 页面显示时，确保后台计时器在运行
      console.log('智能场景页面显示，后台计时器已在运行');
    },
    
    stopPolling() { 
      // 页面隐藏时，不停止后台计时器，让它继续在后台运行
      console.log('智能场景页面隐藏，后台计时器继续运行');
    },

    gotoaddsmart() { 
      uni.navigateTo({ url: '/pages/smart/addsmart/addsmart' }); 
    },
    goLogPage() { 
      uni.navigateTo({ url: '/pages/smart/rizhi/rizhi' }); 
    },

    // 头像：优先读取本地保存的路径，其次（可选）走后端；都失败则默认头像
    async fetchAvatar() {
      try {
        const local = uni.getStorageSync('localAvatar');
        if (local) { this.userImage = local; return; }
        // API调用逻辑
        this.userImage = this.defaultAvatar;
      } catch (_) {
        this.userImage = this.defaultAvatar;
      }
    },

    changeImage() {
      uni.chooseImage({
        count: 1,
        success: (res) => {
          const path = (res.tempFilePaths && res.tempFilePaths[0]) || (res.tempFiles && res.tempFiles[0] && (res.tempFiles[0].path || res.tempFiles[0].tempFilePath));
          if (path) {
            this.userImage = path;
            try { uni.setStorageSync('localAvatar', path); } catch(_) {}
          }
        }
      });
    },

    /* ===== 场景（横向卡片） ===== */
    async fetchScenes() {
      this.useDefaultScenes();
      try {
        // API调用逻辑
        const backendList = [];
        const localScenes = uni.getStorageSync('localScenes') || [];
        const hidden = uni.getStorageSync('hiddenSceneIds') || [];
        const merged = [ ...localScenes, ...(Array.isArray(backendList) ? backendList : []) ]
          .filter(it => {
            const id = it?.id ?? it?.sceneId;
            return !hidden.includes(id);
          });
        if (Array.isArray(merged) && merged.length > 0) {
          this.cards = merged.map((it, i) => {
            const name = it?.name || it?.sceneName || it?.title || `场景${i+1}`
            const type = it?.type ?? it?.sceneType
            const sty  = this.mapTypeToStyle(type)
            const enabled = (it?.enabled !== undefined) ? !!it.enabled
                             : (it?.active !== undefined) ? !!it.active
                             : (it?.status !== undefined) ? (String(it.status) === '1' || it.status === true)
                             : false
            return {
              id: it?.id ?? it?.sceneId ?? `m-${i}`,
              title: name,
              subtitle: '1项   任务',
              bgColor: sty.bgColor,
              icon: sty.icon,
              icon2: '/static/icons/shenglue.svg',
              titleColor: '#000000',
              subtitleColor: '#6E6E6E',
              enabled,
              conditionTime: it?.conditionTime
            }
          })
        } else {
          this.cards = []
        }
      } catch (e) { /* ignore */ }
    },

    useDefaultScenes() {
      const defs = [
        { name: '早晨起床', type: '震动', conditionTime: '07:00', enabled: true },
        { name: '休息时间', type: '静音', conditionTime: '22:00', enabled: false },
        { name: '安防提醒', type: '铃声', conditionTime: '12:00', enabled: true },
      ]
      this.cards = defs.map((it, i) => {
        const sty = this.mapTypeToStyle(it.type)
        return {
          id: `default-${i+1}`,
          title: it.name,
          subtitle: '1项   任务',
          bgColor: sty.bgColor,
          icon: sty.icon,
          icon2: '/static/icons/shenglue.svg',
          titleColor: '#000000',
          subtitleColor: '#6E6E6E',
          enabled: !!it.enabled,
          conditionTime: it.conditionTime
        }
      })
    },

    mapTypeToStyle(type) {
      const t = typeof type === 'number' ? String(type) : String(type || '').toLowerCase()
      const SHENG = { icon: '/static/icons/lingdang.png',    bgColor: '#FDF3E7' }
      const ZHEN  = { icon: '/static/icons/shake.svg',   bgColor: '#EEE7FD' }
      const JING  = { icon: '/static/icons/jingyin.svg', bgColor: '#FDE7EF' }
      if (t.includes('铃') || t.includes('ring') || t === '0') return SHENG
      if (t.includes('震') || t.includes('vib')  || t === '1') return ZHEN
      if (t.includes('静') || t.includes('sil')  || t === '2') return JING
      return ZHEN
    },

    /* ===== 动作（纵向卡片） ===== */
    async fetchActions() {
      this.useDefaultActions()
      try {
        // API调用逻辑
        const backendList = [];
        const localActions = uni.getStorageSync('localActions') || [];
        const list = [ ...localActions, ...(Array.isArray(backendList) ? backendList : []) ];
        if (Array.isArray(list) && list.length > 0) {
          this.actions = list.map((it, i) => ({
            id: it.id ?? it.actionId ?? `a-${i}`,
            type: it.type,                            // 可能是 'scene_name:场景名' 或 'liv_lit=1'
            condition: it.condition || it.conditionType,
            conditionType: it.conditionType || it.condition,
            conditionTime: it.conditionTime,
            enabled: (it?.enabled !== undefined) ? !!it.enabled
                    : (it?.active !== undefined) ? !!it.active
                    : (it?.status !== undefined) ? (String(it.status) === '1' || it.status === true)
                    : false,
          }))
        } else {
          this.actions = []
        }
      } catch (e) { /* ignore */ }
    },

    useDefaultActions() {
      this.actions = [
        { id: 'd1', type: 'liv_lit=1',   condition: '离家',   enabled: true  },
        { id: 'd2', type: 'kit_lit=1',   condition: '归家',   enabled: false },
        { 
          id: 'd3', 
          type: 'fan_level=2', 
          condition: '某个时间', 
          conditionTime: '08:00', 
          enabled: true,
          tasks: [{ field: 'fan_level', value: 2 }]
        },
      ]
    },

    // ★ 若是本地聚合项 'scene_name:xxx'，主标题只展示场景名
    titleForAction(t) {
      if (!t) return '动作'
      const s = String(t)
      if (s.startsWith('scene_name:')) return s.slice('scene_name:'.length)

      const sl = s.toLowerCase()
      if (sl.startsWith('liv_lit=1')) return '开启客厅灯'
      if (sl.startsWith('kit_lit=1')) return '开启厨房灯'
      if (sl.startsWith('tol_lit=1')) return '开启卧室灯'
      if (sl.startsWith('fan_level=1')) return '开启空调，风速1档'
      if (sl.startsWith('fan_level=2')) return '开启空调，风速2档'
      if (sl.startsWith('fan_level=3')) return '开启空调，风速3档'
      return '动作'
    },
    iconForAction(t) {
      const s = String(t || '').toLowerCase()
      if (s.startsWith('liv_lit=1'))  return '/static/icons/livingroom.svg'
      if (s.startsWith('kit_lit=1'))  return '/static/icons/kitchen.svg'
      if (s.startsWith('tol_lit=1'))  return '/static/icons/bedroom.svg'
      if (s.startsWith('fan_level=1')) return '/static/icons/kongtiao.svg'
      if (s.startsWith('fan_level=2')) return '/static/icons/kongtiao.svg'
      if (s.startsWith('fan_level=3')) return '/static/icons/kongtiao.svg'
      return '/static/icons/zhineng.svg'
    },
    iconForCondition(c) {
      const s = String(c || '')
      if (s.includes('离家')) return '/static/icons/weizhi.png'
      if (s.includes('归家')) return '/static/icons/weizhi.png'
      return '/static/icons/shijian.png'
    },

    onDeleteAction(item, idx) {
      uni.showModal({
        title: '删除该任务？',
        content: '删除后将从数据库与本地列表移除',
        confirmText: '删除',
        confirmColor: '#EA1763',
        success: async ({ confirm }) => {
          if (!confirm) return
          try {
            // API调用逻辑
          } catch (e) { /* 忽略失败 */ }
          this.actions.splice(idx, 1)
          // 同步移除本地聚合项
          try {
            const arr = (uni.getStorageSync('localActions') || []).filter(a => a.id !== item.id);
            uni.setStorageSync('localActions', arr);
          } catch (_) {}
          uni.showToast({ title: '已删除', icon: 'success' })
        }
      })
    },

    /* ★ 新增：横向卡片长按删除场景（本地隐藏/删除） */
    onDeleteScene(card, idx) {
      uni.showModal({
        title: '删除该场景？',
        content: '删除后将从本地列表移除（后端删除待接入）',
        confirmText: '删除',
        confirmColor: '#EA1763',
        success: ({ confirm }) => {
          if (!confirm) return;

          const id = card?.id || '';
          // 1) 若为本地创建的通知场景，直接从 localScenes 删除
          if (String(id).startsWith('local-scene-')) {
            try {
              const ls = uni.getStorageSync('localScenes') || [];
              const after = ls.filter(s => s.id !== id);
              uni.setStorageSync('localScenes', after);
            } catch (_) {}
          } else {
            // 2) 后端场景：加入隐藏列表，后续 fetchScenes 时自动过滤
            try {
              const hidden = uni.getStorageSync('hiddenSceneIds') || [];
              if (!hidden.includes(id)) {
                hidden.push(id);
                uni.setStorageSync('hiddenSceneIds', hidden);
              }
            } catch (_) {}
          }

          // 3) 立即从 UI 移除
          this.cards.splice(idx, 1);
          uni.showToast({ title: '已删除', icon: 'success' });
        }
      });
    },

    /* 场景开关（横向卡片） */
    async toggleScene(card) {
      const prev = !!card.enabled
      card.enabled = !prev // 乐观更新
      try {
        // 檢查是否為本地場景
        if (String(card.id || '').startsWith('local-scene-')) {
          try {
            const arr = uni.getStorageSync('localScenes') || [];
            const i = arr.findIndex(s => s.id === card.id);
            if (i >= 0) { 
              arr[i].enabled = card.enabled; 
              uni.setStorageSync('localScenes', arr); 
              console.log('本地通知場景狀態已更新:', card.name, card.enabled);
            }
          } catch (_) {}
          
          // 记录智能场景开关日志
          try {
            const { logSceneToggle } = await import('@/libs/util/apiLogManager.js');
            await logSceneToggle(card.title || card.name, card.enabled);
          } catch (logError) {
            console.warn('记录智能场景开关日志失败:', logError);
          }
          
          uni.showToast({ title: card.enabled ? '已开启' : '已关闭', icon: 'none' })
          return
        }

        // API调用逻辑
        const success = true; // 模拟成功
        if (success) {
          // 记录智能场景开关日志
          try {
            const { logSceneToggle } = await import('@/libs/util/apiLogManager.js');
            await logSceneToggle(card.title || card.name, card.enabled);
          } catch (logError) {
            console.warn('记录智能场景开关日志失败:', logError);
          }
          
          uni.showToast({ title: card.enabled ? '已开启' : '已关闭', icon: 'none' })
        } else {
          card.enabled = prev // 回滚
          uni.showToast({ title: '操作失败', icon: 'none' })
        }
      } catch (e) {
        card.enabled = prev // 回滚
        uni.showToast({ title: '操作失败', icon: 'none' })
      }
    },

    /* 动作卡片右侧图片开关 */
    actionSwitchSrc(item) {
      return item?.enabled ? this.actionSwitchOnImg : this.actionSwitchOffImg
    },
    // ★ 本地聚合项只本地切换；后端项走原逻辑
    async toggleAction(item, idx) {
      const prev = !!item.enabled
      item.enabled = !prev  // 乐观更新
      try {
        if (String(item.id || '').startsWith('local-act-')) {
          try {
            const arr = uni.getStorageSync('localActions') || [];
            const i = arr.findIndex(a => a.id === item.id);
            if (i >= 0) { arr[i].enabled = item.enabled; uni.setStorageSync('localActions', arr); }
          } catch (_) {}
          
          // 记录动作开关日志
          try {
            const { logSceneToggle } = await import('@/libs/util/apiLogManager.js');
            const actionName = this.titleForAction(item.type);
            await logSceneToggle(actionName, item.enabled);
          } catch (logError) {
            console.warn('记录动作开关日志失败:', logError);
          }
          
          uni.showToast({ title: item.enabled ? '已开启' : '已关闭', icon: 'none' })
          return
        }

        // API调用逻辑
        const success = true; // 模拟成功
        if (success) {
          // 记录动作开关日志
          try {
            const { logSceneToggle } = await import('@/libs/util/apiLogManager.js');
            const actionName = this.titleForAction(item.type);
            await logSceneToggle(actionName, item.enabled);
          } catch (logError) {
            console.warn('记录动作开关日志失败:', logError);
          }
          
          uni.showToast({ title: item.enabled ? '已开启' : '已关闭', icon: 'none' })
        } else {
          item.enabled = prev  // 回滚
          uni.showToast({ title: '操作失败', icon: 'none' })
        }
      } catch (e) {
        item.enabled = prev  // 回滚
        uni.showToast({ title: '操作失败', icon: 'none' })
      }
    },

    /* 横向卡片的开关图片选择（原有） */
    switchSrc(card) {
      const color = String(card?.bgColor || '').trim().toUpperCase()
      const cfg = this.switchImgs[color]
      if (cfg) return card?.enabled ? cfg.on : cfg.off
      return card?.enabled ? this.switchOnImg : this.switchOffImg
    },

    // 测试后台计时器功能（仅用于开发测试）
    testBackgroundTimer() {
      try {
        // 导入后台计时器
        import('@/libs/util/backgroundTimer').then(module => {
          const backgroundTimer = module.default;
          backgroundTimer.testBackgroundTimer();
          
          uni.showToast({
            title: '后台计时器测试完成，请查看控制台',
            icon: 'success',
            duration: 3000
          });
        });
      } catch (error) {
        console.error('测试后台计时器失败:', error);
        uni.showToast({
          title: '测试失败',
          icon: 'none'
        });
      }
    },

    // 測試位置變化功能（僅用於開發測試）
    testLocationChange() {
      try {
        // 導入後台計時器
        import('@/libs/util/backgroundTimer').then(module => {
          const backgroundTimer = module.default;
          const debugInfo = backgroundTimer.debugScenes();
          
          console.log('場景調試信息:', debugInfo);
          
          uni.showToast({
            title: `場景調試完成，請查看控制台`,
            icon: 'success',
            duration: 3000
          });
        });
      } catch (error) {
        console.error('測試位置變化功能失敗:', error);
        uni.showToast({
          title: '測試失敗',
          icon: 'none'
        });
      }
    },

    // 修復場景數據（僅用於開發測試）
    fixScenesData() {
      try {
        // 導入後台計時器
        import('@/libs/util/backgroundTimer').then(module => {
          const backgroundTimer = module.default;
          const hasChanges = backgroundTimer.fixScenesWithoutCondition();
          
          if (hasChanges) {
            uni.showToast({
              title: '場景數據已修復',
              icon: 'success',
              duration: 3000
            });
          } else {
            uni.showToast({
              title: '沒有需要修復的場景',
              icon: 'none',
              duration: 3000
            });
          }
        });
      } catch (error) {
        console.error('修復場景數據失敗:', error);
        uni.showToast({
          title: '修復失敗',
          icon: 'none'
        });
      }
    },

    // 智能修復場景數據（僅用於開發測試）
    async smartFixScenesData() {
      try {
        // 導入後台計時器
        import('@/libs/util/backgroundTimer').then(async module => {
          const backgroundTimer = module.default;
          const hasChanges = await backgroundTimer.smartFixScenesWithoutCondition();
          
          if (hasChanges) {
            uni.showToast({
              title: '場景數據已智能修復',
              icon: 'success',
              duration: 3000
            });
          } else {
            uni.showToast({
              title: '沒有需要修復的場景',
              icon: 'none',
              duration: 3000
            });
          }
        });
      } catch (error) {
        console.error('智能修復場景數據失敗:', error);
        uni.showToast({
          title: '修復失敗',
          icon: 'none'
        });
      }
    },

    // 测试通知功能（仅用于开发测试）
    /*
    testNotification() {
      try {
        // 导入后台计时器
        import('@/libs/util/backgroundTimer').then(module => {
          const backgroundTimer = module.default;
          backgroundTimer.testNotification();
          
          uni.showToast({
            title: '通知测试完成，请查看弹窗',
            icon: 'success',
            duration: 3000
          });
        });
      } catch (error) {
        console.error('测试通知失败:', error);
        uni.showToast({
          title: '测试失败',
          icon: 'none'
        });
      }
    },

    // 测试系统通知功能（仅用于开发测试）
    /*
    testSystemNotification() {
      try {
        // 导入后台计时器
        import('@/libs/util/backgroundTimer').then(module => {
          const backgroundTimer = module.default;
          backgroundTimer.testSystemNotification();
          
          uni.showToast({
            title: '系统通知测试完成，请查看通知',
            icon: 'success',
            duration: 3000
          });
        });
      } catch (error) {
        console.error('测试系统通知失败:', error);
        uni.showToast({
          title: '测试失败',
          icon: 'none'
        });
      }
    },
    */

    // 创建测试场景（仅用于开发测试）
    createTestScene() {
      try {
        // 获取当前时间，并设置为下一分钟
        const now = new Date();
        const nextMinute = new Date(now.getTime() + 60000); // 下一分钟
        const testTime = `${String(nextMinute.getHours()).padStart(2, '0')}:${String(nextMinute.getMinutes()).padStart(2, '0')}`;
        
        const testAction = {
          id: `test-${Date.now()}`,
          type: `scene_name:测试场景 (${testTime})`,
          condition: '某个时间',
          enabled: true,
          tasks: [
            { deviceId: 'd1', deviceName: '客厅灯', field: 'liv_lit', value: 1 },
            { deviceId: 'ac1', deviceName: '空调', field: 'fan_level', value: 2 }
          ],
          conditionTime: testTime
        };
        
        // 保存到本地
        const list = uni.getStorageSync('localActions') || [];
        list.unshift(testAction);
        uni.setStorageSync('localActions', list);
        
        // 创建对应的通知场景
        const testScene = {
          id: `test-scene-${Date.now()}`,
          name: `测试场景 (${testTime})`,
          type: '铃声',
          enabled: true  // 确保测试场景也是开启状态
        };
        
        const sceneList = uni.getStorageSync('localScenes') || [];
        sceneList.unshift(testScene);
        uni.setStorageSync('localScenes', sceneList);
        
        // 刷新列表
        this.fetchActions();
        
        uni.showToast({
          title: `测试场景已创建，将在 ${testTime} 执行`,
          icon: 'success',
          duration: 3000
        });
        
      } catch (error) {
        console.error('创建测试场景失败:', error);
        uni.showToast({
          title: '创建失败',
          icon: 'none'
        });
      }
    },

    // 初始化活跃家庭ID
    initializeHomeId() {
      try {
        let activeHomeId = uni.getStorageSync('activeHomeId');
        if (!activeHomeId) {
          // 如果没有设置活跃家庭ID，设置一个默认值
          activeHomeId = '1001';
          uni.setStorageSync('activeHomeId', activeHomeId);
          console.log('已设置默认活跃家庭ID:', activeHomeId);
        }
        
        // 确保用户信息存在
        let userInfo = uni.getStorageSync('userInfo');
        if (!userInfo) {
          userInfo = { userId: '2001', username: '测试用户' };
          uni.setStorageSync('userInfo', userInfo);
          console.log('已设置默认用户信息:', userInfo);
        }
        
        // 确保家庭数据存在
        let homes = uni.getStorageSync('homes');
        if (!homes || homes.length === 0) {
          homes = [
            { id: '1001', name: '我的家', rooms: 0, roomCount: 0 }
          ];
          uni.setStorageSync('homes', homes);
          console.log('已设置默认家庭数据:', homes);
        }
        
        // 读取MQTT开关状态
        const mqttEnabled = uni.getStorageSync('enableMqtt');
        if (mqttEnabled !== undefined) {
          this.mqttEnabled = mqttEnabled;
          console.log('MQTT状态:', this.mqttEnabled ? '开启' : '关闭');
        }
        
      } catch (error) {
        console.error('初始化家庭ID失败:', error);
      }
    },

    // 通知相关方法
    showNotificationModal(sceneName, executeTime, success, notifyType = 'info') {
      this.currentNotification = { sceneName, executeTime, success, notifyType };
      this.showNotification = true;
      // 移除自动关闭，需要用户手动点击确定
    },
    closeNotification() {
      this.showNotification = false;
      this.currentNotification = {
        sceneName: '',
        executeTime: '',
        success: false,
        notifyType: 'info'
      };
    },
    getNotificationIcon(type) {
      // 根据通知类型返回对应的图标
      const iconMap = {
        'vibrate': '/static/icons/shake.svg',      // 震动图标
        'ring': '/static/icons/ling.svg',         // 响铃图标
        'silent': '/static/icons/jingyin.svg',    // 静音图标
        'success': '/static/icons/queding.png',   // 成功图标
        'error': '/static/icons/warning.svg',     // 错误图标
        'info': '/static/icons/gantan.svg'        // 信息图标
      };
      
      return iconMap[type] || iconMap['info'];
    },
    
    // 启动后台计时器
    async startBackgroundTimer() {
      try {
        const { default: backgroundTimer } = await import('@/libs/util/backgroundTimer');
        if (!backgroundTimer.isRunning) {
          backgroundTimer.start();
          console.log('后台计时器已启动');
        }
      } catch (error) {
        console.error('启动后台计时器失败:', error);
      }
    },
    
    // 停止后台计时器
    async stopBackgroundTimer() {
      try {
        const { default: backgroundTimer } = await import('@/libs/util/backgroundTimer');
        if (backgroundTimer.isRunning) {
          backgroundTimer.stop();
          console.log('后台计时器已停止');
        }
      } catch (error) {
        console.error('停止后台计时器失败:', error);
      }
    }
  },

  onShow() {
    this.startPolling();
    this.fetchAvatar();
    this.fetchScenes();
    this.fetchActions();
    this.initializeHomeId();
    this.startBackgroundTimer();
	this.computeRole();
  },
  onHide() { 
    this.stopPolling(); 
    this.stopBackgroundTimer();
  },
  beforeDestroy() { 
    this.stopPolling(); 
    this.stopBackgroundTimer();
  },
};
</script>

<style>
/* 设置页面滚动条为白色 */
page {
  height: 100vh !important;
  overflow: auto !important;
  scrollbar-width: thin !important;
  scrollbar-color: white white !important;
}
page::-webkit-scrollbar {
  width: 6px !important;
  height: 6px !important;
}
page::-webkit-scrollbar-track {
  background: white !important;
  border: none !important;
}
page::-webkit-scrollbar-thumb {
  background: white !important;
  border-radius: 3px !important;
  border: 1px solid white !important;
}
page::-webkit-scrollbar-thumb:hover {
  background: white !important;
}
page::-webkit-scrollbar-corner {
  background: white !important;
}

/* 设置所有滚动条为白色 */
html, body {
  height: 100vh !important;
  overflow: auto !important;
  scrollbar-width: thin !important;
  scrollbar-color: white white !important;
}
html::-webkit-scrollbar, body::-webkit-scrollbar {
  width: 6px !important;
  height: 6px !important;
}
html::-webkit-scrollbar-track, body::-webkit-scrollbar-track {
  background: white !important;
  border: none !important;
}
html::-webkit-scrollbar-thumb, body::-webkit-scrollbar-thumb {
  background: white !important;
  border-radius: 3px !important;
  border: 1px solid white !important;
}
html::-webkit-scrollbar-thumb:hover, body::-webkit-scrollbar-thumb:hover {
  background: white !important;
}
html::-webkit-scrollbar-corner, body::-webkit-scrollbar-corner {
  background: white !important;
}

/* 固定uni-app容器 */
.uni-page, .uni-page-body, .uni-content {
  height: 100vh !important;
  overflow: auto !important;
  scrollbar-width: thin !important;
  scrollbar-color: white white !important;
}
.uni-page::-webkit-scrollbar, .uni-page-body::-webkit-scrollbar, .uni-content::-webkit-scrollbar {
  width: 6px !important;
  height: 6px !important;
}
.uni-page::-webkit-scrollbar-track, .uni-page-body::-webkit-scrollbar-track, .uni-content::-webkit-scrollbar-track {
  background: white !important;
  border: none !important;
}
.uni-page::-webkit-scrollbar-thumb, .uni-page-body::-webkit-scrollbar-thumb, .uni-content::-webkit-scrollbar-thumb {
  background: white !important;
  border-radius: 3px !important;
  border: 1px solid white !important;
}
.uni-page::-webkit-scrollbar-thumb:hover, .uni-page-body::-webkit-scrollbar-thumb:hover, .uni-content::-webkit-scrollbar-thumb:hover {
  background: white !important;
}
.uni-page::-webkit-scrollbar-corner, .uni-page-body::-webkit-scrollbar-corner, .uni-content::-webkit-scrollbar-corner {
  background: white !important;
}

/* 设置所有滚动条为白色 */
::-webkit-scrollbar {
  width: 6px !important;
  height: 6px !important;
}
::-webkit-scrollbar-track {
  background: white !important;
  border: none !important;
}
::-webkit-scrollbar-thumb {
  background: white !important;
  border-radius: 3px !important;
  border: 1px solid white !important;
}
::-webkit-scrollbar-thumb:hover {
  background: white !important;
}
::-webkit-scrollbar-corner {
  background: white !important;
}

.page-container {
  position: fixed; /* 固定在屏幕，不随滚动移动 */
  top: 0;
  left: 0;
  right: 0;
  bottom: 0; /* 覆盖全屏 */
  overflow: hidden; /* 彻底禁止容器内滚动 */
  background-color: #fff; /* 继承页面背景色 */
}

/* 锁定整页滚动，只允许局部滚动 */
page, .container { height: 100vh; overflow: hidden; }

/* 空状态 */
.empty-icon { position: absolute; top: 490px; left: 158px; width: 75px; }
.empty-title { position: absolute; top: 580px; left: 85px; font-family: 'alibaba2', sans-serif; font-size: 30px; color: #333; }
.empty-title2 { position: absolute; top: 618px; left: 80px; font-family: 'alibaba1', sans-serif; font-size: 11px; color: #808080; }

.user-image { position: absolute; left: 21px; top: 69px; width: 67px; height: 67px; border-radius: 50%; background-color: #f5f5f5; overflow: hidden; }

@font-face { font-family: 'alibaba2'; src: url('@/static/ziti/Alibaba-PuHuiTi-Medium.ttf'); font-weight: 500; font-style: normal; }
@font-face { font-family: 'alibaba1'; src: url('@/static/ziti/Alibaba1.ttf'); font-weight: 400; font-style: normal; }

/* 页面整体容器 */
.container { padding: 20px; position: relative; }

/* 自定义导航栏 */
.custom-navbar { width: 100%; height: 56px; background-color: #ffffff; display: flex; justify-content: space-between; align-items: center; padding: 0 10px; position: fixed; top: 0; left: 0; z-index: 9999; }

/* 固定元素 */
.rectangle-1 { position: absolute; left: 27px; top: 241px; width: 171px; height: 101px; background-color: #EA1763; border-radius: 30px; border: none; }
.black-circle { position: absolute; left: 29px; top: 355px; width: 36px; height: 36px; background-color: #000; border-radius: 50%; }
.smart-scene-label { position: absolute; left: 75px; top: 365px; font-family: 'alibaba2', sans-serif; font-size: 25px; color: #000; text-align: left; }

/* 文案 */
.page-title { position: absolute; left: 142px; top: 167px; font-family: 'alibaba2', sans-serif; font-size: 30px; color: #000; text-align: left; }
.text2 { position: absolute; left: 118px; top: 208px; font-family: 'alibaba1', sans-serif; font-size: 14px; color: #A8A8A8; text-align: left; }

.text5 { position: absolute; left: 80px; top: 306px; font-family: 'alibaba2', sans-serif; font-size: 15px; color: #fff; text-align: left; }

/* 横向滑动区（原样保留——仅在文末添加白色滚动条覆盖样式） */
.scroll-area {
  position: absolute; top: 404px; left: 20px; width: calc(100% - 20px); height: 278rpx; margin-bottom: 40rpx;
  overflow-x: auto; overflow-y: hidden; -webkit-overflow-scrolling: touch;
  scrollbar-width: none; -ms-overflow-style: none;
}
.scroll-area::-webkit-scrollbar { display: none; width: 0; height: 0; }
.scroll-content { height: 100%; display: flex; flex-direction: row; align-items: stretch; }
.card-item { position: relative; flex: 0 0 312rpx; height: 100%; margin-right: 26rpx; overflow: hidden; }
.card-item:first-child { margin-left: 0; }
.card-bg { width: 156px; height: 139px; border-radius: 70rpx; }
.card-icon  { position: absolute; left: 35px; bottom: 92px; width: 23px; }
.card-icon2 { position: absolute; left: 95px; bottom: 93px; width: 35px; }
.card-title { position: absolute; left: 24px; bottom: 41px; font-family: 'alibaba2', sans-serif; font-size: 16px; line-height: 1.1; }
.card-subtitle { position: absolute; left: 33px; bottom: 27px; font-family: 'alibaba1', sans-serif; font-size: 13px; line-height: 1.1; }

/* 用户名矩形 */
.name-pill { position: absolute; left: 128px; top: 74px; width: 147px; height: 56px; background: #F0F0F0; border-radius: 20px; display: flex; align-items: center; justify-content: center; z-index: 1000; }
.name-pill-text { font-family: 'alibaba2', sans-serif; font-size: 16px; line-height: 1; color: #000000; text-align: center; max-width: 90%; overflow: hidden; white-space: nowrap; text-overflow: ellipsis; }

.zaixian{ position: absolute; left: 95px; top: 271px; width: 25px; height: 25px; }

/* 组合容器 */
.zuhe2 {}

/* 纵向动作列表（原样保留——仅在文末添加白色滚动条覆盖样式） */
.actions-list{
  position: absolute;
  left: 3px;
  top: 558px;
  width: 401px;
  height: 316px;
  overflow-y: auto;
  box-sizing: border-box;
  padding-bottom: 80px; /* 调整底部内边距，既不会太大留空，又能确保最后卡片不被遮挡 */

  display: flex;
  flex-direction: column;
  align-items: flex-start;
  
  /* 添加过渡动画效果 */
  transition: top 0.3s ease-in-out;
}

/* 当没有横向卡片时，纵向列表自动向上移动 */
.actions-list-no-cards {
  top: 404px !important; /* 移动到横向滑动区域的位置 */
  height: 470px !important; /* 增加高度以占据更多空间 */
  padding-bottom: 80px !important; /* 保持底部内边距 */
}

/* 单个任务卡片：360×140（保持不变） */
.action-card{
  width: 360px;
  height: 140px;
  margin: 0 auto 15px;
  background: #efefef;
  border-radius: 30px;
  box-sizing: border-box;
  padding: 18px 16px 14px;
  position: relative;  /* 作为右上角开关的定位参考，不影响其它元素 */
}

.action-top{ display: flex; align-items: center; }
.icon-left{ 
  width: 28px; 
  height: 28px;
  position: relative;
  top:12px;
  left: 22px;
  right: auto;
  bottom: auto;
}

/* 右上角图片开关（仅调整自身，不影响其它文字/图标） */
.action-switch{
  position: absolute;
  right: 30px;
  top: 28px;
  width: 50px;
  height: 30px;
}

.action-middle{ margin-top: 14px; display: flex; align-items: center; justify-content: space-between; }
.action-title{ 
  font-family: 'alibaba2', sans-serif; 
  font-size: 18px; color: #000;
  position: relative;
  top:14px;
  left: 16px;
  right: auto;
  bottom: auto;
}
.icon-chevron{ width: 16px; height: 16px; }

.action-subtitle {
  margin-top: 4px;
  font-family: 'alibaba1', sans-serif;
  font-size: 14px;
  color: #6E6E6E;
  position: relative;
  top: 12px;
  left: 25px;
  right: auto;
  bottom: auto;
}

.add-card-img{
  position: absolute;
  left: 208px;            
  top: 241px;
  width: 171px;
  height: 101px;
}

/* ===================== */
/* 仅新增：白色滚动条样式  */
/* ===================== */

/* 横向列表（覆盖早前隐藏滚动条的设置） */
.scroll-area{
  /* IE/旧 Edge */
  -ms-overflow-style: auto;
  /* Firefox */
  scrollbar-width: thin;
  scrollbar-color: #ffffff transparent;
}
.scroll-area::-webkit-scrollbar{
  display: initial;     /* 覆盖原来的 display:none */
  height: 6px;
}
.scroll-area::-webkit-scrollbar-thumb{
  background-color: #ffffff;
  border-radius: 999px;
}
.scroll-area::-webkit-scrollbar-track{
  background: transparent;
}

/* 纵向列表（白色滚动条） */
.actions-list{
  -ms-overflow-style: auto;         /* 覆盖可能的 none */
  scrollbar-width: thin;            /* Firefox */
  scrollbar-color: #ffffff transparent;
}
.actions-list::-webkit-scrollbar{
  display: initial;
  width: 6px;
}
.actions-list::-webkit-scrollbar-thumb{
  background-color: #ffffff;
  border-radius: 999px;
}
.actions-list::-webkit-scrollbar-track{
  background: transparent;
}

/* 通知弹窗样式 */
.notification-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
  animation: fadeIn 0.3s ease-out;
}

.notification-modal {
  width: 280px;
  background: #F0F0F0;
  border-radius: 40px;
  padding: 30px 25px;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  animation: slideIn 0.4s ease-out;
  overflow: hidden;
}

.notification-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
}

.notification-icon-container {
  width: 60px;
  height: 60px;
  margin-bottom: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f5f5f5;
  border-radius: 50%;
  border: 1px solid #e0e0e0;
}

.notification-icon {
  width: 35px;
  height: 35px;
}

.notification-title {
  font-family: 'alibaba2', sans-serif;
  font-size: 20px;
  font-weight: bold;
  color: #333333;
  margin-bottom: 25px;
  text-align: center;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.notification-close {
  width: 100%;
  text-align: center;
  padding: 20px 20px;
  background: linear-gradient(45deg, #EA1763, #d4145a);
  border-radius: 40px;
  color: #fff;
  font-family: 'alibaba2', sans-serif;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  box-shadow: 0 6px 20px rgba(234, 23, 99, 0.5);
  transition: all 0.3s ease;
  border: none;
  outline: none;
}

.notification-close:active {
  transform: scale(0.95);
  box-shadow: 0 2px 8px rgba(234, 23, 99, 0.6);
}

/* 动画效果 */
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideIn {
  from { 
    opacity: 0;
    transform: translateY(-50px) scale(0.9);
  }
  to { 
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(255, 255, 255, 0.7); }
  70% { box-shadow: 0 0 0 10px rgba(255, 255, 255, 0); }
  100% { box-shadow: 0 0 0 0 rgba(255, 255, 255, 0); }
}

.page-container {
  position: fixed; /* 固定在屏幕，不随滚动移动 */
  top: 0;
  left: 0;
  right: 0;
  bottom: 0; /* 覆盖全屏 */
  overflow: hidden; /* 彻底禁止容器内滚动 */
  background-color: #fff; /* 继承页面背景色 */
}
.forbid-mask{
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.45);
  z-index: 99999;              /* 比通知弹窗更高，确保彻底拦截 */
  display: flex;
  align-items: center;
  justify-content: center;
}

.forbid-panel{
  min-width: 220px;
  max-width: 80%;
  padding: 22px 18px;
  background: #fff;
  color: #333;
  font-size: 16px;
  border-radius: 16px;
  text-align: center;
  box-shadow: 0 8px 30px rgba(0,0,0,0.2);
}

</style>
