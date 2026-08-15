// 引入http请求模块
const http = require('../request');

/**
 * 创建家庭接口
 * @param {Object} familyData - 家庭信息
 * @param {string} familyData.name - 家庭名称
 * @param {string} familyData.address - 家庭地址
 * @returns {Promise}
 */
const createFamily = (familyData) => {
  // 使用POST请求，将家庭数据作为JSON请求体发送
  console.log('创建家庭请求参数:', JSON.stringify(familyData, null, 2));
  return http.post('/home', familyData);
};

/**
 * 获取家庭列表接口
 * @returns {Promise}
 */
const getFamilyList = () => {
  return http.get('/api/family/list');
};

/**
 * 获取家庭详情接口
 * @param {string} familyId - 家庭ID
 * @returns {Promise}
 */
const getFamilyDetail = (familyId) => {
  return http.get(`/api/family/detail/${familyId}`);
};

/**
 * 更新家庭信息接口
 * @param {string} familyId - 家庭ID
 * @param {Object} familyData - 家庭信息
 * @param {string} familyData.name - 家庭名称
 * @param {string} familyData.address - 家庭地址
 * @returns {Promise}
 */
const updateFamily = (familyId, familyData) => {
  return http.put(`/api/family/update/${familyId}`, familyData);
};

/**
 * 删除家庭接口
 * @param {string} homeId - 家庭ID
 * @param {string} userId - 用户ID
 * @returns {Promise}
 */
const deleteFamily = (homeId, userId) => {
  console.log('删除家庭已执行请求参数:', { homeId, userId });
  return http.delete(`/home/${homeId}?userId=${userId}`);
};

/**
 * 获取用户家庭信息接口
 * @param {string} userId - 用户ID
 * @returns {Promise}
 * @description 登录后获取用户家庭信息，接口为/home/user/:userId，只上传Authorization头
 */
const getUserFamily = async (userId) => {
  try {
    console.log('请求用户家庭信息...',userId);
    const response = await http.get(`/home/user/${userId}`);
    console.log('用户家庭信息获取成功:',userId);
    console.log(JSON.stringify(response, null, 2));
    return response;
  } catch (error) {
    console.error('用户家庭信息获取失败:', userId, error);
    throw error;
  }
};

/**
 * 新增家庭成员接口
 * @param {string} homeId - 家庭ID
 * @param {string} userId - 受邀者ID
 * @param {number} role - 角色(0: 管理员, 1: 家庭成员)
 * @returns {Promise}
 */
const addFamilyMember = (homeId, userId, role = 1) => {
  return http.post(`/home-member?homeId=${homeId}&userId=${userId}&role=${role}`);
};

/**
 * 删除家庭成员接口
 * @param {string} homeId - 家庭ID
 * @param {string} userId - 要删除的用户ID
 * @param {string} operatorId - 操作人ID
 * @returns {Promise}
 */
const deleteFamilyMember = (homeId, userId, operatorId) => {
  console.log('删除家庭成员请求参数:', { homeId, userId, operatorId });
  return http.delete(`/home-member?homeId=${homeId}&userId=${userId}&operatorId=${operatorId}`);
};

/**
 * 获取家庭成员详情接口
 * @param {string} homeId - 家庭ID
 * @returns {Promise}
 * @description 获取指定家庭的成员详情，包括成员、访客和管理员列表
 */
const getFamilyMembers = async (homeId) => {
  try {
    console.log(`获取家庭${homeId}成员信息...`);
    const response = await http.get(`/home-member/home/${homeId}/details`);
    console.log(`家庭${homeId}成员信息获取成功`);
    console.log(JSON.stringify(response, null, 2));
    return response;
  } catch (error) {
    console.error(`家庭${homeId}成员信息获取失败:`, error);
    throw error;
  }
};

/**
 * 更新家庭角色权限接口
 * @param {string} homeId - 家庭ID
 * @param {string} userId - 被更新角色的用户ID
 * @param {number} role - 角色(0: 管理员, 1: 家庭成员)
 * @param {string} operatorId - 操作人ID
 * @returns {Promise}
 */
const updateFamilyMemberRole = (homeId, userId, role, operatorId) => {
  console.log('更新家庭角色权限请求参数:', { homeId, userId, role, operatorId });
  return http.put(`/home-member?homeId=${homeId}&userId=${userId}&role=${role}&operatorId=${operatorId}`);
};

// 导出所有家庭相关API
module.exports = {
  updateFamilyMemberRole,
  createFamily,
  getFamilyList,
  getFamilyDetail,
  updateFamily,
  deleteFamily,
  getUserFamily,
  addFamilyMember,
  deleteFamilyMember,
  getFamilyMembers
};