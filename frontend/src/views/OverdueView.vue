<template>
  <div class="page-container">
    <div class="view-header">
      <div class="header-content">
        <h1 class="view-title">逾期统计</h1>
        <p class="view-subtitle">实时监控超期未还记录，降低资产流失</p>
      </div>
    </div>

    <div class="content-body">
      <el-skeleton v-if="loading" :rows="10" animated class="apple-skeleton" />
      
      <div v-else>
        <!-- Desktop Table View -->
        <div class="table-card hidden-xs-only">
          <el-table :data="overdue" class="apple-table">
            <el-table-column prop="userName" label="借阅人" width="140">
              <template #default="{ row }">
                <div class="user-cell">
                  <div class="user-avatar-mini danger">{{ row.userName?.charAt(0) }}</div>
                  <span class="user-name-text">{{ row.userName }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="bookTitle" label="逾期图书" min-width="200">
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
                <span class="date-text overdue-highlight">{{ formatDate(row.dueDate) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="逾期时长" width="120">
              <template #default="{ row }">
                <span class="overdue-days">{{ getOverdueDays(row.dueDate) }} 天</span>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- Mobile Card View -->
        <div class="mobile-cards hidden-sm-and-up">
          <div v-for="record in overdue" :key="record.id" class="overdue-mobile-card">
            <div class="card-header">
              <div class="book-info">
                <h3 class="book-title">{{ record.bookTitle }}</h3>
                <p class="borrower-name">借阅人：{{ record.userName }}</p>
              </div>
              <div class="overdue-badge">逾期 {{ getOverdueDays(record.dueDate) }} 天</div>
            </div>
            <div class="card-details">
              <div class="detail-row">
                <div class="detail-item">
                  <span class="label">应还时间</span>
                  <span class="value overdue-highlight">{{ formatDate(record.dueDate) }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">借阅时间</span>
                  <span class="value">{{ formatDate(record.borrowDate) }}</span>
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
const overdue = computed(() => store.getters["borrows/overdue"]);
const loading = computed(() => store.getters["borrows/loading"]);

const formatDate = (dateStr) => {
  if (!dateStr) return "-";
  return dateStr.split('T')[0];
};

const getOverdueDays = (dueDateStr) => {
  const now = new Date();
  const dueDate = new Date(dueDateStr);
  const diffTime = Math.abs(now - dueDate);
  return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
};

store.dispatch("borrows/fetchOverdue");
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

.user-avatar-mini.danger {
  background-color: rgba(255, 59, 48, 0.1);
  color: #ff3b30;
}

.book-title-text {
  font-weight: 600;
  color: var(--apple-text);
}

.date-text {
  font-variant-numeric: tabular-nums;
  font-size: 14px;
}

.overdue-highlight {
  color: #ff3b30;
  font-weight: 700;
}

.overdue-days {
  color: #ff3b30;
  font-weight: 600;
  font-size: 13px;
}

/* Mobile Cards */
.mobile-cards {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.overdue-mobile-card {
  background: white;
  border-radius: var(--apple-radius);
  padding: 20px;
  box-shadow: var(--apple-shadow);
  border-left: 4px solid #ff3b30;
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

.overdue-badge {
  background-color: #ff3b30;
  color: white;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 700;
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
}
</style>
