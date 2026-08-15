<template>
  <view class="canvas">
    <!-- 返回按钮 -->
    <image class="back-icon" src="/static/icons/back.svg" @click="goBack" />

    <!-- 标题 -->
    <text class="forgot">忘记密码</text>

    <!-- 1. 邮箱 -->
    <view class="input-wrap" style="top:210px;">
      <input
        class="native-input"
        v-model="email"
        type="text"
        inputmode="email"
        placeholder="请输入邮箱"
        placeholder-style="color:#9aa0a6;font-size:14px;text-indent:16px"
        confirm-type="done"
      />
    </view>

    <!-- 2. 验证码 -->
    <view class="input-wrap" style="top:290px;">
      <input
        class="native-input"
        v-model="code"
        type="text"
        inputmode="numeric"
        placeholder="请输入验证码"
        placeholder-style="color:#9aa0a6;font-size:14px;text-indent:16px"
        confirm-type="done"
      />
      <view
        class="code-btn"
        :class="{ disabled: countdown>0 || sending }"
        @click="(countdown>0||sending)?null:onSendCode()"
      >
        <text v-if="countdown===0">{{ sending ? '发送中...' : '发送验证码' }}</text>
        <text v-else>重新发送 {{ countdown }}s</text>
      </view>
    </view>

    <!-- 新密码 -->
    <view class="input-wrap" style="top:379px;">
      <input
        class="native-input"
        v-model="password"
        type="text"
        :class="{ masked: !showPwd1 }"
        autocomplete="new-password"
        autocapitalize="off"
        spellcheck="false"
        placeholder="请输入新密码"
        placeholder-style="color:#9aa0a6;font-size:14px;text-indent:16px"
        confirm-type="done"
      />
      <uni-icons
        class="eye-icon"
        :type="showPwd1 ? 'eye-filled' : 'eye-slash-filled'"
        size="22"
        color="#333"
        @click="showPwd1 = !showPwd1"
      />
    </view>

    <!-- ★ 新增：密码强度条（位于两个密码框之间） -->
    <view
      class="pwd-meter"
      v-show="password && pwdStrengthPercent > 0"
      :style="{ '--fillWidth': pwdStrengthPercent + '%', '--fillColor': pwdStrengthColor }"
    >
      <view class="pwd-meter-fill"></view>
    </view>

    <!-- 确认新密码 -->
    <view class="input-wrap" style="top:459px;">
      <input
        class="native-input"
        v-model="confirmPassword"
        type="text"
        :class="{ masked: !showPwd2 }"
        autocomplete="new-password"
        autocapitalize="off"
        spellcheck="false"
        placeholder="请确认新密码"
        placeholder-style="color:#9aa0a6;font-size:14px;text-indent:16px"
        confirm-type="done"
      />
      <uni-icons
        class="eye-icon"
        :type="showPwd2 ? 'eye-filled' : 'eye-slash-filled'"
        size="22"
        color="#333"
        @click="showPwd2 = !showPwd2"
      />
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

    <!-- 提交按钮 -->
    <view class="login-button" @click="submitResetPassword">
      <text class="login-button-text">修改密码</text>
    </view>

    <!-- 其他登录方式 -->
    <text class="other-login-text">使用其他账号登录</text>
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
// 引入认证API
import { sendResetPasswordCode, resetPassword } from '../../libs/api/auth';

export default {
  data() {
    return {
      email: '',
      code: '',
      password: '',
      confirmPassword: '',
      // 默认明文显示，方便你检查；要默认隐藏改为 false
      showPwd1: true,
      showPwd2: true,
      countdown: 0,
      sending: false,
      _timer: null,
      checked: false,
    }
  },
  computed: {
    // ★ 新增：根据密码计算强度（0~4）
    pwdStrength() {
      const s = String(this.password || '')
      if (!s) return 0
      let score = 0
      if (s.length >= 8) score++
      if (/[a-z]/.test(s) && /[A-Z]/.test(s)) score++
      if (/\d/.test(s)) score++
      if (/[^A-Za-z0-9]/.test(s)) score++
      return score
    },
    // 百分比（用于进度宽度）
    pwdStrengthPercent() {
      return Math.min(100, Math.max(0, (this.pwdStrength / 4) * 100))
    },
    // 颜色（弱-中-强）
    pwdStrengthColor() {
      const p = this.pwdStrength
      if (p <= 1) return '#FF6B6B'    // 红
      if (p === 2) return '#F7B731'   // 橙
      if (p === 3) return '#2ECC71'   // 绿
      return '#19B67F'                // 更强
    }
  },
  methods: {
    goBack() {
      uni.navigateBack({ delta: 1 })
    },
    async onSendCode() {
      if (this.countdown > 0 || this.sending) return
      // 验证邮箱格式
      const ok = /^[\w.-]+@[\w.-]+$/.test(this.email)
      if (!ok) return uni.showToast({ title: '请先输入正确的邮箱', icon: 'none' })
      
      this.sending = true
      try {
        // 调用发送验证码接口
        await sendResetPasswordCode({ email: this.email })
        this.startCountdown(60)
        uni.showToast({ title: '验证码已发送到邮箱', icon: 'success' })
      } catch (error) {
        uni.showToast({ title: error.message || '发送验证码失败', icon: 'none' })
      } finally {
        this.sending = false
      }
    },
    startCountdown(sec) {
      this.clearTimer()
      this.countdown = sec
      this._timer = setInterval(() => {
        if (this.countdown <= 1) {
          this.clearTimer()
          this.countdown = 0
        } else {
          this.countdown -= 1
        }
      }, 1000)
    },
    clearTimer() {
      if (this._timer) {
        clearInterval(this._timer)
        this._timer = null
      }
    },
    toggleCheck() {
      this.checked = !this.checked
    },
    goToLogon() {
      uni.navigateTo({ url: '../logon/logon' })
    },
    
    // 提交密码重置
    async submitResetPassword() {
      if (!this.checked) {
        return uni.showToast({ title: '请同意隐私政策和用户协议', icon: 'none' })
      }
      
      if (!this.email || !/^[\w.-]+@[\w.-]+$/.test(this.email)) {
        return uni.showToast({ title: '请输入正确的邮箱', icon: 'none' })
      }
      
      if (!this.code) {
        return uni.showToast({ title: '请输入验证码', icon: 'none' })
      }
      
      if (!this.password || this.password.length < 6) {
        return uni.showToast({ title: '密码长度至少为6位', icon: 'none' })
      }
      
      if (this.password !== this.confirmPassword) {
        return uni.showToast({ title: '两次输入的密码不一致', icon: 'none' })
      }
      
      try {
        // 调用密码重置接口
        await resetPassword({ email: this.email, code: this.code, newPassword: this.password })
        
        uni.showToast({
          title: '密码重置成功',
          icon: 'success'
        })
        
        // 跳转到登录页面
        setTimeout(() => {
          uni.navigateTo({ url: '../logon/logon' })
        }, 1500)
      } catch (error) {
        uni.showToast({ title: error.message || '密码重置失败', icon: 'none' })
      }
    }
  },
  beforeDestroy() {
    this.clearTimer()
  }
}
</script>

<style scoped>
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

/* 输入区域（统一 348×58，圆角22） */
.input-wrap{
  position:absolute;
  left:50%;
  transform:translateX(-50%);
  width:348px;
  height:58px;
  border-radius:20px;
  background:#efefef;
  display:flex;
  align-items:center;
  padding:0 14px;
  box-sizing:border-box;
}

/* 原生 input */
.native-input{
  flex:1;
  height:100%;
  font-size:16px;
  color:#333;
  background:transparent;
  border:0;
  outline:0;
  text-indent:16px;
}

/* 验证码按钮（文字样式） */
.code-btn {
  margin-left: 8px;
  height: auto;
  line-height: normal;
  padding: 0;
  background: transparent;
  border: none;
  box-shadow: none;
  font-size: 14px;
  color: #EE417F;
  text-decoration: underline;
}
.code-btn.disabled {
  color: #999;
  text-decoration: none;
}

/* ★ 新增：密码强度条（位于两个密码框之间） */
.pwd-meter{
  position:absolute;
  left:50%;
  transform:translateX(-50%);
  top: 443px;            /* 379px的输入框底部约437px，这里放在中间区域 */
  width:348px;
  height:6px;
  background:#eaeaea;
  border-radius:4px;
  overflow:hidden;
}
.pwd-meter-fill{
  width: var(--fillWidth, 0%);
  height:100%;
  background: var(--fillColor, #FF6B6B);
  transition: width .25s ease;
}

/* 复选框位置 */
.checkbox-icon {
  position: absolute;
  top: 549px;
  left: 40px;
  width: 20px;
  height: 20px;
}

/* 协议文字 */
.agreement-text {
  position: absolute;
  top: 549px;
  left: 70px;
  display: flex;
  align-items: center;
  font-family: 'Inter', sans-serif;
  font-size: 12px;
  color: #BEBEBE;
  line-height: 20px;
}
.link {
  color: #EE417F;
  text-decoration: underline;
}

/* 提交按钮：350×55，居中，y=608 */
.login-button {
  position: absolute;
  top: 590px;
  left: 50%;
  transform: translateX(-50%);
  width: 350px;
  height: 55px;
  background-color: #000000;
  border-radius: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
}
.login-button-text {
  font-size: 16px;
  color: #fff;
  font-family: 'Taipei Sans TC', sans-serif;
}

/* 其他登录方式 */
.other-login-text {
  position: absolute;
  top: 665px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 12px;
  color: #9B9B9B;
  font-family: 'alibaba2', sans-serif;
  text-align: center;
}

.social-login-icons {
  position: absolute;
  top: 698px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}
.social-icon {
  width: 50px;
  height: 50px;
}

/* 已有账户？登录 */
.register-text {
  position: absolute;
  top: 765px;
  left: 50%;
  transform: translateX(-50%);
  font-family: 'alibaba1', sans-serif;
  font-size: 12px;
  color: #000000;
}

.register-highlight { color: #EA1763; }

/* 密文模拟（仅在 .masked 时生效） */
.masked {
  -webkit-text-security: disc;
  text-security: disc;
}

/* 自定义眼睛图标位置与可点击区域 */
.eye-icon {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  padding: 8px;
  cursor: pointer;
}

/* 隐藏浏览器一些清除/自动填充按钮 */
:deep(input.native-input::-webkit-clear-button),
:deep(input.native-input::-webkit-credentials-auto-fill-button),
::v-deep input.native-input::-webkit-clear-button,
::v-deep input.native-input::-webkit-credentials-auto-fill-button {
  display: none !important;
}
</style>
