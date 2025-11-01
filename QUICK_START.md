# ⚡ Quick Reference - How to Run

## 🚀 Super Quick Start (Easiest)

```powershell
# 1. Start everything with Docker
.\start-all.ps1

# 2. In a new terminal, start frontend
cd apps\web-portal-next
pnpm install
pnpm dev
```

**Done!** Access:

- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html
- phpMyAdmin: http://localhost:8081

---

## 🛠️ Manual Development Setup

### Step-by-Step

```powershell
# 1. Start database only
.\start-dev.ps1

# 2. In new terminal - Run backend
cd apps\admin-backend-spring
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 3. In new terminal - Run frontend
cd apps\web-portal-next
pnpm dev
```

---

## 📦 Common Commands

### Docker

```powershell
# Start all services (database + backend + phpMyAdmin)
.\start-all.ps1

# Start only database + phpMyAdmin
.\start-dev.ps1

# Stop everything
.\stop-all.ps1

# View logs
cd infra\docker
docker-compose logs -f admin-backend
```

### Backend

```powershell
# Build
mvn clean install

# Run dev mode
cd apps\admin-backend-spring
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run tests
mvn test

# Package JAR
mvn clean package
```

### Frontend

```powershell
# Install dependencies
pnpm install

# Run dev server
cd apps\web-portal-next
pnpm dev

# Type check
pnpm type-check

# Build for production
pnpm build
```

---

## 🔍 Check if Services are Running

```powershell
# Backend health
curl http://localhost:8080/actuator/health

# View all Docker services
cd infra\docker
docker-compose ps

# Check port usage
netstat -ano | findstr :8080
netstat -ano | findstr :3000
```

---

## 🐛 Troubleshooting

### Port already in use

```powershell
# Find and kill process on port 8080
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### Database connection failed

```powershell
cd infra\docker
docker-compose restart mysql
docker-compose logs mysql
```

### Frontend module not found

```powershell
# Clean and reinstall
rm -rf node_modules
pnpm install
```

---

## 📖 Full Documentation

See `HOW_TO_RUN.md` for complete guide!
