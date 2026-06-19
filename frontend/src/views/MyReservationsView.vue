<template>
  <div class="page-container">
    <div class="view-header">
      <div class="header-content">
        <h1 class="view-title">我的预约</h1>
        <p class="view-subtitle">查看您的图书预约排队状态</p>
      </div>
    </div>

    <div class="content-body">
      <el-skeleton v-if="loading" :rows="10" animated class="apple-skeleton" />

      <div v-else>
        <el-empty v-if="reservations.length === 0" description="暂无预约记录" />

        <template v-else>
          <div class="table-card hidden-xs-only">
            <el-table :data="reservations" class="apple-table" row-class-name="apple-table-row">
              <el-table-column prop="bookTitle" label="书名" min-width="200">
                <template #default="{ row }">
                  <div class="book-title-cell">
                    <div class="book-icon"><el-icon><IepNotebook /></el-icon></div>
                    <span class="title-text">{{ row.bookTitle }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="排队序号" width="120" align="center">
                <template #default="{ row }">
                  <span v-if="row.status === 'WAITING'" class="queue-position">
                    第 {{ row.queuePosition }} 位
                  </span>
                  <span v-else>-</span>
                </template>
              </el-table-column>
              <el-table-column label="预约时间" width="180">
                <template #default="{ row }">
                  {{ formatDateTime(row.reservationTime) }}
                </template>
              </el-table-column>
              <el-table-column label="状态" width="120" align="center">
                <template #default="{ row }">
                  <el-tag :type="getStatusType(row.status)" effect="light" class="status-tag">
                    {{ getStatusText(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120" align="right" fixed="right">
                <template #default="{ row }">
                  <el-button
                    v-if="row.status === 'WAITING'"
                    size="small"
                    class="apple-btn-text danger"
                    @click="handleCancel(row)"
                  >
                    取消预约
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <div class="mobile-cards hidden-sm-and-up">
            <div v-for="item in reservations" :key="item.id" class="reservation-mobile-card">
              <div class="card-header">
                <div class="book-info">
                  <h3 class="book-title">{{ item.bookTitle }}</h3>
                  <p class="reserve-time">{{ formatDateTime(item.reservationTime) }}</p>
                </div>
                <el-tag :type="getStatusType(item.status)" effect="light" class="status-tag">
                  {{ getStatusText(item.status) }}
                </el-tag>
              </div>
              <div class="card-details">
                <div v-if="item.status === 'WAITING'" class="queue-info">
                  <el-icon><IepBell /></el-icon>
                  <span>当前排队：第 <strong>{{ item.queuePosition }}</strong> 位</span>
                </div>
              </div>
              <div v-if="item.status === 'WAITING'" class="card-actions">
                <el-button size="default" class="apple-btn danger-light full-width" @click="handleCancel(item)">
                  取消预约
                </el-button>
              </div>
            </div>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from "vue";
import { useStore } from "vuex";
import { ElMessage, ElMessageBox } from "element-plus";
import { Notebook as IepNotebook, Bell as IepBell } from "@element-plus/icons-vue";

const store = useStore();
const loading = computed(() => store.getters["reservations/loading"]);
const reservations = computed(() => store.getters["reservations/myReservations"]);

const formatDateTime = (dateStr) => {
  if (!dateStr) return "-";
  const date = new Date(dateStr);
  return date.toLocaleString("zh-CN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit"
  });
};

const getStatusType = (status) => {
  switch (status) {
    case "WAITING": return "warning";
    case "CANCELLED": return "info";
    case "FULFILLED": return "success";
    default: return "info";
  }
};

const getStatusText = (status) => {
  switch (status) {
    case "WAITING": return "排队中";
    case "CANCELLED": return "已取消";
    case "FULFILLED": return "已通知";
    default: return status;
  }
};

const handleCancel = (row) => {
  ElMessageBox.confirm(`确定要取消《${row.bookTitle}》的预约吗？`, "提示", {
    confirmButtonText: "确定取消",
    cancelButtonText: "返回",
    type: "warning",
    confirmButtonClass: "apple-btn danger",
    cancelButtonClass: "apple-btn secondary"
  }).then(async () => {
    try {
      await store.dispatch("reservations/cancelReservation", row.id);
      ElMessage({ message: "预约已取消", type: "success", plain: true });
    } catch (e) {
      ElMessage.error(e.message || "取消失败");
    }
  });
};

onMounted(() => {
  store.dispatch("reservations/fetchMyReservations");
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

.apple-btn-text {
  border: none;
  background: transparent;
  color: #ff3b30;
  font-weight: 500;
  padding: 4px 8px;
}

.apple-btn-text:hover {
  background-color: rgba(255, 59, 48, 0.08);
  border-radius: 4px;
}

.apple-btn.danger-light {
  background-color: rgba(255, 59, 48, 0.1);
  color: #ff3b30;
  border-radius: 20px;
  padding: 10px 20px;
  height: auto;
  font-weight: 500;
  border: none;
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
  background-color: rgba(255, 149, 0, 0.1);
  color: #ff9500;
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

.queue-position {
  font-weight: 600;
  color: #ff9500;
}

.status-tag {
  border-radius: 6px;
  font-weight: 600;
  font-size: 12px;
}

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
  font-size: 18px;
  font-weight: 700;
}

.reserve-time {
  margin: 4px 0 0 0;
  color: var(--apple-text-secondary);
  font-size: 13px;
}

.card-details {
  margin-bottom: 16px;
}

.queue-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background-color: rgba(255, 149, 0, 0.08);
  border-radius: 10px;
  color: #ff9500;
  font-size: 14px;
}

.queue-info strong {
  font-size: 18px;
}

.card-actions {
  display: flex;
  gap: 12px;
}

.full-width {
  flex: 1;
  justify-content: center;
  border-radius: 20px;
  padding: 10px 20px;
  height: auto;
  font-weight: 500;
  border: none;
}

@media (max-width: 768px) {
  .view-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
