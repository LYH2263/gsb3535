# 🍎 智慧图书馆借书系统 (Smart Library System)

一款基于 **Spring Boot + Vue 3** 构建的现代化图书馆管理系统，深度融合 **Apple 级审美** 与 **响应式交互** 体验。本项目符合 **Github 高星项目标准**：开箱即用、架构清晰、审美现代、完全容器化。

##  技术栈
- **Frontend**: Vue 3 (Composition API) + Vuex + Vue Router + Element Plus + Axios
- **Backend**: Spring Boot 2.7 + Spring Security (JWT) + MyBatisPlus + Hibernate Validator
- **Database**: MySQL 8.0
- **DevOps**: Docker + Docker Compose

## 🚀 启动指南 (How to Run)
1. 确保 **Docker Desktop** 已启动。
2. 在项目根目录执行：
   ```bash
   docker compose up --build
   ```
3. 等待容器启动完成，通过浏览器访问下方服务地址。

## 🔗 服务地址 (Services)
- **Frontend**: [http://localhost:3355](http://localhost:3355)
- **Backend API**: [http://localhost:8355/api](http://localhost:8355/api)
- **Database**: `localhost:3306` (user: `root` / pass: `root`)

## 🧪 测试账号
| 角色 | 用户名 | 密码 | 权限范围 |
| :--- | :--- | :--- | :--- |
| **管理员** | `admin` | `123456` | 图书增删改、用户管理、全馆借阅监控 |
| **普通读者** | `reader` | `123456` | 图书浏览、个人借阅、在线归还 |

---

## 🐳 Docker 镜像源配置 (Docker Registry Configuration)

### 推荐配置（基于实际项目验证）

#### 1. Docker 镜像源
使用官方 Docker Hub 镜像（已验证稳定可用）。本项目 `docker-compose.yml` 已配置完整的服务链路与持久化挂载。

#### 2. npm 依赖源
前端构建已内置淘宝镜像源加速：
```dockerfile
RUN npm config set registry https://registry.npmmirror.com
```

#### 3. Maven 依赖源
后端构建已内置阿里云镜像源配置（通过挂载 `settings.xml` 实现加速）。

#### 4. 前端构建加速规范 (Fast Build with npm ci)
为了极致的构建速度和依赖一致性，本项目遵循以下流程：
1. **锁文件提交**: `package-lock.json` 已提交至仓库，确保环境确定性。
2. **容器内安装**: 使用 `npm ci` 代替 `npm install`，构建速度提升 2-3 倍。

---

### 常用镜像推荐

| 技术栈 | 推荐镜像 | 说明 |
| :--- | :--- | :--- |
| MySQL | `mysql:8.0` | 官方稳定版 |
| Node.js | `node:20-slim` | 轻量化构建镜像 |
| Nginx | `nginx:alpine` | 高性能生产环境镜像 |
| Java (构建) | `maven:3.9-eclipse-temurin-17` | 分层构建加速 |
| Java (运行) | `eclipse-temurin:17-jre` | 基于 Ubuntu 的稳定运行环境 |

---

## ✨ 核心亮点 (Key Features)

### 🎨 极致视觉与交互 (UI/UX)
- **Apple 级审美**：遵循苹果式简洁设计，应用毛玻璃（Glassmorphism）效果、柔和阴影与 SF Pro 级排版。
- **全端自适应**：移动端（≤768px）自动由数据表格切换为**精致卡片流**，响应式抽屉菜单。
- **丝滑动效**：全站应用 0.3s 缓动转场，配备骨架屏（Skeleton）加载。

### 🛡️ 工程质量与健壮性 (Robustness)
- **严苛校验**：前后端双重校验，后端 DTO 具备全量参数校验（@Size, @NotBlank 等）。
- **统一响应**：API 遵循 `ApiResponse<T>` 封装协议，内置全局异常拦截。
- **RBAC 权限**：严格的基于角色的访问控制，确保数据安全。
- **数据库规范**：使用 MyBatisPlus ORM 管理，容器启动自动填充 (Seeding) 演示数据。
