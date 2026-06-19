<template>
  <div class="page-container">
    <div class="view-header">
      <div class="header-content">
        <h1 class="view-title">图书浏览</h1>
        <p class="view-subtitle">发现精彩书籍，开启阅读之旅</p>
      </div>
      <div class="header-actions">
        <div class="search-wrapper">
          <el-input
            v-model="queryParams.keyword"
            placeholder="搜索书名、作者或 ISBN"
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
        <el-button v-if="isAdmin" type="primary" class="apple-btn primary" @click="openDialog()">
          <el-icon><IepPlus /></el-icon>
          <span>新增图书</span>
        </el-button>
      </div>
    </div>

    <div class="content-body">
      <el-skeleton v-if="loading" :rows="10" animated class="apple-skeleton" />
      
      <div v-else>
        <!-- Desktop Table View -->
        <div class="table-card hidden-xs-only">
          <el-table :data="books" class="apple-table" row-class-name="apple-table-row">
            <el-table-column prop="title" label="书名" min-width="200">
              <template #default="{ row }">
                <div class="book-title-cell">
                  <div class="book-icon"><el-icon><IepNotebook /></el-icon></div>
                  <span class="title-text">{{ row.title }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="author" label="作者" width="160" />
            <el-table-column prop="isbn" label="ISBN" width="140" />
            <el-table-column prop="category" label="分类" width="120">
              <template #default="{ row }">
                <el-tag size="small" class="apple-tag">{{ row.category || '未分类' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="totalCopies" label="总数" width="100" align="center" />
            <el-table-column prop="availableCopies" label="可借" width="100" align="center">
              <template #default="{ row }">
                <span :class="['stock-count', row.availableCopies > 0 ? 'in-stock' : 'out-of-stock']">
                  {{ row.availableCopies }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="操作" :width="isAdmin ? 200 : 200" align="right" fixed="right">
              <template #default="{ row }">
                <div class="action-buttons">
                  <template v-if="isAdmin">
                    <el-button size="small" class="apple-btn-text" @click="openDialog(row)">编辑</el-button>
                    <el-button size="small" class="apple-btn-text danger" @click="remove(row.id)">删除</el-button>
                  </template>
                  <template v-else>
                    <el-button
                      size="small"
                      class="apple-btn secondary"
                      :disabled="row.availableCopies <= 0"
                      @click="openBorrowDialog(row)"
                    >
                      借阅
                    </el-button>
                    <el-button
                      size="small"
                      class="apple-btn reserve-btn"
                      :disabled="row.availableCopies > 0"
                      @click="handleReserve(row)"
                    >
                      {{ row.availableCopies > 0 ? '可直接借阅' : '预约' }}
                    </el-button>
                  </template>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- Mobile Card View -->
        <div class="mobile-cards hidden-sm-and-up">
          <div v-for="book in books" :key="book.id" class="book-mobile-card">
            <div class="card-header">
              <div class="book-info">
                <h3 class="book-title">{{ book.title }}</h3>
                <p class="book-author">{{ book.author }}</p>
              </div>
              <el-tag size="small" class="apple-tag">{{ book.category }}</el-tag>
            </div>
            <div class="card-details">
              <div class="detail-item">
                <span class="label">ISBN:</span>
                <span class="value">{{ book.isbn }}</span>
              </div>
              <div class="detail-item">
                <span class="label">可借/总数:</span>
                <span class="value">{{ book.availableCopies }} / {{ book.totalCopies }}</span>
              </div>
            </div>
            <div class="card-actions">
              <template v-if="isAdmin">
                <el-button size="small" class="apple-btn secondary" @click="openDialog(book)">编辑</el-button>
                <el-button size="small" class="apple-btn danger-light" @click="remove(book.id)">删除</el-button>
              </template>
              <template v-else>
                <el-button
                  size="default"
                  class="apple-btn primary"
                  :disabled="book.availableCopies <= 0"
                  @click="openBorrowDialog(book)"
                >
                  借阅
                </el-button>
                <el-button
                  size="default"
                  class="apple-btn reserve-btn"
                  :disabled="book.availableCopies > 0"
                  @click="handleReserve(book)"
                >
                  {{ book.availableCopies > 0 ? '可借' : '预约' }}
                </el-button>
              </template>
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

    <!-- 借阅弹窗 -->
    <el-dialog v-model="borrowDialogVisible" title="确认借阅" :width="dialogWidth" class="apple-dialog" :close-on-click-modal="false">
      <div class="borrow-confirm-content">
        <div class="selected-book-preview">
          <div class="preview-icon"><el-icon><IepNotebook /></el-icon></div>
          <div class="preview-info">
            <div class="preview-title">{{ selectedBook?.title }}</div>
            <div class="preview-author">{{ selectedBook?.author }}</div>
          </div>
        </div>
        <el-form :model="borrowForm" label-position="top" class="apple-form">
          <el-form-item label="计划借阅时长" required>
            <div class="days-selector">
              <el-input-number v-model="borrowForm.borrowDays" :min="1" :max="30" class="apple-number-input" />
              <span class="unit">天</span>
            </div>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button class="apple-btn secondary" @click="borrowDialogVisible = false">取消</el-button>
          <el-button type="primary" class="apple-btn primary" :loading="borrowing" @click="handleBorrow">
            确认借阅
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 编辑/新增弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" :width="dialogWidth" class="apple-dialog" :close-on-click-modal="false">
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top" class="apple-form">
        <el-form-item label="图书名称" prop="title">
          <el-input v-model="form.title" placeholder="例如：云原生架构实践" class="apple-input" />
        </el-form-item>
        <div class="form-row">
          <el-form-item label="作者" prop="author" class="flex-1">
            <el-input v-model="form.author" placeholder="作者姓名" class="apple-input" />
          </el-form-item>
          <el-form-item label="分类" prop="category" class="flex-1">
            <el-input v-model="form.category" placeholder="分类名称" class="apple-input" />
          </el-form-item>
        </div>
        <el-form-item label="ISBN 编号" prop="isbn">
          <el-input v-model="form.isbn" placeholder="标准 ISBN 号" class="apple-input" />
        </el-form-item>
        <el-form-item label="总库存数量" prop="totalCopies">
          <el-input-number v-model="form.totalCopies" :min="1" class="apple-number-input" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button class="apple-btn secondary" @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" class="apple-btn primary" @click="submit">
            保存信息
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, reactive, ref, onMounted, onUnmounted } from "vue";
import { useStore } from "vuex";
import { z } from "zod";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  Search as IepSearch,
  Plus as IepPlus,
  Notebook as IepNotebook
} from "@element-plus/icons-vue";

const store = useStore();
const loading = computed(() => store.getters["books/loading"]);
const books = computed(() => store.getters["books/books"]);
const total = computed(() => store.getters["books/total"]);
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
  return screenWidth.value < 768 ? '90%' : '500px';
});

const queryParams = reactive({
  current: 1,
  size: 10,
  keyword: ""
});

const loadData = () => {
  store.dispatch("books/fetchBooks", queryParams);
};

const handleSearch = () => {
  queryParams.current = 1;
  loadData();
};

const borrowDialogVisible = ref(false);
const borrowing = ref(false);
const selectedBook = ref(null);
const borrowForm = reactive({
  borrowDays: 14
});

const openBorrowDialog = (row) => {
  selectedBook.value = row;
  borrowForm.borrowDays = 14;
  borrowDialogVisible.value = true;
};

const handleBorrow = async () => {
  if (!selectedBook.value || !profile.value) {
    return;
  }
  borrowing.value = true;
  try {
    await store.dispatch("borrows/borrow", {
      bookId: selectedBook.value.id,
      userId: profile.value.id,
      borrowDays: borrowForm.borrowDays
    });
    ElMessage({
      message: `成功借阅《${selectedBook.value.title}》`,
      type: 'success',
      plain: true
    });
    borrowDialogVisible.value = false;
    await store.dispatch("books/fetchBooks");
  } finally {
    borrowing.value = false;
  }
};

const handleReserve = async (book) => {
  if (!profile.value) return;
  try {
    await store.dispatch("reservations/reserve", {
      bookId: book.id,
      userId: profile.value.id
    });
    ElMessage({
      message: `成功预约《${book.title}》`,
      type: 'success',
      plain: true
    });
  } catch (e) {
    ElMessage.error(e.message || "预约失败");
  }
};

const dialogVisible = ref(false);
const dialogTitle = ref("新增图书");
const formRef = ref(null);
const form = reactive({
  id: null,
  title: "",
  author: "",
  isbn: "",
  category: "",
  totalCopies: 1
});

const rules = {
  title: [{ required: true, message: "请输入书名", trigger: "blur" }],
  author: [{ required: true, message: "请输入作者", trigger: "blur" }],
  isbn: [{ required: true, message: "请输入ISBN", trigger: "blur" }],
  totalCopies: [{ required: true, message: "请输入库存数量", trigger: "blur" }]
};

const schema = z.object({
  title: z.string().min(1, "请输入书名"),
  author: z.string().min(1, "请输入作者"),
  isbn: z.string().min(1, "请输入ISBN"),
  category: z.string().optional(),
  totalCopies: z.number().min(0, "库存必须大于等于0")
});

const resetForm = () => {
  form.id = null;
  form.title = "";
  form.author = "";
  form.isbn = "";
  form.category = "";
  form.totalCopies = 1;
};

const openDialog = (row) => {
  resetForm();
  if (row) {
    form.id = row.id;
    form.title = row.title;
    form.author = row.author;
    form.isbn = row.isbn;
    form.category = row.category;
    form.totalCopies = row.totalCopies;
    dialogTitle.value = "编辑图书";
  } else {
    dialogTitle.value = "新增图书";
  }
  dialogVisible.value = true;
};

const submit = async () => {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid) => {
    if (!valid) return;
    const result = schema.safeParse(form);
    if (!result.success) {
      ElMessage.error(result.error.errors[0]?.message);
      return;
    }
    if (form.id) {
      await store.dispatch("books/updateBook", form);
    } else {
      await store.dispatch("books/createBook", form);
    }
    dialogVisible.value = false;
  });
};

const remove = (id) => {
  ElMessageBox.confirm("确定要删除这本书吗？此操作不可撤销。", "提示", {
    confirmButtonText: "确定删除",
    cancelButtonText: "取消",
    type: "warning",
    confirmButtonClass: 'apple-btn danger',
    cancelButtonClass: 'apple-btn secondary'
  }).then(async () => {
    await store.dispatch("books/deleteBook", id);
    ElMessage.success("已成功删除");
  });
};

loadData();
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

/* Apple Style Search Input */
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

.apple-btn.primary {
  background-color: var(--apple-blue);
  color: white;
}

.apple-btn.primary:hover {
  background-color: #0077ed;
  transform: translateY(-1px);
}

.apple-btn.secondary {
  background-color: rgba(0, 0, 0, 0.05);
  color: var(--apple-text);
}

.apple-btn.secondary:hover {
  background-color: rgba(0, 0, 0, 0.08);
}

.apple-btn.danger {
  background-color: #ff3b30;
  color: white;
}

.apple-btn.danger-light {
  background-color: rgba(255, 59, 48, 0.1);
  color: #ff3b30;
}

.apple-btn.reserve-btn {
  background-color: rgba(255, 149, 0, 0.1);
  color: #ff9500;
}

.apple-btn.reserve-btn:hover:not(:disabled) {
  background-color: rgba(255, 149, 0, 0.2);
}

.apple-btn.reserve-btn:disabled {
  background-color: rgba(0, 0, 0, 0.03);
  color: var(--apple-text-secondary);
  cursor: not-allowed;
}

.apple-btn-text {
  border: none;
  background: transparent;
  color: var(--apple-blue);
  font-weight: 500;
  padding: 4px 8px;
}

.apple-btn-text:hover {
  background-color: rgba(0, 113, 227, 0.08);
  border-radius: 4px;
}

.apple-btn-text.danger {
  color: #ff3b30;
}

.apple-btn-text.danger:hover {
  background-color: rgba(255, 59, 48, 0.08);
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

.apple-tag {
  border-radius: 6px;
  background-color: rgba(0, 0, 0, 0.04);
  border: none;
  color: var(--apple-text-secondary);
  font-weight: 500;
}

.stock-count {
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}

.stock-count.in-stock {
  color: #34c759;
}

.stock-count.out-of-stock {
  color: #ff3b30;
}

/* Mobile Card Styles */
.mobile-cards {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.book-mobile-card {
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

.book-author {
  margin: 4px 0 0 0;
  color: var(--apple-text-secondary);
  font-size: 14px;
}

.card-details {
  margin-bottom: 20px;
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

.detail-item .value {
  font-weight: 500;
}

.card-actions {
  display: flex;
  gap: 12px;
}

.card-actions .apple-btn {
  flex: 1;
  justify-content: center;
  border-radius: 20px;
  padding: 10px 20px;
  height: auto;
  font-weight: 500;
  border: none;
}

.full-width {
  flex: 1;
  justify-content: center;
}

/* Pagination */
.pagination-wrapper {
  margin-top: 32px;
  display: flex;
  justify-content: center;
}

/* Apple Style Dialog */
.apple-dialog :deep(.el-dialog) {
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.apple-dialog :deep(.el-dialog__header) {
  padding: 24px 24px 0;
  margin: 0;
}

.apple-dialog :deep(.el-dialog__title) {
  font-weight: 700;
  font-size: 20px;
}

.apple-dialog :deep(.el-dialog__body) {
  padding: 24px;
}

.apple-dialog :deep(.el-dialog__footer) {
  padding: 0 24px 24px;
}

.borrow-confirm-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.selected-book-preview {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background-color: rgba(0, 0, 0, 0.03);
  border-radius: 12px;
}

.preview-icon {
  width: 48px;
  height: 48px;
  background-color: white;
  color: var(--apple-blue);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.preview-title {
  font-weight: 700;
  font-size: 16px;
}

.preview-author {
  color: var(--apple-text-secondary);
  font-size: 13px;
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

/* Responsive */
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
  
  .apple-btn.primary {
    width: 100%;
    justify-content: center;
  }
}
</style>
