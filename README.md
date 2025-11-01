# 🌱 Carbon Credit Marketplace - Monorepo

**Hệ thống marketplace giao dịch carbon credit từ xe điện**

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Next.js](https://img.shields.io/badge/Next.js-15-black.svg)](https://nextjs.org/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

---

## 📋 Tổng Quan

Carbon Credit Marketplace là một nền tảng giao dịch carbon credit được tạo ra từ việc sử dụng xe điện. Hệ thống kết nối:

- **EV Owners:** Chủ xe điện - tạo carbon credits
- **Buyers:** Doanh nghiệp/cá nhân - mua carbon credits
- **CVA (Carbon Verification Auditors):** Kiểm chứng credits
- **Admins:** Quản trị nền tảng

---

## 🏗️ Kiến Trúc Monorepo

```
carbon-credit-marketplace/
├── apps/                           # Ứng dụng chạy độc lập
│   ├── admin-backend-spring/       # ✅ Backend Admin (Spring Boot)
│   ├── cva-backend-spring/         # 🔲 Backend CVA (Placeholder)
│   ├── owner-backend-spring/       # 🔲 Backend EV Owner (Placeholder)
│   ├── buyer-backend-spring/       # 🔲 Backend Buyer (Placeholder)
│   └── web-portal-next/            # 🔲 Frontend Portal (Next.js)
├── packages/                       # Thư viện dùng chung
│   ├── java-common/                # ✅ Java shared library
│   ├── ts-sdk/                     # 🔲 TypeScript SDK
│   └── ui/                         # 🔲 Shared UI components
├── infra/                          # Infrastructure as Code
│   ├── docker/                     # Docker Compose
│   ├── kubernetes/                 # K8s manifests
│   └── migrations/                 # Database migrations
├── configs/                        # Shared configurations
│   ├── eslint/                     # ESLint configs
│   ├── typescript/                 # TypeScript configs
│   └── checkstyle/                 # Java Checkstyle
├── scripts/                        # Automation scripts
│   ├── build/                      # Build scripts
│   ├── dev/                        # Development utilities
│   └── migration/                  # Migration scripts
├── docs/                           # Documentation
│   ├── architecture/               # Architecture docs
│   ├── development/                # Dev guides
│   └── api/                        # API documentation
└── .github/                        # CI/CD workflows
```

**Legend:**

- ✅ Hoàn thiện / Đã migrate
- 🔲 Placeholder / Chưa hoàn thiện

---

## 🚀 Bắt Đầu Nhanh

### Prerequisites

- **Java:** 21+
- **Maven:** 3.8+
- **Node.js:** 20+
- **Docker:** 24+
- **pnpm:** 9+

### Backend (Admin Module)

```bash
# Build tất cả modules
mvn clean install

# Chỉ build admin-backend
mvn -pl apps/admin-backend-spring clean install

# Run tests
mvn test

# Start dev environment
cd infra/docker
docker-compose up -d

# Access admin backend
curl http://localhost:8080/actuator/health
```

### Frontend (Coming Soon)

```bash
# Install dependencies
pnpm install

# Run dev server
pnpm dev

# Access web portal
open http://localhost:3000
```

---

## 📦 Modules

### Admin Backend (`apps/admin-backend-spring/`)

**Status:** ✅ Hoàn thiện (Day 12)

**Features:**

- ✅ User Management (USR-\*)
- ✅ Transaction Monitoring (TXN-\*)
- ✅ Reporting (REP-\*)
- ✅ Dispute Management (DIS-\*)
- ✅ Analytics (ANA-\*)
- ✅ Settings (SET-\*)
- ✅ Audit & Observability (AUD-\*)
- ✅ Security & JWT (SEC-\*)

**Tech Stack:**

- Spring Boot 3.5.6
- Java 21
- MySQL 8.0
- Flyway (migrations)
- JWT (authentication)
- Caffeine (caching)
- Bucket4j (rate limiting)
- SpringDoc OpenAPI

**API Docs:** http://localhost:8080/swagger-ui.html

### Java Common (`packages/java-common/`)

**Status:** ✅ Hoàn thiện

**Exports:**

- `ApiError` - Standardized error responses
- `GlobalExceptionHandler` - Exception handling
- `WebMvcConfig` - Web MVC configuration

**Usage:**

```xml
<dependency>
    <groupId>com.ccm</groupId>
    <artifactId>java-common</artifactId>
</dependency>
```

### Other Modules

**CVA Backend:** 🔲 Placeholder  
**Owner Backend:** 🔲 Placeholder  
**Buyer Backend:** 🔲 Placeholder  
**Web Portal:** 🔲 Placeholder  
**TypeScript SDK:** 🔲 Placeholder  
**UI Library:** 🔲 Placeholder

---

## 🔧 Development

### Build All

```bash
# Build entire monorepo
mvn clean install

# Build specific module
mvn -pl packages/java-common clean install
mvn -pl apps/admin-backend-spring clean install
```

### Run Tests

```bash
# All tests
mvn test

# Specific module tests
mvn -pl apps/admin-backend-spring test
```

### Local Development

```bash
# Start all services (MySQL, PHPMyAdmin, Admin Backend)
cd infra/docker
docker-compose up -d

# View logs
docker-compose logs -f admin-backend

# Stop services
docker-compose down
```

### Database Migrations

Flyway migrations tự động chạy khi start application.

**Manually run migrations:**

```bash
cd apps/admin-backend-spring
mvn flyway:migrate
```

**Migration files:** `apps/admin-backend-spring/src/main/resources/db/migration/`

---

## 📖 Documentation

### Main Docs

| Document                                     | Mô tả                                 |
| -------------------------------------------- | ------------------------------------- |
| [MIGRATION_LOG.md](MIGRATION_LOG.md)         | Chi tiết migration backend → monorepo |
| [MIGRATION_SUMMARY.md](MIGRATION_SUMMARY.md) | Tóm tắt migration                     |
| [DRY_RUN_REPORT.md](DRY_RUN_REPORT.md)       | Dry-run analysis                      |
| [QUICK_REFERENCE.md](QUICK_REFERENCE.md)     | Cheat sheet commands                  |
| [DELIVERABLES.md](DELIVERABLES.md)           | Danh sách deliverables                |

### Module Docs

- **Admin Backend:** `apps/admin-backend-spring/README.md`
- **Java Common:** `packages/java-common/README.md`

### Architecture

- **ADRs:** `docs/architecture/ADR/`
- **Diagrams:** `docs/architecture/diagrams/`

---

## 🔄 Migration Status

### Prompt 1: ✅ Monorepo Structure Definition

**Completed:** 2025-10-31

Đã định nghĩa:

- Cấu trúc thư mục monorepo
- Package naming conventions
- Build tooling (Maven multi-module + pnpm workspaces)
- CI/CD strategy

### Prompt 2: ✅ Backend Migration

**Completed:** 2025-10-31

Đã thực hiện:

- Di chuyển `backend/` → `apps/admin-backend-spring/`
- Trích xuất shared code → `packages/java-common/`
- Chuẩn hóa package: `ccm.admin.admin_backend.*` → `ccm.admin.*`
- Tạo Root POM (Maven multi-module)
- Update Docker Compose

**Scripts:** `scripts/migration/`

### Prompt 3: 🔲 Frontend Migration (Next)

**Planned:**

- Di chuyển `frontend/` → `apps/web-portal-next/`
- Tạo `packages/ts-sdk/` (TypeScript SDK)
- Tạo `packages/ui/` (Shared UI components)
- Setup pnpm workspaces
- CI/CD workflows

---

## 🚢 Deployment

### Docker (Development)

```bash
cd infra/docker
docker-compose up -d
```

**Services:**

- MySQL: `localhost:3306`
- PHPMyAdmin: `localhost:8081`
- Admin Backend: `localhost:8080`

### Production (Coming Soon)

- Kubernetes manifests: `infra/kubernetes/`
- CI/CD: `.github/workflows/`

---

## 🧪 Testing

### Backend Tests

```bash
# Unit tests
mvn test

# Integration tests
mvn verify

# Specific test class
mvn -Dtest=UserServiceImplTest test
```

**Test coverage target:** >70%

### Frontend Tests (Coming Soon)

```bash
pnpm test
pnpm test:e2e
```

---

## 📊 Project Status

| Component          | Status         | Progress | Last Updated |
| ------------------ | -------------- | -------- | ------------ |
| **Admin Backend**  | ✅ Complete    | 100%     | 2025-10-31   |
| **Java Common**    | ✅ Complete    | 100%     | 2025-10-31   |
| **CVA Backend**    | 🔲 Placeholder | 0%       | -            |
| **Owner Backend**  | 🔲 Placeholder | 0%       | -            |
| **Buyer Backend**  | 🔲 Placeholder | 0%       | -            |
| **Web Portal**     | 🔲 Placeholder | 0%       | -            |
| **TypeScript SDK** | 🔲 Planned     | 0%       | -            |
| **UI Library**     | 🔲 Planned     | 0%       | -            |
| **CI/CD**          | 🔲 Planned     | 0%       | -            |

---

## 🛠️ Tech Stack

### Backend

- **Framework:** Spring Boot 3.5.6
- **Language:** Java 21
- **Build Tool:** Maven 3.8+
- **Database:** MySQL 8.0
- **ORM:** Spring Data JPA + Hibernate
- **Migration:** Flyway
- **Security:** Spring Security + JWT (JJWT)
- **Caching:** Caffeine
- **Rate Limiting:** Bucket4j
- **API Docs:** SpringDoc OpenAPI
- **Testing:** JUnit 5, Mockito

### Frontend (Planned)

- **Framework:** Next.js 15
- **Language:** TypeScript 5
- **UI Library:** React 19
- **Styling:** Tailwind CSS, shadcn/ui
- **State:** React Context / Zustand
- **HTTP Client:** Axios / Fetch
- **Build Tool:** Turbopack

### Infrastructure

- **Containers:** Docker, Docker Compose
- **Orchestration:** Kubernetes (planned)
- **CI/CD:** GitHub Actions
- **Monitoring:** Actuator + Prometheus (planned)

---

## 👥 Roles & Permissions

| Role         | Backend Module         | Status         |
| ------------ | ---------------------- | -------------- |
| **Admin**    | `admin-backend-spring` | ✅ Complete    |
| **CVA**      | `cva-backend-spring`   | 🔲 Placeholder |
| **EV Owner** | `owner-backend-spring` | 🔲 Placeholder |
| **Buyer**    | `buyer-backend-spring` | 🔲 Placeholder |

**Frontend:** Tất cả roles tích hợp trong `web-portal-next`

---

## 🔐 Security

- **Authentication:** JWT tokens
- **Authorization:** Role-based access control (RBAC)
- **Password:** BCrypt hashing
- **Rate Limiting:** Bucket4j (per-user, per-IP)
- **CORS:** Configurable origins
- **SQL Injection:** Parameterized queries (JPA)
- **XSS:** Input validation + sanitization

---

## 📝 Contributing

### Branch Strategy

- `main` - Production-ready code
- `develop` - Integration branch
- `feature/*` - Feature branches
- `hotfix/*` - Hotfix branches

### Commit Convention

**Format:** `<type>(<scope>): <subject>`

**Types:**

- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation
- `refactor`: Refactoring
- `test`: Tests
- `chore`: Build/CI

**Example:**

```
feat(admin-backend): add user bulk delete API
fix(web-portal): fix transaction table pagination
docs(architecture): add ADR for API versioning
```

### Code Quality

- **Linting:** ESLint (TS/JS), Checkstyle (Java)
- **Formatting:** Prettier (TS/JS), Spotless (Java)
- **Testing:** >70% coverage target
- **Pre-commit:** Husky hooks (lint + test)

---

## 📅 Roadmap

### Q4 2025

- ✅ Monorepo structure definition (Prompt 1)
- ✅ Backend migration (Prompt 2)
- 🔲 Frontend migration (Prompt 3)
- 🔲 TypeScript SDK development
- 🔲 UI library development

### Q1 2026

- 🔲 CVA backend development
- 🔲 EV Owner backend development
- 🔲 Buyer backend development
- 🔲 API integration & testing
- 🔲 CI/CD setup

### Q2 2026

- 🔲 Production deployment
- 🔲 Monitoring & observability
- 🔲 Performance optimization
- 🔲 Security audit

---

## 📞 Support

- **Documentation:** `docs/`
- **Migration Help:** `MIGRATION_LOG.md`, `QUICK_REFERENCE.md`
- **Issues:** GitHub Issues (nếu có)
- **API Docs:** http://localhost:8080/swagger-ui.html (dev)

---

## 📄 License

MIT License - see [LICENSE](LICENSE) for details

---

## 🙏 Acknowledgments

- **Architecture:** Monorepo best practices
- **Backend:** Spring Boot ecosystem
- **Frontend:** Next.js + React
- **Database:** Flyway migrations
- **Build:** Maven multi-module + pnpm workspaces

---

**Built with ❤️ by the Carbon Credit Marketplace Team**

**Last Updated:** 2025-10-31  
**Version:** 1.0.0-SNAPSHOT
