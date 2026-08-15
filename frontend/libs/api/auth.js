// libs/api/auth.js
const http = require('../request');

/** 后端基地址 */
const BASE_URL = 'http://localhost:8088';

function API(path) {
  const a = String(BASE_URL).replace(/\/+$/, '');
  const b = String(path || '').replace(/^\/+/, '');
  return `${a}/${b}`;
}

function wrap(promise, fallbackMsg = '请求失败') {
  return Promise.resolve(promise)
    .then((res) => {
      if (res && (typeof res.status === 'number' || typeof res.statusCode === 'number')) {
        const code = (typeof res.status === 'number') ? res.status : res.statusCode;
        const data = (res.data !== undefined) ? res.data : res.body;
        if (code < 200 || code >= 300) {
          const msg =
            (typeof data === 'string' ? data :
              data?.message || data?.error || data?.msg || data?.detail || data?.title) ||
            `${fallbackMsg}(${code})`;
          const e = new Error(msg);
          e.response = res;
          throw e;
        }
        return (res.data !== undefined) ? res.data : res;
      }
      if (res && typeof res === 'object' && res.success === false &&
          (res.message || res.error || res.msg)) {
        const msg = res.message || res.error || res.msg || fallbackMsg;
        const e = new Error(msg);
        e.response = { data: res, status: 200 };
        throw e;
      }
      return res;
    })
    .catch((err) => {
      const r = err?.response;
      let msg;
      if (r && (typeof r.status === 'number' || typeof r.statusCode === 'number')) {
        const code = r.status ?? r.statusCode;
        const data = r.data ?? r.body;
        msg =
          (typeof data === 'string' ? data :
            data?.message || data?.error || data?.msg || data?.detail || data?.title) ||
          `${fallbackMsg}${code ? `(${code})` : ''}`;
      } else {
        msg = err?.message || err?.errMsg || fallbackMsg;
      }
      const e = new Error(msg || fallbackMsg);
      e.raw = err;
      e.response = r;
      throw e;
    });
}

/** 提取手机号/头像/邮箱 */
function pickPhone(obj) {
  if (!obj || typeof obj !== 'object') return '';
  return obj.phone || obj.mobile || obj?.user?.phone || obj?.data?.phone || obj?.data?.user?.phone || '';
}
function pickAvatar(info = {}) {
  const url = info.avatarUrl || info.avatar || info.headimgurl || '';
  const base64 = info.avatarBase64 || '';
  if (base64 && typeof base64 === 'string' && base64.trim()) {
    const hasPrefix = /^data:image\//i.test(base64);
    return hasPrefix ? base64 : `data:image/png;base64,${base64}`;
  }
  return url || '';
}
function pickEmail(info = {}) {
  return (
    info.email ||
    info.mail ||
    info?.user?.email ||
    info?.data?.email ||
    info?.data?.user?.email ||
    ''
  );
}

/** 缓存用户信息（含 phone / userNickname / userAvatar / userEmail） */
function cacheUserInfo(payload, identifier) {
  const info = payload?.data || payload || {};

  const normalized = {
    ...info,
    username: info.username || info.nickname || info.name || info.email || '',
    nickname: info.nickname || info.username || info.name || '',
    phone: info.phone || info.mobile || '',
    email: info.email || pickEmail(info) || ''
  };

  if (!normalized.phone && typeof identifier === 'string' && /^\d{6,}$/.test(identifier)) {
    normalized.phone = identifier;
  }

  if (normalized.phone) uni.setStorageSync('phone', normalized.phone);
  uni.setStorageSync('userInfo', normalized);

  // —— 给“我的”页用的本地键 —— //
  const nick = normalized.nickname || normalized.username || normalized.name || normalized.email || 'Yolo';
  uni.setStorageSync('userNickname', nick);

  const avatar = pickAvatar(info);
  if (avatar) uni.setStorageSync('userAvatar', avatar);

  const email = pickEmail(info);
  if (email) uni.setStorageSync('userEmail', email);

  if (identifier) uni.setStorageSync('lastIdentifier', identifier);
  return normalized;
}

/** 注册 */
const registerUser = (userData) => {
  console.log('用户注册请求参数:', JSON.stringify(userData, null, 2));
  return wrap(http.post(API('/api/auth/register'), userData), '注册失败')
    .then((res) => {
      cacheUserInfo(res, userData?.phone || userData?.email || userData?.username);
      return res;
    });
};

/** 登录 */
const loginUser = (loginData) =>
  wrap(http.post(API('/api/auth/login'), loginData), '登录失败')
    .then((res) => {
      cacheUserInfo(res, loginData?.identifier);
      return res;
    });

/** 获取用户信息（必须 phone） */
const getUserInfo = (params = {}) => {
  const p = { ...(params || {}) };
  if (!p.phone) {
    const fromCache = uni.getStorageSync('phone');
    if (fromCache) p.phone = fromCache;
  }
  if (!p.phone) {
    return Promise.reject(new Error('缺少 phone 参数，无法获取用户信息'));
  }
  return wrap(http.get(API('/api/auth/user'), { phone: p.phone }), '获取用户信息失败')
    .then((info) => {
      cacheUserInfo(info, p.phone);
      return info;
    });
};

/** 发送验证码/重置密码/测试邮件 */
const sendVerificationCode = (codeData) => {
  console.log('发送验证码请求参数:', JSON.stringify(codeData, null, 2));
  return wrap(
    http.post(API('/api/auth/register/send-code'), {}, { params: { email: codeData.email } }),
    '发送验证码失败'
  );
};
const sendResetPasswordCode = (codeData) => {
  console.log('发送重置密码验证码请求参数:', JSON.stringify(codeData, null, 2));
  return wrap(
    http.post(API('/api/auth/reset-password/send-code'), {}, { params: { email: codeData.email } }),
    '发送重置验证码失败'
  );
};
const resetPassword = (resetData) => {
  console.log('重置密码请求参数:', JSON.stringify(resetData, null, 2));
  return wrap(
    http.post(
      API('/api/auth/reset-password'),
      {},
      { params: { email: resetData.email, code: resetData.code, newPassword: resetData.newPassword } }
    ),
    '重置密码失败'
  );
};
const testEmailSend = (email) =>
  wrap(http.get(API('/api/auth/test-email'), { email }), '测试邮件发送失败');

/** 修改用户名 */
const updateUsername = (username) => {
  try {
    // 获取用户ID
    const userInfo = uni.getStorageSync('userInfo') || {};
    let userId = userInfo.userId || -1;

    // 确保userId是数字类型
    userId = parseInt(userId, 10);
    if (isNaN(userId)) {
      userId = -1;
    }

    if (userId === -1) {
      return Promise.reject(new Error('用户ID无效'));
    }

    const requestData = {
      userId: userId,
      username: username
    };

    return wrap(http.put(API('/user'), requestData), '修改用户名失败')
      .then((res) => {
        // 更新本地存储的用户信息
        if (res && res.data) {
          const updatedUserInfo = {...userInfo, username: username};
          uni.setStorageSync('userInfo', updatedUserInfo);
          uni.setStorageSync('userNickname', username);
        }
        return res;
      });
  } catch (error) {
    return Promise.reject(new Error('修改用户名失败: ' + error.message));
  }
};

module.exports = {
  registerUser,
  loginUser,
  getUserInfo,
  sendVerificationCode,
  sendResetPasswordCode,
  resetPassword,
  testEmailSend,
  updateUsername
};
