<template>
  <div class="common-layout">
    <el-container>
      <el-aside width="240px" class="aside-container">
        <div class="logo">
          <el-icon :size="28" color="#409EFF" style="margin-right: 10px"><Monitor /></el-icon>
          <h2>选课管理系统</h2>
        </div>
        <el-menu
          router
          :default-active="$route.path"
          class="el-menu-vertical-demo"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
        >
<template v-if="role === 'admin'">
          <el-menu-item index="/students">
            <el-icon><User /></el-icon>
            <template #title>学生管理</template>
          </el-menu-item>
          <el-menu-item index="/majors">
            <el-icon><School /></el-icon>
            <template #title>专业管理</template>
          </el-menu-item>
          <el-menu-item index="/courses">
            <el-icon><Reading /></el-icon>
            <template #title>课程管理</template>
          </el-menu-item>
          <el-menu-item index="/selections">
            <el-icon><Notebook /></el-icon>
            <template #title>选课管理</template>
          </el-menu-item>
          <el-menu-item index="/stats">
            <el-icon><DataLine /></el-icon>
            <template #title>统计分析</template>
          </el-menu-item>
        </template>

        <template v-else>
          <el-menu-item index="/courses">
            <el-icon><Reading /></el-icon>
            <template #title>可选课程</template>
          </el-menu-item>
          <el-menu-item index="/selections">
            <el-icon><Notebook /></el-icon>
            <template #title>我的选课</template>
          </el-menu-item>
        </template>
        </el-menu>
      </el-aside>
      <el-container class="main-container">
        <el-header height="60px">
          <div class="header-left">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item>{{ $route.meta.title || '控制台' }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="header-right">
            <el-dropdown trigger="click">
              <div class="user-info">
                <el-avatar :size="32" style="background: #409EFF; margin-right: 8px">
                  {{ username.charAt(0).toUpperCase() }}
                </el-avatar>
                <span class="username">{{ username }}</span>
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-if="role === 'student'" @click="handleChangePassword">
                    <el-icon><Lock /></el-icon>修改密码
                  </el-dropdown-item>
                  <el-dropdown-item @click="handleLogout">
                    <el-icon><SwitchButton /></el-icon>退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        <el-main>
          <router-view v-slot="{ Component }">
            <transition name="fade-transform" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </el-main>
      </el-container>
    </el-container>

    <el-dialog
      v-model="passwordDialogVisible"
      title="修改密码"
      width="400px"
      destroy-on-close
    >
      <el-form :model="passwordForm" ref="passwordFormRef" :rules="passwordRules" label-width="100px">
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="passwordDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitPasswordChange">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const router = useRouter()
const username = ref(localStorage.getItem('username') || 'Admin')
const role = ref(localStorage.getItem('role') || 'student')
const userId = ref(localStorage.getItem('userId'))

const passwordDialogVisible = ref(false)
const passwordFormRef = ref(null)
const passwordForm = reactive({
  newPassword: '',
  confirmPassword: ''
})

const passwordRules = {
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const handleChangePassword = () => {
  passwordDialogVisible.value = true
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
}

const submitPasswordChange = async () => {
  if (!passwordFormRef.value) return
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await request.put(`/students/${userId.value}/password`, passwordForm.newPassword, {
           headers: { 'Content-Type': 'text/plain' }
        })
        ElMessage.success('密码修改成功')
        passwordDialogVisible.value = false
      } catch (error) {
        ElMessage.error('密码修改失败')
      }
    }
  })
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('role')
  localStorage.removeItem('userId')
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.common-layout {
  height: 100vh;
  background-color: #f0f2f5;
}

.el-container {
  height: 100%;
}

.aside-container {
  background-color: #304156;
  color: #fff;
  transition: width 0.3s;
  box-shadow: 2px 0 6px rgba(0,21,41,.35);
  z-index: 10;
  overflow-x: hidden;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #2b2f3a;
  overflow: hidden;
}

.logo h2 {
  margin: 0;
  color: #fff;
  font-size: 20px;
  font-weight: 600;
  white-space: nowrap;
}

.el-menu {
  border-right: none;
}

.main-container {
  flex-direction: column;
}

.el-header {
  background-color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
  z-index: 9;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 0 8px;
  border-radius: 4px;
  transition: all 0.3s;
}

.user-info:hover {
  background: #f5f7fa;
}

.username {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.el-main {
  padding: 20px;
  background-color: #f0f2f5;
  overflow-y: auto;
}

/* Transition Animations */
.fade-transform-leave-active,
.fade-transform-enter-active {
  transition: all 0.5s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>
