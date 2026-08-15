<template>
  <view class="logon-container">
    <!-- Logo -->
    <image class="logo" src="/static/images/logo.png" />

    <!-- 登录表单 -->
    <view class="login-form">
      <!-- 账号输入框 -->
      <view class="input-container username-container">
        <uni-icons class="left-icon" type="person-filled" size="22" color="#333" />
        <input
          class="input-field username"
          v-model="identifier"
          type="text"
          placeholder="请输入电话或邮箱"
        />
      </view>

      <!-- 密码输入框 -->
      <view class="input-container password-container">
        <uni-icons
          class="left-icon"
          :type="showPwd ? 'eye-filled' : 'eye-slash-filled'"
          size="22"
          color="#333"
          @click="showPwd = !showPwd"
        />
        <input
          class="input-field password"
          v-model="password"
          type="text"
          :class="{ masked: !showPwd }"
          placeholder="请输入密码"
          autocomplete="new-password"
          autocapitalize="off"
          spellcheck="false"
        />
      </view>

      <!-- 找回密码文本 -->
      <text class="forgot-password-text" @click="goToPassword">找回密码</text>

      <!-- 登录按钮 -->
      <view class="login-button" @click="doLogin">
        <text class="login-button-text">登录</text>
      </view>

      <!-- 指纹图片 -->
      <image class="login-button-image" src="/static/images/zhiwen.png" />
    </view>

    <!-- 三个社交登录图标 -->
    <view class="social-login-icons">
      <image class="social-icon" src="/static/images/weixin.png" />
      <image class="social-icon" src="/static/images/weibo.png" />
      <image class="social-icon" src="/static/images/qq.png" />
    </view>

    <!-- 没有账户？注册 -->
    <text class="register-text">
      没有账户？
      <text class="register-highlight" @click="goToRegister">注册</text>
    </text>

    <!-- 欢迎文本 -->
    <text class="login-text">欢迎登录！</text>
    <!-- 使用其他账号登录 -->
    <text class="other-login-text">使用其他账号登录</text>
  </view>
</template>

<script>
// 引入认证API、家庭API和用户信息API
import { loginUser, getUserInfo } from '../../libs/api/auth';
const { getUserFamily, getFamilyMembers } = require('@/libs/api/family');

export default {
  data() {
    return {
      identifier: '',
      password: '',
      showPwd: true   // 密码显隐
    };
  },
  mounted() {
    window.addEventListener('scroll', this.handleScroll);
    this.animateLogo();
    this.animateText();

    const u = this.$el.querySelector('.username-container');
    const p = this.$el.querySelector('.password-container');
    requestAnimationFrame(() => {
      u && u.classList.add('slide-in');
      p && p.classList.add('slide-in');
    });
  },
  beforeDestroy() {
    window.removeEventListener('scroll', this.handleScroll);
  },
  methods: {
        handleScroll() {
          const logo = this.$el.querySelector('.logo');
          const scrollY = window.scrollY || 0;
          if (logo) logo.style.transform = `translateY(${scrollY * 0.3}px)`;
        },
        animateLogo() {
          const logo = this.$el.querySelector('.logo');
          setTimeout(() => {
            if (logo) {
              logo.style.transform = 'translateY(0)';
              logo.style.opacity = '1';
            }
          }, 100);
        },
        animateText() {
          const loginText = this.$el.querySelector('.login-text');
          setTimeout(() => {
            if (loginText) {
              loginText.style.transform = 'translateY(0)';
              loginText.style.opacity = '1';
            }
          }, 300);
        },
        goToPassword() {
          uni.navigateTo({ url: '../password/password' });
        },
        goToRegister() {
          uni.navigateTo({ url: '../zhuce/zhuce' });
        },
        // 登录方法
        async doLogin() {
          // 表单验证
          if (!this.identifier) {
            uni.showToast({
              title: '请输入电话',
              icon: 'none'
            });
            return;
          }
          
          // 验证电话格式
          const isPhone = /^1[3-9]\d{9}$/.test(this.identifier);
          if (!isPhone) {
            uni.showToast({
              title: '请输入正确的电话',
              icon: 'none'
            });
            return;
          }
          
          if (!this.password) {
            uni.showToast({
              title: '请输入密码',
              icon: 'none'
            });
            return;
          }
          
          try {
            // 输出输入参数
            const loginParams = {
              identifier: this.identifier,
              password: this.password
            };
            console.log('登录请求参数:', JSON.stringify(loginParams, null, 2));

            // 调用登录API
            const res = await loginUser({
              identifier: this.identifier,
              password: this.password
            });
            
            // 登录成功，输出响应结果
            console.log('登录成功响应:', JSON.stringify(res, null, 2));

            // 保存token到本地存储
            let token = '';
            if (res.data && res.data.token) {
              token = res.data.token;
            } else if (res.token) {
              token = res.token;
            } else if (typeof res === 'string') {
              token = res;
            }
            
            if (token) {
              uni.setStorageSync('token', token);
              console.log('token 已保存');
            } else {
              console.error('未找到有效的token');
            }

            // 显示登录成功提示
            uni.showToast({
              title: '登录成功',
              icon: 'success'
            });
            
            // 获取用户信息
            try {
              const me = await getUserInfo()
              const name = me?.username || me?.nickname ||
                           me?.data?.username || me?.data?.nickname ||
                           me?.user?.username || me?.user?.nickname ||
                           me?.name || ''
              if (name) {
                this.displayName = name
                // 同步本地 userInfo 供其它页面兜底使用
                const local = uni.getStorageSync('userInfo') || {}
                uni.setStorageSync('userInfo', { ...local, username: name, nickname: name })
              }
            } catch (e) {
              console.warn('获取用户信息失败：', e?.message || e)
            }

            const userInfo = uni.getStorageSync('userInfo') || {};
            console.log('userInfo:', userInfo);
            const userId = userInfo.id || userInfo.userId || '';
            console.log('userId:', userId);
            // 如果有用户ID，获取家庭数据
            if (userId) {
              try {
                uni.showLoading({ title: '获取家庭信息中...' });
                const familyData = await getUserFamily(userId);
                console.log('familyData:', familyData);
                uni.hideLoading();

                const list = familyData?.data || [];
                if (Array.isArray(list)) {
                  // 格式化家庭数据
                  const normalizedHomes = list.map(h => ({
                    id: String(h.id || h.familyId || Date.now()),
                    name: h.name || h.familyName || '未命名家庭',
                    address: h.address || '',
                    rooms: typeof h.rooms === 'number' ? h.rooms : (h.roomCount || 0),
                    roomCount: typeof h.roomCount === 'number' ? h.roomCount : (h.rooms || 0),
                    members: typeof h.members === 'number' ? h.members : (h.memberCount || 0),
                    createdAt: h.createdAt || Date.now()
                  }));

                  // 保存家庭数据到本地
                  uni.setStorageSync('homes', normalizedHomes);
                  console.log('家庭信息已更新:', normalizedHomes);

                  // 设置活跃家庭ID（如果有家庭）
                  if (normalizedHomes.length > 0) {
                    const activeHomeId = normalizedHomes[0].id;
                    uni.setStorageSync('activeHomeId', activeHomeId);
                    
                    // 获取家庭成员信息
                    try {
                      console.log(`获取了${activeHomeId}的家庭用户`);
                      const membersData = await getFamilyMembers(activeHomeId);
                      
                      // 存储家庭成员信息
                      if (membersData && membersData.data) {
                        const { memberList = [], guestList = [], adminList = [] } = membersData.data;
                        uni.setStorageSync(`members_${activeHomeId}`, memberList);
                        uni.setStorageSync(`guests_${activeHomeId}`, guestList);
                        uni.setStorageSync(`admins_${activeHomeId}`, adminList);
                        console.log(`家庭${activeHomeId}成员信息已存储`);
                      }
                    } catch (membersError) {
                      console.error(`获取家庭${activeHomeId}成员信息失败:`, membersError);
                    }
                  }

                  // 跳转到主页
                  setTimeout(() => {
                    uni.navigateTo({
                      url: '../welcome/welcome'
                    });
                  }, 1000);
                } else {
                  console.error('家庭数据格式不正确:', familyData);
                }
              } catch (familyError) {
                uni.hideLoading();
                console.error('获取家庭信息失败:', familyError);
              }
            }
          } catch (error) {
        // 登录失败，输出错误信息
        console.log('登录失败错误:', JSON.stringify(error, null, 2));

        // 显示错误信息
        uni.showToast({
          title: error.message || '用户名或密码错误',
          icon: 'none'
        });
      }
    }
  }
};
</script>

<style scoped>
.logon-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #fff;
  position: relative;
  flex-direction: column;
}

/* Logo 渐入 */
.logo {
  position: absolute;
  top: 137px;
  left: 164px;
  width: 75px;
  height: 75px;
  opacity: 0;
  transform: translateY(20px);
  transition: transform 0.6s ease-out, opacity 0.6s ease-out;
}

.login-form {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 20px;
  position: relative;
  top: 50px;
}

.input-container {
  position: relative;
  width: 311px;
  display: flex;
  align-items: center;
  opacity: 0;
  transform: translateX(60px);
  will-change: transform, opacity;
}

/* 密文模拟（当 showPwd=false 时生效） */
.masked {
  -webkit-text-security: disc;
  text-security: disc; /* 非标准，兜底 */
}

/* 左边图标统一样式 */
.left-icon {
  position: absolute;
  left: 20px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 2;
  width: 20px;
  height: 20px;
}


/* 输入框统一样式，左边留空 —— 去掉描边 */
.input-field {
  width: 100%;
  height: 60px;
  padding-left: 40px; /* 给左边图标留空间 */
  padding-right: 10px;
  border: none;                 /* ← 取消描边 */
  outline: none;                /* ← 聚焦时也不显示外轮廓 */
  border-radius: 25px;
  background-color: #EFEFEF;
  font-size: 16px;
}
.input-field:focus {
  border: none;
  outline: none;
}

/* 找回密码 */
.forgot-password-text {
  width: 100%;
  font-size: 12px;
  color: #EA1763;
  text-decoration: underline;
  font-family: 'alibaba1', sans-serif; /* 改这里 */
  text-align: right;
  margin-top: 10px;
}


/* 欢迎文本 渐入 */
.login-text {
  position: absolute;
  top: 244px;
  left: 141px;
  font-size: 24px;
  font-family: 'Taipei Sans TC', sans-serif;
  opacity: 0;
  transform: translateY(20px);
  transition: transform 0.6s ease-out, opacity 0.6s ease-out;
}

/* 登录按钮 */
.login-button {
  width: 266px;
  height: 56px;
  background-color: #000;
  border-radius: 25px;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  left: -30px;
  top: 5px;
}

.login-button-text {
  font-size: 16px;
  color: white;
  font-family: 'Taipei Sans TC', sans-serif;
}

/* 指纹图标 */
.login-button-image {
  position: absolute;
  right: -20px;
  top: 240px;
  transform: translateY(-50%);
  width: 56px;
  height: 56px;
}

/* 其他账号登录 */
.other-login-text {
  font-size: 12px;
  color: #9B9B9B;
  font-family: 'Taipei Sans TC', sans-serif;
  margin-top: 100px;
  text-align: center;
}

/* 社交图标 */
.social-login-icons {
  position: absolute;
  top: 655px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 10px;
}
.social-icon { width: 50px; height: 50px; }

/* 注册 */
.register-text {
  position: absolute;
  top: 745px;
  left: 50%;
  transform: translateX(-50%);
  font-family: 'alibaba1', sans-serif;  /* ← 改这里 */
  font-size: 12px;
  color: #000;
}

.register-highlight { color: #EA1763; }

/* 动画 */
@keyframes slideInRight {
  from { transform: translateX(60px); opacity: 0; }
  to   { transform: translateX(0);    opacity: 1; }
}
.username-container.slide-in {
  animation: slideInRight 0.6s cubic-bezier(.2,.8,.2,1) 0.5s forwards;
}
.password-container.slide-in {
  animation: slideInRight 0.6s cubic-bezier(.2,.8,.2,1) 0.7s forwards;
}

/* 隐藏系统自带的默认小眼睛 */
:deep(input[type="password"]::-ms-reveal),
:deep(input[type="password"]::-ms-clear),
:deep(input[type="password"]::-webkit-credentials-auto-fill-button),
:deep(input[type="password"]::-webkit-clear-button),
:deep(input[type="password"]::-webkit-textfield-decoration-container) {
  display: none !important;
}
:deep(input[type="password"]) {
  -webkit-appearance: none;
     -moz-appearance: none;
          appearance: none;
}
/* 仅挪动“请输入电话或邮箱”的文字到更右侧 */
.input-field.username {
  padding-left: 64px;  /* 原来是 40/50，可按需调大或调小 */
}
.input-field.password {
  padding-left: 64px;
}



</style>
