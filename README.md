# Carbon Credit Marketplace

Nền tảng giao dịch và kiểm duyệt carbon credit cho hệ sinh thái xe điện, gồm các vai trò Admin, Buyer, EV Owner và CVA. Monorepo chứa toàn bộ backend Spring Boot, frontend Next.js và hạ tầng Docker/Flyway.

## 🚀 Tech Stack
- **Backend**: Java 21, Spring Boot 3.5, Maven multi-module, Flyway, MapStruct, Spring Security/JWT
- **Frontend**: Next.js 15, React 19, TypeScript, Tailwind/PostCSS tooling
- **Infrastructure**: Docker/Docker Compose, MySQL 8, phpMyAdmin, Yarn/NPM scripts, batch/PowerShell utilities

## 📁 Cấu Trúc Thư Mục
```
apps/
  admin-backend-spring/   # Backend quản trị
  buyer-backend-spring/   # Backend người mua
  owner-backend-spring/   # Backend EV owner
  cva-backend-spring/     # Backend kiểm duyệt
  web-portal-next/        # Frontend Next.js
packages/
  java-common/            # Thư viện Java dùng chung
infra/
  docker/                 # docker-compose.yml & .env
  db/                     # Snapshot/migration SQL
scripts/                  # start.bat, stop-all.bat, setup.bat
README.md, pom.xml, package.json, ...
```

## 🔧 Install
1. **Prerequisites**: Java 21+, Maven 3.9+, Node.js 18+/npm, Docker Desktop, Git.
2. Clone repo: `git clone <repo-url> && cd carbon-credit-marketplace`.
3. Copy biến môi trường:  
   `cd infra/docker && copy .env.example .env` (Windows) hoặc `cp .env.example .env`.
4. (Tuỳ chọn) Cấp quyền thực thi cho `mvnw`/scripts khi chạy trên Unix-like.

## ▶️ Run
### Tất cả dịch vụ bằng Docker
```bash
cd infra/docker
docker compose up -d        # build + start MySQL + toàn bộ backend
```
Trên Windows có thể chạy `setup.bat` ở root để kiểm tra môi trường, build và khởi động Docker tự động.

### Backend thủ công (ví dụ Admin)
```bash
./mvnw -pl apps/admin-backend-spring -am clean package -DskipTests
./mvnw -f apps/admin-backend-spring/pom.xml -DskipTests spring-boot:run
```
Các module Buyer/Owner/CVA chạy tương tự sau khi Admin đã lên (cần bảng chung).  
API mặc định:
- Admin: http://localhost:8080
- Buyer: http://localhost:8081
- Owner: http://localhost:8082
- CVA:   http://localhost:8083

### Frontend
```bash
cd apps/web-portal-next
npm install --legacy-peer-deps
npm run dev      # http://localhost:3000
```
Sản xuất: `npm run build && npm run start`.

## 🗄️ ENV
- File mẫu: `infra/docker/.env.example` → copy thành `.env`.
- Biến chính:
  - `MYSQL_ROOT_PASSWORD`, `MYSQL_DATABASE`, `MYSQL_PORT`
  - `ADMIN_BACKEND_PORT`, `BUYER_BACKEND_PORT`, `OWNER_BACKEND_PORT`, `CVA_BACKEND_PORT`
  - `FRONTEND_PORT`, `NEXT_PUBLIC_*_API_URL`
  - `JWT_SECRET`, `JWT_EXPIRATION_MS`, `JWT_REFRESH_EXPIRATION_MS`
  - `SPRING_PROFILES_ACTIVE`, `FRONTEND_ORIGIN`
- Frontend đọc các biến `NEXT_PUBLIC_*` khi build.

## 📝 Ghi Chú
- Khởi động Admin backend trước để tạo schema chung; các service khác phụ thuộc DB và các entity Admin.
- Flyway migration nằm trong từng module `apps/*/src/main/resources/db/migration`.
- `start.bat` mở từng backend/FE ở cửa sổ PowerShell riêng và chờ health-check tự động; `stop-all.bat` dừng toàn bộ process + Docker.
- Ports mặc định: MySQL 3306, phpMyAdmin 8090, Frontend 3000, Backends 8080–8083 (cấu hình qua `.env`).
