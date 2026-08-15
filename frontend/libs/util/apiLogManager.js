/**
 * API日志管理器
 * 用于将操作日志发送到后端API
 */

const API_LOG_URL = 'http://localhost:8088/api/logs';

/**
 * 发送日志到API
 * @param {string} logMessage - 日志内容
 * @param {number} userId - 用户ID，默认为3
 * @returns {Promise<boolean>} 是否发送成功
 */
export async function sendLogToAPI(logMessage, userId = 3) {
  try {
    console.log('正在发送日志到API:', logMessage);
    
    const response = await fetch(API_LOG_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        log: logMessage,
        userId: userId
      })
    });
    
    if (response.ok) {
      const data = await response.json();
      console.log('API日志发送成功:', data);
      return true;
    } else {
      console.warn('API日志发送失败，状态码:', response.status);
      return false;
    }
  } catch (error) {
    console.error('API日志发送出错:', error);
    return false;
  }
}

/**
 * 获取用户和家庭信息
 * @returns {Object} 包含用户名和家庭名的对象
 */
function getUserAndHomeInfo() {
  try {
    // 获取用户信息
    const userInfo = uni.getStorageSync('userInfo') || {};
    const userName = userInfo.username || userInfo.nickname || userInfo.name || '未知用户';
    
    // 获取家庭信息
    const activeHomeId = uni.getStorageSync('activeHomeId') || '';
    const homes = uni.getStorageSync('homes') || [];
    const activeHome = homes.find(h => String(h.id) === String(activeHomeId));
    const homeName = activeHome?.name || activeHome?.homeName || '默认家庭';
    
    return { userName, homeName };
  } catch (error) {
    console.error('获取用户和家庭信息失败:', error);
    return { userName: '未知用户', homeName: '默认家庭' };
  }
}

/**
 * 记录设备操作日志
 * @param {string} actionType - 操作类型（开/关/控制）
 * @param {string} deviceName - 设备名称
 * @param {string} deviceType - 设备类型
 * @param {number} value - 操作值
 * @param {number} userId - 用户ID
 */
export async function logDeviceOperation(actionType, deviceName, deviceType = '设备', value = null, userId = 3) {
  try {
    const { userName, homeName } = getUserAndHomeInfo();
    let logMessage;
    
    if (value !== null) {
      if (actionType === '开' || actionType === '关') {
        logMessage = `${userName}在${homeName}中${actionType}了${deviceName}`;
      } else {
        logMessage = `${userName}在${homeName}中${actionType}了${deviceName} (值: ${value})`;
      }
    } else {
      logMessage = `${userName}在${homeName}中${actionType}了${deviceName}`;
    }
    
    // 直接保存到本地存储，不再调用API
    console.log('保存设备操作日志到本地:', logMessage);
    const { saveLog } = await import('./logManager.js');
    const success = saveLog(actionType, deviceType, deviceName);
    
    return success;
  } catch (error) {
    console.error('记录设备操作日志失败:', error);
    return false;
  }
}

/**
 * 记录场景操作日志
 * @param {string} sceneName - 场景名称
 * @param {string} actionType - 操作类型
 * @param {string} details - 详细信息
 * @param {number} userId - 用户ID
 */
export async function logSceneOperation(sceneName, actionType, details = '', userId = 3) {
  try {
    const { userName, homeName } = getUserAndHomeInfo();
    let logMessage = `${userName}在${homeName}中${actionType}了智能场景：${sceneName}`;
    if (details) {
      logMessage += ` (${details})`;
    }
    
    // 直接保存到本地存储，不再调用API
    console.log('保存场景操作日志到本地:', logMessage);
    const { saveLog } = await import('./logManager.js');
    const success = saveLog(actionType, '场景', sceneName, '智能场景');
    
    return success;
  } catch (error) {
    console.error('记录场景操作日志失败:', error);
    return false;
  }
}

/**
 * 记录智能场景开关操作日志
 * @param {string} sceneName - 场景名称
 * @param {boolean} enabled - 是否启用
 * @param {number} userId - 用户ID
 */
export async function logSceneToggle(sceneName, enabled, userId = 3) {
  try {
    const { userName, homeName } = getUserAndHomeInfo();
    const actionType = enabled ? '开启' : '关闭';
    const logMessage = `${userName}在${homeName}中${actionType}了智能场景：${sceneName}`;
    
    // 直接保存到本地存储，不再调用API
    console.log('保存智能场景开关日志到本地:', logMessage);
    const { saveLog } = await import('./logManager.js');
    const success = saveLog(actionType, '智能场景', sceneName);
    
    return success;
  } catch (error) {
    console.error('记录智能场景开关日志失败:', error);
    return false;
  }
}
