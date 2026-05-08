<template>
  <div class="checkin-calendar">
    <!-- 日历头部：月份切换 -->
    <div class="calendar-header">
      <el-button link size="small" @click="prevMonth" :disabled="loading">
        <el-icon size="12"><ArrowLeft /></el-icon>
      </el-button>
      <span class="current-month">{{ currentYear }}.{{ currentMonth }}</span>
      <el-button link size="small" @click="nextMonth" :disabled="loading">
        <el-icon size="12"><ArrowRight /></el-icon>
      </el-button>
    </div>

    <!-- 星期标题行 -->
    <div class="calendar-weekdays">
      <span v-for="day in weekdays" :key="day" class="weekday">{{ day }}</span>
    </div>

    <!-- 日期格子 -->
    <div class="calendar-grid">
      <!-- 填充空白格子 -->
      <div v-for="n in startDayOffset" :key="'empty-' + n" class="calendar-day empty"></div>
      
      <!-- 实际日期 -->
      <div 
        v-for="day in daysInMonth" 
        :key="day" 
        class="calendar-day"
        :class="{ 
          'checked-in': isCheckedIn(day),
          'today': isToday(day)
        }"
      >
        <span class="day-number">{{ day }}</span>
      </div>
    </div>

    <!-- 打卡统计 -->
    <div class="checkin-summary">
      <span>🔥 <strong>{{ checkedInDays.length }}</strong> 天</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { workoutApi } from '@/api/workout'

const props = defineProps({
  userId: {
    type: Number,
    default: null
  }
})

// 当前显示的年月
const currentDate = ref(new Date())
const currentYear = computed(() => currentDate.value.getFullYear())
const currentMonth = computed(() => currentDate.value.getMonth() + 1)

// 打卡日期列表
const checkedInDays = ref([])

// 加载状态
const loading = ref(false)

// 星期标题
const weekdays = ['日', '一', '二', '三', '四', '五', '六']

// 计算当月有多少天
const daysInMonth = computed(() => {
  return new Date(currentYear.value, currentMonth.value, 0).getDate()
})

// 计算当月第一天是星期几
const startDayOffset = computed(() => {
  return new Date(currentYear.value, currentMonth.value - 1, 1).getDay()
})

// 判断某天是否打卡
const isCheckedIn = (day) => {
  const dateStr = `${currentYear.value}-${String(currentMonth.value).padStart(2, '0')}-${String(day).padStart(2, '0')}`
  return checkedInDays.value.includes(dateStr)
}

// 判断是否是今天
const isToday = (day) => {
  const today = new Date()
  return today.getFullYear() === currentYear.value && 
         (today.getMonth() + 1) === currentMonth.value && 
         today.getDate() === day
}

// 加载打卡数据
const loadCheckInDates = async () => {
  loading.value = true
  try {
    const dates = await workoutApi.getCheckInDates(currentYear.value, currentMonth.value)
    // 转换日期格式为字符串比较
    checkedInDays.value = (dates || []).map(d => {
      if (typeof d === 'string') return d.split('T')[0]
      if (d && d.year) {
        return `${d.year}-${String(d.month).padStart(2, '0')}-${String(d.day || d.dayOfMonth).padStart(2, '0')}`
      }
      return String(d)
    })
  } catch (e) {
    console.error('加载打卡数据失败', e)
    checkedInDays.value = []
  } finally {
    loading.value = false
  }
}

// 上个月
const prevMonth = () => {
  currentDate.value = new Date(currentYear.value, currentMonth.value - 2, 1)
  loadCheckInDates()
}

// 下个月
const nextMonth = () => {
  currentDate.value = new Date(currentYear.value, currentMonth.value, 1)
  loadCheckInDates()
}

// 监听月份变化，重新加载数据
watch(() => [currentYear.value, currentMonth.value], () => {
  loadCheckInDates()
})

// 初始加载
onMounted(() => {
  loadCheckInDates()
})
</script>

<style scoped>
.checkin-calendar {
  padding: 2px;
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.current-month {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}

.calendar-weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  margin-bottom: 2px;
}

.weekday {
  text-align: center;
  font-size: 10px;
  color: #909399;
  padding: 1px 0;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 2px;
}

.calendar-day {
  position: relative;
  width: 100%;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: all 0.2s;
}

.calendar-day.empty {
  background: transparent;
}

.calendar-day:not(.empty) {
  background: #f5f7fa;
}

.calendar-day.today {
  border: 1px solid #409EFF;
}

.calendar-day.checked-in {
  background: linear-gradient(135deg, #67C23A 0%, #85ce61 100%);
  color: white;
}

.day-number {
  font-size: 11px;
  font-weight: 500;
}

.checkin-summary {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 8px;
  padding-top: 6px;
  border-top: 1px dashed #e4e7ed;
  font-size: 11px;
  color: #606266;
}

.checkin-summary strong {
  color: #67C23A;
  font-size: 13px;
}
</style>
