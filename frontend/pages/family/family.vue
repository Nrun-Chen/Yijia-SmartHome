<template>
  <view class="container">
    <!-- 返回按钮 -->
    <view class="back-card" @click="goMine">
      <uni-icons type="arrowleft" size="25" color="#666"></uni-icons>
    </view>

    <!-- 家庭设置（居中显示） -->
    <view class="family-setting-text">家庭设置</view>

    <!-- 三个矩形卡片 -->
    <view class="cards">
      <!-- 家庭成员卡片 -->
      <view class="card">
        <view class="card-header">
          <uni-icons type="personadd-filled" size="28" color="#5b17ea"></uni-icons>
          <text class="card-title">家庭成员</text>
          <uni-icons type="plusempty" size="28" color="#333" class="add-icon" @click="openPopup('member')"></uni-icons>
        </view>

        <!-- 家庭成员展示区域 -->
        <view class="member-container">
          <!-- 自己的矩形框 -->
          <view class="member-box">
            <image 
              class="member-avatar"
              :src="myAvatar" 
              mode="widthFix"
            ></image>
            <text class="member-name">自己</text>
          </view>

          <!-- 动态渲染家庭成员 -->
          <view class="member-box" v-for="(member, idx) in memberList" :key="`member-${idx}`"
            @longpress="openDeletePopup('member', idx)">
			<image 
              class="member-avatar"
              src="/static/家人.png" 
              mode="widthFix"
            ></image>
            <text class="member-name">{{ member.nickname }}</text>
          </view>
        </view>
      </view>

      <!-- 访客卡片：新增访客展示区域，与成员结构一致 -->
      <view class="card">
        <view class="card-header">
          <uni-icons type="contact-filled" size="30" color="#00C5C5"></uni-icons>
          <text class="card-title">访客</text>
          <uni-icons type="plusempty" size="28" color="#333" class="add-icon" @click="openPopup('guest')"></uni-icons>
        </view>

        <!-- 访客展示区域：复用成员容器样式，确保横向滚动和布局一致 -->
        <view class="member-container">
          <!-- 动态渲染访客：contact-filled图标 + “访客n”（n为序号，从1开始） -->
          <view class="member-box" v-for="(guest, idx) in guestList" :key="`guest-${idx}`"
            @longpress="openDeletePopup('guest', idx)"> <!-- 新增这行长按事件 -->
            <view class="member-avatar guest-icon">
              <uni-icons type="contact" size="45" color="#4F4F4F"></uni-icons>
            </view>
            <text class="member-name">访客{{ idx + 1 }}</text>
          </view>
        </view>
      </view>

      <!-- 管理员权限卡片 -->
      <view class="card">
              <view class="card-header">
                <image
                  src="/static/人员管理.png" 
                  mode="widthFix" 
                  style="width: 28px; height: 28px;"
                ></image>
                <text class="card-title">管理员权限</text>
                <uni-icons type="plusempty" size="28" color="#333" class="add-icon" @click="openPopup('admin')"></uni-icons>
              </view>
              <!-- 管理员展示区域 -->
              <view class="member-container">
                <!-- 自己（不能删除） -->
                <view class="member-box">
                  <image class="member-avatar" :src="myAvatar" mode="widthFix"></image>
                  <text class="member-name">自己</text>
                </view>
              
                <!-- 动态渲染管理员，可长按删除 -->
                <view
                  class="member-box"
                  v-for="(admin, idx) in adminList"
                  :key="`admin-${idx}`"
                  @longpress="openDeletePopup('admin', idx)"
                >
                  <image class="member-avatar" src="/static/家人.png" mode="widthFix"></image>
                  <text class="member-name">{{ admin.nickname }}</text>
                </view>
              </view>
            </view>
          </view>

    <!-- 弹窗：添加家庭成员 -->
    <uni-popup ref="popupMember" type="center">
      <view class="popup-wrapper">
        <view class="popup-title">添加家庭成员</view>
        <view class="popup-input-group">
          <view class="popup-input-item">
            <input 
              type="text" 
              v-model="memberForm.id" 
              placeholder="请输入成员ID" 
              class="popup-input"
            />
          </view>
          <view class="popup-input-item">
            <input 
              type="text" 
              v-model="memberForm.nickname" 
              placeholder="请输入成员昵称" 
              class="popup-input"
            />
          </view>
        </view>
        <button class="popup-confirm-btn" @click="confirmAddMember">确定</button>
      </view>
    </uni-popup>

    <!-- 弹窗：添加访客 -->
    <uni-popup ref="popupGuest" type="center">
      <view class="popup-wrapper">
        <view class="popup-title">添加访客</view>
        <view class="popup-input-group">
          <view class="popup-input-item">
            <input 
              type="text" 
              v-model="guestForm.id" 
              placeholder="请输入访客ID" 
              class="popup-input"
            />
          </view>
        </view>
        <button class="popup-confirm-btn" @click="confirmAddGuest">确定</button>
      </view>
    </uni-popup>

    <!-- 弹窗：管理员设置 -->
    <uni-popup ref="popupAdmin" type="center">
          <view class="popup-wrapper">
            <view class="popup-title">添加管理员</view>
            <view class="popup-input-group">
              <view class="popup-input-item">
                <input 
                  type="text" 
                  v-model="adminForm.memberId" 
                  placeholder="请输入家庭成员ID" 
                  class="popup-input"
                />
              </view>
            </view>
            <button class="popup-confirm-btn" @click="confirmAddAdmin">确定</button>
          </view>
        </uni-popup>
    
        <!-- 失败提示弹窗（用于显示“未查询到ID”） -->
        <uni-popup ref="popupFail" type="center" :mask-click="false">
          <view class="fail-popup">
            <!-- 红色错误图标 -->
            <view class="fail-icon">
              <uni-icons type="closecircle" size="90rpx" color="#EA1763"></uni-icons>
            </view>
            <view class="fail-title">添加失败</view>
            <view class="fail-text">未查询到对应的家庭成员ID</view>
            <button class="fail-btn" @click="$refs.popupFail.close()">我知道了</button>
          </view>
        </uni-popup>
		<!-- 删除确认弹窗 -->
		<uni-popup ref="popupDelete" type="center">
		  <view class="delete-popup">
		    <view class="delete-title">确定要删除该{{ 
		      deleteType === 'member' ? '成员' : 
		      deleteType === 'guest' ? '访客' : '管理员' }}吗</view>
		    <view class="delete-text">删除后不可恢复</view>
		    <view class="delete-btns">
		      <button class="cancel-btn" @click="$refs.popupDelete.close()">取消</button>
		      <button class="confirm-btn" @click="confirmDelete">删除</button>
		    </view>
		  </view>
		</uni-popup>
      </view>
    </template>

<script>
// 导入家庭相关API
import { addFamilyMember, deleteFamilyMember, updateFamilyMemberRole } from '@/libs/api/family';
export default {
  name: "family",
  data() {
    return {
      // 家庭成员表单与列表
      memberForm: { id: "", nickname: "" },
      memberList: [],
      // 访客表单与列表
      guestForm: { id: "" },
      guestList: [],
      // 管理员表单与列表
      adminForm: { memberId: "" },
      adminList: [],
      // 删除相关
      deleteType: "",
      deleteIndex: -1,
	  myAvatar: uni.getStorageSync('userAvatar') || "/static/choose/4.jpg",
      // 当前活跃家庭ID
      activeHomeId: '',
    }
  },
  onShow() {
      this.myAvatar = uni.getStorageSync('userAvatar') || "/static/choose/4.jpg";
      // 获取活跃家庭ID
      this.activeHomeId = uni.getStorageSync('activeHomeId') || '';
      console.log('活跃家庭ID:', this.activeHomeId);
      if (!this.activeHomeId) {
        // 如果没有活跃家庭ID，提示用户
        uni.showToast({ title: "请先选择活跃家庭", icon: "none" });
        // 可以添加跳转到选择家庭页面的逻辑
        // uni.navigateTo({ url: '/pages/homeManage/homeManage' });
      }
      // 从本地存储加载家庭成员、访客和管理员
      this.memberList = uni.getStorageSync(`members_${this.activeHomeId}`) || [];
      this.guestList = uni.getStorageSync(`guests_${this.activeHomeId}`) || [];
      this.adminList = uni.getStorageSync(`admins_${this.activeHomeId}`) || [];
    },
  methods: {
    // 返回“我的”页面
    goMine() {
      uni.navigateBack({ url: '/pages/mine/mine' });
    },

    // 打开对应弹窗
    openPopup(type) {
      if (type === 'member') {
        this.memberForm = { id: "", nickname: "" };
        this.$refs.popupMember.open();
      } else if (type === 'guest') {
        this.guestForm = { id: "" };
        this.$refs.popupGuest.open();
      } else if (type === 'admin') {
        this.adminForm = { memberId: "" };
        this.$refs.popupAdmin.open();
      }
    },

    // 确认添加家庭成员
    async confirmAddMember() {
      if (!this.memberForm.id.trim()) {
        return uni.showToast({ title: "请输入成员ID", icon: "none" });
      }
      if (!this.memberForm.nickname.trim()) {
        return uni.showToast({ title: "请输入成员昵称", icon: "none" });
      }
      // 避免重复ID
      const isDuplicate = this.memberList.some(item => item.id === this.memberForm.id);
      if (isDuplicate) {
        return uni.showToast({ title: "该成员ID已存在", icon: "none" });
      }
      // 获取当前活跃家庭ID (假设存储在本地)
      const homeId = uni.getStorageSync('activeHomeId');
      if (!homeId) {
        return uni.showToast({ title: "请先选择家庭", icon: "none" });
      }
      // 调用API添加家庭成员
      try {
        // 调用API添加家庭成员
        await addFamilyMember(homeId, this.memberForm.id);
        // 添加到本地列表
        this.memberList.push({ ...this.memberForm });

        uni.setStorageSync(`members_${homeId}`, this.memberList);
        uni.setStorageSync(`guests_${homeId}`, this.guestList);
        uni.setStorageSync(`admins_${homeId}`, this.adminList);

        console.log('添加家庭成员成功:', this.memberForm);

        this.$refs.popupMember.close();
        uni.showToast({ title: "添加成功", icon: "success" });
      } catch (error) {
        console.error('添加家庭成员失败:', error);
        this.$refs.popupFail.open();
      }
    },

    // 确认添加访客
    async confirmAddGuest() {
      if (!this.guestForm.id.trim()) {
        return uni.showToast({ title: "请输入访客ID", icon: "none" });
      }
      // 避免重复ID
      const isDuplicate = this.guestList.some(item => item.id === this.guestForm.id);
      if (isDuplicate) {
        return uni.showToast({ title: "该访客ID已存在", icon: "none" });
      }
      // 获取当前活跃家庭ID和操作人ID
      const homeId = this.activeHomeId;
      const operatorId = uni.getStorageSync('userInfo')?.userId || '';
      const userId = this.guestForm.id;
      
      if (!homeId || !operatorId || !userId) {
        return uni.showToast({ title: "缺少必要的参数", icon: "none" });
      }
      
      try {
        // 调用API添加访客，设置right=2
        console.log('添加访客参数:', homeId, userId, 2);
        await addFamilyMember(homeId, userId, 2);
        
        // 添加到本地列表
        this.guestList.push({ ...this.guestForm });
        
        // 更新本地存储
        uni.setStorageSync(`guests_${homeId}`, this.guestList);
        
        this.$refs.popupGuest.close();
        uni.showToast({ title: "访客添加成功", icon: "success" });
      } catch (error) {
        console.error('添加访客失败:', error);
        uni.showToast({ title: "添加访客失败", icon: "none" });
      }
    },

    // 确认添加管理员
    async confirmAddAdmin() {
      const targetMemberId = this.adminForm.memberId.trim();
      if (!targetMemberId) {
        return uni.showToast({ title: "请输入成员ID", icon: "none" });
      }
      // 查找家庭成员
      const matchedMember = this.memberList.find(item => item.id === targetMemberId);
      if (matchedMember) {
        const isAdminDuplicate = this.adminList.some(item => item.id === targetMemberId);
        if (isAdminDuplicate) {
          return uni.showToast({ title: "该成员已是管理员", icon: "none" });
        }
        try {
          // 获取当前活跃家庭ID和操作人ID
          const homeId = this.activeHomeId;
          const operatorId = uni.getStorageSync('userInfo')?.userId || '';
          
          if (!homeId || !operatorId) {
            throw new Error('缺少必要的参数');
          }
          
          // 调用API更新角色权限为管理员(role=0)
          await updateFamilyMemberRole(homeId, targetMemberId, 0, operatorId);
          
          // 加入管理员，带头像
          this.adminList.push({
            id: matchedMember.id,
            nickname: matchedMember.nickname,
            avatar: "/static/家人.png"
          });
          
          // 更新本地存储
          uni.setStorageSync(`admins_${homeId}`, this.adminList);
          
          this.$refs.popupAdmin.close();
          uni.showToast({ title: "管理员添加成功", icon: "success" });
        } catch (error) {
          console.error('添加管理员失败:', error);
          uni.showToast({ title: "添加管理员失败", icon: "none" });
        }
      } else {
        this.$refs.popupFail.open();
      }
    },

    // 打开删除确认弹窗
    openDeletePopup(type, index) {
      this.deleteType = type;
      this.deleteIndex = index;
      this.$refs.popupDelete.open();
    },

    // 确认删除
    async confirmDelete() {
      if (this.deleteType === "member") {
        try {
          // 删除成员
          const deletedMember = this.memberList[this.deleteIndex];
          const homeId = this.activeHomeId;
          const operatorId = uni.getStorageSync('userInfo')?.userId || '';
          const userId = deletedMember.id;
          
          if (!homeId || !operatorId || !userId) {
            throw new Error('缺少必要的删除参数');
          }
          
          // 调用API删除家庭成员
          await deleteFamilyMember(homeId, userId, operatorId);
          
          // 更新本地数据
          this.memberList.splice(this.deleteIndex, 1);
          this.adminList = this.adminList.filter(admin => admin.id !== deletedMember.id);
          
          // 更新本地存储
          uni.setStorageSync(`members_${homeId}`, this.memberList);
          uni.setStorageSync(`admins_${homeId}`, this.adminList);
          
          uni.showToast({ title: "成员已删除", icon: "success" });
        } catch (error) {
          console.error('删除成员失败:', error);
          uni.showToast({ title: "删除失败", icon: "none" });
        }
      } else if (this.deleteType === "guest") {
        try {
          // 删除访客
          const deletedGuest = this.guestList[this.deleteIndex];
          const homeId = this.activeHomeId;
          const operatorId = uni.getStorageSync('userInfo')?.userId || '';
          const userId = deletedGuest.id;
          
          if (!homeId || !operatorId || !userId) {
            throw new Error('缺少必要的删除参数');
          }
          
          // 调用API删除访客
          await deleteFamilyMember(homeId, userId, operatorId);
          
          // 更新本地数据
          this.guestList.splice(this.deleteIndex, 1);
          
          // 更新本地存储
          if (homeId) {
            uni.setStorageSync(`guests_${homeId}`, this.guestList);
          }
          
          uni.showToast({ title: "访客已删除", icon: "success" });
        } catch (error) {
          console.error('删除访客失败:', error);
          uni.showToast({ title: "删除失败", icon: "none" });
        }
      } else if (this.deleteType === "admin") {
        try {
          // 删除管理员
          const deletedAdmin = this.adminList[this.deleteIndex];
          
          // 获取当前活跃家庭ID和操作人ID
          const homeId = this.activeHomeId;
          const operatorId = uni.getStorageSync('userInfo')?.userId || '';
          const userId = deletedAdmin.id;
          
          if (!homeId || !operatorId || !userId) {
            throw new Error('缺少必要的参数');
          }
          
          // 调用API更新角色权限为普通成员(role=1)
          await updateFamilyMemberRole(homeId, userId, 1, operatorId);
          
          // 从管理员列表中删除
          this.adminList.splice(this.deleteIndex, 1);
          
          // 更新本地存储
          if (homeId) {
            uni.setStorageSync(`admins_${homeId}`, this.adminList);
          }
          
          uni.showToast({ title: "管理员已删除", icon: "success" });
        } catch (error) {
          console.error('删除管理员失败:', error);
          uni.showToast({ title: "删除失败", icon: "none" });
        }
      }
      
      this.$refs.popupDelete.close();
      this.deleteType = "";
      this.deleteIndex = -1;
    }
  }
};
</script>


<style>
@font-face {
  font-family: 'MyFont';
  src: url('/static/font/taibei1.ttf') format('truetype'); /* 字体文件路径 */
  font-weight: normal;
  font-style: normal;
}
.container {
  width: 100%;
  height: 100vh;
  background-color: #ffffff;
  position: relative;
  font-family: 'MyFont'; /* 容器字体设置，子元素可继承 */
}

/* 返回按钮样式 */
.back-card {
  position: absolute;
  top: 98rpx;  
  left: 38rpx; 
  width: 120rpx;
  height: 120rpx;
  background-color: #ffffff;
  border: 2rpx solid #d9d9d9;
  border-radius: 45rpx;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 家庭设置标题 */
.family-setting-text {
  position: absolute;
  left: 0;
  right: 0;
  top: 282rpx; 
  font-size: 56rpx;
  font-weight: bold; 
  color: #101010; 
  text-align: center;
  width: 100%;
  font-family: 'MyFont';
}

/* 卡片容器 */
.cards {
  position: absolute;
  top: 400rpx;
  left: 35rpx;
  width: 750rpx;
  display: flex;
  flex-direction: column;
  gap: 30rpx;
}

/* 卡片通用样式 */
.card {
  width: 680rpx;
  min-height: 340rpx;
  background-color: #f2f2f2;
  border-radius: 25rpx;
  position: relative;
  padding: 20rpx;
  box-sizing: border-box;
}

/* 卡片头部 */
.card-header {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 30rpx;
}

/* 卡片标题文字 */
.card-title {
  flex: 1;
  font-size: 28rpx;
  font-weight: bold;
  margin-left: 20rpx;
  font-family: 'MyFont';
}

/* 加号图标 */
.add-icon {
  cursor: pointer;
}

/* 成员/访客展示容器 */
.member-container {
  display: flex;
  gap: 24rpx;
  padding: 0 0 10rpx 0;
  overflow-x: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
  flex-wrap: nowrap;
  width: 100%;
  box-sizing: border-box;
}

/* 隐藏滚动条 */
.member-container::-webkit-scrollbar {
  display: none;
}

/* 成员/访客矩形框 */
.member-box {
  width: 190rpx;
  height: 220rpx;
  background-color: #ffffff;
  border-radius: 40rpx;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  align-items: center;
  padding: 30rpx 0 10rpx; 
  box-sizing: border-box;
  gap: 15rpx; 
  flex-shrink: 0;
}

/* 成员头像 + 访客图标容器 */
.member-avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%; 
  object-fit: cover;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #ffffff; 
}

/* 成员名称 + 访客名称 */
.member-name {
  font-size: 25rpx;
  color: #333333;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  width: 85%;
  font-family: 'MyFont';
}

/* 弹窗通用样式 */
.popup-wrapper {
  width: 600rpx;
  padding: 40rpx;
  background-color: #ffffff;
  border-radius: 40rpx;
  box-sizing: border-box;
}

.popup-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  text-align: center;
  margin-bottom: 35rpx;
  font-family: 'MyFont';
}

.popup-input-group {
  display: flex;
  flex-direction: column;
  gap: 25rpx;
  margin-bottom: 40rpx;
}

.popup-input-item {
  width: 100%;
}

.popup-input {
  width: 100%;
  height: 80rpx;
  padding: 0 20rpx;
  border: 2rpx solid #eee;
  border-radius: 40rpx;
  font-size: 28rpx;
  box-sizing: border-box;
  font-family: 'MyFont';
}

.popup-input::placeholder {
  color: #999;
  font-size: 26rpx;
  font-family: 'MyFont'; /* 占位符文字也应用自定义字体 */
}

.popup-confirm-btn {
  width: 100%;
  height: 90rpx;
  line-height: 90rpx;
  background-color: #101010;
  color: #ffffff;
  font-size: 32rpx;
  border-radius: 40rpx;
  border: none;
  padding: 0;
  font-family: 'MyFont';
}

.popup-confirm-btn::after {
  border: none;
}

/* 管理员设置弹窗样式 */
.popup-content {
  padding: 40rpx;
  background-color: #ffffff;
  border-radius: 40rpx;
}

/* 失败弹窗容器 */
.fail-popup {
  width: 600rpx;
  padding: 50rpx 40rpx;
  background-color: #ffffff;
  border-radius: 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-sizing: border-box;
}

/* 失败弹窗标题 */
.fail-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 20rpx;
  font-family: 'MyFont';
}

/* 失败弹窗提示文字 */
.fail-text {
  font-size: 28rpx;
  color: #666666;
  text-align: center;
  line-height: 1.6;
  margin-bottom: 40rpx;
  font-family: 'MyFont';
}

/* 失败弹窗按钮 */
.fail-btn {
  width: 100%;
  height: 90rpx;
  line-height: 90rpx;
  background-color: #101010;
  color: #ffffff;
  font-size: 32rpx;
  border-radius: 40rpx;
  border: none;
  padding: 0;
  font-family: 'MyFont';
}

.fail-btn::after {
  border: none;
}

/* 删除弹窗样式 */
.delete-popup {
  width: 530rpx;
  padding: 50rpx 40rpx;
  background: #fff;
  border-radius: 40rpx;
  text-align: center;
}

.delete-title {
  font-size: 36rpx;
  font-weight: bold;
  margin-bottom: 20rpx;
  font-family: 'MyFont';
}

.delete-text {
  font-size: 28rpx;
  color: #666;
  margin-bottom: 40rpx;
  font-family: 'MyFont';
}

.delete-btns {
  display: flex;
  justify-content: space-between;
}

.cancel-btn {
  flex: 1;
  margin-right: 20rpx;
  height: 80rpx;
  line-height: 80rpx;
  border-radius: 30rpx;
  background-color: #f5f5f5;
  color: #333;
  font-size: 28rpx;
  font-family: 'MyFont';
}

.confirm-btn {
  flex: 1;
  margin-left: 20rpx;
  height: 80rpx;
  line-height: 80rpx;
  border-radius: 30rpx;
  background-color: #EA1763;
  color: #fff;
  font-size: 28rpx;
  font-family: 'MyFont';
}

.cancel-btn::after, .confirm-btn::after {
  border: none;
}
</style>
    
