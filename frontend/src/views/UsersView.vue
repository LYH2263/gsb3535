<template>
  <div class="page-container">
    <div class="view-header">
      <div class="header-content">
        <h1 class="view-title">用户管理</h1>
        <p class="view-subtitle">维护读者档案，配置系统访问权限</p>
      </div>
      <el-button type="primary" class="apple-btn primary" @click="openDialog()">
        <el-icon><IepPlus /></el-icon>
        <span>新增用户</span>
      </el-button>
    </div>

    <div class="content-body">
      <el-skeleton v-if="loading" :rows="10" animated class="apple-skeleton" />
      
      <div v-else>
        <!-- Desktop Table View -->
        <div class="table-card hidden-xs-only">
          <el-table :data="users" class="apple-table">
            <el-table-column prop="username" label="用户名" width="160">
              <template #default="{ row }">
                <div class="user-cell">
                  <div class="user-avatar">{{ row.fullName?.charAt(0) }}</div>
                  <span class="username-text">{{ row.username }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="fullName" label="全名" min-width="140" />
            <el-table-column prop="role" label="系统角色" width="140">
              <template #default="{ row }">
                <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'success'" size="small" class="apple-tag">
                  {{ row.role === 'ADMIN' ? '管理员' : '普通读者' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" align="right" fixed="right">
              <template #default="{ row }">
                <div class="action-buttons" v-if="row.role !== 'ADMIN'">
                  <el-button size="small" class="apple-btn-text" @click="openDialog(row)">编辑</el-button>
                  <el-button size="small" class="apple-btn-text danger" @click="remove(row.id)">删除</el-button>
                </div>
                <div v-else class="admin-placeholder">
                  <el-tag size="small" type="info" effect="plain" class="apple-tag">系统保护</el-tag>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- Mobile Card View -->
        <div class="mobile-cards hidden-sm-and-up">
          <div v-for="user in users" :key="user.id" class="user-mobile-card">
            <div class="card-header">
              <div class="user-main">
                <div class="user-avatar large">{{ user.fullName?.charAt(0) }}</div>
                <div class="user-info">
                  <h3 class="full-name">{{ user.fullName }}</h3>
                  <p class="username">@{{ user.username }}</p>
                </div>
              </div>
              <el-tag :type="user.role === 'ADMIN' ? 'danger' : 'success'" size="small" class="apple-tag">
                {{ user.role === 'ADMIN' ? '管理员' : '读者' }}
              </el-tag>
            </div>
            <div class="card-actions" v-if="user.role !== 'ADMIN'">
              <el-button size="small" class="apple-btn secondary" @click="openDialog(user)">编辑</el-button>
              <el-button size="small" class="apple-btn danger-light" @click="remove(user.id)">删除</el-button>
            </div>
            <div v-else class="admin-notice">
              管理员账号受系统保护，不可编辑或删除
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 用户编辑/新增弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" :width="dialogWidth" class="apple-dialog" :close-on-click-modal="false">
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top" class="apple-form">
        <el-form-item label="登录用户名" prop="username">
          <el-input v-model="form.username" placeholder="建议使用英文或拼音" class="apple-input" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="fullName">
          <el-input v-model="form.fullName" placeholder="请输入读者全名" class="apple-input" />
        </el-form-item>
        <el-form-item v-if="!form.id" label="设置初始密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="至少6位字符" class="apple-input" />
        </el-form-item>
        <el-form-item label="分配权限角色" prop="role">
          <el-select v-model="form.role" class="apple-select">
            <el-option label="普通读者 (USER)" value="USER" />
            <el-option label="管理员 (ADMIN)" value="ADMIN" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button class="apple-btn secondary" @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" class="apple-btn primary" @click="submit">保存账户</el-button>
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
import { Plus as IepPlus } from "@element-plus/icons-vue";

const store = useStore();
const loading = computed(() => store.getters["users/loading"]);
const users = computed(() => store.getters["users/users"]);

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
  return screenWidth.value < 768 ? '90%' : '440px';
});

const dialogVisible = ref(false);
const dialogTitle = ref("新增用户");
const formRef = ref(null);
const form = reactive({
  id: null,
  username: "",
  fullName: "",
  password: "",
  role: "USER"
});

const rules = {
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  fullName: [{ required: true, message: "请输入全名", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }],
  role: [{ required: true, message: "请选择角色", trigger: "change" }]
};

const schema = z.object({
  username: z.string().min(3, "用户名至少3个字符"),
  fullName: z.string().min(1, "请输入全名"),
  password: z.string().min(6, "密码至少6位").optional().or(z.literal("")),
  role: z.enum(["ADMIN", "USER"])
});

const resetForm = () => {
  form.id = null;
  form.username = "";
  form.fullName = "";
  form.password = "";
  form.role = "USER";
};

const openDialog = (row) => {
  resetForm();
  if (row) {
    form.id = row.id;
    form.username = row.username;
    form.fullName = row.fullName;
    form.role = row.role;
    dialogTitle.value = "编辑用户";
  } else {
    dialogTitle.value = "新增用户";
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
    try {
      if (form.id) {
        await store.dispatch("users/updateUser", form);
        ElMessage.success("用户信息已更新");
      } else {
        await store.dispatch("users/createUser", form);
        ElMessage.success("新用户已创建");
      }
      dialogVisible.value = false;
    } catch (error) {
      // 错误已由拦截器处理
    }
  });
};

const remove = (id) => {
  ElMessageBox.confirm("确定要注销此用户吗？此操作将移除该用户的所有访问权限。", "注销确认", {
    confirmButtonText: "确认注销",
    cancelButtonText: "取消",
    type: "warning",
    confirmButtonClass: 'apple-btn danger',
    cancelButtonClass: 'apple-btn secondary'
  }).then(async () => {
    await store.dispatch("users/deleteUser", id);
    ElMessage.success("用户已注销");
  });
};

store.dispatch("users/fetchUsers");
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

.apple-btn.secondary {
  background-color: rgba(0, 0, 0, 0.05);
  color: var(--apple-text);
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
  gap: 12px;
}

.user-avatar {
  width: 32px;
  height: 32px;
  background-color: #e5e5ea;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  color: var(--apple-text-secondary);
}

.username-text {
  font-weight: 600;
  color: var(--apple-text);
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

.user-mobile-card {
  background: white;
  border-radius: var(--apple-radius);
  padding: 20px;
  box-shadow: var(--apple-shadow);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.user-main {
  display: flex;
  gap: 12px;
  align-items: center;
}

.user-avatar.large {
  width: 48px;
  height: 48px;
  font-size: 20px;
}

.full-name {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
}

.username {
  margin: 2px 0 0 0;
  font-size: 14px;
  color: var(--apple-text-secondary);
}

.card-actions {
  display: flex;
  gap: 12px;
}

.admin-notice {
  font-size: 12px;
  color: var(--apple-text-secondary);
  background-color: rgba(0, 0, 0, 0.03);
  padding: 10px;
  border-radius: 8px;
  text-align: center;
}

/* Dialog & Form */
.apple-dialog :deep(.el-dialog) {
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.apple-input :deep(.el-input__wrapper) {
  border-radius: 10px;
  background-color: rgba(0, 0, 0, 0.03);
  box-shadow: none !important;
  padding: 4px 12px;
}

.apple-select {
  width: 100%;
}

.apple-select :deep(.el-input__wrapper) {
  border-radius: 10px;
  background-color: rgba(0, 0, 0, 0.03);
  box-shadow: none !important;
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
