<template>
  <el-container class="layout">
    <!-- Desktop Sidebar -->
    <el-aside width="240px" class="aside hidden-xs-only">
      <div class="brand">
        <div class="brand-icon">
          <el-icon><IepCollection /></el-icon>
        </div>
        <div class="brand-info">
          <div class="brand-title">智慧图书馆</div>
          <div class="brand-sub">Borrowing Center</div>
        </div>
      </div>
      <el-menu :default-active="activeMenu" class="menu" router>
        <el-menu-item index="/books">
          <el-icon><IepNotebook /></el-icon>
          <span>图书浏览</span>
        </el-menu-item>
        <el-menu-item index="/borrows">
          <el-icon><IepReading /></el-icon>
          <span>借阅管理</span>
        </el-menu-item>
        <el-menu-item index="/my-reservations" v-if="!isAdmin">
          <el-icon><IepBell /></el-icon>
          <span>我的预约</span>
        </el-menu-item>
        <el-menu-item index="/history" v-if="isAdmin">
          <el-icon><IepTimer /></el-icon>
          <span>全馆历史</span>
        </el-menu-item>
        <el-menu-item index="/overdue" v-if="isAdmin">
          <el-icon><IepWarning /></el-icon>
          <span>逾期统计</span>
        </el-menu-item>
        <el-menu-item index="/reservation-manage" v-if="isAdmin">
          <el-icon><IepBell /></el-icon>
          <span>预约管理</span>
        </el-menu-item>
        <el-menu-item index="/users" v-if="isAdmin">
          <el-icon><IepUser /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- Mobile Drawer Menu -->
    <el-drawer
      v-model="drawerVisible"
      direction="ltr"
      size="280px"
      :with-header="false"
      class="mobile-drawer hidden-sm-and-up"
    >
      <div class="brand mobile">
        <div class="brand-icon">
          <el-icon><IepCollection /></el-icon>
        </div>
        <div class="brand-info">
          <div class="brand-title">智慧图书馆</div>
        </div>
      </div>
      <el-menu :default-active="activeMenu" class="menu" router @select="closeDrawer">
        <el-menu-item index="/books">
          <el-icon><IepNotebook /></el-icon>
          <span>图书浏览</span>
        </el-menu-item>
        <el-menu-item index="/borrows">
          <el-icon><IepReading /></el-icon>
          <span>借阅管理</span>
        </el-menu-item>
        <el-menu-item index="/my-reservations" v-if="!isAdmin">
          <el-icon><IepBell /></el-icon>
          <span>我的预约</span>
        </el-menu-item>
        <el-menu-item index="/history" v-if="isAdmin">
          <el-icon><IepTimer /></el-icon>
          <span>全馆历史</span>
        </el-menu-item>
        <el-menu-item index="/overdue" v-if="isAdmin">
          <el-icon><IepWarning /></el-icon>
          <span>逾期统计</span>
        </el-menu-item>
        <el-menu-item index="/reservation-manage" v-if="isAdmin">
          <el-icon><IepBell /></el-icon>
          <span>预约管理</span>
        </el-menu-item>
        <el-menu-item index="/users" v-if="isAdmin">
          <el-icon><IepUser /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
      </el-menu>
    </el-drawer>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-button
            class="menu-toggle hidden-sm-and-up"
            @click="drawerVisible = true"
            circle
          >
            <el-icon><IepMenu /></el-icon>
          </el-button>
          <div class="page-title">{{ currentPageName }}</div>
        </div>
        <div class="header-right">
          <div class="user-profile hidden-xs-only">
            <span class="user-name">{{ profile?.fullName || "访客" }}</span>
            <el-tag size="small" :type="isAdmin ? 'danger' : 'success'" effect="plain" class="role-tag">
              {{ isAdmin ? '管理员' : '读者' }}
            </el-tag>
          </div>
          <el-button class="logout-btn" @click="logout">
            退出登录
          </el-button>
        </div>
      </el-header>
      <el-main class="content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useStore } from "vuex";
import {
  Collection as IepCollection,
  Notebook as IepNotebook,
  Reading as IepReading,
  Timer as IepTimer,
  Warning as IepWarning,
  User as IepUser,
  Menu as IepMenu,
  Bell as IepBell
} from "@element-plus/icons-vue";

const route = useRoute();
const router = useRouter();
const store = useStore();
const profile = computed(() => store.getters["auth/profile"]);
const isAdmin = computed(() => profile.value?.role === "ADMIN");
const activeMenu = computed(() => route.path);
const drawerVisible = ref(false);

const closeDrawer = () => {
  drawerVisible.value = false;
};

const currentPageName = computed(() => {
  const map = {
    "/books": "图书浏览",
    "/borrows": "借阅管理",
    "/history": "全馆历史",
    "/overdue": "逾期统计",
    "/users": "用户管理",
    "/my-reservations": "我的预约",
    "/reservation-manage": "预约管理"
  };
  return map[route.path] || "图书馆系统";
});

const logout = () => {
  store.dispatch("auth/logout");
  router.replace({ name: "login" });
};
</script>

<style scoped>
.layout {
  min-height: 100vh;
  background-color: var(--apple-bg);
}

.aside {
  background-color: #ffffff;
  border-right: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  z-index: 10;
}

.brand {
  padding: 32px 24px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand.mobile {
  padding: 24px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  margin-bottom: 12px;
}

.brand-icon {
  width: 40px;
  height: 40px;
  background-color: var(--apple-blue);
  color: white;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  box-shadow: 0 4px 12px rgba(0, 113, 227, 0.2);
}

.brand-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--apple-text);
  line-height: 1.2;
}

.brand-sub {
  font-size: 11px;
  color: var(--apple-text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-top: 2px;
}

.menu {
  border-right: none;
  padding: 0 12px;
}

:deep(.el-menu-item) {
  height: 44px;
  line-height: 44px;
  border-radius: 8px;
  margin-bottom: 4px;
  color: var(--apple-text-secondary);
  transition: all 0.2s ease;
}

:deep(.el-menu-item .el-icon) {
  margin-right: 12px;
  font-size: 18px;
  width: 18px;
  height: 18px;
}

:deep(.el-menu-item .el-icon svg) {
  width: 18px;
  height: 18px;
}

:deep(.el-menu-item:hover) {
  background-color: rgba(0, 0, 0, 0.03);
  color: var(--apple-text);
}

:deep(.el-menu-item.is-active) {
  background-color: rgba(0, 113, 227, 0.08);
  color: var(--apple-blue);
  font-weight: 500;
}

.header {
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  background-color: rgba(245, 245, 247, 0.7);
  backdrop-filter: var(--apple-glass);
  -webkit-backdrop-filter: var(--apple-glass);
  position: sticky;
  top: 0;
  z-index: 100;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.menu-toggle {
  border: none;
  background: rgba(0, 0, 0, 0.05);
  font-size: 20px;
  color: var(--apple-text);
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--apple-text);
  letter-spacing: -0.5px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--apple-text);
}

.role-tag {
  border-radius: 6px;
  font-weight: 600;
  font-size: 10px;
}

.logout-btn {
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  padding: 8px 16px;
  height: auto;
  border: 1px solid #d2d2d7;
  background: transparent;
  color: var(--apple-text);
  transition: all 0.2s ease;
}

.logout-btn:hover {
  background-color: rgba(0, 0, 0, 0.05);
  border-color: #86868b;
}

.content {
  padding: 32px;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
}

@media (max-width: 768px) {
  .header {
    padding: 0 16px;
    height: 64px;
  }
  
  .page-title {
    font-size: 20px;
  }
  
  .content {
    padding: 16px;
  }
}

:deep(.mobile-drawer .el-drawer__body) {
  padding: 0;
  background-color: #ffffff;
}
</style>
