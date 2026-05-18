<template>
  <div class="login-container">
    <div class="left-panel">
      <div class="brand-content">
        <div class="logo-wrapper">
          <div class="logo-icon">
            <el-icon :size="40" color="#fff"><Monitor /></el-icon>
          </div>
          <span class="brand-name">EduSystem</span>
        </div>
        <div class="hero-text">
          <h1>学生选课管理系统</h1>
          <p>智能化 · 高效 · 便捷</p>
          <p class="sub-desc">致力于提供最优质的教务管理体验，让选课变得简单。</p>
        </div>
        <div class="decoration-circles">
          <div class="circle c1"></div>
          <div class="circle c2"></div>
          <div class="circle c3"></div>
        </div>
      </div>
    </div>
    
    <div class="right-panel">
      <div class="login-box">
        <div class="login-header">
          <h2>欢迎登录</h2>
          <p class="subtitle">请输入您的账号和密码开始使用</p>
        </div>
        
        <el-form :model="loginForm" :rules="rules" ref="loginFormRef" size="large" class="login-form">
          <el-form-item prop="username">
            <div class="input-label">账号</div>
            <el-input 
              v-model="loginForm.username" 
              placeholder="请输入用户名/学号" 
              :prefix-icon="User"
              class="custom-input"
            />
          </el-form-item>
          <el-form-item prop="password">
            <div class="input-label">密码</div>
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              show-password
              @keyup.enter="handleLogin"
              class="custom-input"
            />
          </el-form-item>
          
          <div class="form-options">
            <el-checkbox v-model="rememberMe">记住我</el-checkbox>
          </div>

          <el-form-item>
            <el-button type="primary" :loading="loading" @click="handleLogin" class="login-btn">
              立即登录
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../api/request'
import { User, Lock, Monitor } from '@element-plus/icons-vue'

const router = useRouter()
const loginFormRef = ref(null)
const loading = ref(false)
const rememberMe = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await request.post('/auth/login', loginForm)
        localStorage.setItem('token', res.token)
        localStorage.setItem('username', res.username)
        localStorage.setItem('role', res.role)
        if (res.userId) {
          localStorage.setItem('userId', res.userId)
        }
        
        ElMessage.success('登录成功')
        
        // Redirect based on role
        if (res.role === 'student') {
          router.push('/courses')
        } else {
          router.push('/students')
        }
      } catch (error) {
        console.error('Login error:', error)
        const msg = error.response?.data || '登录失败，请检查网络或账号密码'
        // Ensure msg is a string
        const displayMsg = typeof msg === 'object' ? JSON.stringify(msg) : msg
        ElMessage.error(displayMsg)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  width: 100vw;
  display: flex;
  overflow: hidden;
  background: #fff;
}

/* Left Panel */
.left-panel {
  width: 45%;
  background: linear-gradient(135deg, #2c3e50 0%, #3498db 100%);
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 0 80px;
  color: #fff;
  overflow: hidden;
}

.brand-content {
  position: relative;
  z-index: 2;
}

.logo-wrapper {
  display: flex;
  align-items: center;
  margin-bottom: 60px;
}

.logo-icon {
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
  margin-right: 15px;
}

.brand-name {
  font-size: 28px;
  font-weight: 700;
  letter-spacing: 1px;
}

.hero-text h1 {
  font-size: 48px;
  font-weight: 800;
  margin-bottom: 20px;
  line-height: 1.2;
}

.hero-text p {
  font-size: 20px;
  opacity: 0.9;
  margin-bottom: 10px;
}

.hero-text .sub-desc {
  font-size: 16px;
  opacity: 0.7;
  font-weight: 300;
  max-width: 400px;
  margin-top: 20px;
  line-height: 1.6;
}

/* Decorative Circles */
.decoration-circles .circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.05);
}

.c1 {
  width: 300px;
  height: 300px;
  top: -50px;
  right: -50px;
}

.c2 {
  width: 500px;
  height: 500px;
  bottom: -100px;
  left: -100px;
}

.c3 {
  width: 100px;
  height: 100px;
  top: 40%;
  right: 20%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 6s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-20px); }
}

/* Right Panel */
.right-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
}

.login-box {
  width: 100%;
  max-width: 400px;
  padding: 40px;
  animation: fadeIn 0.8s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.login-header {
  margin-bottom: 40px;
}

.login-header h2 {
  font-size: 32px;
  color: #1a1a1a;
  margin-bottom: 10px;
  font-weight: 600;
}

.subtitle {
  color: #909399;
  font-size: 14px;
}

.input-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
  font-weight: 500;
}

.custom-input :deep(.el-input__wrapper) {
  padding: 12px 15px;
  box-shadow: 0 0 0 1px #e4e7ed inset;
  transition: all 0.3s;
  background: #fcfcfc;
}

.custom-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c0c4cc inset;
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #409EFF inset !important;
  background: #fff;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 8px;
  background: linear-gradient(90deg, #409EFF 0%, #3a8ee6 100%);
  border: none;
  transition: all 0.3s;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(64, 158, 255, 0.3);
}

.login-btn:active {
  transform: translateY(0);
}

/* Responsive */
@media (max-width: 900px) {
  .left-panel {
    display: none;
  }
  
  .right-panel {
    background: #f5f7fa;
  }
  
  .login-box {
    background: #fff;
    border-radius: 16px;
    box-shadow: 0 8px 30px rgba(0,0,0,0.05);
  }
}
</style>
