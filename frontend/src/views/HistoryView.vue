<template>
  <div class="page-container">
    <div class="view-header">
      <div class="header-content">
        <h1 class="view-title">全馆历史</h1>
        <p class="view-subtitle">追溯图书流转足迹，沉淀借阅数据</p>
      </div>
    </div>

    <div class="content-body">
      <el-skeleton v-if="loading" :rows="10" animated class="apple-skeleton" />
      
      <div v-else>
        <!-- Desktop Table View -->
        <div class="table-card hidden-xs-only">
          <el-table :data="history" class="apple-table">
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
            <el-table-column prop="returnDate" label="归还日期" width="160">
              <template #default="{ row }">
                <span class="date-text">{{ formatDate(row.returnDate) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small" class="apple-tag status">
                  {{ getStatusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- Mobile Card View -->
        <div class="mobile-cards hidden-sm-and-up">
          <div v-for="record in history" :key="record.id" class="history-mobile-card">
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
                  <span class="label">借阅日期</span>
                  <span class="value">{{ formatDate(record.borrowDate) }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">归还日期</span>
                  <span class="value">{{ formatDate(record.returnDate) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from "vue";
import { useStore } from "vuex";

const store = useStore();
const history = computed(() => store.getters["borrows/history"]);
const loading = computed(() => store.getters["borrows/loading"]);

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

store.dispatch("borrows/fetchHistory");
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
  color: var(--apple-text-secondary);
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

.history-mobile-card {
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
  color: var(--apple-text);
}
</style>
