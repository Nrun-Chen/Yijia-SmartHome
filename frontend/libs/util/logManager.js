/**
 * 本地日志管理器
 * 用于记录用户操作日志并本地保存
 */
const LOG_KEY = 'local_operation_logs';
const MAX_LOGS = 100; // 最大保留日志数量

/**
 * 获取当前活跃家庭
 * @returns {string} 活跃家庭名称
 */
function getActiveHome() {
  try {
    const activeHomeId = uni.getStorageSync('activeHomeId');
    console.log('当前活跃家庭ID:', activeHomeId);
    
    if (!activeHomeId) {
      console.log('未找到活跃家庭ID，返回默认家庭');
      return '默认家庭';
    }
    
    const homes = uni.getStorageSync('homes') || [];
    console.log('所有家庭数据:', homes);
    
    const activeHome = homes.find(home => String(home.id) === String(activeHomeId));
    console.log('找到的活跃家庭:', activeHome);
    
    if (activeHome && activeHome.name) {
      console.log('返回家庭名称:', activeHome.name);
      return activeHome.name;
    } else {
      console.log('未找到家庭名称，返回默认家庭');
      return '默认家庭';
    }
  } catch (error) {
    console.error('获取家庭名称失败:', error);
    return '默认家庭';
  }
}

/**
 * 获取当前用户名
 * @returns {string} 用户名
 */
function getUsername() {
  // 假设用户名存储在localStorage中，实际实现可能需要调整
  const userInfo = uni.getStorageSync('userInfo') || {};
  return userInfo.name || userInfo.username || '未知用户';
}

/**
 * 获取当前用户ID
 * @returns {string} 用户ID
 */
function getUserId() {
  const userInfo = uni.getStorageSync('userInfo') || {};
  return userInfo.userId || '2001';
}

/**
 * 保存日志到本地存储
 * @param {string} actionType - 操作类型：添加/开/关/设备控制
 * @param {string} targetType - 目标类型：房间/设备/场景
 * @param {string} targetName - 目标名称
 * @param {string} [category] - 分类：智能場景等
 */
export function saveLog(actionType, targetType, targetName, category = '') {
  try {
    const logs = JSON.parse(uni.getStorageSync(LOG_KEY) || '[]');
    
    let message;
    if (category === '智能場景') {
      // 智能場景的特殊日誌格式
      if (actionType === '設備控制') {
        message = `智能場景自動執行：${targetType}`;
      } else {
        // 检查actionType是否已经包含"了"字，避免重复
        const actionText = actionType.endsWith('了') ? actionType : `${actionType}了`;
        message = `${getUsername()} 在 ${getActiveHome()} 中 ${actionText} ${targetName}`;
      }
    } else {
      // 检查actionType是否已经包含"了"字，避免重复
      const actionText = actionType.endsWith('了') ? actionType : `${actionType}了`;
      message = `${getUsername()} 在 ${getActiveHome()} 中 ${actionText} ${targetName}`;
    }
    
    const newLog = {
      id: Date.now() + Math.random().toString(36).substr(2, 9),
      timestamp: new Date().toLocaleString(),
      username: getUsername(),
      userId: getUserId(),
      home: getActiveHome(),
      actionType,
      targetType,
      targetName,
      category,
      message
    };

    // 添加新日志到数组开头
    logs.unshift(newLog);

    // 限制日志数量
    if (logs.length > MAX_LOGS) {
      logs.pop();
    }

    // 保存到本地存储
    uni.setStorageSync(LOG_KEY, JSON.stringify(logs));
    return true;
  } catch (error) {
    console.error('保存日志失败:', error);
    return false;
  }
}

/**
 * 从本地存储读取日志
 * @returns {Array} 日志数组
 */
export function getLogs() {
  try {
    return JSON.parse(uni.getStorageSync(LOG_KEY) || '[]');
  } catch (error) {
    console.error('读取日志失败:', error);
    return [];
  }
}

/**
 * 删除指定ID的日志
 * @param {number|string} logId - 要删除的日志ID
 * @returns {boolean} 是否删除成功
 */
export function deleteLog(logId) {
  try {
    const logs = JSON.parse(uni.getStorageSync(LOG_KEY) || '[]');
    const filteredLogs = logs.filter(log => String(log.id) !== String(logId));
    
    if (filteredLogs.length === logs.length) {
      console.warn('未找到要删除的日志，ID:', logId);
      return false;
    }
    
    uni.setStorageSync(LOG_KEY, JSON.stringify(filteredLogs));
    console.log('日志删除成功，ID:', logId);
    return true;
  } catch (error) {
    console.error('删除日志失败:', error);
    return false;
  }
}

/**
 * 清除所有日志
 * @returns {boolean} 是否清除成功
 */
export function clearLogs() {
  try {
    uni.setStorageSync(LOG_KEY, '[]');
    return true;
  } catch (error) {
    console.error('清除日志失败:', error);
    return false;
  }
}