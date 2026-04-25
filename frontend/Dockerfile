# 前端 Dockerfile - 两阶段构建
# 阶段一：Node.js 打包生成静态资源
FROM node:18-alpine AS build
WORKDIR /app
# 先复制依赖描述文件，利用缓存层加速
COPY package.json package-lock.json ./
RUN npm ci
# 复制源码并构建生产版本
COPY . .
RUN npm run build

# 阶段二：Nginx 托管静态资源
FROM nginx:alpine
# 复制构建产物到 Nginx 默认目录
COPY --from=build /app/dist /usr/share/nginx/html
# 复制自定义 Nginx 配置（含 API 反代 + WebSocket 支持）
COPY nginx.conf /etc/nginx/conf.d/default.conf
# 暴露 HTTP 端口
EXPOSE 80
