<template>
  <div class="diet-container">
    <el-row :gutter="24">
      <el-col :span="16">
        <el-card shadow="never" class="diet-main-card">
          <template #header>
            <div class="card-header">
              <span class="title">今日饮食记录</span>
              <el-button type="success" @click="isAddMealVisible = true">+ 手动添加</el-button>
            </div>
          </template>

          <el-table :data="todayMeals" style="width: 100%">
            <el-table-column prop="time" label="时间" width="100" />
            <el-table-column prop="type" label="餐次" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.tagType">{{ scope.row.type }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="name" label="食物名称" />
            <el-table-column prop="calories" label="热量" width="120">
              <template #default="scope">{{ scope.row.calories }} kcal</template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default>
                <el-button link type="danger">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="never" class="ai-vision-card">
          <div class="ai-vision-header">
            <el-icon class="ai-icon"><CameraFilled /></el-icon>
            <h3>AI 拍照识餐</h3>
            <p>上传餐盘照片，AI 自动估算热量与营养</p>
          </div>
          
          <el-upload
            class="meal-uploader"
            drag
            action="#"
            :auto-upload="false"
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">将照片拖到此处，或<em>点击上传</em></div>
          </el-upload>

          <div class="ai-analysis-result" v-if="showResult">
            <el-divider content-position="center">AI 实时分析</el-divider>
            <p><strong>识别结果：</strong> 煎鸡胸肉、水煮西兰花、糙米饭</p>
            <p><strong>预估热量：</strong> 450 kcal</p>
            <el-button type="success" style="width: 100%; margin-top: 10px;">一键记录至午餐</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { CameraFilled, UploadFilled } from '@element-plus/icons-vue'

const showResult = ref(true) // 演示用
const todayMeals = ref([
  { time: '08:00', type: '早餐', name: '全麦面包, 燕麦片, 煮鸡蛋', calories: 350, tagType: 'info' },
  { time: '12:30', type: '午餐', name: '鸡胸肉沙拉, 紫薯', calories: 520, tagType: 'success' },
  { time: '16:00', type: '加餐', name: '蛋白粉, 香蕉', calories: 210, tagType: 'warning' }
])
</script>

<style scoped>
.diet-container { max-width: 1400px; margin: 0 auto; }
.diet-main-card { border-radius: 16px; border: none; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.title { font-size: 20px; font-weight: bold; }

.ai-vision-card { border-radius: 16px; border: none; background: linear-gradient(to bottom, #f0f9eb, #ffffff); text-align: center; }
.ai-icon { font-size: 48px; color: #67C23A; margin-bottom: 10px; }
.ai-vision-header h3 { margin: 10px 0; color: #303133; }
.ai-vision-header p { font-size: 13px; color: #909399; margin-bottom: 20px; }

.ai-analysis-result { text-align: left; font-size: 14px; color: #606266; }
</style>