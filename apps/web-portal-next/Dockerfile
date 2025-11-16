# Stage 1: Build
FROM node:18-alpine AS builder

WORKDIR /app

# Chỉ copy file package để cache npm install
COPY apps/web-portal-next/package*.json ./

RUN npm install --legacy-peer-deps

# Copy toàn bộ source của web-portal-next
COPY apps/web-portal-next ./

# Build Next.js (tạo .next)
RUN npm run build

# Stage 2: Runtime
FROM node:18-alpine

WORKDIR /app
ENV NODE_ENV=production

# Copy app đã build từ stage builder
COPY --from=builder /app ./

EXPOSE 3000

CMD ["npm", "start"]
