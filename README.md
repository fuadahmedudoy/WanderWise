
 # WanderWise (Group Project) -Travel Planning Application

🌍 A smart travel planner app that generates personalized itineraries.  
Tech stack: Spring Boot, Flask, React, PostgreSQL, OpenAI API.

### My Contribution
- Designed and implemented the backend authentication system (JWT-based).  
- Integrated weather data (WeatherAPI) with travel planning logic.  
- Did all the backend and frontend tests with ci/cd integration


A comprehensive travel planning application built with React frontend and Java Spring Boot backend.

## 🚀 CI/CD Pipeline

This project uses GitHub Actions for continuous integration and deployment:

### Test Pipeline (`.github/workflows/test.yml`)
- **Triggers:** On every push to `main` and pull requests
- **Frontend Tests:** Runs unit tests with Vitest and generates coverage reports
- **Future:** Backend tests with Maven (commented out for now)

### Deployment Pipeline (`.github/workflows/deploy.yml`)
- **Triggers:** After tests pass successfully
- **Target:** Azure VM deployment using Docker Compose
- **Process:** 
  1. Validates project structure
  2. Deploys to Azure VM via SSH
  3. Updates containers with latest code
  4. Provides deployment summary

## 🧪 Testing

### Frontend Tests
```bash
# Run tests in development
cd Frontend
npm test

# Run tests for CI with coverage
npm run test:ci
```

### Test Coverage
- Tests are located in `src/**/__tests__/` and `src/**/*.test.jsx`
- Coverage reports are generated automatically in CI
- Includes tests for:
  - Authentication (Login/Signup)
  - Components (NotificationCenter, TripImage, etc.)
  - Pages (Home, TravelPlanner)
  - Services (notificationService)
  - Context (AuthContext)

## 📁 Project Structure

```
WanderWise/
├── Backend/                 # Java Spring Boot backend
├── Frontend/               # React frontend application
├── database/              # Database initialization scripts
├── travel-service/        # Python travel service
├── docker-compose.yml     # Docker composition
└── .github/workflows/     # CI/CD workflows
```

## 🛠 Development

### Prerequisites
- Node.js 18+
- Java 17+
- Docker & Docker Compose
- Maven

### Local Development
```bash
# Frontend
cd Frontend
npm install
npm start

# Backend
cd Backend
mvn spring-boot:run

# Full stack with Docker
docker-compose up
```

## 🚢 Deployment

Deployment is automated through GitHub Actions:
1. Push code to `main` branch
2. Tests run automatically
3. If tests pass, deployment to Azure VM triggers
4. Application is updated using Docker Compose

## 📊 Monitoring

- Test results are displayed in GitHub Actions
- Coverage reports are uploaded as artifacts
- Deployment status is tracked in action summaries

---


*Ensure all tests pass before merging to main branch to maintain deployment pipeline integrity.*

