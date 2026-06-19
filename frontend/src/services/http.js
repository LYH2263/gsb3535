import axios from "axios";
import { ElMessage } from "element-plus";

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || "http://localhost:8355/api";

export const http = axios.create({
  baseURL: apiBaseUrl,
  timeout: 10000
});

export function setupHttpInterceptors(store, router) {
  http.interceptors.request.use((config) => {
    const token = store.getters["auth/token"];
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  });

  http.interceptors.response.use(
    (response) => {
      const res = response.data;
      // 处理统一响应结构
      if (res && typeof res === 'object' && 'code' in res) {
        if (res.code === 200) {
          return res; // 返回整个 res 包含 data
        } else {
          ElMessage.error(res.message || "操作失败");
          return Promise.reject(new Error(res.message || "操作失败"));
        }
      }
      return response;
    },
    (error) => {
      const status = error?.response?.status;
      const data = error?.response?.data;
      const message = data?.message || "网络请求失败，请稍后重试";
      
      ElMessage.error(message);
      
      if (status === 401) {
        store.dispatch("auth/logout");
        router.replace({ name: "login" });
      }
      return Promise.reject(error);
    }
  );
}
