<template>
  <div class="page-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>数据统计与分析</span>
        </div>
      </template>
      
      <el-tabs v-model="activeTab" class="custom-tabs">
        <el-tab-pane label="班级成绩统计" name="class">
          <div class="filter-bar">
            <el-input 
              v-model="className" 
              placeholder="输入班级名称 (如 计科1班)" 
              prefix-icon="Search"
              style="width: 240px; margin-right: 15px;"
              clearable
              @keyup.enter="fetchClassStats"
            />
            <el-button type="primary" @click="fetchClassStats" :loading="classLoading">
              <el-icon style="margin-right: 5px"><Search /></el-icon> 查询
            </el-button>
          </div>
          
          <div v-show="classStatsData.length > 0" class="chart-container">
            <div ref="classChartRef" style="width: 100%; height: 400px;"></div>
          </div>
          
          <el-table 
            :data="classStatsData" 
            style="width: 100%; margin-top: 20px" 
            v-loading="classLoading"
            stripe
            border
            header-cell-class-name="table-header"
          >
            <el-table-column prop="studentNumber" label="学号" min-width="120" />
            <el-table-column prop="name" label="姓名" min-width="100" />
            <el-table-column prop="totalGrade" label="总成绩" min-width="100" sortable>
              <template #default="scope">
                <el-tag type="info">{{ scope.row.totalGrade }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="averageGrade" label="平均成绩" min-width="100" sortable>
              <template #default="scope">
                <span :class="getGradeClass(scope.row.averageGrade)">
                  {{ scope.row.averageGrade ? scope.row.averageGrade.toFixed(2) : '-' }}
                </span>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="补考名单查询" name="makeup">
          <div class="filter-bar">
            <el-input 
              v-model="majorName" 
              placeholder="输入专业名称 (如 计算机科学与技术)" 
              prefix-icon="Search"
              style="width: 280px; margin-right: 15px;"
              clearable
              @keyup.enter="fetchMakeupStats"
            />
            <el-button type="primary" @click="fetchMakeupStats" :loading="makeupLoading">
              <el-icon style="margin-right: 5px"><Search /></el-icon> 查询
            </el-button>
          </div>
          
          <div v-show="makeupData.length > 0" class="chart-container">
             <div ref="makeupChartRef" style="width: 100%; height: 400px;"></div>
          </div>
          
          <el-table 
            :data="makeupData" 
            style="width: 100%; margin-top: 20px" 
            v-loading="makeupLoading"
            stripe
            border
            header-cell-class-name="table-header"
          >
            <el-table-column prop="studentNumber" label="学号" min-width="120" />
            <el-table-column prop="studentName" label="姓名" min-width="100" />
            <el-table-column prop="className" label="班级" min-width="120" />
            <el-table-column prop="courseName" label="课程" min-width="150" />
            <el-table-column prop="grade" label="成绩" min-width="80">
              <template #default="scope">
                <el-tag type="danger">{{ scope.row.grade }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="majorName" label="专业" min-width="150" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, nextTick, onUnmounted } from 'vue'
import request from '../api/request'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const activeTab = ref('class')

// Charts
const classChartRef = ref(null)
const makeupChartRef = ref(null)
let classChart = null
let makeupChart = null

// Class Stats
const className = ref('')
const classStatsData = ref([])
const classLoading = ref(false)

const getGradeClass = (grade) => {
  if (!grade) return ''
  if (grade >= 90) return 'text-success'
  if (grade < 60) return 'text-danger'
  return 'text-primary'
}

const fetchClassStats = async () => {
  if (!className.value) {
    ElMessage.warning('请输入班级名称')
    return
  }
  classLoading.value = true
  try {
    const res = await request.get('/students/stats', {
      params: { className: className.value }
    })
    classStatsData.value = res
    
    // Render Chart
    if (res.length > 0) {
      await nextTick()
      initClassChart(res)
    }
  } catch (error) {
    ElMessage.error('获取班级统计失败')
  } finally {
    classLoading.value = false
  }
}

const initClassChart = (data) => {
  if (classChart) classChart.dispose()
  if (!classChartRef.value) return
  
  classChart = echarts.init(classChartRef.value)
  
  const names = data.map(item => item.name)
  const avgs = data.map(item => item.averageGrade || 0)
  
  const option = {
    title: { text: '班级学生平均成绩分布', left: 'center' },
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { 
      type: 'category', 
      data: names,
      axisLabel: { interval: 0, rotate: 30 }
    },
    yAxis: { type: 'value', name: '分数' },
    series: [
      {
        name: '平均成绩',
        type: 'bar',
        data: avgs,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' }
          ])
        },
        showBackground: true,
        backgroundStyle: { color: 'rgba(180, 180, 180, 0.2)' }
      }
    ]
  }
  classChart.setOption(option)
}

// Makeup Exams
const majorName = ref('')
const makeupData = ref([])
const makeupLoading = ref(false)

const fetchMakeupStats = async () => {
  if (!majorName.value) {
    ElMessage.warning('请输入专业名称')
    return
  }
  makeupLoading.value = true
  try {
    const res = await request.get('/selections/makeup', {
      params: { majorName: majorName.value }
    })
    makeupData.value = res
    
    // Render Chart
    if (res.length > 0) {
      await nextTick()
      initMakeupChart(res)
    }
  } catch (error) {
    ElMessage.error('获取补考名单失败')
  } finally {
    makeupLoading.value = false
  }
}

const initMakeupChart = (data) => {
  if (makeupChart) makeupChart.dispose()
  if (!makeupChartRef.value) return
  
  makeupChart = echarts.init(makeupChartRef.value)
  
  // Count by course
  const courseCount = {}
  data.forEach(item => {
    courseCount[item.courseName] = (courseCount[item.courseName] || 0) + 1
  })
  
  const chartData = Object.keys(courseCount).map(key => ({
    name: key,
    value: courseCount[key]
  }))
  
  const option = {
    title: { text: '各课程挂科人数分布', left: 'center' },
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', left: 'left' },
    series: [
      {
        name: '挂科人数',
        type: 'pie',
        radius: '50%',
        data: chartData,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  makeupChart.setOption(option)
}

// Resize charts on window resize
window.addEventListener('resize', () => {
  classChart?.resize()
  makeupChart?.resize()
})

onUnmounted(() => {
  classChart?.dispose()
  makeupChart?.dispose()
})
</script>

<style scoped>
.page-container {
  padding: 0;
}

.box-card {
  border-radius: 8px;
  border: none;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.card-header {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.filter-bar {
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  background: #f8f9fa;
  padding: 16px;
  border-radius: 6px;
}

.chart-container {
  margin: 20px 0;
  padding: 20px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.text-success {
  color: #67C23A;
  font-weight: bold;
}
.text-danger {
  color: #F56C6C;
  font-weight: bold;
}
.text-primary {
  color: #409EFF;
  font-weight: bold;
}

/* Deep selector for table header */
:deep(.table-header) {
  background-color: #f5f7fa !important;
  color: #606266;
  font-weight: 600;
}
</style>
