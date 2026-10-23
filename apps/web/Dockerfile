# ============================================================
# firstfuse-web — Vue3 + Vite 多阶段构建 + nginx 托管
# ============================================================
FROM node:20-alpine AS builder

WORKDIR /app

# 利用层缓存：先装依赖再 COPY 源码
COPY pnpm-workspace.yaml pnpm-lock.yaml package.json ./
COPY shared/package.json shared/pnpm-lock.yaml ./shared/
COPY apps/web/package.json apps/web/pnpm-lock.yaml ./apps/web/

RUN npm install -g pnpm@9 && pnpm install --frozen-lockfile --filter firstfuse-web...

COPY shared ./shared
COPY apps/web ./apps/web

RUN cd apps/web && pnpm build

# ============================================================
# nginx 运行阶段
# ============================================================
FROM nginx:1.27-alpine AS serve

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/apps/web/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
