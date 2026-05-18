<template>
  <div class="page-container">
    <el-card class="box-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="title">学生管理</span>
          <template v-if="role === 'admin'">
            <div class="header-actions">
              <el-button type="primary" @click="handleAdd" icon="Plus">添加学生</el-button>
              <el-button type="success" @click="handleBatchAdd" icon="Upload">批量导入</el-button>
            </div>
          </template>
        </div>
      </template>
      
      <div class="toolbar">
        <el-input
          v-model="searchQuery"
          placeholder="按姓名或学号搜索"
          style="width: 300px; margin-right: 15px;"
          clearable
          @clear="handleSearch"
          @keyup.enter="handleSearch"
          prefix-icon="Search"
        />
        <el-button type="primary" @click="handleSearch" icon="Search">搜索</el-button>
      </div>
    
      <el-table 
        :data="filteredData" 
        style="width: 100%" 
        v-loading="loading"
        stripe
        border
        highlight-current-row
      >
        <el-table-column prop="studentNumber" label="学号" width="140" sortable />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.gender === '男' ? '' : 'danger'" disable-transitions>{{ scope.row.gender }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="birthDate" label="出生日期" width="140" sortable />
        <el-table-column prop="hometown" label="籍贯" />
        <el-table-column prop="className" label="班级" width="140" sortable />
        <el-table-column prop="major.name" label="专业" width="180">
          <template #default="scope">
            <el-tag type="success" effect="plain">{{ scope.row.major ? scope.row.major.name : '未分配' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <template v-if="role === 'admin'">
              <el-button size="small" type="primary" plain icon="Edit" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button
                size="small"
                type="danger"
                plain
                icon="Delete"
                @click="handleDelete(scope.row)"
              >删除</el-button>
            </template>
            <template v-else>
               <el-button size="small" type="primary" plain icon="Key" @click="handleChangePassword(scope.row)">修改密码</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑学生' : '添加学生'"
      width="600px"
      destroy-on-close
    >
      <el-form :model="form" label-width="100px" class="demo-ruleForm">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学号" required>
              <el-input v-model="form.studentNumber" placeholder="请输入学号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" required>
              <el-input v-model="form.name" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
             <el-form-item label="性别">
              <el-select v-model="form.gender" placeholder="请选择性别" style="width: 100%">
                <el-option label="男" value="男" />
                <el-option label="女" value="女" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
             <el-form-item label="出生日期">
              <el-date-picker 
                v-model="form.birthDate" 
                type="date" 
                value-format="YYYY-MM-DD" 
                placeholder="选择日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
           <el-col :span="12">
            <el-form-item label="籍贯">
              <el-input v-model="form.hometown" placeholder="请输入籍贯" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
             <el-form-item label="班级">
              <el-input v-model="form.className" placeholder="请输入班级" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="专业" required>
          <el-select v-model="form.majorId" placeholder="请选择专业" style="width: 100%">
            <el-option
              v-for="item in majors"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSave">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog
      v-model="batchDialogVisible"
      title="批量导入学生"
      width="500px"
      destroy-on-close
    >
      <div style="text-align: center;">
        <el-upload
          class="upload-demo"
          drag
          action=""
          :auto-upload="false"
          :on-change="handleFileChange"
          :limit="1"
          accept=".xlsx, .xls"
        >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">
            Drop file here or <em>click to upload</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              请上传 Excel 文件，包含列：学号, 姓名, 性别, 出生日期, 籍贯, 班级, 专业名称, 密码(可选)
            </div>
          </template>
        </el-upload>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="batchDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleBatchUpload" :loading="batchLoading">导入</el-button>
        </span>
      </template>
    </el-dialog>
    <el-dialog
      v-model="passwordDialogVisible"
      title="修改密码"
      width="400px"
      destroy-on-close
    >
      <el-form :model="passwordForm" label-width="100px" :rules="passwordRules" ref="passwordFormRef">
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
import { ref, onMounted } from 'vue'
import request from '../api/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete, UploadFilled, Upload, Key } from '@element-plus/icons-vue'
import * as XLSX from 'xlsx'

const tableData = ref([])
const filteredData = ref([])
const majors = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const batchDialogVisible = ref(false)
const passwordDialogVisible = ref(false)
const batchLoading = ref(false)
const uploadFile = ref(null)
const isEdit = ref(false)
const searchQuery = ref('')
const passwordFormRef = ref(null)

const passwordForm = ref({
  id: null,
  newPassword: '',
  confirmPassword: ''
})

const passwordRules = {
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, message: '密码长度至少6位', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.value.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}
const form = ref({
  id: null,
  studentNumber: '',
  name: '',
  gender: '',
  birthDate: '',
  hometown: '',
  className: '',
  majorId: null
})

const role = ref(localStorage.getItem('role'))

const fetchData = async () => {
  loading.value = true
  try {
    const [studentsRes, majorsRes] = await Promise.all([
      request.get('/students'),
      request.get('/majors')
    ])
    tableData.value = studentsRes
    
    // Initial filtering based on role
    const currentUserId = localStorage.getItem('userId')
    if (role.value === 'student' && currentUserId) {
      filteredData.value = studentsRes.filter(item => String(item.id) === String(currentUserId))
    } else {
      filteredData.value = studentsRes
    }
    
    majors.value = majorsRes
  } catch (error) {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  const role = localStorage.getItem('role')
  const currentUserId = localStorage.getItem('userId')

  if (role === 'student' && currentUserId) {
    // Student can only see their own info
    filteredData.value = tableData.value.filter(item => String(item.id) === String(currentUserId))
  } else if (!searchQuery.value) {
    filteredData.value = tableData.value
    return
  } else {
    // Admin search logic
    const query = searchQuery.value.toLowerCase()
    filteredData.value = tableData.value.filter(item => 
      item.name.toLowerCase().includes(query) || 
      item.studentNumber.toLowerCase().includes(query)
    )
  }
}

const handleAdd = () => {
  isEdit.value = false
  form.value = {
    id: null,
    studentNumber: '',
    name: '',
    gender: '',
    birthDate: '',
    hometown: '',
    className: '',
    majorId: null
  }
  dialogVisible.value = true
}

const handleBatchAdd = () => {
  batchDialogVisible.value = true
  uploadFile.value = null
}

const handleFileChange = (file) => {
  uploadFile.value = file.raw
}

const handleBatchUpload = async () => {
  if (!uploadFile.value) {
    ElMessage.warning('请选择文件')
    return
  }
  
  batchLoading.value = true
  const reader = new FileReader()
  reader.onload = async (e) => {
    try {
      const data = new Uint8Array(e.target.result)
      const workbook = XLSX.read(data, { type: 'array' })
      const firstSheetName = workbook.SheetNames[0]
      const worksheet = workbook.Sheets[firstSheetName]
      const jsonData = XLSX.utils.sheet_to_json(worksheet)
      
      const students = jsonData.map(item => {
        // Find major by name
        const major = majors.value.find(m => m.name === (item['专业'] || item['专业名称']))
        return {
          studentNumber: String(item['学号'] || ''),
          name: item['姓名'] || '',
          gender: item['性别'] || '',
          birthDate: item['出生日期'] || null,
          hometown: item['籍贯'] || '',
          className: item['班级'] || '',
          password: String(item['密码'] || '123456'), // Default password
          major: major ? { id: major.id } : null
        }
      })

      if (students.length === 0) {
        ElMessage.warning('文件内容为空或格式不正确')
        batchLoading.value = false
        return
      }

      await request.post('/students/batch', students)
      ElMessage.success(`成功导入 ${students.length} 名学生`)
      batchDialogVisible.value = false
      fetchData()
    } catch (error) {
      console.error(error)
      ElMessage.error('导入失败，请检查文件格式')
    } finally {
      batchLoading.value = false
    }
  }
  reader.readAsArrayBuffer(uploadFile.value)
}

const handleEdit = (row) => {
  isEdit.value = true
  form.value = { 
    ...row,
    majorId: row.major ? row.major.id : null
  }
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该学生？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await request.delete(`/students/${row.id}`)
      ElMessage.success('删除成功')
      fetchData()
    } catch (error) {
      const msg = error.response?.data?.message || '删除失败'
      ElMessage.error(msg)
    }
  })
}

const handleSave = async () => {
  try {
    const payload = {
      ...form.value,
      major: { id: form.value.majorId }
    }
    if (isEdit.value) {
      await request.put(`/students/${form.value.id}`, payload)
    } else {
      await request.post('/students', payload)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}
.box-card {
  min-height: calc(100vh - 120px);
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.toolbar {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}
</style>
