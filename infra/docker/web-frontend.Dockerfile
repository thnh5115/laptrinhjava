# =======================
# 1) BUILDER STAGE
# =======================
FROM node:20-alpine AS builder

# Install pnpm globally
RUN npm install -g pnpm

# Set workspace root
WORKDIR /workspace

# Build-time API endpoints (override via build args)
ARG NEXT_PUBLIC_API_URL=http://localhost:8080/api
ARG NEXT_PUBLIC_ADMIN_API_URL=http://localhost:8080/api
ARG NEXT_PUBLIC_BUYER_API_URL=http://localhost:8081/api
ARG NEXT_PUBLIC_OWNER_API_URL=http://localhost:8082/api/owner
ARG NEXT_PUBLIC_CVA_API_URL=http://localhost:8083/api/cva

ENV NEXT_PUBLIC_API_URL=${NEXT_PUBLIC_API_URL} \
    NEXT_PUBLIC_ADMIN_API_URL=${NEXT_PUBLIC_ADMIN_API_URL} \
    NEXT_PUBLIC_BUYER_API_URL=${NEXT_PUBLIC_BUYER_API_URL} \
    NEXT_PUBLIC_OWNER_API_URL=${NEXT_PUBLIC_OWNER_API_URL} \
    NEXT_PUBLIC_CVA_API_URL=${NEXT_PUBLIC_CVA_API_URL}

# Copy workspace configs
COPY pnpm-workspace.yaml pnpm-lock.yaml package.json ./

# Copy app manifest
COPY apps/web-portal-next/package.json ./apps/web-portal-next/

# Copy shared package manifests (optional; tolerate absence)
COPY packages ./packages

# Install dependencies (lockfile currently out of sync)
RUN pnpm install --no-frozen-lockfile

# Copy full source
COPY . .

# Build the Next.js app
WORKDIR /workspace/apps/web-portal-next
RUN pnpm build

# =======================
# 2) RUNTIME STAGE
# =======================
FROM node:20-alpine AS runner

WORKDIR /app
ENV NODE_ENV=production
ENV PORT=3000

# pnpm needed to run the start script
RUN npm install -g pnpm

# Copy root configs (optional, keeps workspace context)
COPY --from=builder /workspace/pnpm-workspace.yaml ./
COPY --from=builder /workspace/package.json ./

# Copy all node_modules (root + app-level symlinks)
COPY --from=builder /workspace/node_modules ./node_modules

# Copy the app folder (includes .next and public)
COPY --from=builder /workspace/apps/web-portal-next ./apps/web-portal-next

WORKDIR /app/apps/web-portal-next

EXPOSE 3000
CMD ["pnpm", "start"]
