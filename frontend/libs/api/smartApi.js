// smartApi.js - API functions for smart scenes and actions
// 引入http请求模块
const http = require('../request');
// 导入日志管理器
const { saveLog } = require('../util/logManager');
// 导入API日志管理器
const { logSceneOperation } = require('../util/apiLogManager');

/**
 * 获取用户头像 (已禁用)
 * @param {string} userId - 用户ID
 * @returns {Promise<string>} - 头像URL
 */
const fetchAvatar = async (userId) => {
  console.warn('fetchAvatar API is temporarily disabled');
  return '/static/images/logo.png'; // 返回默认头像
};

/**
 * 获取场景列表 (已禁用)
 * @param {string} userId - 用户ID
 * @returns {Promise<Array>} - 场景列表
 */
const fetchScenes = async (userId) => {
  console.warn('fetchScenes API is temporarily disabled');
  return [];
};

/**
 * 获取动作列表 (已禁用)
 * @param {string} userId - 用户ID
 * @returns {Promise<Array>} - 动作列表
 */
const fetchActions = async (userId) => {
  console.warn('fetchActions API is temporarily disabled');
  return [];
};

/**
 * 删除动作 (已禁用)
 * @param {string} actionId - 动作ID
 * @returns {Promise<boolean>} - 是否删除成功
 */
const deleteAction = async (actionId) => {
  console.warn('deleteAction API is temporarily disabled');
  return false;
};

/**
 * 切换场景状态 (已禁用)
 * @param {string} sceneId - 场景ID
 * @param {boolean} status - 目标状态
 * @returns {Promise<boolean>} - 是否切换成功
 */
const toggleScene = async (sceneId, status) => {
  console.warn('toggleScene API is temporarily disabled');
  return false;
};

/**
 * 切换动作状态 (已禁用)
 * @param {string} actionId - 动作ID
 * @param {boolean} status - 目标状态
 * @returns {Promise<boolean>} - 是否切换成功
 */
const toggleAction = async (actionId, status) => {
  console.warn('toggleAction API is temporarily disabled');
  return false;
};

/**
 * 获取特定家庭的场景列表 (已禁用)
 * @param {string} homeId - 家庭ID
 * @returns {Promise<Array>} - 场景列表
 */
const fetchHomeScenes = async (homeId) => {
  console.warn('fetchHomeScenes API is temporarily disabled');
  return [];
};

/**
 * 發送設備控制命令
 * @param {Object} deviceCommands - 設備控制命令對象
 * @param {number} [deviceCommands.liv_lit] - 客廳燈狀態 (0/1)
 * @param {number} [deviceCommands.kit_lit] - 廚房燈狀態 (0/1)
 * @param {number} [deviceCommands.tol_lit] - 臥室燈狀態 (0/1)
 * @param {number} [deviceCommands.senser_lit] - 感應燈狀態 (0/1)
 * @param {number} [deviceCommands.fan_level] - 風扇等級 (0/1/2/3)
 * @param {number} [deviceCommands.water_pump_level] - 水泵等級 (0/1/2/3)
 * @param {number} [deviceCommands.bell] - 響鈴狀態 (0/1)
 * @returns {Promise<Object>} - 控制結果
 */
const sendDeviceCommands = async (deviceCommands) => {
  try {
    // 檢查是否啟用MQTT功能（開發環境可關閉）
    const enableMqtt = uni.getStorageSync('enableMqtt') !== false; // 默認啟用
    
    if (!enableMqtt) {
      console.log('MQTT功能已禁用，使用模擬響應');
      return { success: true, message: '模擬設備控制成功', data: deviceCommands };
    }

    // 檢查後端服務器狀態
    const serverStatus = await checkServerStatus();
    if (serverStatus.status === 'offline') {
      console.error('後端服務器離線，無法發送設備控制命令');
      console.warn('請啟動後端服務器並確保它在 http://localhost:8088 上運行');
      console.log('啟用模擬模式進行測試...');
      return { success: true, message: '模擬設備控制成功（後端離線）', data: deviceCommands };
    }

    // 構建MQTT消息格式 - 只包含設備控制命令
    const messageParts = [];
    
    // 添加所有設備控制命令到消息中（只包含需要的5個設備）
    if (deviceCommands.liv_lit !== undefined) {
      messageParts.push(`liv_lit=${deviceCommands.liv_lit}`);
    }
    if (deviceCommands.kit_lit !== undefined) {
      messageParts.push(`kit_lit= ${deviceCommands.kit_lit}`);
    }
    if (deviceCommands.tol_lit !== undefined) {
      messageParts.push(`tol_lit= ${deviceCommands.tol_lit}`);
    }
    if (deviceCommands.fan_level !== undefined) {
      messageParts.push(`fan_level= ${deviceCommands.fan_level}`);
    }
    if (deviceCommands.water_pump_level !== undefined) {
      messageParts.push(`water_pump_level= ${deviceCommands.water_pump_level}`);
    }

    // 構建完整的MQTT消息 - 純設備控制命令
    const mqttMessage = messageParts.join(', ');
    const topic = 'bigroom'; // 使用固定的topic

    console.log('發送MQTT設備控制命令:', {
      topic: topic,
      message: mqttMessage,
      originalCommands: deviceCommands
    });

    // 發送POST請求到MQTT接口（只發送設備控制命令）
    const url = `/api/mqtt/send?topic=${encodeURIComponent(topic)}&message=${encodeURIComponent(mqttMessage)}`;
    const res = await http.post(url);

    console.log('MQTT設備控制響應:', res);

    // 檢查響應是否有效
    if (!res) {
      console.error('MQTT設備控制失敗: 無服務器響應');
      return { success: false, message: '無服務器響應' };
    }

    // 檢查是否有錯誤狀態
    if (res.error || res.code === -1) {
      console.error('MQTT設備控制失敗:', res.error || res.message || '服務器返回錯誤');
      return { success: false, message: res.error || res.message || '設備控制失敗' };
    }

    // 記錄設備控制日誌到本地
    const commandDetails = Object.entries(deviceCommands)
      .map(([key, value]) => `${key}=${value}`)
      .join(', ');
    // 記錄設備控制日誌到API（只記錄一次）
    await logSceneOperation('智能場景執行', '設備控制', commandDetails);

    return { success: true, message: 'MQTT設備控制成功', data: res.data || res };
  } catch (error) {
    console.error('MQTT設備控制API調用失敗:', error);
    
    // 檢查是否是連接錯誤
    if (error.message && error.message.includes('ERR_CONNECTION_REFUSED')) {
      console.error('後端服務器連接被拒絕，請檢查服務器是否運行');
      return { success: false, message: '後端服務器未運行，請啟動服務器' };
    }
    
    return { success: false, message: '設備控制失敗', error: error.message };
  }
};
/**
 * 檢查後端服務器狀態
 */
const checkServerStatus = async () => {
  try {
    // 直接檢查MQTT API端點是否可用
    const testMessage = 'liv_lit=0, kit_lit= 0, tol_lit= 0, fan_level= 0, water_pump_level= 0';
    const url = `/api/mqtt/send?topic=bigroom&message=${encodeURIComponent(testMessage)}`;
    
    // 發送一個測試請求來檢查服務器狀態
    const response = await http.post(url);
    
    // 如果請求成功，說明服務器在線
    return { status: 'online', response };
  } catch (error) {
    console.warn('後端服務器連接失敗:', error.message);
    return { status: 'offline', error: error.message };
  }
};

const createScene = async (sceneData) => {
  try {
    console.log('模擬場景創建（僅本地保存）:', sceneData);
    
    // 模擬成功響應
    const mockResponse = {
      id: `mock-scene-${Date.now()}`,
      name: sceneData.name,
      status: sceneData.status || 0,
      message: '場景已保存到本地（模擬響應）'
    };
    
    console.log('模擬場景創建響應:', mockResponse);
    return mockResponse;
  } catch (error) {
    console.error('模擬場景創建失敗:', error);
    return null;
  }
};

// 导出所有API函数
module.exports = {
  fetchAvatar,
  fetchScenes,
  fetchHomeScenes,
  fetchActions,
  deleteAction,
  toggleScene,
  toggleAction,
  createScene,
  sendDeviceCommands,
  checkServerStatus
};