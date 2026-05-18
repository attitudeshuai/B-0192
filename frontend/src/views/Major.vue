<template>
  <div class="page-container">
    <el-card class="box-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="title">专业管理</span>
          <el-button type="primary" @click="handleAdd" icon="Plus">添加专业</el-button>
        </div>
      </template>

      <div class="toolbar">
        <el-input
          v-model="searchQuery"
          placeholder="按专业名称搜索"
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
        <el-table-column prop="id" label="编号" width="100" sortable />
        <el-table-column prop="name" label="专业名称" min-width="150" />
        <el-table-column prop="department" label="所属学院" min-width="150">
           <template #default="scope">
            <el-tag effect="plain" type="info">{{ scope.row.department }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" plain icon="Edit" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button
              size="small"
              type="danger"
              plain
              icon="Delete"
              @click="handleDelete(scope.row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑专业' : '添加专业'"
      width="500px"
      destroy-on-close
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="专业名称" required>
          <el-input v-model="form.name" placeholder="请输入专业名称" />
        </el-form-item>
        <el-form-item label="所属学院" required>
          <el-input v-model="form.department" placeholder="请输入所属学院" />
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
import { Search, Plus, Edit, Delete } from '@element-plus/icons-vue'

const tableData = ref([])
const filteredData = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const searchQuery = ref('')
const form = ref({
  id: null,
  name: '',
  department: ''
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/majors')
    tableData.value = res
    filteredData.value = res
  } catch (error) {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  if (!searchQuery.value) {
    filteredData.value = tableData.value
    return
  }
  const query = searchQuery.value.toLowerCase()
  filteredData.value = tableData.value.filter(item => 
    item.name.toLowerCase().includes(query)
  )
}

const handleAdd = () => {
  isEdit.value = false
  form.value = { id: null, name: '', department: '' }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该专业？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await request.delete(`/majors/${row.id}`)
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
      await request.put(`/majors/${form.value.id}`, form.value)
    } else {
      await request.post('/majors', form.value)
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
