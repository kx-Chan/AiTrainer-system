<template>
  <div class="diet-page">
    <!-- 左侧：饮食记录 -->
    <div class="diet-left">
      <!-- 日期选择器 - 控制饮食记录和额外运动消耗 -->
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
        <!-- 饮食列表 - 卡片风格 -->
        <div class="meal-list" v-if="summary.meals && summary.meals.length">
          <div class="meal-item" v-for="meal in summary.meals" :key="meal.id">
            <div class="meal-left">
              <div class="meal-time">{{ meal.time }}</div>
              <el-tag :type="meal.tagType" size="small" effect="dark" round>{{ meal.type }}</el-tag>
            </div>
            <div class="meal-center">
              <span class="meal-food">{{ meal.foodName }}</span>
              <span class="meal-weight" v-if="meal.weight">{{ meal.weight }}g</span>
            </div>
            <div class="meal-right">
              <span class="meal-cal">{{ meal.calories }} <small>kcal</small></span>
              <div class="meal-actions">
                <el-tooltip content="编辑记录" placement="top">
                  <el-button type="primary" link class="action-btn" @click="showEditDialog(meal)">
                    <el-icon :size="20">
                      <Edit />
                    </el-icon>
                  </el-button>
                </el-tooltip>
                <el-tooltip content="删除记录" placement="top">
                  <el-button type="danger" link class="action-btn" @click="handleDelete(meal.id)">
                    <el-icon :size="20">
                      <Delete />
                    </el-icon>
                  </el-button>
                </el-tooltip>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无饮食记录，点击上方添加" :image-size="60" />
      </el-card>
      <!-- 额外运动消耗 -->
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
              <span class="exercise-detail">
                <el-icon>
                  <Timer />
                </el-icon> {{ ex.durationMinutes }} 分钟
              </span>
            </div>
            <div class="exercise-right">
              <span class="exercise-cal">-{{ ex.caloriesBurned }} <small>kcal</small></span>
              <div class="meal-actions">
                <el-tooltip content="编辑记录" placement="top">
                  <el-button type="primary" link class="action-btn" @click="showEditExerciseDialog(ex)">
                    <el-icon :size="20">
                      <Edit />
                    </el-icon>
                  </el-button>
                </el-tooltip>
                <el-tooltip content="删除记录" placement="top">
                  <el-button type="danger" link class="action-btn" @click="handleDeleteExercise(ex.id)">
                    <el-icon :size="20">
                      <Delete />
                    </el-icon>
                  </el-button>
                </el-tooltip>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无额外运动记录" :image-size="50" />
      </el-card>
      <!-- 训练消耗明细 -->
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
              <span class="exercise-detail">
                <el-icon>
                  <Timer />
                </el-icon> {{ w.durationMinutes }} 分钟
              </span>
            </div>
            <div class="exercise-right">
              <span class="exercise-cal">-{{ w.caloriesBurned }} <small>kcal</small></span>
            </div>
          </div>
        </div>
      </el-card>
    </div>
    <!-- 右侧：热量可视化 -->
    <div class="diet-right">
      <el-card shadow="never">
        <h3 style="text-align:center;margin:0 0 8px">今日热量概览</h3>
        <div ref="chartRef" class="chart-box"></div>
        <div class="calorie-info">
          <div class="info-row">
            <el-tooltip placement="left" :show-after="300" popper-class="science-tooltip" effect="light">
              <template #content>
                <div class="tip-title">📖 基础代谢率 (BMR)</div>
                <div class="tip-body">
                  基础代谢是人体在安静状态下维持生命所需的最低热量。<br />
                  <b>Mifflin-St Jeor 公式：</b><br />
                  男性：BMR = 10×体重(kg) + 6.25×身高(cm) - 5×年龄 + 5<br />
                  女性：BMR = 10×体重(kg) + 6.25×身高(cm) - 5×年龄 - 161<br />
                  <span style="color:#909399;font-size:12px">* 在没有运动的情况下，基础代谢约占人体总热量消耗的 60%~70%</span>
                </div>
              </template>
              <span class="info-label-with-tip">基础代谢 <el-icon :size="14" style="vertical-align:middle;color:#409EFF">
                  <QuestionFilled />
                </el-icon></span>
            </el-tooltip>
            <span>{{ summary.bmrCalories }} kcal</span>
          </div>
          <div class="info-row"><span>训练消耗</span><span>{{ summary.workoutBurnedCalories }} kcal</span></div>
          <div class="info-row"><span>额外运动消耗</span><span>{{ summary.extraBurnedCalories }} kcal</span></div>
          <div class="info-row">
            <el-tooltip placement="left" :show-after="300" popper-class="science-tooltip" effect="light">
              <template #content>
                <div class="tip-title">📖 目标摄入热量计算</div>
                <div class="tip-body">
                  <b>第一步：计算理论平衡热量</b><br />
                  基础代谢（BMR）约占无运动状态总消耗的 70%，<br />
                  因此：无运动总消耗 = BMR ÷ 0.7<br />
                  理论平衡热量 = BMR ÷ 0.7 + 运动消耗<br /><br />
                  <b>第二步：按目标调整应吃热量</b><br />
                  研究表明人们在定量饮食中会不自觉多吃约 20%，<br />
                  因此应吃热量需在理论值基础上 × 0.8 进行修正。<br /><br />
                  • <b>保持身材：</b>平衡热量 × 80%<br />
                  • <b>减脂：</b>在保持基础上再减 20% 缺口 → 平衡热量 × 80% × 80% = 平衡热量 × 64%<br />
                  • <b>增肌：</b>干净增肌盈余取 10% → 平衡热量 × 110% × 80% = 平衡热量 × 88%<br /><br />
                  <span style="color:#909399;font-size:12px">* 减脂缺口过大（超 30%）会导致基础代谢降低，不推荐极端节食</span>
                </div>
              </template>
              <span class="info-label-with-tip">目标摄入 <el-icon :size="14" style="vertical-align:middle;color:#409EFF">
                  <QuestionFilled />
                </el-icon></span>
            </el-tooltip>
            <span class="primary">{{ summary.targetCalories }} kcal</span>
          </div>
          <div class="info-row"><span>已摄入</span><span class="warn">{{ summary.totalIntakeCalories }} kcal</span></div>
          <div class="info-row remain"><span>{{ summary.remainingCalories >= 0 ? '还可摄入' : '已超标' }}</span>
            <span :class="summary.remainingCalories >= 0 ? 'success' : 'danger'">{{ Math.abs(summary.remainingCalories)
            }}
              kcal</span>
          </div>
          <div class="info-row"><span>健身目标</span><span>{{ goalText }}</span></div>
        </div>
      </el-card>
      <!-- AI 拍照识餐占位 -->
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
    <!-- 添加弹窗 -->
    <el-dialog v-model="addDialogVisible" title="添加饮食记录" width="420px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="餐次">
          <el-select v-model="form.mealType" placeholder="请选择">
            <el-option label="早餐" value="breakfast" :disabled="isMealTypeUsed('breakfast')" />
            <el-option label="午餐" value="lunch" :disabled="isMealTypeUsed('lunch')" />
            <el-option label="晚餐" value="dinner" :disabled="isMealTypeUsed('dinner')" />
            <el-option label="加餐" value="snack" />
          </el-select>
        </el-form-item>
        <el-form-item label="食物名称"><el-input v-model="form.foodName" placeholder="如：全麦面包, 煮鸡蛋" /></el-form-item>
        <el-form-item label="热量"><el-input-number v-model="form.calories" :min="0" :max="9999" /><span
            style="margin-left:8px">kcal</span></el-form-item>
        <el-form-item label="重量"><el-input-number v-model="form.weight" :min="0" :max="9999" /><span
            style="margin-left:8px">g</span></el-form-item>
        <el-form-item label="进餐时间"><el-time-picker v-model="form.mealTime" format="HH:mm" value-format="HH:mm"
            placeholder="选择时间" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleAdd">确认添加</el-button>
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
    <!-- 编辑饮食记录弹窗 -->
    <el-dialog v-model="editDialogVisible" title="编辑饮食记录" width="420px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="食物名称"><el-input v-model="editForm.foodName" placeholder="如：全麦面包, 煮鸡蛋" /></el-form-item>
        <el-form-item label="热量"><el-input-number v-model="editForm.calories" :min="0" :max="9999" /><span
            style="margin-left:8px">kcal</span></el-form-item>
        <el-form-item label="重量"><el-input-number v-model="editForm.weight" :min="0" :max="9999" /><span
            style="margin-left:8px">g</span></el-form-item>
        <el-form-item label="进餐时间"><el-time-picker v-model="editForm.mealTime" format="HH:mm" value-format="HH:mm"
            placeholder="选择时间" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="editSubmitting" @click="handleEdit">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick, watch } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { UploadFilled, Plus, Edit, Delete, Timer, Calendar, QuestionFilled } from "@element-plus/icons-vue";
import * as echarts from "echarts";
import { dietApi } from "@/api/diet";

// ---------- 状态 ----------
const selectedDate = ref(new Date().toISOString().slice(0, 10));
const summary = reactive({
  meals: [],
  totalIntakeCalories: 0,
  bmrCalories: 0,
  workoutBurnedCalories: 0,
  extraBurnedCalories: 0,
  targetCalories: 0,
  remainingCalories: 0,
  goal: "maintain",
  usedMealTypes: [],
});

// 判断某餐次是否已被使用（加餐snack不限制）
const isMealTypeUsed = (type) => type !== "snack" && (summary.usedMealTypes || []).includes(type);
const addDialogVisible = ref(false);
const submitting = ref(false);
const chartRef = ref(null);
let chartInstance = null;

const form = reactive({
  mealType: "breakfast",
  foodName: "",
  calories: 0,
  weight: 0,
  mealTime: "08:00",
});

const goalMap = { lose: "减脂", gain: "增肌", maintain: "保持身材" };
const goalText = computed(() => goalMap[summary.goal] || "保持身材");

// ---------- 加载数据 ----------
async function loadSummary() {
  try {
    const data = await dietApi.getSummary(selectedDate.value);
    Object.assign(summary, data);
    await nextTick();
    renderChart();
  } catch (e) {
    console.error("加载饮食数据失败", e);
  }
}

// ---------- 添加记录 ----------
const mealTypeOrder = ["breakfast", "lunch", "dinner", "snack"];
const defaultTimeMap = { breakfast: "08:00", lunch: "12:00", dinner: "18:00", snack: "15:00" };

function showAddDialog() {
  // 自动选择第一个未使用的餐次
  const firstAvailable = mealTypeOrder.find((t) => !isMealTypeUsed(t)) || "snack";
  form.mealType = firstAvailable;
  form.foodName = "";
  form.calories = 0;
  form.weight = 0;
  form.mealTime = defaultTimeMap[firstAvailable] || "08:00";
  addDialogVisible.value = true;
}

async function handleAdd() {
  if (!form.foodName) return ElMessage.warning("请输入食物名称");
  if (!form.calories) return ElMessage.warning("请输入热量");
  submitting.value = true;
  try {
    await dietApi.addMeal({ ...form, date: selectedDate.value });
    ElMessage.success("添加成功");
    addDialogVisible.value = false;
    await loadSummary();
  } catch (e) {
    // 拦截器已弹出后端错误消息，这里不再重复提示
    console.error("添加饮食记录失败", e);
  } finally {
    submitting.value = false;
  }
}

// ---------- 删除记录 ----------
async function handleDelete(id) {
  try {
    await ElMessageBox.confirm("确认删除该饮食记录？", "提示", { type: "warning", confirmButtonText: "确认", cancelButtonText: "取消" });
    await dietApi.deleteMeal(id);
    ElMessage.success("已删除");
    await loadSummary();
  } catch (e) {
    if (e !== "cancel") ElMessage.error("删除失败");
  }
}

// ---------- 编辑记录 ----------
const editDialogVisible = ref(false);
const editSubmitting = ref(false);
const editForm = reactive({ id: null, foodName: "", calories: 0, weight: 0, mealTime: "08:00" });

function showEditDialog(row) {
  editForm.id = row.id;
  editForm.foodName = row.foodName;
  editForm.calories = row.calories || 0;
  editForm.weight = row.weight || 0;
  editForm.mealTime = row.time || "08:00";
  editDialogVisible.value = true;
}

async function handleEdit() {
  if (!editForm.foodName) return ElMessage.warning("请输入食物名称");
  if (!editForm.calories) return ElMessage.warning("请输入热量");
  editSubmitting.value = true;
  try {
    await dietApi.updateMeal(editForm.id, {
      foodName: editForm.foodName,
      calories: editForm.calories,
      weight: editForm.weight,
      mealTime: editForm.mealTime,
    });
    ElMessage.success("修改成功");
    editDialogVisible.value = false;
    await loadSummary();
  } catch (e) {
    console.error("编辑饮食记录失败", e);
  } finally {
    editSubmitting.value = false;
  }
}

// ---------- 额外运动 ----------
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
  } catch (e) {
    console.error("添加额外运动失败", e);
  } finally {
    exerciseSubmitting.value = false;
  }
}

async function handleDeleteExercise(id) {
  try {
    await ElMessageBox.confirm("确认删除该运动记录？", "提示", { type: "warning", confirmButtonText: "确认", cancelButtonText: "取消" });
    await dietApi.deleteExtraExercise(id);
    ElMessage.success("已删除");
    await loadSummary();
  } catch (e) {
    if (e !== "cancel") ElMessage.error("删除失败");
  }
}

// ---------- 编辑额外运动 ----------
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
    await dietApi.updateExtraExercise(editExerciseForm.id, {
      exerciseName: editExerciseForm.exerciseName,
      caloriesBurned: editExerciseForm.caloriesBurned,
      durationMinutes: editExerciseForm.durationMinutes,
    });
    ElMessage.success("修改成功");
    editExerciseDialogVisible.value = false;
    await loadSummary();
  } catch (e) {
    console.error("编辑额外运动失败", e);
  } finally {
    editExerciseSubmitting.value = false;
  }
}

// ---------- 饼形图 ----------
function renderChart() {
  if (!chartRef.value) return;
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value);
  }
  const intake = summary.totalIntakeCalories || 0;
  const target = summary.targetCalories || 0;
  const remaining = Math.max(0, summary.remainingCalories || 0);
  const over = Math.max(0, -(summary.remainingCalories || 0));

  // 当目标和摄入都为 0 时，显示一个灰色圆环占位
  const isEmpty = intake === 0 && target === 0;

  let chartData;
  let centerLabel;
  if (isEmpty) {
    chartData = [{ value: 1, name: "暂无数据", itemStyle: { color: "#E4E7ED" } }];
    centerLabel = { formatter: "暂无数据", fontSize: 14, color: "#909399" };
  } else if (over > 0) {
    chartData = [
      { value: target, name: "目标热量", itemStyle: { color: "#409EFF" } },
      { value: over, name: "超标热量", itemStyle: { color: "#F56C6C" } },
    ];
    centerLabel = { formatter: `超标\n${over} kcal`, fontSize: 16, fontWeight: "bold", color: "#F56C6C" };
  } else {
    chartData = [
      { value: intake, name: "已摄入", itemStyle: { color: "#E6A23C" } },
      { value: remaining || 1, name: "剩余可摄入", itemStyle: { color: "#67C23A" } },
    ];
    centerLabel = { formatter: `剩余\n${remaining} kcal`, fontSize: 16, fontWeight: "bold", color: "#67C23A" };
  }

  const option = {
    tooltip: { trigger: "item", formatter: isEmpty ? "" : "{b}: {c} kcal ({d}%)" },
    legend: { bottom: 0, textStyle: { fontSize: 12 } },
    series: [
      {
        type: "pie",
        radius: ["45%", "70%"],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 6, borderColor: "#fff", borderWidth: 2 },
        label: { show: true, position: "center", ...centerLabel },
        data: chartData,
      },
    ],
  };
  chartInstance.setOption(option, true);
}

// ---------- 生命周期 ----------
onMounted(() => {
  loadSummary();
  window.addEventListener("resize", () => chartInstance?.resize());
});
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-left .title {
  font-size: 18px;
  font-weight: 600;
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

/* ===== 饮食记录卡片头部 ===== */
.card-header-fancy {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.header-icon {
  font-size: 28px;
  line-height: 1;
}

.header-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.card-title {
  margin: 0;
  font-size: 17px;
  font-weight: 600;
  color: #303133;
}

/* ===== 饮食列表项 ===== */
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

/* ===== 运动列表项 ===== */
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

/* ===== 训练消耗明细卡片绿色边框区分 ===== */
.workout-card .exercise-item {
  background: linear-gradient(135deg, #fff8f0 0%, #fff3e6 100%);
  border-color: #fde2c8;
}

.workout-card .exercise-item:hover {
  box-shadow: 0 4px 12px rgba(230, 162, 60, 0.1);
  border-color: #f0c78a;
}

.workout-card .exercise-cal {
  color: #E6A23C;
}

/* ===== 日期选择栏 ===== */
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

/* ===== 操作按钮（编辑/删除）===== */
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

/* ===== 头部操作按钮统一字号 ===== */
.header-action-btn {
  font-size: 14px !important;
}

/* ===== 科普提示标签 ===== */
.info-label-with-tip {
  cursor: help;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.info-label-with-tip:hover {
  color: #409EFF;
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

<!-- 全局样式：tooltip popper-class 不受 scoped 限制 -->
<style>
.science-tooltip.el-popper {
  max-width: 440px !important;
  background: #ffffff !important;
  border: 1px solid #e4e7ed !important;
  border-radius: 12px !important;
  box-shadow: 0 6px 30px rgba(0, 0, 0, 0.12) !important;
  padding: 16px 20px !important;
  color: #303133 !important;
}

.science-tooltip.el-popper .el-popper__arrow::before {
  background: #ffffff !important;
  border-color: #e4e7ed !important;
}

.science-tooltip .tip-title {
  font-size: 16px;
  font-weight: 700;
  margin-bottom: 12px;
  color: #303133;
  padding-bottom: 8px;
  border-bottom: 2px solid #409EFF;
  display: inline-block;
}

.science-tooltip .tip-body {
  font-size: 13px;
  line-height: 2;
  color: #606266;
}

.science-tooltip .tip-body b {
  color: #303133;
}
</style>
