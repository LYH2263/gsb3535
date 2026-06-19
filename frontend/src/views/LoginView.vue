<template>
  <div class="login-page">
    <el-card class="login-card">
      <h2 class="title">智慧图书馆借书系统</h2>
      <p class="subtitle">请使用账号登录以继续管理与借阅</p>
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-button type="primary" class="submit" :loading="loading" @click="handleLogin">登录</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { useStore } from "vuex";
import { z } from "zod";
import { ElMessage } from "element-plus";

const router = useRouter();
const store = useStore();
const loading = ref(false);
const formRef = ref(null);
const form = reactive({
  username: "",
  password: ""
});

const rules = {
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }]
};

const schema = z.object({
  username: z.string().min(1, "请输入用户名"),
  password: z.string().min(1, "请输入密码")
});

const handleLogin = async () => {
  if (!formRef.value) {
    return;
  }
  await formRef.value.validate(async (valid) => {
    if (!valid) {
      return;
    }
    const result = schema.safeParse(form);
    if (!result.success) {
      ElMessage.error(result.error.errors[0]?.message || "表单校验失败");
      return;
    }
    loading.value = true;
    try {
      await store.dispatch("auth/login", form);
      router.replace({ name: "books" });
    } finally {
      loading.value = false;
    }
  });
};
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.login-card {
  width: 100%;
  max-width: 420px;
  border-radius: 16px;
}

.title {
  margin-bottom: 8px;
  text-align: center;
}

.subtitle {
  margin-bottom: 24px;
  text-align: center;
  color: #8b8fa8;
}

.submit {
  width: 100%;
  margin-top: 8px;
}

.hint {
  margin-top: 16px;
}
</style>
