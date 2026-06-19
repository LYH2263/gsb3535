<template>
  <div class="page-container">
    <div class="view-header">
      <div class="header-content">
        <h1 class="view-title">借阅管理</h1>
        <p class="view-subtitle">处理图书流转，维护馆藏动态</p>
      </div>
      <el-button type="primary" class="apple-btn primary" @click="openDialog()">
        <el-icon><IepPlus /></el-icon>
        <span>发起借阅</span>
      </el-button>
    </div>

    <div class="content-body">
      <el-skeleton v-if="loading" :rows="10" animated class="apple-skeleton" />
      
      <div v-else>
        <!-- Desktop Table View -->
        <div class="table-card hidden-xs-only">
          <el-table :data="records" class="apple-table">
            <el-table-column prop="userName" label="借阅人" width="140">
              <template #default="{ row }">
                <div class="user-cell">
                  <div class="user-avatar-mini">{{ row.userName?.charAt(0) }}</div>
                  <span class="user-name-text">{{ row.userName }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="bookTitle" label="图书名称" min-width="200">
              <template #default="{ row }">
                <span class="book-title-text">{{ row.bookTitle }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="borrowDate" label="借阅日期" width="160">
              <template #default="{ row }">
                <span class="date-text">{{ formatDate(row.borrowDate) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="应还日期" width="160">
              <template #default="{ row }">
                <span :class="['date-text', getDueDateClass(row)]">{{ formatDate(row.dueDate) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small" class="apple-tag status">
                  {{ getStatusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" align="right" fixed="right">
              <template #default="{ row }">
                <el-button
                  v-if="!row.returnDate"
                  size="small"
                  class="apple-btn success-light"
                  @click="returnBook(row.id)"
                >
                  归还
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- Mobile Card View -->
        <div class="mobile-cards hidden-sm-and-up">
          <div v-for="record in records" :key="record.id" class="borrow-mobile-card">
            <div class="card-header">
              <div class="book-info">
                <h3 class="book-title">{{ record.bookTitle }}</h3>
                <p class="borrower-name">借阅人：{{ record.userName }}</p>
              </div>
              <el-tag :type="getStatusType(record.status)" size="small" class="apple-tag">{{ getStatusLabel(record.status) }}</el-tag>
            </div>
            <div class="card-details">
              <div class="detail-row">
                <div class="detail-item">
                  <span class="label">借阅时间</span>
                  <span class="value">{{ formatDate(record.borrowDate) }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">应还时间</span>
                  <span :class="['value', getDueDateClass(record)]">{{ formatDate(record.dueDate) }}</span>
                </div>
              </div>
            </div>
            <div class="card-actions" v-if="!record.returnDate">
              <el-button
                class="apple-btn success full-width"
                @click="returnBook(record.id)"
              >
                确认归还图书
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 发起借阅弹窗 -->
    <el-dialog v-model="dialogVisible" title="发起新借阅" :width="dialogWidth" class="apple-dialog" :close-on-click-modal="false">
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top" class="apple-form">
        <el-form-item v-if="isAdmin" label="选择借阅人" prop="userId">
          <el-select v-model="form.userId" placeholder="搜索或选择读者" class="apple-select" filterable>
            <el-option v-for="user in users" :key="user.id" :label="user.fullName" :value="user.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择待借图书" prop="bookId">
          <el-select v-model="form.bookId" placeholder="搜索图书标题" class="apple-select" filterable>
            <el-option
              v-for="book in books"
              :key="book.id"
              :label="book.title"
              :value="book.id"
              :disabled="book.availableCopies <= 0"
            >
              <div class="book-option">
                <span>{{ book.title }}</span>
                <el-tag size="small" type="info" effect="plain" class="stock-tag">库存: {{ book.availableCopies }}</el-tag>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="计划借阅天数" prop="borrowDays">
          <div class="days-selector">
            <el-input-number v-model="form.borrowDays" :min="1" :max="60" class="apple-number-input" />
            <span class="unit">天</span>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button class="apple-btn secondary" @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" class="apple-btn primary" @click="submit">确认发起</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, reactive, ref, onMounted, onUnmounted } from "vue";
import { useStore } from "vuex";
import { z } from "zod";
import { ElMessage } from "element-plus";
import { Plus as IepPlus } from "@element-plus/icons-vue";

const store = useStore();
const records = computed(() => store.getters["borrows/records"]);
const loading = computed(() => store.getters["borrows/loading"]);
const books = computed(() => store.getters["books/books"]);
const users = computed(() => store.getters["users/users"]);
const profile = computed(() => store.getters["auth/profile"]);
const isAdmin = computed(() => profile.value?.role === "ADMIN");

const screenWidth = ref(window.innerWidth);
const handleResize = () => {
  screenWidth.value = window.innerWidth;
};

onMounted(() => {
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
});

const dialogWidth = computed(() => {
  return screenWidth.value < 768 ? '90%' : '480px';
});

const dialogVisible = ref(false);
const formRef = ref(null);
const form = reactive({
  userId: profile.value?.id || null,
  bookId: null,
  borrowDays: 14
});

const rules = {
  userId: [{ required: true, message: "请选择借阅人", trigger: "change" }],
  bookId: [{ required: true, message: "请选择图书", trigger: "change" }],
  borrowDays: [{ required: true, message: "请输入借阅天数", trigger: "blur" }]
};

const schema = z.object({
  userId: z.number().int().positive("请选择借阅人"),
  bookId: z.number().int().positive("请选择图书"),
  borrowDays: z.number().int().min(1, "借阅天数至少为1天")
});

const openDialog = () => {
  form.userId = isAdmin.value ? null : profile.value?.id || null;
  form.bookId = null;
  form.borrowDays = 14;
  dialogVisible.value = true;
};

const getDueDateClass = (row) => {
  if (row.returnDate) return "";
  const now = new Date();
  const dueDate = new Date(row.dueDate);
  const diffDays = (dueDate - now) / (1000 * 60 * 60 * 24);

  if (diffDays < 0) return "overdue-red";
  if (diffDays <= 3) return "approaching-yellow";
  return "";
};

const submit = async () => {
  if (!formRef.value) {
    return;
  }
  await formRef.value.validate(async (valid) => {
    if (!valid) {
      return;
    }
    const payload = { ...form };
    if (!isAdmin.value) {
      payload.userId = profile.value?.id;
    }
    const result = schema.safeParse(payload);
    if (!result.success) {
      ElMessage.error(result.error.errors[0]?.message || "表单校验失败");
      return;
    }
    try {
      await store.dispatch("borrows/borrow", payload);
      ElMessage.success("借阅记录已创建");
      dialogVisible.value = false;
    } catch (error) {
      // 错误已由拦截器处理
    }
  });
};

const returnBook = async (borrowId) => {
  await store.dispatch("borrows/returnBook", { borrowId });
  ElMessage.success("图书已成功归还");
};

const formatDate = (dateStr) => {
  if (!dateStr) return "-";
  return dateStr.split('T')[0];
};

const getStatusType = (status) => {
  const map = {
    'BORROWED': 'warning',
    'RETURNED': 'success',
    'OVERDUE': 'danger'
  };
  return map[status] || 'info';
};

const getStatusLabel = (status) => {
  const map = {
    'BORROWED': '借阅中',
    'RETURNED': '已归还',
    'OVERDUE': '已逾期'
  };
  return map[status] || status;
};

store.dispatch("borrows/fetchAll");
store.dispatch("books/fetchBooks");
if (isAdmin.value) {
  store.dispatch("users/fetchUsers");
}
</script>

<style scoped>
.page-container {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.view-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  flex-wrap: wrap;
  gap: 24px;
}

.view-title {
  font-size: 32px;
  font-weight: 700;
  color: var(--apple-text);
  margin: 0;
  letter-spacing: -1px;
}

.view-subtitle {
  font-size: 16px;
  color: var(--apple-text-secondary);
  margin: 4px 0 0 0;
}

/* Apple Style Buttons */
.apple-btn {
  border-radius: 20px;
  padding: 10px 20px;
  height: auto;
  font-weight: 500;
  transition: all 0.2s ease;
  border: none;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.apple-btn.primary {
  background-color: var(--apple-blue);
  color: white;
}

.apple-btn.success {
  background-color: #34c759;
  color: white;
}

.apple-btn.success-light {
  background-color: rgba(52, 199, 89, 0.1);
  color: #34c759;
}

.apple-btn.secondary {
  background-color: rgba(0, 0, 0, 0.05);
  color: var(--apple-text);
}

.full-width {
  width: 100%;
  justify-content: center;
}

/* Apple Style Table */
.table-card {
  background: white;
  border-radius: var(--apple-radius);
  box-shadow: var(--apple-shadow);
  overflow: hidden;
  padding: 8px;
}

.apple-table :deep(th.el-table__cell) {
  background-color: transparent;
  color: var(--apple-text-secondary);
  font-weight: 600;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  padding: 16px 8px;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar-mini {
  width: 24px;
  height: 24px;
  background-color: #e5e5ea;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  color: var(--apple-text-secondary);
}

.book-title-text {
  font-weight: 600;
  color: var(--apple-text);
}

.date-text {
  font-variant-numeric: tabular-nums;
  font-size: 14px;
}

.overdue-red {
  color: #ff3b30;
  font-weight: 700;
}

.approaching-yellow {
  color: #ff9500;
  font-weight: 700;
}

.apple-tag {
  border-radius: 6px;
  border: none;
  font-weight: 600;
}

/* Mobile Cards */
.mobile-cards {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.borrow-mobile-card {
  background: white;
  border-radius: var(--apple-radius);
  padding: 20px;
  box-shadow: var(--apple-shadow);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.book-title {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  line-height: 1.3;
}

.borrower-name {
  margin: 4px 0 0 0;
  font-size: 13px;
  color: var(--apple-text-secondary);
}

.detail-row {
  display: flex;
  gap: 24px;
  margin-bottom: 20px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-item .label {
  font-size: 11px;
  text-transform: uppercase;
  color: var(--apple-text-secondary);
  letter-spacing: 0.5px;
}

.detail-item .value {
  font-size: 14px;
  font-weight: 500;
}

/* Dialog & Form */
.apple-dialog :deep(.el-dialog) {
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.apple-select {
  width: 100%;
}

.apple-select :deep(.el-input__wrapper) {
  border-radius: 10px;
  background-color: rgba(0, 0, 0, 0.03);
  box-shadow: none !important;
  padding: 4px 12px;
}

.book-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.stock-tag {
  font-size: 10px;
  height: 18px;
  line-height: 18px;
}

.days-selector {
  display: flex;
  align-items: center;
  gap: 12px;
}

.unit {
  font-weight: 600;
  color: var(--apple-text-secondary);
}

@media (max-width: 768px) {
  .view-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .apple-btn.primary {
    width: 100%;
    justify-content: center;
  }
}
</style>
