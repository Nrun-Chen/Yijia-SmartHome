// 引入http请求模块
const http = require('../request');
// 导入日志管理器
const { saveLog } = require('../util/logManager');
// 导入API日志管理器
const { logDeviceOperation } = require('../util/apiLogManager');

// 设备ID映射
const DEVICE_MAP = {
  'living_room': 'liv_lit', // 客厅灯
  'kitchen': 'kit_lit',     // 厨房灯
  'bedroom': 'tol_lit',     // 卧室灯(使用厕所灯接口)
  'fan': 'fan_level',       // 风扇档位
  'door_bell': 'bell'       // 门铃
};

// 设备代码到名称的映射
const DEVICE_NAME_MAP = {
  'liv_lit': '客厅灯',
  'kit_lit': '厨房灯',
  'tol_lit': '卧室灯',
  'fan_level': '风扇',
  'bell': '门铃'
};

/**
 * 根据设备代码获取设备名称
 * @param {string} code - 设备代码
 * @returns {string} 设备名称
 */
function getDeviceNameByCode(code) {
  return DEVICE_NAME_MAP[code] || '未知设备';
}

/**
 * 发送后端指令
 * @param {string} code - 设备代码 (liv_lit, kit_lit, tol_lit, fan_level)
 * @param {number|string} value - 设备值
 * @param {Object} options - 可选参数
 * @returns {Promise}
 */
const sendBackendPayload = ({ code, value, device = {}, shouldLog = true }) => {
  if (!code) {
    return Promise.reject(new Error('设备代码不能为空'));
  }

  // 构建message参数，格式为 "code=value"
  const message = `${code}=${value}`;
  
  console.log('设备控制请求参数:', message);

  // 发送POST请求，参数通过URL查询字符串传递
  // 获取设备名称
  const deviceName = device?.name || getDeviceNameByCode(code);
  // 获取操作类型
  const actionType = value === 1 || value === '1' ? '开' : '关';

  return http.post('/api/mqtt/send?topic=bigroom&message=' + encodeURIComponent(message), {})
    .then(async res => {
      uni.showToast({ title: '指令发送成功', icon: 'success' });
      // 根据参数决定是否记录日志
      if (shouldLog) {
        await logDeviceOperation(actionType, deviceName, '设备', value);
      }
      return res;
    }).catch(e => {
      console.error('设备控制失败:', e);
      uni.showToast({ title: '指令发送失败', icon: 'none' });
      throw e;
    });
};

/**
 * 控制设备接口
 * @param {Object} deviceData - 设备控制数据
 * @param {string} deviceData.room - 房间类型 (living_room, kitchen, bedroom)
 * @param {number} deviceData.status - 设备状态 (1: 开, 0: 关)
 * @returns {Promise}
 */
const controlDevice = (deviceData) => {
  // 验证房间类型
  if (!DEVICE_MAP[deviceData.room]) {
    return Promise.reject(new Error('不支持的房间类型'));
  }

  // 验证状态值
  if (deviceData.status !== 0 && deviceData.status !== 1) {
    return Promise.reject(new Error('状态值必须是0或1'));
  }

  // 发送控制指令
  return sendBackendPayload({
    code: DEVICE_MAP[deviceData.room],
    value: deviceData.status
  });
};

/**
 * 控制客厅灯
 * @param {number} status - 状态 (1: 开, 0: 关)
 * @param {Object} device - 设备对象
 * @returns {Promise}
 */
const controlLivingRoomLight = (status, device = {}) => {
  return sendBackendPayload({
    code: DEVICE_MAP.living_room,
    value: status,
    device
  });
};

/**
 * 控制厨房灯
 * @param {number} status - 状态 (1: 开, 0: 关)
 * @param {Object} device - 设备对象
 * @returns {Promise}
 */
const controlKitchenLight = (status, device = {}) => {
  return sendBackendPayload({
    code: DEVICE_MAP.kitchen,
    value: status,
    device
  });
};

/**
 * 控制卧室灯
 * @param {number} status - 状态 (1: 开, 0: 关)
 * @param {Object} device - 设备对象
 * @returns {Promise}
 */
const controlBedroomLight = (status, device = {}) => {
  return sendBackendPayload({
    code: DEVICE_MAP.bedroom,
    value: status,
    device
  });
};

/**
 * 发送灯控命令
 * @param {Object} device - 设备对象
 * @param {Object} options - 选项 { action: 'on'|'off'|'level', level: 1-3 }
 * @returns {Promise}
 */
const sendLightCmd = (device, { action, level }) => {
  if (!device?.backend?.code) {
    return Promise.reject(new Error('该房间的灯未接后端'));
  }

  let value = 0;
  if (action === 'off') value = 0;
  else if (action === 'on') value = 1;
  else if (action === 'level') value = 1; // 所有档位都设为1

  return sendBackendPayload({
    code: device.backend.code,
    value: value,
    device
  });
};

/**
 * 控制风扇档位
 * @param {number} level - 档位 (1-3)
 * @param {Object} device - 设备对象
 * @returns {Promise}
 */
const sendFanLevelCmd = (device, level) => {
  const v = [1, 2, 3].includes(level) ? level : 1;
  return sendBackendPayload({
    code: DEVICE_MAP.fan,
    value: v,
    device
  });
};

/**
 * 控制门铃
 * @param {number} status - 状态 (1: 响铃, 0: 停止)
 * @param {Object} device - 设备对象
 * @returns {Promise}
 */
const controlDoorBell = (status, device = {}) => {
  return sendBackendPayload({
    code: DEVICE_MAP.door_bell,
    value: status,
    device
  });
}

// 导出所有设备相关API
module.exports = {
  controlDevice,
  controlLivingRoomLight,
  controlKitchenLight,
  controlBedroomLight,
  sendBackendPayload,
  sendLightCmd,
  sendFanLevelCmd,
  controlDoorBell
};