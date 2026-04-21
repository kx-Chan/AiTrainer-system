<template>
  <div class="diet-page">
    <div class="diet-left">
      <div class="date-picker-bar">
        <div class="date-picker-left">
          <el-icon :size="20" color="#409EFF">
            <Calendar />
          </el-icon>
          <span class="date-label">日期选择</span>
        </div>
        <el-date-picker v-model="selectedDate" type="date" placeholder="选择日期" format="YYYY-MM-DD"
          value-format="YYYY-MM-DD" @change="loadSummary" size="default" style="width:180px" />
      </div>

      <el-card shadow="hover" class="diet-card">
        <div class="card-header-fancy">
          <div class="header-left">
            <div class="header-icon">🍽️</div>
            <h3 class="card-title">饮食记录</h3>
          </div>
          <el-button type="primary" round size="default" class="header-action-btn" @click="showAddDialog">
            <el-icon style="margin-right:4px">
              <Plus />
            </el-icon>记录饮食
          </el-button>
        </div>
        <!-- 按餐次分组的饮食记录 -->
        <div class="meal-grouped-list" v-if="groupedMeals && Object.keys(groupedMeals).length">
          <div class="meal-group" v-for="(meals, mealType) in groupedMeals" :key="mealType">
            <div class="meal-group-header">
              <el-tag :type="mealType.startsWith('snack') ? 'warning' : meals[0].tagType" size="default" effect="dark"
                round>
                {{ getMealTypeIcon(mealType) }} {{ getMealTypeName(mealType) }}
              </el-tag>
              <span class="meal-group-time">{{ meals[0].time }}</span>
              <span class="meal-group-total">
                共 {{ meals.length }} 道菜 · {{ getMealGroupCalories(meals) }} kcal
              </span>
            </div>
            <div class="meal-group-dishes">
              <div class="meal-dish-item" v-for="meal in meals" :key="meal.id">
                <div class="dish-left">
                  <span class="dish-name">{{ meal.foodName }}</span>
                  <span class="dish-weight" v-if="meal.weight">{{ meal.weight }}g</span>
                  <div class="dish-nutrition">
                    <span class="nutrition-tag protein">蛋白质 {{ meal.protein || 0 }}g</span>
                    <span class="nutrition-tag fat">脂肪 {{ meal.fat || 0 }}g</span>
                    <span class="nutrition-tag carbs">碳水 {{ meal.carbs || 0 }}g</span>
                  </div>
                </div>
                <div class="dish-right">
                  <span class="dish-cal">{{ meal.calories }} kcal</span>
                  <div class="meal-actions">
                    <el-tooltip content="编辑记录" placement="top">
                      <el-button type="primary" link class="action-btn" @click="showEditDialog(meal)"><el-icon
                          :size="18">
                          <Edit />
                        </el-icon></el-button>
                    </el-tooltip>
                    <el-tooltip content="删除记录" placement="top">
                      <el-button type="danger" link class="action-btn" @click="handleDelete(meal.id)"><el-icon
                          :size="18">
                          <Delete />
                        </el-icon></el-button>
                    </el-tooltip>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无饮食记录，点击上方添加" :image-size="60" />
      </el-card>

      <el-card shadow="hover" class="exercise-card" style="margin-top:16px">
        <div class="card-header-fancy">
          <div class="header-left">
            <div class="header-icon">🏃</div>
            <h3 class="card-title">额外运动消耗</h3>
          </div>
          <el-button type="success" round size="default" class="header-action-btn" @click="showExerciseDialog">
            <el-icon style="margin-right:4px">
              <Plus />
            </el-icon>记录运动
          </el-button>
        </div>
        <div class="exercise-list" v-if="(summary.extraExercises || []).length">
          <div class="exercise-item" v-for="ex in summary.extraExercises" :key="ex.id">
            <div class="exercise-info">
              <span class="exercise-name">{{ ex.exerciseName }}</span>
              <span class="exercise-detail"><el-icon>
                  <Timer />
                </el-icon> {{ ex.durationMinutes }} 分钟</span>
            </div>
            <div class="exercise-right">
              <span class="exercise-cal">-{{ ex.caloriesBurned }} <small>kcal</small></span>
              <div class="meal-actions">
                <el-button type="primary" link class="action-btn" @click="showEditExerciseDialog(ex)"><el-icon
                    :size="20">
                    <Edit />
                  </el-icon></el-button>
                <el-button type="danger" link class="action-btn" @click="handleDeleteExercise(ex.id)"><el-icon
                    :size="20">
                    <Delete />
                  </el-icon></el-button>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无额外运动记录" :image-size="50" />
      </el-card>

      <el-card shadow="hover" class="workout-card" style="margin-top:16px"
        v-if="(summary.workoutDetails || []).length > 0">
        <div class="card-header-fancy">
          <div class="header-left">
            <div class="header-icon">🏋️</div>
            <h3 class="card-title">项目训练消耗明细</h3>
          </div>
        </div>
        <div class="exercise-list">
          <div class="exercise-item" v-for="(w, idx) in summary.workoutDetails" :key="idx">
            <div class="exercise-info">
              <span class="exercise-name">{{ w.workoutName }}</span>
              <span class="exercise-detail"><el-icon>
                  <Timer />
                </el-icon> {{ w.durationMinutes }} 分钟</span>
            </div>
            <div class="exercise-right">
              <span class="exercise-cal">-{{ w.caloriesBurned }} <small>kcal</small></span>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <div class="diet-right">
      <el-card shadow="never">
        <h3 style="text-align:center;margin:0 0 8px">今日热量概览</h3>
        <div ref="chartRef" class="chart-box"></div>
        <div class="calorie-info">
          <div class="info-row">
            <el-tooltip placement="left" :show-after="300" popper-class="science-tooltip" effect="light">
              <template #content>
                <div class="tip-title">📖 基础代谢率 (BMR)</div>
                <div class="tip-body">基础代谢是人体在安静状态下维持生命所需的最低热量。</div>
              </template>
              <span class="info-label-with-tip">基础代谢 <el-icon :size="14" style="vertical-align:middle;color:#409EFF">
                  <QuestionFilled />
                </el-icon></span>
            </el-tooltip>
            <span>{{ summary.bmrCalories }} kcal</span>
          </div>
          <div class="info-row"><span>训练消耗</span><span>{{ summary.workoutBurnedCalories }} kcal</span></div>
          <div class="info-row"><span>额外运动消耗</span><span>{{ summary.extraBurnedCalories }} kcal</span></div>
          <div class="info-row"><span>目标摄入</span><span class="primary">{{ summary.targetCalories }} kcal</span></div>
          <div class="info-row"><span>已摄入</span><span class="warn">{{ summary.totalIntakeCalories }} kcal</span></div>
          <div class="info-row remain">
            <span>{{ summary.remainingCalories >= 0 ? '还可摄入' : '已超标' }}</span>
            <span :class="summary.remainingCalories >= 0 ? 'success' : 'danger'">{{ Math.abs(summary.remainingCalories)
              }}
              kcal</span>
          </div>
          <div class="info-row"><span>健身目标</span><span>{{ goalText }}</span></div>
        </div>
      </el-card>
      <el-card shadow="never" style="margin-top:16px">
        <h3 style="text-align:center;margin:0 0 8px">🍽️ AI 拍照识餐</h3>
        <p style="color:#999;text-align:center;font-size:13px">上传餐盘照片，AI 自动估算热量与营养（即将上线）</p>
        <div class="upload-placeholder">
          <el-icon :size="40" color="#c0c4cc">
            <UploadFilled />
          </el-icon>
          <p style="color:#999;font-size:13px">将照片拖到此处，或<el-link type="primary">点击上传</el-link></p>
        </div>
      </el-card>
    </div>

    <!-- 添加多菜品弹窗 -->
    <el-dialog v-model="addDialogVisible" title="🍽️ 添加饮食记录" width="600px" destroy-on-close>
      <div class="add-meal-form">
        <div class="meal-type-row">
          <span class="form-label">餐次</span>
          <el-radio-group v-model="form.mealType" @change="onMealTypeChange">
            <el-radio-button v-for="mt in mealTypes" :key="mt.value" :value="mt.value">
              {{ mt.icon }} {{ mt.label }}
            </el-radio-button>
          </el-radio-group>
        </div>
        <div class="time-row" v-if="!isMealTypeUsed(form.mealType) || form.mealType === 'snack'">
          <span class="form-label">进餐时间</span>
          <el-time-picker v-model="form.mealTime" format="HH:mm" value-format="HH:mm" placeholder="选择时间" />
        </div>
        <div class="time-row" v-else>
          <span class="form-label">进餐时间</span>
          <el-tag type="info" size="default">{{ form.mealTime }}（保持原记录时间）</el-tag>
        </div>
        <div class="dishes-section">
          <div class="section-header">
            <span class="form-label">菜品明细</span>
            <el-button type="primary" plain size="small" @click="addDishItem"><el-icon>
                <Plus />
              </el-icon> 添加菜品</el-button>
          </div>
          <div class="dish-list">
            <div v-for="(dish, index) in form.dishes" :key="index" class="dish-item" v-loading="dish.loading">
              <div class="dish-index">{{ index + 1 }}</div>
              <div class="dish-fields">
                <el-input v-model="dish.foodName" placeholder="菜品名称，如：红烧肉" class="dish-name-input" @input="debouncedEstimate(dish)">
                  <template #prefix>
                    <el-icon v-if="dish.loading" class="is-loading"><Loading /></el-icon>
                  </template>
                </el-input>
                <div class="dish-numbers">
                  <div class="input-with-unit wide">
                    <el-input-number v-model="dish.weight" :min="0" :max="9999" :precision="0" placeholder="0"
                      controls-position="right" @change="debouncedEstimate(dish)" />
                    <span class="unit-label">克(g)</span>
                  </div>
                  <div class="input-with-unit wide">
                    <el-input-number v-model="dish.calories" :min="0" :max="9999" :precision="0" placeholder="0"
                      controls-position="right" />
                    <span class="unit-label">千卡(kcal)</span>
                  </div>
                </div>
                <!-- 营养成分显示区域 - 标签样式 -->
                <div class="nutrition-tags" v-if="dish.isAiEstimated">
                  <span class="ai-estimate-tag">
                    <el-icon class="is-loading"><Loading /></el-icon>
                    AI 已估算
                  </span>
                  <div class="nutrition-badge protein">
                    <span class="badge-value">{{ dish.protein || 0 }}</span>
                    <span class="badge-unit">g</span>
                    <span class="badge-name">蛋白质</span>
                  </div>
                  <div class="nutrition-badge fat">
                    <span class="badge-value">{{ dish.fat || 0 }}</span>
                    <span class="badge-unit">g</span>
                    <span class="badge-name">脂肪</span>
                  </div>
                  <div class="nutrition-badge carbs">
                    <span class="badge-value">{{ dish.carbs || 0 }}</span>
                    <span class="badge-unit">g</span>
                    <span class="badge-name">碳水</span>
                  </div>
                </div>
              </div>
              <el-button type="danger" plain circle size="small" @click="removeDishItem(index)"
                :disabled="form.dishes.length <= 1"><el-icon>
                  <Delete />
                </el-icon></el-button>
            </div>
          </div>
        </div>
        <div class="calorie-summary">
          <span>本次合计：</span>
          <span class="total-cal">{{ totalCalories }} kcal</span>
          <span class="total-count">共 {{ form.dishes.length }} 种食材</span>
          <div class="ai-disclaimer">
            <el-icon><QuestionFilled /></el-icon> 
            <span>营养数据由 AI 估算，仅供参考</span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleAdd" :disabled="!isFormValid">确认添加 ({{
          totalCalories }} kcal)</el-button>
      </template>
    </el-dialog>

    <!-- 编辑饮食记录弹窗 -->
    <el-dialog v-model="editDialogVisible" title="编辑饮食记录" width="480px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="食物名称"><el-input v-model="editForm.foodName" placeholder="如：全麦面包, 煮鸡蛋" /></el-form-item>
        <el-form-item label="热量"><el-input-number v-model="editForm.calories" :min="0" :max="9999" /><span
            style="margin-left:8px">kcal</span></el-form-item>
        <div class="edit-nutrition-row">
          <el-form-item label="蛋白质" class="nutrition-form-item">
            <el-input-number v-model="editForm.protein" :min="0" :max="9999" /><span style="margin-left:8px">g</span>
          </el-form-item>
          <el-form-item label="脂肪" class="nutrition-form-item">
            <el-input-number v-model="editForm.fat" :min="0" :max="9999" /><span style="margin-left:8px">g</span>
          </el-form-item>
          <el-form-item label="碳水" class="nutrition-form-item">
            <el-input-number v-model="editForm.carbs" :min="0" :max="9999" /><span style="margin-left:8px">g</span>
          </el-form-item>
        </div>
        <el-form-item label="重量"><el-input-number v-model="editForm.weight" :min="0" :max="9999" /><span
            style="margin-left:8px">g</span></el-form-item>
        <el-form-item label="进餐时间" v-if="editForm.mealType === 'snack'">
          <el-time-picker v-model="editForm.mealTime" format="HH:mm" value-format="HH:mm" placeholder="选择时间" />
        </el-form-item>
        <el-form-item label="进餐时间" v-else>
          <el-tag type="info" size="default">{{ editForm.mealTime }}（正餐时间不可修改）</el-tag>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="editSubmitting" @click="handleEdit">确认修改</el-button>
      </template>
    </el-dialog>

    <!-- 额外运动弹窗 -->
    <el-dialog v-model="exerciseDialogVisible" title="添加额外运动消耗" width="420px">
      <el-form :model="exerciseForm" label-width="80px">
        <el-form-item label="运动名称"><el-input v-model="exerciseForm.exerciseName" placeholder="如：跑步、游泳" /></el-form-item>
        <el-form-item label="消耗热量"><el-input-number v-model="exerciseForm.caloriesBurned" :min="0" :max="9999" /><span
            style="margin-left:8px">kcal</span></el-form-item>
        <el-form-item label="运动时长"><el-input-number v-model="exerciseForm.durationMinutes" :min="0" :max="999" /><span
            style="margin-left:8px">分钟</span></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="exerciseDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="exerciseSubmitting" @click="handleAddExercise">确认添加</el-button>
      </template>
    </el-dialog>

    <!-- 编辑额外运动弹窗 -->
    <el-dialog v-model="editExerciseDialogVisible" title="编辑额外运动消耗" width="420px">
      <el-form :model="editExerciseForm" label-width="80px">
        <el-form-item label="运动名称"><el-input v-model="editExerciseForm.exerciseName"
            placeholder="如：跑步、游泳" /></el-form-item>
        <el-form-item label="消耗热量"><el-input-number v-model="editExerciseForm.caloriesBurned" :min="0"
            :max="9999" /><span style="margin-left:8px">kcal</span></el-form-item>
        <el-form-item label="运动时长"><el-input-number v-model="editExerciseForm.durationMinutes" :min="0"
            :max="999" /><span style="margin-left:8px">分钟</span></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editExerciseDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="editExerciseSubmitting" @click="handleEditExercise">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { UploadFilled, Plus, Edit, Delete, Timer, Calendar, QuestionFilled, Loading } from "@element-plus/icons-vue";
import { debounce } from "lodash-es";
import * as echarts from "echarts";
import { dietApi } from "@/api/diet";

const selectedDate = ref(new Date().toISOString().slice(0, 10));
const summary = reactive({ meals: [], totalIntakeCalories: 0, bmrCalories: 0, workoutBurnedCalories: 0, extraBurnedCalories: 0, targetCalories: 0, remainingCalories: 0, goal: "maintain", usedMealTypes: [] });
const addDialogVisible = ref(false);
const submitting = ref(false);
const chartRef = ref(null);
let chartInstance = null;
const goalMap = { lose: "减脂", gain: "增肌", maintain: "保持身材" };
const goalText = computed(() => goalMap[summary.goal] || "保持身材");

// 菜品表单 - 增加 AI 估算相关字段
const createDishItem = () => ({
  foodName: "",
  weight: 100,
  calories: 0,
  protein: 0,
  fat: 0,
  carbs: 0,
  loading: false,
  isAiEstimated: false
});

const form = reactive({ 
  mealType: "breakfast", 
  mealTime: "08:00", 
  dishes: [createDishItem()] 
});

const defaultTimeMap = { breakfast: "08:00", lunch: "12:00", dinner: "18:00", snack: "15:00" };
const totalCalories = computed(() => form.dishes.reduce((sum, dish) => sum + (dish.calories || 0), 0));
const isFormValid = computed(() => form.dishes.some(dish => dish.foodName && dish.calories > 0));

// AI 智能估算功能 - 带防抖处理
const requestAiEstimate = async (dish) => {
  // 校验：名称长度太短或重量为0则不触发
  if (!dish.foodName || dish.foodName.trim().length < 2 || !dish.weight) return;

  dish.loading = true;
  try {
    const data = await dietApi.analyzeFood({
      foodName: dish.foodName,
      weight: dish.weight
    });

    if (data) {
      const { calories, protein, fat, carbs } = data;
      dish.calories = calories || 0;
      dish.protein = protein || 0;
      dish.fat = fat || 0;
      dish.carbs = carbs || 0;
      dish.isAiEstimated = true; // 标记为 AI 估算
    }
  } catch (e) {
    console.error("AI 分析失败", e);
    dish.isAiEstimated = false;
  } finally {
    dish.loading = false;
  }
};

// 创建防抖版本的函数 - 1500ms 内如果重复触发，之前的请求会被取消
const debouncedEstimate = debounce((dish) => {
  requestAiEstimate(dish);
}, 1500);

// 餐次选项
// 餐次选项和图标映射
const mealTypes = [
  { value: "breakfast", label: "早餐", icon: "🌅" },
  { value: "lunch", label: "午餐", icon: "☀️" },
  { value: "dinner", label: "晚餐", icon: "🌙" },
  { value: "snack", label: "加餐", icon: "🍪" }
];
const mealTypeIconMap = { breakfast: "🌅", lunch: "☀️", dinner: "🌙", snack: "🍪" };

// 按餐次分组显示的computed - 加餐按时间分组
const groupedMeals = computed(() => {
  const groups = {};
  const order = ["breakfast", "lunch", "dinner"];

  // 先按时间排序
  const meals = [...(summary.meals || [])].sort((a, b) => {
    return (a.time || "").localeCompare(b.time || "");
  });

  for (const meal of meals) {
    const type = meal.mealType || meal.type;

    // 加餐按时间分组（相同时间的放一起，不同时间分开）
    if (type === "snack") {
      const time = meal.time || "";
      const key = `snack_${time}`;
      if (!groups[key]) {
        groups[key] = { type: "snack", time, meals: [] };
      }
      groups[key].meals.push(meal);
    } else {
      // 正餐按餐次分组
      if (!groups[type]) groups[type] = { type, time: meal.time, meals: [] };
      groups[type].meals.push(meal);
    }
  }

  // 按固定顺序返回
  const result = {};
  for (const t of order) {
    if (groups[t]) result[t] = groups[t].meals;
  }
  // 加餐按时间顺序
  const snackKeys = Object.keys(groups).filter(k => k.startsWith("snack_")).sort();
  for (const key of snackKeys) {
    result[key] = groups[key].meals;
  }
  return result;
});

function getMealTypeIcon(type) {
  if (type.startsWith("snack")) return "🍪";
  return mealTypeIconMap[type] || "🍽️";
}

function getMealTypeName(type) {
  const typeNameMap = { breakfast: "早餐", lunch: "午餐", dinner: "晚餐", snack: "加餐" };
  // 提取基础类型（处理 snack_10:30 这样的情况）
  const baseType = type.split("_")[0];
  return typeNameMap[baseType] || baseType;
}

function getMealGroupCalories(meals) {
  return meals.reduce((sum, m) => sum + (m.calories || 0), 0);
}

// 判断餐次是否已使用
function isMealTypeUsed(type) {
  if (type === "snack") return false;
  const used = summary.usedMealTypes;
  if (!used) return false;
  // 支持 Set 或 Array
  return used instanceof Set ? used.has(type) : used.includes(type);
}

function addDishItem() { form.dishes.push(createDishItem()); }
function removeDishItem(index) { if (form.dishes.length > 1) form.dishes.splice(index, 1); }
function onMealTypeChange(type) {
  // 如果该餐次已有记录，保留原时间；否则使用默认时间
  if (type !== "snack") {
    const existingMeal = summary.meals?.find(m => m.mealType === type);
    if (existingMeal) {
      form.mealTime = existingMeal.time || defaultTimeMap[type] || "08:00";
      return;
    }
  }
  form.mealTime = defaultTimeMap[type] || "08:00";
}

function showAddDialog() {
  // 自动选择第一个未记录的餐次
  const order = ["breakfast", "lunch", "dinner", "snack"];
  const firstAvailable = order.find(t => !isMealTypeUsed(t)) || "snack";
  form.mealType = firstAvailable;
  // 如果该餐次已有记录，使用已有记录的时间
  const existingMeal = summary.meals?.find(m => m.mealType === firstAvailable);
  if (existingMeal) {
    form.mealTime = existingMeal.time || defaultTimeMap[firstAvailable] || "08:00";
  } else {
    form.mealTime = defaultTimeMap[firstAvailable] || "08:00";
  }
  form.dishes = [createDishItem()];
  addDialogVisible.value = true;
}

async function handleAdd() {
  const validDishes = form.dishes.filter(d => d.foodName && d.calories > 0);
  if (validDishes.length === 0) return ElMessage.warning("请至少添加一种菜品");
  submitting.value = true;
  try {
    for (const dish of validDishes) {
      await dietApi.addMeal({ 
        mealType: form.mealType, 
        foodName: dish.foodName, 
        calories: dish.calories, 
        protein: dish.protein || 0,
        fat: dish.fat || 0,
        carbs: dish.carbs || 0,
        weight: dish.weight, 
        mealTime: form.mealTime, 
        date: selectedDate.value 
      });
    }
    ElMessage.success(`已添加 ${validDishes.length} 种食材`);
    addDialogVisible.value = false;
    await loadSummary();
  } catch (e) { console.error("添加饮食记录失败", e); }
  finally { submitting.value = false; }
}

async function loadSummary() {
  try {
    const data = await dietApi.getSummary(selectedDate.value);
    Object.assign(summary, data);
    await nextTick();
    renderChart();
  } catch (e) { console.error("加载饮食数据失败", e); }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm("确认删除该饮食记录？", "提示", { type: "warning", confirmButtonText: "确认", cancelButtonText: "取消" });
    await dietApi.deleteMeal(id);
    ElMessage.success("已删除");
    await loadSummary();
  } catch (e) { if (e !== "cancel") ElMessage.error("删除失败"); }
}

const editDialogVisible = ref(false);
const editSubmitting = ref(false);
const editForm = reactive({ id: null, mealType: "", foodName: "", calories: 0, protein: 0, fat: 0, carbs: 0, weight: 0, mealTime: "08:00" });

function showEditDialog(row) {
  editForm.id = row.id;
  editForm.mealType = row.mealType;
  editForm.foodName = row.foodName;
  editForm.calories = row.calories || 0;
  editForm.protein = row.protein || 0;
  editForm.fat = row.fat || 0;
  editForm.carbs = row.carbs || 0;
  editForm.weight = row.weight || 0;
  editForm.mealTime = row.time || "08:00";
  editDialogVisible.value = true;
}

async function handleEdit() {
  if (!editForm.foodName) return ElMessage.warning("请输入食物名称");
  if (!editForm.calories) return ElMessage.warning("请输入热量");
  editSubmitting.value = true;
  try {
    // 正餐不可修改时间，只发送必要字段
    const updateData = { 
      foodName: editForm.foodName, 
      calories: editForm.calories, 
      protein: editForm.protein,
      fat: editForm.fat,
      carbs: editForm.carbs,
      weight: editForm.weight 
    };
    if (editForm.mealType === "snack") {
      updateData.mealTime = editForm.mealTime;
    }
    await dietApi.updateMeal(editForm.id, updateData);
    ElMessage.success("修改成功");
    editDialogVisible.value = false;
    await loadSummary();
  } catch (e) { console.error("编辑饮食记录失败", e); }
  finally { editSubmitting.value = false; }
}

const exerciseDialogVisible = ref(false);
const exerciseSubmitting = ref(false);
const exerciseForm = reactive({ exerciseName: "", caloriesBurned: 0, durationMinutes: 30 });

function showExerciseDialog() {
  exerciseForm.exerciseName = "";
  exerciseForm.caloriesBurned = 0;
  exerciseForm.durationMinutes = 30;
  exerciseDialogVisible.value = true;
}

async function handleAddExercise() {
  if (!exerciseForm.exerciseName) return ElMessage.warning("请输入运动名称");
  if (!exerciseForm.caloriesBurned) return ElMessage.warning("请输入消耗热量");
  exerciseSubmitting.value = true;
  try {
    await dietApi.addExtraExercise({ ...exerciseForm, date: selectedDate.value });
    ElMessage.success("添加成功");
    exerciseDialogVisible.value = false;
    await loadSummary();
  } catch (e) { console.error("添加额外运动失败", e); }
  finally { exerciseSubmitting.value = false; }
}

async function handleDeleteExercise(id) {
  try {
    await ElMessageBox.confirm("确认删除该运动记录？", "提示", { type: "warning", confirmButtonText: "确认", cancelButtonText: "取消" });
    await dietApi.deleteExtraExercise(id);
    ElMessage.success("已删除");
    await loadSummary();
  } catch (e) { if (e !== "cancel") ElMessage.error("删除失败"); }
}

const editExerciseDialogVisible = ref(false);
const editExerciseSubmitting = ref(false);
const editExerciseForm = reactive({ id: null, exerciseName: "", caloriesBurned: 0, durationMinutes: 0 });

function showEditExerciseDialog(row) {
  editExerciseForm.id = row.id;
  editExerciseForm.exerciseName = row.exerciseName;
  editExerciseForm.caloriesBurned = row.caloriesBurned || 0;
  editExerciseForm.durationMinutes = row.durationMinutes || 0;
  editExerciseDialogVisible.value = true;
}

async function handleEditExercise() {
  if (!editExerciseForm.exerciseName) return ElMessage.warning("请输入运动名称");
  if (!editExerciseForm.caloriesBurned) return ElMessage.warning("请输入消耗热量");
  editExerciseSubmitting.value = true;
  try {
    await dietApi.updateExtraExercise(editExerciseForm.id, { exerciseName: editExerciseForm.exerciseName, caloriesBurned: editExerciseForm.caloriesBurned, durationMinutes: editExerciseForm.durationMinutes });
    ElMessage.success("修改成功");
    editExerciseDialogVisible.value = false;
    await loadSummary();
  } catch (e) { console.error("编辑额外运动失败", e); }
  finally { editExerciseSubmitting.value = false; }
}

function renderChart() {
  if (!chartRef.value) return;
  if (!chartInstance) chartInstance = echarts.init(chartRef.value);
  const intake = summary.totalIntakeCalories || 0;
  const target = summary.targetCalories || 0;
  const remaining = Math.max(0, summary.remainingCalories || 0);
  const over = Math.max(0, -(summary.remainingCalories || 0));
  const isEmpty = intake === 0 && target === 0;

  let chartData, centerLabel;
  if (isEmpty) {
    chartData = [{ value: 1, name: "暂无数据", itemStyle: { color: "#E4E7ED" } }];
    centerLabel = { formatter: "暂无数据", fontSize: 14, color: "#909399" };
  } else if (over > 0) {
    chartData = [{ value: target, name: "目标热量", itemStyle: { color: "#409EFF" } }, { value: over, name: "超标热量", itemStyle: { color: "#F56C6C" } }];
    centerLabel = { formatter: `超标\n${over} kcal`, fontSize: 16, fontWeight: "bold", color: "#F56C6C" };
  } else {
    chartData = [{ value: intake, name: "已摄入", itemStyle: { color: "#E6A23C" } }, { value: remaining || 1, name: "剩余可摄入", itemStyle: { color: "#67C23A" } }];
    centerLabel = { formatter: `剩余\n${remaining} kcal`, fontSize: 16, fontWeight: "bold", color: "#67C23A" };
  }

  const option = {
    tooltip: { trigger: "item", formatter: isEmpty ? "" : "{b}: {c} kcal ({d}%)" },
    legend: { bottom: 0, textStyle: { fontSize: 12 } },
    series: [{ type: "pie", radius: ["45%", "70%"], avoidLabelOverlap: false, itemStyle: { borderRadius: 6, borderColor: "#fff", borderWidth: 2 }, label: { show: true, position: "center", ...centerLabel }, data: chartData }]
  };
  chartInstance.setOption(option, true);
}

onMounted(() => { loadSummary(); window.addEventListener("resize", () => chartInstance?.resize()); });
</script>

<style scoped>
.diet-page {
  display: flex;
  gap: 20px;
  max-width: 1200px;
  margin: 24px auto;
  padding: 0 16px;
}

.diet-left {
  flex: 1;
  min-width: 0;
}

.diet-right {
  width: 380px;
  flex-shrink: 0;
}

.card-header-fancy {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon {
  font-size: 28px;
  line-height: 1;
}

.card-title {
  margin: 0;
  font-size: 17px;
  font-weight: 600;
  color: #303133;
}

.chart-box {
  width: 100%;
  height: 260px;
}

.calorie-info {
  margin-top: 12px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 6px 0;
  font-size: 14px;
  border-bottom: 1px dashed #eee;
}

.info-row.remain {
  font-weight: 600;
  font-size: 15px;
  border-bottom: none;
  padding-top: 10px;
}

.primary {
  color: #409EFF;
}

.warn {
  color: #E6A23C;
}

.success {
  color: #67C23A;
}

.danger {
  color: #F56C6C;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  padding: 32px 0;
  margin-top: 12px;
  cursor: pointer;
  transition: border-color 0.3s;
}

.upload-placeholder:hover {
  border-color: #409EFF;
}

.meal-list {
  margin-top: 12px;
}

.meal-item {
  display: flex;
  align-items: center;
  padding: 14px 12px;
  margin-bottom: 8px;
  border-radius: 10px;
  background: linear-gradient(135deg, #f8f9ff 0%, #f0f5ff 100%);
  border: 1px solid #e8ecf4;
  transition: all 0.25s ease;
}

.meal-item:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.1);
  border-color: #b3d8ff;
}

.meal-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  min-width: 70px;
}

.meal-time {
  font-size: 15px;
  font-weight: 600;
  color: #409EFF;
}

.meal-center {
  flex: 1;
  padding: 0 16px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.meal-food {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.meal-weight {
  font-size: 12px;
  color: #909399;
}

.meal-right {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 120px;
  justify-content: flex-end;
}

.meal-cal {
  font-size: 16px;
  font-weight: 700;
  color: #E6A23C;
  white-space: nowrap;
}

.meal-cal small {
  font-size: 11px;
  font-weight: 400;
  color: #909399;
}

.meal-actions {
  display: flex;
  gap: 2px;
}

.exercise-list {
  margin-top: 12px;
}

.exercise-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px;
  margin-bottom: 8px;
  border-radius: 10px;
  background: linear-gradient(135deg, #f0faf0 0%, #e8f8e8 100%);
  border: 1px solid #d4edda;
  transition: all 0.25s ease;
}

.exercise-item:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.1);
  border-color: #a3d98a;
}

.exercise-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.exercise-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.exercise-detail {
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 3px;
}

.exercise-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.exercise-cal {
  font-size: 15px;
  font-weight: 700;
  color: #67C23A;
  white-space: nowrap;
}

.exercise-cal small {
  font-size: 11px;
  font-weight: 400;
  color: #909399;
}

.workout-card .exercise-item {
  background: linear-gradient(135deg, #fff8f0 0%, #fff3e6 100%);
  border-color: #fde2c8;
}

.workout-card .exercise-cal {
  color: #E6A23C;
}

.date-picker-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-radius: 10px;
  padding: 12px 16px;
  margin-bottom: 14px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: 1px solid #e8ecf4;
}

.date-picker-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.date-label {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.action-btn {
  padding: 6px !important;
  width: 34px !important;
  height: 34px !important;
  display: inline-flex !important;
  align-items: center !important;
  justify-content: center !important;
  border-radius: 8px !important;
  transition: all 0.2s ease;
  cursor: pointer;
}

.action-btn:hover {
  background: rgba(64, 158, 255, 0.1) !important;
  transform: scale(1.1);
}

.header-action-btn {
  font-size: 14px !important;
}

.info-label-with-tip {
  cursor: help;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.info-label-with-tip:hover {
  color: #409EFF;
}

/* 添加多菜品表单样式 */
.add-meal-form {
  padding: 8px 0;
}

.meal-type-row {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.form-label {
  font-size: 14px;
  font-weight: 600;
  color: #606266;
  min-width: 70px;
}

.time-row {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.dishes-section {
  margin-bottom: 16px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.dish-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.dish-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  background: #f8f9fb;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.dish-index {
  width: 24px;
  height: 24px;
  background: #409EFF;
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
  flex-shrink: 0;
  margin-top: 6px;
}

.dish-fields {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.dish-name-input {
  width: 100%;
}

.dish-numbers {
  display: flex;
  gap: 8px;
}

.dish-numbers .el-input-number {
  width: calc(100% - 50px);
}

.input-with-unit {
  display: flex;
  align-items: center;
  gap: 6px;
}

.input-with-unit .el-input-number {
  flex: 1;
}

.unit-label {
  font-size: 13px;
  color: #909399;
  white-space: nowrap;
  min-width: 55px;
}

.calorie-summary {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e6f0ff 100%);
  border-radius: 8px;
  border: 1px solid #d9ecff;
}

.total-cal {
  font-size: 20px;
  font-weight: bold;
  color: #409EFF;
}

.total-count {
  font-size: 12px;
  color: #909399;
  margin-left: auto;
}

.ai-badge-tag {
  margin-left: 8px;
  font-weight: bold;
  animation: pulse-green 2s infinite;
}

@keyframes pulse-green {
  0% { opacity: 1; }
  50% { opacity: 0.7; }
  100% { opacity: 1; }
}

.ai-disclaimer {
  margin-left: auto;
  font-size: 11px;
  color: #a8abb2;
  display: flex;
  align-items: center;
  gap: 4px;
}

.is-loading {
  animation: rotating 2s linear infinite;
}

@keyframes rotating {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 按餐次分组显示样式 */
.meal-grouped-list {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.meal-group {
  background: linear-gradient(135deg, #f8f9ff 0%, #f0f5ff 100%);
  border-radius: 12px;
  border: 1px solid #e8ecf4;
  overflow: hidden;
  transition: all 0.25s ease;
}

.meal-group:hover {
  border-color: #b3d8ff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.08);
}

.meal-group-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: rgba(64, 158, 255, 0.06);
  border-bottom: 1px solid #e8ecf4;
}

.meal-group-time {
  font-size: 14px;
  font-weight: 600;
  color: #409EFF;
}

.meal-group-total {
  font-size: 13px;
  color: #606266;
  margin-left: auto;
  font-weight: 500;
}

.meal-group-dishes {
  padding: 8px 16px;
}

.meal-dish-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px dashed #e8ecf4;
}

.meal-dish-item:last-child {
  border-bottom: none;
}

.dish-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  flex-wrap: wrap;
}

.dish-name {
  font-size: 14px;
  color: #303133;
}

.dish-weight {
  font-size: 12px;
  color: #909399;
}

.dish-nutrition {
  display: flex;
  gap: 6px;
  margin-top: 4px;
  flex-wrap: wrap;
}

.nutrition-tag {
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 500;
}

.nutrition-tag.protein {
  background: #E8F4FD;
  color: #409EFF;
}

.nutrition-tag.fat {
  background: #FDF6EC;
  color: #E6A23C;
}

.nutrition-tag.carbs {
  background: #F0F9EE;
  color: #67C23A;
}

.dish-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.dish-cal {
  font-size: 14px;
  font-weight: 600;
  color: #E6A23C;
}

/* 营养成分标签样式 - AI估算结果显示 */
.nutrition-tags {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  padding: 8px 0;
}

.ai-estimate-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  background: linear-gradient(135deg, #67C23A 0%, #85CE61 100%);
  color: #fff;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  box-shadow: 0 2px 6px rgba(103, 194, 58, 0.3);
}

.nutrition-badge {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
}

.nutrition-badge.protein {
  background: linear-gradient(135deg, #E8F4FD 0%, #D4EDFC 100%);
  color: #409EFF;
}

.nutrition-badge.fat {
  background: linear-gradient(135deg, #FDF6EC 0%, #FCEBD8 100%);
  color: #E6A23C;
}

.nutrition-badge.carbs {
  background: linear-gradient(135deg, #F0F9EE 0%, #E4F4DD 100%);
  color: #67C23A;
}

.badge-value {
  font-size: 14px;
  font-weight: 700;
}

.badge-unit {
  font-size: 11px;
  opacity: 0.8;
}

.badge-name {
  font-size: 11px;
  margin-left: 2px;
  opacity: 0.7;
}

/* 编辑弹窗营养成分行 */
.edit-nutrition-row {
  display: flex;
  gap: 8px;
}

.nutrition-form-item {
  flex: 1;
}

.nutrition-form-item .el-form-item__label {
  font-size: 13px;
}

@media (max-width: 860px) {
  .diet-page {
    flex-direction: column;
  }

  .diet-right {
    width: 100%;
  }
}
</style>
