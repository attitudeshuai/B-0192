<template>
  <div class="page-container">
    <el-card class="box-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="title">{{ role === 'student' ? '可选课程' : '课程管理' }}</span>
          <el-button v-if="role !== 'student'" type="primary" @click="handleAdd" icon="Plus">添加课程</el-button>
        </div>
      </template>

      <div class="toolbar">
        <el-input
          v-model="searchQuery"
          placeholder="按课程名称或课程号搜索"
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
        <el-table-column prop="courseNumber" label="课程号" width="150" sortable />
        <el-table-column prop="name" label="课程名" min-width="200" />
        <el-table-column prop="credits" label="学分" width="120" sortable>
          <template #default="scope">
             <el-tag :type="scope.row.credits >= 4 ? 'danger' : scope.row.credits >= 2 ? 'warning' : 'info'" effect="dark">
                {{ scope.row.credits }} 学分
             </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <template v-if="role === 'student'">
               <el-button 
                 size="small" 
                 type="success" 
                 plain 
                 icon="Check" 
                 @click="handleSelectCourse(scope.row)"
                 :disabled="isCourseSelected(scope.row.id)"
               >
                 {{ isCourseSelected(scope.row.id) ? '已选' : '选课' }}
               </el-button>
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
      :title="isEdit ? '编辑课程' : '添加课程'"
      width="500px"
      destroy-on-close
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="课程号" required>
          <el-input v-model="form.courseNumber" placeholder="请输入课程号" />
        </el-form-item>
        <el-form-item label="课程名" required>
          <el-input v-model="form.name" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="学分" required>
          <el-input-number v-model="form.credits" :min="0.5" :step="0.5" style="width: 100%" />
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
import { ref, onMounted } from 'vue'
import request from '../api/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete, Check } from '@element-plus/icons-vue'

const tableData = ref([])
const filteredData = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const searchQuery = ref('')
const role = ref(localStorage.getItem('role') || 'student')
const userId = ref(localStorage.getItem('userId'))
const mySelections = ref([])
const form = ref({
  id: null,
  courseNumber: '',
  name: '',
  credits: 0
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/courses')
    tableData.value = res
    filteredData.value = res
    
    if (role.value === 'student' && userId.value) {
       const selections = await request.get(`/selections/student/${userId.value}`)
       mySelections.value = selections.map(s => s.course.id)
    }
  } catch (error) {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

const isCourseSelected = (courseId) => {
  return mySelections.value.includes(courseId)
}

const handleSelectCourse = async (row) => {
  try {
    await request.post('/selections', {
       student: { id: userId.value },
       course: { id: row.id }
    })
    ElMessage.success('选课成功')
    fetchData()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '选课失败')
  }
}

const handleSearch = () => {
  if (!searchQuery.value) {
    filteredData.value = tableData.value
    return
  }
  const query = searchQuery.value.toLowerCase()
  filteredData.value = tableData.value.filter(item => 
    item.name.toLowerCase().includes(query) ||
    item.courseNumber.toLowerCase().includes(query)
  )
}

const handleAdd = () => {
  isEdit.value = false
  form.value = { id: null, courseNumber: '', name: '', credits: 0 }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该课程？相关选课记录也将被删除。', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await request.delete(`/courses/${row.id}`)
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
    if (isEdit.value) {
      await request.put(`/courses/${form.value.id}`, form.value)
    } else {
      await request.post('/courses', form.value)
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
