import { createApp } from "vue";
import ElementPlus from "element-plus";
import "element-plus/dist/index.css";
import "element-plus/theme-chalk/display.css";
import App from "./App.vue";
import router from "./router";
import store from "./store";
import { setupHttpInterceptors } from "./services/http";

const app = createApp(App);

setupHttpInterceptors(store, router);

app.use(ElementPlus);
app.use(store);
app.use(router);

app.config.errorHandler = (error) => {
  store.dispatch("ui/setGlobalError", error?.message || "页面发生异常");
};

app.mount("#app");
