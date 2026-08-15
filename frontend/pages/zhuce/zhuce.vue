<template>
  <view class="canvas">
    <!-- 返回按钮 -->
    <image class="back-icon" src="/static/icons/back.svg" @click="goBack" />

    <!-- 标题 -->
    <text class="forgot">让我们开始吧</text>

    <!-- 1. 姓名 -->
    <view class="input-wrap" style="top:210px;">
      <input
        class="native-input"
        v-model="username"
        type="text"
        placeholder="用户名（长度在1-10）"
        placeholder-style="color:#9aa0a6;font-size:14px;text-indent:16px"
        confirm-type="done"
      />
    </view>

    <!-- 2. 电话 -->
    <view class="input-wrap" style="top:290px;">
      <input
        class="native-input"
        v-model="phone"
        type="number"
        placeholder="电话"
        placeholder-style="color:#9aa0a6;font-size:14px;text-indent:16px"
        confirm-type="done"
      />
    </view>

    <!-- 3. 邮箱 -->
    <view class="input-wrap" style="top:370px;">
      <input
        class="native-input"
        v-model="email"
        type="text"
        placeholder="邮箱"
        placeholder-style="color:#9aa0a6;font-size:14px;text-indent:16px"
        confirm-type="done"
      />
    </view>

    <!-- 4. 邮箱验证码 -->
    <view class="input-wrap" style="top:450px;">
      <input
        class="native-input"
        v-model="emailCode"
        type="text"
        placeholder="邮箱验证码"
        placeholder-style="color:#9aa0a6;font-size:14px;text-indent:16px"
        confirm-type="done"
      />
      <button class="code-button" @click="getEmailCode">{{ codeText }}</button>
    </view>

    <!-- 5. 密码 -->
    <view class="input-wrap" style="top:530px;">
      <input
        class="native-input"
        v-model="password"
        :type="showPwd ? 'text' : 'password'"
        placeholder="请输入密码"
        placeholder-style="color:#9aa0a6;font-size:14px;text-indent:16px"
        confirm-type="done"
      />
      <uni-icons
        class="eye-icon"
        :type="showPwd ? 'eye-filled' : 'eye-slash-filled'"
        size="22"
        color="#333"
        @click="showPwd = !showPwd"
      />
    </view>
    <!-- 密码强度（弱/中/强），只有输入时才显示 -->
      <view v-if="password" class="pwd-meter">
        <view class="pwd-meter-track">
          <view class="pwd-meter-bar"
                :class="strength.class"
                :style="{ width: strength.percent + '%' }"></view>
        </view>
        <text class="pwd-meter-text" :class="strength.class">{{ strength.label }}</text>
      </view>
    <!-- 复选框 -->
    <uni-icons
      class="checkbox-icon"
      :type="checked ? 'checkbox-filled' : 'checkbox'"
      size="20"
      color="#333"
      @click="toggleCheck"
    />

    <!-- 协议文字 -->
    <view class="agreement-text">
      <text>我同意 </text>
      <text class="link">隐私政策</text>
      <text> 与 </text>
      <text class="link">用户协议</text>
    </view>

    <!-- 注册并登录按钮 -->
    <view class="login-button" @click="register">
      <text class="login-button-text">注册并登录</text>
    </view>

    <!-- 使用其他账号登录 -->
    <text class="other-login-text">使用其他账号登录</text>

    <!-- 三个社交登录图标 -->
    <view class="social-login-icons">
      <image class="social-icon" src="/static/images/weixin.png" />
      <image class="social-icon" src="/static/images/weibo.png" />
      <image class="social-icon" src="/static/images/qq.png" />
    </view>

    <!-- 已有账户？登录 -->
    <text class="register-text">
      已有账户？
      <text class="register-highlight" @click="goToLogon">登录</text>
    </text>
  </view>
</template>

<script>
import { sendVerificationCode, registerUser } from '../../libs/api/auth.js'

export default {
  data() {
    return {
      username: '',
      phone: '',
      email: '',
      emailCode: '',
      password: '',
      showPwd: false,
      checked: false,
      codeText: '获取验证码',
      countdown: 0,
      strength: { percent: 0, label: '弱', class: 'weak' }
    }
  },
  watch: {
    password(newVal) {
      this.updateStrength(newVal)
    }
  },
  methods: {
    goBack() { uni.navigateBack({ delta: 1 }) },
    toggleCheck() { this.checked = !this.checked },
    goToLogon() { uni.navigateTo({ url: '../logon/logon' }) },
    // 计算密码强度：长度 + 小写 + 大写 + 数字 + 符号，共5分
    updateStrength(pw = '') {
      let score = 0
      if (pw.length >= 8) score += 1
      if (/[a-z]/.test(pw)) score += 1
      if (/[A-Z]/.test(pw)) score += 1
      if (/\d/.test(pw)) score += 1
      if (/[^A-Za-z0-9]/.test(pw)) score += 1

      // 百分比映射（0~5分）
      const percent = Math.max(0, Math.min(100, Math.round((score / 5) * 100)))

      // 分档：<=2 弱，=3 中，>=4 强
      let label = '弱'
      let cls = 'weak'
      if (score === 3) { label = '中'; cls = 'medium' }
      if (score >= 4) { label = '强'; cls = 'strong' }

      this.strength = { percent, label, class: cls }
    },
    async getEmailCode() {
      if (!this.email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.email)) {
        return uni.showToast({ title: '请输入正确的邮箱地址', icon: 'none' })
      }
      if (this.countdown > 0) return

      try {
        const res = await sendVerificationCode({ email: this.email })
        const okMsg = typeof res === 'string' ? res : (res && (res.message || res.msg)) || '验证码已发送，请注意查收'
        uni.showToast({ title: okMsg, icon: 'none' })

        this.countdown = 60
        this.codeText = `${this.countdown}s后重新获取`
        const timer = setInterval(() => {
          this.countdown--
          this.codeText = `${this.countdown}s后重新获取`
          if (this.countdown <= 0) {
            clearInterval(timer)
            this.codeText = '获取验证码'
          }
        }, 1000)
      } catch (e) {
        const msg = e?.message || '发送失败，请重试'
        uni.showToast({ title: msg, icon: 'none' })
        console.error('发送验证码错误：', e?.stack || e?.message || e)
      }
    },

    async register() {
      if (!this.username)   return uni.showToast({ title: '请输入姓名', icon: 'none' })
      if (!this.password)   return uni.showToast({ title: '请输入密码', icon: 'none' })
      if (!this.phone)      return uni.showToast({ title: '请输入电话', icon: 'none' })
      if (!this.email)      return uni.showToast({ title: '请输入邮箱', icon: 'none' })
      if (!this.emailCode)  return uni.showToast({ title: '请输入邮箱验证码', icon: 'none' })
      if (!this.checked)    return uni.showToast({ title: '请同意隐私政策和用户协议', icon: 'none' })

      const registerParams = {
        username: this.username,
        password: this.password,
        phone: this.phone,
        email: this.email,
        verificationCode: this.emailCode
      }
      console.log('注册请求参数:', JSON.stringify(registerParams, null, 2))

      try {
        await registerUser(registerParams)
        uni.showToast({ title: '注册成功', icon: 'success' })
        setTimeout(() => { this.goToLogon() }, 1500)
      } catch (e) {
        const msg = e?.message || '注册失败，请重试'
        uni.showToast({ title: msg, icon: 'none' })
        console.error('注册失败：', e?.stack || e?.message || e)
      }
    }
  }
}
</script>

<style scoped>
/* ===== 保持你之前的样式不变 ===== */
.canvas {
  width: 402px;
  height: 874px;
  position: relative;
  background: #fff;
}
.back-icon {
  position: absolute;
  top: 85px;
  left: 21px;
  width: 24px;
  height: 24px;
}
.forgot {
  position: absolute;
  top: 139px;
  left: 0;
  width: 100%;
  text-align: center;
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
  font-family: "Taipei Sans TC","Taipei Sans","Noto Sans CJK SC","PingFang SC","Microsoft YaHei",Arial,sans-serif;
  color: #222;
}
.input-wrap {
  position:absolute;
  left:50%;
  transform:translateX(-50%);
  width:348px;
  height:58px;
  border-radius:22px;
  background:#efefef;
  display:flex;
  align-items:center;
  padding:0 14px;
  box-sizing:border-box;
}
.native-input {
  flex:1;
  height:100%;
  font-size:15px;
  color:#333;
  background:transparent;
  border:0;
  outline:0;
  /* 让光标与输入文字统一右移，且与 placeholder 一致 */
  text-indent:16px;
}
/* 密码显隐图标 */
.eye-icon {
  position: absolute;
  right: 16px;
}
/* 验证码按钮 */
.code-button {
  position: absolute;
  right: 16px;
  height: 36px;
  padding: 0 12px;
  background-color: #EE417F;
  color: white;
  border-radius: 18px;
  font-size: 14px;
  line-height: 36px;
  border: none;
}
/* 复选框 */
.checkbox-icon {
  position: absolute;
  top: 610px;
  left: 40px;
  width: 20px;
  height: 20px;
}
/* 协议文字 */
.agreement-text {
  position: absolute;
  top: 610px;
  left: 70px;
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #BEBEBE;
  line-height: 20px;
}
.link { color: #EE417F; text-decoration: underline; }
/* 注册按钮 */
.login-button {
  position: absolute;
  top: 645px;
  left: 50%;
  transform: translateX(-50%);
  width: 350px;
  height: 55px;
  background-color: #000;
  border-radius: 25px;
  display:flex;
  justify-content:center;
  align-items:center;
}
.login-button-text { font-size: 16px; color: #fff; }
/* 其他账号登录 */
.other-login-text {
  position: absolute;
  top: 710px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 12px;
  color: #9B9B9B;
}
/* 三个社交图标 */
.social-login-icons {
  position: absolute;
  top: 750px;
  left: 50%;
  transform: translateX(-50%);
  display:flex;
  gap:10px;
}
@font-face{
  font-family: 'alibaba1';
  src: url('/static/ziti/Alibaba1.ttf');
  font-weight: 400;
  font-style: normal;
  font-display: swap;
}
.social-icon { width:50px; height:50px; }
/* 已有账户？登录 */
.register-text {
  position: absolute;
  top: 812px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 12px;
  color: #000;
  font-family: 'alibaba1', 'PingFang SC', 'Microsoft YaHei', Arial, sans-serif;
}
.register-highlight {
  color:#EA1763;
  font-family: 'alibaba1', 'PingFang SC', 'Microsoft YaHei', Arial, sans-serif;
}
.password-container.slide-in {
  animation: slideInRight 0.6s cubic-bezier(.2,.8,.2,1) 0.7s forwards;
}
/* 密码强度组件 */
.pwd-meter {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  top: 596px;           /* 放在密码输入框下面，按你的布局微调 */
  width: 348px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.pwd-meter-track {
  flex: 1;
  height: 6px;
  background: #eee;
  border-radius: 999px;
  overflow: hidden;
}

.pwd-meter-bar {
  height: 100%;
  width: 0%;
  border-radius: 999px;
  transition: width 260ms ease;
}

/* 文案“弱/中/强” */
.pwd-meter-text {
  font-size: 12px;
  min-width: 2em;
}

/* 颜色分档（可换成你的品牌变量） */
.pwd-meter-bar.weak   { background: #F87171; } /* 红 */
.pwd-meter-bar.medium { background: #F59E0B; } /* 橙 */
.pwd-meter-bar.strong { background: #10B981; } /* 绿 */

.pwd-meter-text.weak   { color: #F87171; }
.pwd-meter-text.medium { color: #F59E0B; }
.pwd-meter-text.strong { color: #10B981; }
/* === 隐藏系统自带的密码“显示/隐藏”眼睛与清除按钮（兼容多端） === */
:deep(input[type="password"]::-ms-reveal),
:deep(input[type="password"]::-ms-clear) { display: none !important; }

:deep(input[type="password"]::-webkit-credentials-auto-fill-button),
:deep(input[type="password"]::-webkit-clear-button),
:deep(input[type="password"]::-webkit-textfield-decoration-container) { display: none !important; }

::v-deep input[type="password"]::-ms-reveal,
::v-deep input[type="password"]::-ms-clear,
::v-deep input[type="password"]::-webkit-credentials-auto-fill-button,
::v-deep input[type="password"]::-webkit-clear-button,
::v-deep input[type="password"]::-webkit-textfield-decoration-container { display: none !important; }

:deep(input[type="password"]) {
  -webkit-appearance: none;
     -moz-appearance: none;
          appearance: none;
}
</style>
