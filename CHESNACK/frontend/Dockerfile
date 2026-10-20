# ============ 构建阶段 ============
# 基础镜像走 daocloud 镜像源（国内网络直连 Docker Hub 不稳定）
FROM docker.m.daocloud.io/library/node:20-alpine AS build
WORKDIR /build
COPY package.json ./
RUN npm install --registry=https://registry.npmmirror.com
COPY . .
RUN npm run build

# ============ 运行阶段 ============
FROM docker.m.daocloud.io/library/nginx:1.27-alpine
COPY --from=build /build/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
