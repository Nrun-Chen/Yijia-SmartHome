/**
 * 全局後台計時器工具
 * 用於在整個應用運行期間檢查智能場景
 */

class BackgroundTimer {
  constructor() {
    this.timeCheckInterval = null;
    this.isRunning = false;
    this.lastCheckTime = null;
    this.lastHumanStatus = null;
    this.locationSceneExecuted = new Set();
    this.humanStatusCount = 0;
    this.targetHumanStatus = null;
    this.locationCheckInterval = null;
  }

  start() {
    if (this.isRunning) {
      console.log('後台計時器已在運行中');
      return;
    }

    console.log('啟動後台計時器');
    this.isRunning = true;

    // 立即執行一次檢查
    this.checkTimeBasedScenes();

    // 每秒檢查時間觸發的場景
    this.timeCheckInterval = setInterval(() => {
      this.checkTimeBasedScenes();
    }, 1000);

    // 每10秒檢查位置觸發的場景
    this.locationCheckInterval = setInterval(() => {
      this.checkLocationBasedScenes();
    }, 10000);
    
    console.log('後台計時器已啟動，時間檢查間隔: 1秒，位置檢查間隔: 10秒');
  }

  stop() {
    if (!this.isRunning) {
      console.log('後台計時器未在運行');
      return;
    }

    console.log('停止後台計時器');
    this.isRunning = false;

    if (this.timeCheckInterval) {
      clearInterval(this.timeCheckInterval);
      this.timeCheckInterval = null;
    }

    if (this.locationCheckInterval) {
      clearInterval(this.locationCheckInterval);
      this.locationCheckInterval = null;
    }
  }

  async checkTimeBasedScenes() {
    try {
      const now = new Date();
      const currentTime = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;
      
      if (this.lastCheckTime === currentTime) {
        return;
      }
      
      this.lastCheckTime = currentTime;
      console.log('後台計時器檢查時間點:', currentTime);
      
      const localActions = uni.getStorageSync('localActions') || [];
      const localScenes = uni.getStorageSync('localScenes') || [];
      const enabledActions = localActions.filter(action => action.enabled);
      const enabledScenes = localScenes.filter(scene => scene.enabled);
      
      console.log('啟用的設備場景數量:', enabledActions.length);
      console.log('啟用的通知場景數量:', enabledScenes.length);
      
      await this.checkTimeBasedScenesLogic(enabledActions, enabledScenes, currentTime);
      
    } catch (error) {
      console.error('後台計時器檢查場景時出錯:', error);
    }
  }

  async checkLocationBasedScenes() {
    try {
      console.log('=== 開始檢查位置變化場景 ===');
      
      const localActions = uni.getStorageSync('localActions') || [];
      const localScenes = uni.getStorageSync('localScenes') || [];
      const enabledActions = localActions.filter(action => action.enabled);
      const enabledScenes = localScenes.filter(scene => scene.enabled);
      
      console.log('所有啟用的設備場景:', enabledActions);
      console.log('所有啟用的通知場景:', enabledScenes);
      
      // 自動修復缺少condition字段的場景
      let hasFixedScenes = false;
      const currentHumanStatus = await this.getHumanStatus();
      console.log('當前人員狀態:', currentHumanStatus);
      
      for (let i = 0; i < localScenes.length; i++) {
        const scene = localScenes[i];
        if (!scene.condition && scene.enabled) {
          console.log('自動修復場景:', scene.name);
          // 根據當前人員狀態設置條件
          if (currentHumanStatus === 0) {
            scene.condition = '离家';
            console.log('設置為离家條件（當前無人）');
          } else {
            scene.condition = '归家';
            console.log('設置為归家條件（當前有人）');
          }
          hasFixedScenes = true;
        }
      }
      
      if (hasFixedScenes) {
        uni.setStorageSync('localScenes', localScenes);
        console.log('場景數據已自動修復並保存');
        // 重新獲取修復後的場景
        const updatedLocalScenes = uni.getStorageSync('localScenes') || [];
        const updatedEnabledScenes = updatedLocalScenes.filter(scene => scene.enabled);
        enabledScenes.length = 0;
        enabledScenes.push(...updatedEnabledScenes);
      }
      
      const hasLocationScenes = this.hasLocationBasedScenes(enabledActions, enabledScenes);
      
      if (!hasLocationScenes) {
        console.log('沒有位置變化場景，跳過檢查');
        return;
      }
      
      const statusChanged = this.lastHumanStatus !== null && this.lastHumanStatus !== currentHumanStatus;
      console.log('人員狀態變化:', statusChanged, '從', this.lastHumanStatus, '到', currentHumanStatus);
      
      this.lastHumanStatus = currentHumanStatus;
      
      if (statusChanged) {
        this.locationSceneExecuted.clear();
        console.log('狀態變化，重置執行記錄');
      }
      
      // 檢查設備場景（位置變化 + 開關設備）
      for (const action of enabledActions) {
        if (action.condition === '位置变化' || action.condition === '离家' || action.condition === '归家' || action.condition === '回家') {
          const locationType = action.condition;
          const sceneKey = `${action.type}_${locationType}`;
          
          if (this.locationSceneExecuted.has(sceneKey)) {
            console.log('設備場景已執行，跳過:', sceneKey);
            continue;
          }
          
          let targetStatus = null;
          if (locationType === '离家') {
            targetStatus = 0;
          } else if (locationType === '回家' || locationType === '归家') {
            targetStatus = 1;
          }
          
          if (targetStatus !== null && currentHumanStatus === targetStatus) {
            console.log(`執行${locationType}設備場景:`, action.type);
            
            let sceneName = action.type;
            if (sceneName.startsWith('scene_name:')) {
              sceneName = sceneName.replace('scene_name:', '');
            }
            
            const result = await this.executeSceneTasks(action.tasks);
            
            this.locationSceneExecuted.add(sceneKey);
            
            this.saveOperationLog('智能场景执行', {
              sceneName,
              executeTime: new Date().toLocaleTimeString(),
              success: result.success,
              type: 'device_only',
              locationType: locationType,
              humanStatus: currentHumanStatus
            });
          }
        }
      }
      
      // 檢查通知場景（位置變化 + 通知）
      for (const scene of enabledScenes) {
        // 檢查是否有condition字段
        if (!scene.condition) {
          console.log('通知場景缺少condition字段，跳過:', scene.name);
          continue;
        }
        
        if (scene.condition === '位置变化' || scene.condition === '离家' || scene.condition === '归家' || scene.condition === '回家') {
          const locationType = scene.condition;
          const sceneKey = `${scene.name}_${locationType}`;
          
          if (this.locationSceneExecuted.has(sceneKey)) {
            console.log('通知場景已執行，跳過:', sceneKey);
            continue;
          }
          
          let targetStatus = null;
          if (locationType === '离家') {
            targetStatus = 0;
          } else if (locationType === '回家' || locationType === '归家') {
            targetStatus = 1;
          }
          
          if (targetStatus !== null && currentHumanStatus === targetStatus) {
            console.log(`執行${locationType}通知場景:`, scene.name);
            
            const typeMap = {
              '震动': 'vibrate',
              '铃声': 'ring', 
              '静音': 'silent'
            };
            const notifyType = typeMap[scene.type] || 'info';
            
            this.locationSceneExecuted.add(sceneKey);
            
            this.saveOperationLog('智能场景执行', {
              sceneName: scene.name,
              executeTime: new Date().toLocaleTimeString(),
              success: true,
              notifyType,
              type: 'notification_only',
              locationType: locationType,
              humanStatus: currentHumanStatus
            });
            
            this.showNotificationIfOnSmartPage(scene.name, new Date().toLocaleTimeString(), true, notifyType);
          }
        }
      }
      
      console.log('=== 位置變化場景檢查完成 ===');
      
    } catch (error) {
      console.error('檢查位置變化場景時出錯:', error);
    }
  }

  async checkTimeBasedScenesLogic(enabledActions, enabledScenes, currentTime) {
    try {
      // 檢查設備場景（時間 + 開關設備）
      for (const action of enabledActions) {
        if (action.condition === '某个时间' && action.conditionTime === currentTime) {
          console.log('後台計時器執行設備場景:', action.type, 'at', currentTime);
          
          let sceneName = action.type;
          if (sceneName.startsWith('scene_name:')) {
            sceneName = sceneName.replace('scene_name:', '');
          }
          
          const result = await this.executeSceneTasks(action.tasks);
          
          this.saveOperationLog('智能场景执行', {
            sceneName,
            executeTime: currentTime,
            success: result.success,
            type: 'device_only'
          });
        }
      }
      
      // 檢查通知場景（時間 + 通知）
      for (const scene of enabledScenes) {
        if (scene.conditionTime === currentTime) {
          console.log('後台計時器執行通知場景:', scene.name, 'at', currentTime);
          
          const typeMap = {
            '震动': 'vibrate',
            '铃声': 'ring', 
            '静音': 'silent'
          };
          const notifyType = typeMap[scene.type] || 'info';
          
          this.saveOperationLog('智能场景执行', {
            sceneName: scene.name,
            executeTime: currentTime,
            success: true,
            notifyType,
            type: 'notification_only'
          });
          
          this.showNotificationIfOnSmartPage(scene.name, currentTime, true, notifyType);
        }
      }
    } catch (error) {
      console.error('檢查時間場景邏輯時出錯:', error);
    }
  }

  hasLocationBasedScenes(enabledActions, enabledScenes) {
    console.log('=== 檢查位置變化場景 ===');
    console.log('設備場景數量:', enabledActions.length);
    console.log('通知場景數量:', enabledScenes.length);
    
    // 檢查設備場景
    for (const action of enabledActions) {
      console.log('設備場景完整數據:', action);
      console.log('設備場景條件:', action.condition);
      if (action.condition === '位置变化' || action.condition === '离家' || action.condition === '归家' || action.condition === '回家') {
        console.log('找到位置變化設備場景:', action.type);
        return true;
      }
    }
    
    // 檢查通知場景 - 修復：檢查是否有condition字段
    for (const scene of enabledScenes) {
      console.log('通知場景完整數據:', scene);
      console.log('通知場景條件:', scene.condition);
      
      // 檢查是否有condition字段，如果沒有則跳過
      if (!scene.condition) {
        console.log('通知場景缺少condition字段，跳過:', scene.name);
        continue;
      }
      
      if (scene.condition === '位置变化' || scene.condition === '离家' || scene.condition === '归家' || scene.condition === '回家') {
        console.log('找到位置變化通知場景:', scene.name);
        return true;
      }
    }
    
    console.log('沒有找到位置變化場景');
    return false;
  }

    async getHumanStatus() {
    try {
      console.log('正在獲取人員狀態...');
      const response = await fetch('http://localhost:8088/mqtt-data/specific-columns?columns=is_human&limit=1');
      
      console.log('API響應狀態:', response.status);
      
      if (response.ok) {
        const data = await response.json();
        console.log('API響應數據:', data);
        
        if (data && data.data && data.data.length > 0) {
          const isHuman = data.data[0].isHuman;
          console.log('獲取人員狀態成功:', isHuman);
          return isHuman;
        } else {
          console.warn('無法獲取人員狀態，默認為有人狀態');
          return 1;
        }
      } else {
        console.warn('API請求失敗，狀態碼:', response.status, response.statusText);
        return 1;
      }
    } catch (error) {
      console.error('獲取人員狀態失敗:', error);
      return 1;
    }
  }

  async executeSceneTasks(tasks) {
    try {
      console.log('後台計時器執行場景任務:', tasks);
      
      const deviceCommands = {
        liv_lit: 0,
        kit_lit: 0,
        tol_lit: 0,
        fan_level: 0,
        water_pump_level: 0
      };
      
      for (const task of tasks) {
        if (task.field && task.value !== undefined) {
          deviceCommands[task.field] = task.value;
        }
      }
      
      console.log('後台計時器完整設備控制命令:', deviceCommands);
      
      const result = await this.sendDeviceCommands(deviceCommands);
      
      return result;
      
    } catch (error) {
      console.error('後台計時器執行場景任務時出錯:', error);
      return { success: false, message: '設備控制失敗', error: error.message };
    }
  }

  async sendDeviceCommands(commands) {
    try {
      const requestData = {
        homeId: uni.getStorageSync('activeHomeId') || '1001',
        userId: uni.getStorageSync('userInfo')?.userId || '2001',
        ...commands
      };
      
      console.log('後台計時器發送設備控制命令:', requestData);
      
      // 直接使用 fetch 而不是動態導入
      const messageParts = [
        `liv_lit=${commands.liv_lit}`,
        `kit_lit= ${commands.kit_lit}`,
        `tol_lit= ${commands.tol_lit}`,
        `fan_level= ${commands.fan_level}`,
        `water_pump_level= ${commands.water_pump_level}`
      ];
      
      const message = messageParts.join(', ');
      const url = `http://localhost:8088/api/mqtt/send?topic=bigroom&message=${encodeURIComponent(message)}`;
      
      const response = await fetch(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        }
      });
      
      if (response.ok) {
        console.log('後台計時器設備控制命令發送成功');
        return { success: true, message: '設備控制成功' };
      } else {
        console.warn('後台計時器設備控制失敗:', response.statusText);
        return { success: false, message: '設備控制失敗' };
      }
      
    } catch (error) {
      console.error('後台計時器發送設備控制命令失敗:', error);
      return { success: false, message: '設備控制失敗', error: error.message };
    }
  }

  async saveOperationLog(action, details) {
    try {
      // 使用统一的日志管理器
      const { saveLog } = await import('./logManager.js');
      const sceneName = details.sceneName || '智能场景';
      
      // 根据类型生成友好的中文描述
      let actionType = '执行';
      if (details.type === 'device_only') {
        actionType = '开启了';
      } else if (details.type === 'notification_only') {
        actionType = '执行了';
      } else if (details.type) {
        actionType = details.type;
      }
      
      // 保存到统一的日志系统
      saveLog(actionType, '智能场景', sceneName, '智能场景');
      
      console.log('后台计时器日志已保存:', actionType, sceneName);
    } catch (error) {
      console.error('后台计时器保存操作日志失败:', error);
    }
  }

  showNotificationIfOnSmartPage(sceneName, executeTime, success, notifyType) {
    try {
      const pages = getCurrentPages();
      const currentPage = pages[pages.length - 1];
      
      if (currentPage && currentPage.route && currentPage.route.includes('smart/smart')) {
        if (currentPage.showNotificationModal && typeof currentPage.showNotificationModal === 'function') {
          currentPage.showNotificationModal(sceneName, executeTime, success, notifyType);
        } else {
          this.showSystemNotification(sceneName, executeTime, success, notifyType);
        }
      } else {
        // 不在智能场景页面时，直接显示系统通知
        this.showSystemNotification(sceneName, executeTime, success, notifyType);
      }
    } catch (error) {
      console.error('显示通知失败:', error);
      this.showSystemNotification(sceneName, executeTime, success, notifyType);
    }
  }


  showSystemNotification(sceneName, executeTime, success, notifyType) {
    try {
      const content = `${sceneName} 已执行`;
      
      // 根据通知类型选择不同的显示方式
      let icon = 'success';
      if (notifyType === 'vibrate') {
        icon = 'none';
        // 震动通知
        uni.vibrateShort && uni.vibrateShort();
      } else if (notifyType === 'ring') {
        icon = 'success';
      } else if (notifyType === 'silent') {
        icon = 'none';
      }
      
      uni.showToast({
        title: content,
        icon: icon,
        duration: 4000
      });
      
      console.log('系统通知:', sceneName, content, notifyType);
    } catch (error) {
      console.error('显示系统通知失败:', error);
    }
  }

  getCurrentTime() {
    const now = new Date();
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');
    return `${hours}:${minutes}`;
  }

  getStatus() {
    return {
      isRunning: this.isRunning,
      lastCheckTime: this.lastCheckTime,
      currentTime: this.getCurrentTime()
    };
  }

  testBackgroundTimer() {
    console.log('=== 後台計時器測試 ===');
    console.log('計時器狀態:', this.getStatus());
    
    const localActions = uni.getStorageSync('localActions') || [];
    const enabledActions = localActions.filter(action => action.enabled);
    
    console.log('啟用的智能場景數量:', enabledActions.length);
    enabledActions.forEach((action, index) => {
      console.log(`場景 ${index + 1}:`, {
        name: action.type,
        condition: action.condition,
        time: action.conditionTime,
        enabled: action.enabled
      });
    });
    
    const testCommands = {
      liv_lit: 1,
      kit_lit: 0,
      tol_lit: 0,
      fan_level: 2,
      water_pump_level: 1
    };
    
    console.log('測試設備指令:', testCommands);
    this.sendDeviceCommands(testCommands).then(result => {
      console.log('測試設備指令發送結果:', result);
    });
  }

  // 手動檢查和修復場景數據
  debugScenes() {
    console.log('=== 調試場景數據 ===');
    
    const localActions = uni.getStorageSync('localActions') || [];
    const localScenes = uni.getStorageSync('localScenes') || [];
    
    console.log('原始本地存儲數據:');
    console.log('localActions:', localActions);
    console.log('localScenes:', localScenes);
    
    // 檢查是否有位置變化場景但沒有正確的condition字段
    const locationScenes = localScenes.filter(scene => {
      return scene.condition === '离家' || scene.condition === '归家' || scene.condition === '回家';
    });
    
    console.log('位置變化通知場景:', locationScenes);
    
    const locationActions = localActions.filter(action => {
      return action.condition === '离家' || action.condition === '归家' || action.condition === '回家';
    });
    
    console.log('位置變化設備場景:', locationActions);
    
    // 檢查啟用狀態
    const enabledScenes = localScenes.filter(scene => scene.enabled);
    const enabledActions = localActions.filter(action => action.enabled);
    
    console.log('啟用的通知場景:', enabledScenes);
    console.log('啟用的設備場景:', enabledActions);
    
    // 檢查缺少condition字段的場景
    const scenesWithoutCondition = localScenes.filter(scene => !scene.condition);
    console.log('缺少condition字段的場景:', scenesWithoutCondition);
    
    return {
      totalScenes: localScenes.length,
      totalActions: localActions.length,
      enabledScenes: enabledScenes.length,
      enabledActions: enabledActions.length,
      locationScenes: locationScenes.length,
      locationActions: locationActions.length,
      scenesWithoutCondition: scenesWithoutCondition.length
    };
  }

  // 智能修復缺少condition字段的場景
  async smartFixScenesWithoutCondition() {
    console.log('=== 智能修復缺少condition字段的場景 ===');
    
    const localScenes = uni.getStorageSync('localScenes') || [];
    let hasChanges = false;
    
    // 獲取當前人員狀態
    const currentHumanStatus = await this.getHumanStatus();
    console.log('當前人員狀態:', currentHumanStatus);
    
    for (let i = 0; i < localScenes.length; i++) {
      const scene = localScenes[i];
      if (!scene.condition) {
        console.log('修復場景:', scene.name);
        
        // 根據當前人員狀態設置條件
        if (currentHumanStatus === 0) {
          scene.condition = '离家';
          console.log('設置為离家條件（當前無人）');
        } else {
          scene.condition = '归家';
          console.log('設置為归家條件（當前有人）');
        }
        
        hasChanges = true;
      }
    }
    
    if (hasChanges) {
      uni.setStorageSync('localScenes', localScenes);
      console.log('場景數據已智能修復並保存');
    } else {
      console.log('沒有需要修復的場景');
    }
    
    return hasChanges;
  }
}

const backgroundTimer = new BackgroundTimer();

export default backgroundTimer;

