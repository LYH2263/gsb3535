<template>
  <div class="page-container">
    <div class="view-header">
      <div class="header-content">
        <h1 class="view-title">预约管理</h1>
        <p class="view-subtitle">管理读者预约排队记录</p>
      </div>
      <div class="header-actions">
        <div class="search-wrapper">
          <el-input
            v-model="queryParams.keyword"
            placeholder="搜索书名或读者名"
            clearable
            class="apple-search"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          >
            <template #prefix>
              <el-icon class="search-icon"><IepSearch /></el-icon>
            </template>
          </el-input>
        </div>
      </div>
    </div>

    <div class="content-body">
      <el-skeleton v-if="loading" :rows="10" animated class="apple-skeleton" />

      <div v-else>
        <!-- Desktop Table View -->
        <div class="table-card hidden-xs-only">
          <el-table :data="reservations" class="apple-table" row-class-name="apple-table-row">
            <el-table-column prop="queuePosition" label="排队序号" width="110" align="center">
              <template #default="{ row }">
                <span class="queue-badge">#{{ row.queuePosition }}</span>
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
            <el-table-column prop="userName" label="预约读者" width="140">
              <template #default="{ row }">
                <div class="user-cell">
                  <div class="user-avatar-mini">{{ row.userName?.charAt(0) }}</div>
                  <span class="user-name-text">{{ row.userName }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="reservationTime" label="预约时间" width="170">
              <template #default="{ row }">
                <span class="date-text">{{ formatDateTime(row.reservationTime) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="110" align="center">
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
                  class="apple-btn danger-light"
                  @click="cancelReservation(row.id)"
                >
                  取消预约
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- Mobile Card View -->
        <div class="mobile-cards hidden-sm-and-up">
          <div v-for="item in reservations" :key="item.id" class="reservation-mobile-card">
            <div class="card-header">
              <div class="book-info">
                <h3 class="book-title">{{ item.bookTitle }}</h3>
                <p class="user-name">读者：{{ item.userName }}</p>
              </div>
              <el-tag :type="getStatusType(item.status)" size="small" class="apple-tag">{{ getStatusLabel(item.status) }}</el-tag>
            </div>
            <div class="card-details">
              <div class="detail-row">
                <div class="detail-item">
                  <span class="label">排队序号</span>
                  <span class="value queue-text">#{{ item.queuePosition }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">预约时间</span>
                  <span class="value">{{ formatDateTime(item.reservationTime) }}</span>
                </div>
              </div>
            </div>
            <div class="card-actions" v-if="item.status === 'WAITING'">
              <el-button
                class="apple-btn danger-light full-width"
                @click="cancelReservation(item.id)"
              >
                取消预约
              </el-button>
            </div>
          </div>
        </div>

        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="queryParams.current"
            v-model:page-size="queryParams.size"
            :total="total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            class="apple-pagination"
            @size-change="handleSearch"
            @current-change="loadData"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, onMounted, onUnmounted } from "vue";
import { useStore } from "vuex";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  Search as IepSearch,
  Notebook as IepNotebook
} from "@element-plus/icons-vue";

const store = useStore();
const reservations = computed(() => store.getters["reservations/items"]);
const total = computed(() => store.getters["reservations/total"]);
const loading = computed(() => store.getters["reservations/loading"]);

const queryParams = reactive({
  current: 1,
  size: 10,
  keyword: ""
});

const loadData = () => {
  store.dispatch("reservations/fetchReservations", queryParams);
};

const handleSearch = () => {
  queryParams.current = 1;
  loadData();
};

const formatDateTime = (dateStr) => {
  if (!dateStr) return "-";
  return dateStr.replace("T", " ").substring(0, 16);
};

const getStatusType = (status) => {
  const map = {
    'WAITING': 'warning',
    'CANCELLED': 'info',
    'FULFILLED': 'success'
  };
  return map[status] || 'info';
};

const getStatusLabel = (status) => {
  const map = {
    'WAITING': '排队中',
    'CANCELLED': '已取消',
    'FULFILLED': '已完成'
  };
  return map[status] || status;
};

const cancelReservation = (id) => {
  ElMessageBox.confirm("确定要取消该预约吗？此操作将更新排队序号。", "提示", {
    confirmButtonText: "确定取消",
    cancelButtonText: "返回",
    type: "warning",
    confirmButtonClass: 'apple-btn danger',
    cancelButtonClass: 'apple-btn secondary'
  }).then(async () => {
    await store.dispatch("reservations/cancelReservation", id);
    ElMessage.success("预约已取消");
  });
};

onMounted(() => {
  loadData();
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
  gap: 16px;
  align-items: center;
}

.search-wrapper {
  width: 280px;
}

.apple-search :deep(.el-input__wrapper) {
  background-color: rgba(0, 0, 0, 0.05);
  box-shadow: none !important;
  border-radius: 10px;
  padding: 8px 12px;
  transition: all 0.2s ease;
}

.apple-search :deep(.el-input__wrapper.is-focus) {
  background-color: #ffffff;
  box-shadow: 0 0 0 1px var(--apple-blue) !important;
}

.search-icon {
  font-size: 18px;
  color: var(--apple-text-secondary);
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

.apple-btn.danger {
  background-color: #ff3b30;
  color: white;
}

.apple-btn.danger-light {
  background-color: rgba(255, 59, 48, 0.1);
  color: #ff3b30;
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

.user-name-text {
  font-weight: 500;
}

.queue-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 36px;
  height: 26px;
  padding: 0 8px;
  background-color: rgba(255, 149, 0, 0.1);
  color: #ff9500;
  border-radius: 13px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  font-size: 13px;
}

.date-text {
  font-variant-numeric: tabular-nums;
  font-size: 14px;
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

.reservation-mobile-card {
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

.user-name {
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

.queue-text {
  color: #ff9500;
  font-weight: 700;
}

.pagination-wrapper {
  margin-top: 32px;
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .view-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-actions {
    width: 100%;
    flex-direction: column;
  }

  .search-wrapper {
    width: 100%;
  }
}
</style>
