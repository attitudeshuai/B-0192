<template>
  <div class="page-container">
    <el-card class="box-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="title">{{ role === 'student' ? '我的选课' : '选课管理' }}</span>
          <el-button v-if="role !== 'student'" type="primary" @click="handleAdd" icon="Plus">添加选课</el-button>
        </div>
      </template>

      <div class="toolbar">
        <el-input
          v-model="searchQuery"
          :placeholder="role === 'student' ? '搜索课程' : '搜索学生或课程'"
          style="width: 300px; margin-right: 15px;"
          clearable
          prefix-icon="Search"
        />
      </div>
    
      <el-table 
        :data="filteredData" 
        style="width: 100%" 
        v-loading="loading"
        stripe
        border
        highlight-current-row
      >
        <el-table-column v-if="role !== 'student'" prop="student.studentNumber" label="学号" width="140" sortable />
        <el-table-column v-if="role !== 'student'" prop="student.name" label="姓名" width="120" />
        <el-table-column prop="course.courseNumber" label="课程号" width="140" sortable />
        <el-table-column prop="course.name" label="课程名" min-width="180" />
        <el-table-column prop="course.credits" label="学分" width="80" />
        <el-table-column prop="grade" label="成绩" width="120" sortable>
          <template #default="scope">
            <el-tag 
              v-if="scope.row.grade !== null"
              :type="scope.row.grade >= 60 ? 'success' : 'danger'"
              effect="dark"
            >
              {{ scope.row.grade }}
            </el-tag>
            <el-tag v-else type="info" effect="plain">未录入</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <template v-if="role === 'student'">
               <el-button
                 size="small"
                 type="danger"
                 plain
                 icon="Delete"
                 @click="handleDelete(scope.row)"
                 :disabled="scope.row.grade !== null"
               >退选</el-button>
            </template>
            <template v-else>
              <el-button size="small" type="primary" plain icon="Edit" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button
                size="small"
                type="danger"
                plain
                icon="Delete"
                @click="handleDelete(scope.row)"
              >删除</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog 
      v-if="role !== 'student'"
      v-model="dialogVisible" 
      :title="isEdit ? '编辑选课' : '添加选课'"
      width="500px"
      destroy-on-close
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="学生" required>
          <el-select 
            v-model="form.studentId" 
            filterable 
            placeholder="搜索学生" 
            style="width: 100%"
            :disabled="isEdit"
          >
            <el-option
              v-for="item in students"
              :key="item.id"
              :label="item.studentNumber + ' - ' + item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="课程" required>
          <el-select 
            v-model="form.courseId" 
            filterable 
            placeholder="搜索课程" 
            style="width: 100%"
            :disabled="isEdit"
          >
            <el-option
              v-for="item in courses"
              :key="item.id"
              :label="item.courseNumber + ' - ' + item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="成绩">
          <el-input-number v-model="form.grade" :min="0" :max="100" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSave">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import request from '../api/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete } from '@element-plus/icons-vue'

const tableData = ref([])
const students = ref([])
const courses = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const searchQuery = ref('')
const role = ref(localStorage.getItem('role') || 'student')
const userId = ref(localStorage.getItem('userId'))

const form = ref({
  id: null,
  studentId: null,
  courseId: null,
  grade: null
})

const filteredData = computed(() => {
  if (!searchQuery.value) return tableData.value
  const query = searchQuery.value.toLowerCase()
  return tableData.value.filter(item => 
    (item.student && (item.student.name.toLowerCase().includes(query) || item.student.studentNumber.includes(query))) ||
    (item.course && (item.course.name.toLowerCase().includes(query) || item.course.courseNumber.includes(query)))
  )
})

const fetchData = async () => {
  loading.value = true
  try {
    if (role.value === 'student' && userId.value) {
       // Students only see their own selections
       const selRes = await request.get(`/selections/student/${userId.value}`)
       tableData.value = selRes
    } else {
       // Admins see all
       const [selRes, stuRes, couRes] = await Promise.all([
         request.get('/selections'),
         request.get('/students'),
         request.get('/courses')
       ])
       tableData.value = selRes
       students.value = stuRes
       courses.value = couRes
    }
  } catch (error) {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}


const handleAdd = () => {
  isEdit.value = false
  form.value = { id: null, studentId: null, courseId: null, grade: null }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  form.value = { 
    id: row.id,
    studentId: row.student.id,
    courseId: row.course.id,
    grade: row.grade
  }
  dialogVisible.value = true
}

const handleDelete = (row) => {
  const msg = role.value === 'student' ? '确认退选该课程？' : '确认删除该选课记录？'
  ElMessageBox.confirm(msg, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await request.delete(`/selections/${row.id}`)
      ElMessage.success(role.value === 'student' ? '退选成功' : '删除成功')
      fetchData()
    } catch (error) {
      const msg = error.response?.data?.message || '操作失败'
      ElMessage.error(msg)
    }
  })
}

const handleSave = async () => {
  try {
    const payload = {
      id: form.value.id,
      student: { id: form.value.studentId },
      course: { id: form.value.courseId },
      grade: form.value.grade
    }
    if (isEdit.value) {
      await request.put(`/selections/${form.value.id}`, payload)
    } else {
      await request.post('/selections', payload)
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
