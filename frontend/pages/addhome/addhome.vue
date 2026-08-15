<template>
  <view class="page">
    <!-- 左上角返回 -->
    <view class="btn-box back-btn" @click="goBack">
      <uni-icons type="back" size="22" color="#101010"></uni-icons>
    </view>

    <!-- 标题 -->
    <text class="title">{{ isEdit ? '创建家庭' : '家庭创建' }}</text>

    <!-- 输入：家庭名称 -->
    <view class="input-wrap" style="top:230px;">
      <input
        class="native-input"
        v-model.trim="form.name"
        type="text"
        placeholder="输入家庭名称"
        placeholder-style="color:#000000;font-size:15px;font-weight:700;font-family:'taibei1';"
        confirm-type="done"
      />
    </view>

    <!-- 输入：家庭地址 -->
    <view class="input-wrap" style="top:313px;">
      <input
        class="native-input"
        v-model.trim="form.address"
        type="text"
        placeholder="输入家庭地址"
        placeholder-style="color:#000000;font-size:15px;font-weight:700;font-family:'taibei1';"
        confirm-type="done"
      />
    </view>

    <!-- 完成按钮 -->
    <view class="submit-btn" @click="finish" style="top:736px;">
      <text class="submit-text">完成</text>
    </view>
  </view>
</template>

<script>
// 导入家庭相关API
const { createFamily, updateFamily, getUserFamily } = require('@/libs/api/family');
const GLOBAL_ROOMS_PREFIX = 'rooms:' // 每个家庭的房间桶：rooms:<homeId>

export default {
  data() {
    return {
      // 表单
      form: { name: '', address: '' },
      // 编辑模式：当带 id 进入时为 true
      isEdit: false,
      editId: null
    }
  },

  onLoad(query) {
    // 支持编辑模式：/pages/addhome/addhome?id=xxxx
    if (query && query.id) {
      this.isEdit = true
      this.editId = Number(query.id)
      const homes = uni.getStorageSync('homes') || []
      console.log(homes)
      const target = homes.find(h => h.id === this.editId)
      if (target) {
        this.form.name = target.name || ''
        this.form.address = target.address || ''
      } else {
        // 传了 id 但找不到，降级为创建模式
        this.isEdit = false
        this.editId = null
      }
    }
  },

  // 每次打开本页，按实际房间桶把 homes 的房间数同步一遍
  onShow() {
    this.syncHomeRoomsFromBuckets()
  },

  methods: {
    goBack() {
      // 返回家庭管理页
      uni.reLaunch({ url: '/pages/homeManage/homeManage' })
    },

    // —— 核心：把 homes 中每个家庭的 rooms/roomCount 与 rooms:<id> 桶长度保持一致 —— //
    syncHomeRoomsFromBuckets() {
      try {
        const homes = uni.getStorageSync('homes') || []
        let changed = false
        homes.forEach((h, i) => {
          const list = uni.getStorageSync(GLOBAL_ROOMS_PREFIX + h.id) || []
          const count = Array.isArray(list) ? list.length : 0
          if (h.rooms !== count || h.roomCount !== count) {
            homes[i] = {
              ...h,
              rooms: count,
              roomCount: count,
              members: typeof h.members === 'number' ? h.members : 0
            }
            changed = true
          }
        })
        if (changed) uni.setStorageSync('homes', homes)
      } catch (e) {}
    },

    // 创建或保存
    async finish() {
      // 检查是否已登录（token是否存在）
      const token = uni.getStorageSync('token')
      if (!token) {
        return uni.showToast({
          title: '请先登录',
          icon: 'none'
        })
      }

      const name = (this.form.name || '').trim()
      const address = (this.form.address || '').trim()

      if (!name && !address) return uni.showToast({ title: '请输入家庭名称和地址', icon: 'none' })
      if (!name) return uni.showToast({ title: '请输入家庭名称', icon: 'none' })
      if (!address) return uni.showToast({ title: '请输入家庭地址', icon: 'none' })

      try {
        uni.showLoading({ title: '处理中...' })

        if (this.isEdit && this.editId) {
          // —— 编辑模式：更新已有家庭 —— //
          const response = await updateFamily(this.editId, { name, address })
          
          // 更新本地存储
          const homes = uni.getStorageSync('homes') || []
          const idx = homes.findIndex(h => h.id === this.editId)
          if (idx !== -1) {
            homes[idx] = {
              ...homes[idx],
              name,
              address
            }
            uni.setStorageSync('homes', homes)
          }

          uni.hideLoading()
          uni.showToast({ title: '已保存', icon: 'none' })
          return this.goBack()
        }

        // —— 创建模式 —— //
        const response = await createFamily({ name, address })
        
        // 创建成功，获取后端返回的家庭ID
        const homeId = response.data.id
        console.log(homeId)
        // 初始化该家庭的房间桶为 []
        try { uni.setStorageSync(GLOBAL_ROOMS_PREFIX + homeId, []) } catch (e) {}
        
        // 更新本地存储
        const homes = uni.getStorageSync('homes') || []
        homes.push({
          id: homeId,
          name,
          address,
          rooms: 0,
          roomCount: 0,
          members: 0,
          createdAt: Date.now()
        })
        uni.setStorageSync('homes', homes)
        uni.setStorageSync('activeHomeId', homeId)

        uni.hideLoading()
        uni.showToast({ title: '创建成功', icon: 'none' })

        // 获取最新的家庭信息
        try {
          // 从本地存储获取用户信息
          const userInfo = uni.getStorageSync('userInfo') || {};
          // 提取用户ID
          const userId = userInfo.id || userInfo.userId || '';
          
          if (!userId) {
            console.error('未找到用户ID，无法获取家庭信息');
          } else {
            const familyData = await getUserFamily(userId);
            //console.log('创建家庭后获取的家庭信息:', JSON.stringify(familyData, null, 2));
            // 更新本地存储
            if (familyData && Array.isArray(familyData)) {
              uni.setStorageSync('homes', familyData);
              console.log('家庭信息已更新');
              console.log(familyData);
            }
          }
        } catch (error) {
          console.error('创建家庭后获取家庭信息失败:', error);
        }

        // 返回家庭管理页
        this.goBack()
      } catch (error) {
        uni.hideLoading()
        console.error('创建/更新家庭失败:', error)
        uni.showToast({
          title: error.message || '创建/更新家庭失败',
          icon: 'none'
        })
      }
    }
  }
}
</script>

<style scoped>
.page { background:#fff; min-height:100vh; position:relative; }

/* 返回按钮 */
.btn-box {
  width:45px; height:45px; border-radius:15px;
  background:#FFFFFF; border:1px solid #D9D9D9;
  display:flex; align-items:center; justify-content:center;
  position:absolute;
}
.back-btn { left:38px; top:79px; }

/* 标题：x=42, y=165；字体 taibei1/27号/000000 */
.title {
  position:absolute; left:42px; top:165px;
  font-family:'taibei1','Taipei Sans TC','PingFang SC',Arial,sans-serif;
  font-size:27px; color:#000000; font-weight:700;
}

/* 输入容器：居中，345*54，圆角15，填充F0F0F0 */
.input-wrap {
  position:absolute; left:50%; transform:translateX(-50%);
  width:345px; height:54px; background:#F0F0F0;
  border-radius:15px; display:flex; align-items:center;
  padding:0 16px; box-sizing:border-box;
}

/* 输入文本：taibei1/15号/加粗/000000 */
.native-input {
  flex:1; height:100%;
  font-family:'taibei1','Taipei Sans TC','PingFang SC',Arial,sans-serif;
  font-size:15px; color:#000000; font-weight:700;
  background:transparent;
}

/* 完成按钮：居中，233*56，圆角22，填充000000 */
.submit-btn {
  position:absolute; left:50%; transform:translateX(-50%);
  width:233px; height:56px; background:#000000;
  border-radius:22px; display:flex; align-items:center; justify-content:center;
}

/* 按钮文字：Alibaba/16号/FFFFFF */
.submit-text {
  font-family:'Alibaba','PingFang SC',Arial,sans-serif;
  font-size:16px; color:#FFFFFF; font-weight:700;
}
</style>
