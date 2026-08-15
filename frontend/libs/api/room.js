// 引入http请求模块
const http = require('../request');
// 导入日志管理器
const { saveLog } = require('../util/logManager');

/**
 * 新增房间接口
 * @param {string} userId - 用户ID
 * @param {Object} roomData - 房间信息
 * @param {string} roomData.name - 房间名称(liv, kit, tol, bed或other-1, other-2等)
 * @param {string} roomData.homeId - 家庭ID(活跃家庭的Id)
 * @param {string} roomData.description - 房间中文备注
 * @returns {Promise}
 */
const addRoom = (userId, roomData) => {
  // 验证必填参数
  if (!userId) {
    return Promise.reject(new Error('用户ID不能为空'));
  }
  
  if (!roomData || !roomData.name || !roomData.homeId) {
    return Promise.reject(new Error('房间名称和家庭ID不能为空'));
  }
  
  // 验证name格式
  const validNames = ['liv', 'kit', 'tol', 'bed'];
  if (!validNames.includes(roomData.name) && !/^other-\d+$/.test(roomData.name)) {
    return Promise.reject(new Error('房间名称格式不正确，应为liv, kit, tol, bed或other-数字'));
  }
  
  console.log('新增房间请求参数:', { userId, roomData });
  
  // 发送POST请求
  // 获取房间中文名称
  const roomCNName = roomData.description || getRoomCNName(roomData.name);

  return http.post(`/room?userId=${userId}`, roomData)
    .then(res => {
      uni.showToast({ title: '房间创建成功', icon: 'success' });
      // 记录房间创建日志
      saveLog('添加', '房间', roomCNName);
      return res;
    })
    .catch(e => {
      console.error('房间创建失败:', e);
      uni.showToast({ title: '房间创建失败', icon: 'none' });
      throw e;
    });
};

/**
 * 根据房间代码获取房间中文名称
 * @param {string} code - 房间代码
 * @returns {string} 房间中文名称
 */
function getRoomCNName(code) {
  const roomMap = {
    'liv': '客厅',
    'kit': '厨房',
    'tol': '厕所',
    'bed': '卧室'
  };

  if (roomMap[code]) {
    return roomMap[code];
  } else if (code.startsWith('other-')) {
    const num = code.replace('other-', '');
    return `其他房间${num}`;
  } else {
    return '未知房间';
  }
}

// 导出所有房间相关API
module.exports = {
  addRoom
};