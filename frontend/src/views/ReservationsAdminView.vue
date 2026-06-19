<template>
  <div class="page-container">
    <div class="view-header">
      <div class="header-content">
        <h1 class="view-title">预约管理</h1>
        <p class="view-subtitle">查看与处理所有读者的图书预约队列</p>
      </div>
      <div class="header-actions">
        <div class="search-wrapper">
          <el-input
            v-model="bookKeyword"
            placeholder="按书名筛选"
            clearable
            class="apple-search"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          >
            <template #prefix>
              <el-icon class="search-icon"><IepNotebook /></el-icon>
            </template>
          </el-input>
        </div>
        <div class="search-wrapper">
          <el-input
            v-model="userKeyword"
            placeholder="按读者名筛选"
            clearable
            class="apple-search"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          >
            <template #prefix>
              <el-icon class="search-icon"><IepUser /></el-icon>
            </template>
          </el-input>
        </div>
        <el-button class="apple-btn primary" @click="handleSearch">
          <el-icon><IepSearch /></el-icon>
          <span>查询</span>
        </el-button>
        <el-button class="apple-btn secondary" @click="reset">重置</el-button>
      </div>
    </div>

    <div class="content-body">
      <el-skeleton v-if="loading" :rows="8" animated class="apple-skeleton" />

      <div v-else>
        <el-empty v-if="!records.length" description="暂无预约记录" class="apple-empty" />

        <div v-else>
          <!-- Desktop Table -->
          <div class="table-card hidden-xs-only">
            <el-table :data="records" class="apple-table" row-class-name="apple-table-row">
              <el-table-column label="队列序号" width="100" align="center">
                <template #default="{ row }">
                  <span class="queue-num" v-if="row.status === 'WAITING'">#{{ row.queuePosition || '-' }}</span>
                  <span class="queue-num muted" v-else>—</span>
                </template>
              </el-table-column>
              <el-table-column prop="bookTitle" label="图书名称" min-width="200">
                <template #default="{ row }">
                  <div class="book-title-cell">
                    <div class="book-icon"><el-icon><IepNotebook /></el-icon></div>
                    <span class="title-text">{{ row.bookTitle }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="userName" label="预约读者" width="160">
                <template #default="{ row }">
                  <div class="user-cell">
                    <div class="user-avatar-mini">{{ row.userName?.charAt(0) }}</div>
                    <span>{{ row.userName }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="预约时间" width="180">
                <template #default="{ row }">
                  <span class="date-text">{{ formatDate(row.reservedAt) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="120">
                <template #default="{ row }">
                  <el-tag :type="getStatusType(row.status)" size="small" class="apple-tag status">
                    {{ getStatusLabel(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120" align="right" fixed="right">
                <template #default="{ row }">
                  <el-button
                    v-if="row.status === 'WAITING'"
                    size="small"
                    class="apple-btn-text danger"
                    @click="cancel(row)"
                  >
                    取消预约
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- Mobile Cards -->
          <div class="mobile-cards hidden-sm-and-up">
            <div v-for="r in records" :key="r.id" class="reservation-card">
              <div class="card-header">
                <div class="book-info">
                  <h3 class="book-title">{{ r.bookTitle }}</h3>
                  <p class="reader-name">读者：{{ r.userName }}</p>
                </div>
                <el-tag :type="getStatusType(r.status)" size="small" class="apple-tag">
                  {{ getStatusLabel(r.status) }}
                </el-tag>
              </div>
              <div class="card-details">
                <div class="detail-item">
                  <span class="label">队列序号</span>
                  <span class="value">
                    <span class="queue-num" v-if="r.status === 'WAITING'">#{{ r.queuePosition || '-' }}</span>
                    <span class="queue-num muted" v-else>—</span>
                  </span>
                </div>
                <div class="detail-item">
                  <span class="label">预约时间</span>
                  <span class="value">{{ formatDate(r.reservedAt) }}</span>
                </div>
              </div>
              <div class="card-actions" v-if="r.status === 'WAITING'">
                <el-button class="apple-btn danger-light full-width" @click="cancel(r)">
                  取消该预约
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { useStore } from "vuex";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  Search as IepSearch,
  Notebook as IepNotebook,
  User as IepUser
} from "@element-plus/icons-vue";

const store = useStore();
const records = computed(() => store.getters["reservations/adminReservations"]);
const loading = computed(() => store.getters["reservations/loading"]);

const bookKeyword = ref("");
const userKeyword = ref("");

const handleSearch = () => {
  store.dispatch("reservations/applyFilters", {
    bookKeyword: bookKeyword.value,
    userKeyword: userKeyword.value
  });
};

const reset = () => {
  bookKeyword.value = "";
  userKeyword.value = "";
  store.dispatch("reservations/clearFilters");
};

const formatDate = (val) => {
  if (!val) return "-";
  const d = new Date(val);
  if (Number.isNaN(d.getTime())) return val;
  const pad = (n) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
};

const getStatusLabel = (status) => {
  switch (status) {
    case "WAITING": return "排队中";
    case "FULFILLED": return "已完成";
    case "CANCELLED": return "已取消";
    default: return status || "未知";
  }
};

const getStatusType = (status) => {
  switch (status) {
    case "WAITING": return "warning";
    case "FULFILLED": return "success";
    case "CANCELLED": return "info";
    default: return "info";
  }
};

const cancel = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认取消「${row.userName}」对《${row.bookTitle}》的预约？`,
      "提示",
      {
        confirmButtonText: "确认取消",
        cancelButtonText: "返回",
        type: "warning",
        confirmButtonClass: 'apple-btn danger',
        cancelButtonClass: 'apple-btn secondary'
      }
    );
  } catch (e) {
    return;
  }
  try {
    await store.dispatch("reservations/cancelReservation", row.id);
    ElMessage.success("已取消该预约");
  } catch (err) {
    // handled by interceptor
  }
};

onMounted(() => {
  store.dispatch("reservations/clearFilters");
});
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

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.search-wrapper {
  width: 220px;
}

.apple-search :deep(.el-input__wrapper) {
  background-color: rgba(0, 0, 0, 0.05);
  box-shadow: none !important;
  border-radius: 10px;
  padding: 8px 12px;
}

.apple-search :deep(.el-input__wrapper.is-focus) {
  background-color: #ffffff;
  box-shadow: 0 0 0 1px var(--apple-blue) !important;
}

.search-icon {
  font-size: 18px;
  color: var(--apple-text-secondary);
}

.apple-btn {
  border-radius: 20px;
  padding: 10px 20px;
  height: auto;
  font-weight: 500;
  border: none;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s ease;
}

.apple-btn.primary {
  background-color: var(--apple-blue);
  color: white;
}

.apple-btn.primary:hover {
  background-color: #0077ed;
}

.apple-btn.secondary {
  background-color: rgba(0, 0, 0, 0.05);
  color: var(--apple-text);
}

.apple-btn.secondary:hover {
  background-color: rgba(0, 0, 0, 0.08);
}

.apple-btn.danger-light {
  background-color: rgba(255, 59, 48, 0.1);
  color: #ff3b30;
}

.apple-btn-text {
  border: none;
  background: transparent;
  color: var(--apple-blue);
  font-weight: 500;
  padding: 4px 8px;
}

.apple-btn-text.danger {
  color: #ff3b30;
}

.apple-btn-text.danger:hover {
  background-color: rgba(255, 59, 48, 0.08);
  border-radius: 4px;
}

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
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.apple-table :deep(td.el-table__cell) {
  padding: 16px 8px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.03);
}

.book-title-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.book-icon {
  width: 32px;
  height: 32px;
  background-color: rgba(0, 113, 227, 0.08);
  color: var(--apple-blue);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.title-text {
  font-weight: 600;
  color: var(--apple-text);
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar-mini {
  width: 28px;
  height: 28px;
  background-color: var(--apple-blue);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
}

.date-text {
  font-variant-numeric: tabular-nums;
  color: var(--apple-text-secondary);
}

.queue-num {
  font-weight: 700;
  color: var(--apple-blue);
  font-variant-numeric: tabular-nums;
}

.queue-num.muted {
  color: var(--apple-text-secondary);
  font-weight: 500;
}

.apple-tag {
  border-radius: 6px;
  border: none;
  font-weight: 500;
}

.mobile-cards {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.reservation-card {
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
}

.reader-name {
  margin: 4px 0 0 0;
  color: var(--apple-text-secondary);
  font-size: 13px;
}

.card-details {
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
}

.detail-item .label {
  color: var(--apple-text-secondary);
}

.full-width {
  flex: 1;
  width: 100%;
  justify-content: center;
}

.apple-empty {
  padding: 60px 0;
}

@media (max-width: 768px) {
  .view-header {
    flex-direction: column;
    align-items: stretch;
  }
  .header-actions {
    flex-direction: column;
    align-items: stretch;
  }
  .search-wrapper {
    width: 100%;
  }
}
</style>
