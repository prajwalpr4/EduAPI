# EduAPI — Student Management System

A production-ready full-stack Student Management System featuring role-based access control, a modern frontend, and scalable containerized deployment.

## Tech Stack
- **Backend**: Java 17, Spring Boot 3.2, Spring Data JPA, Hibernate, MySQL, Spring Security (JWT)
- **Frontend**: Next.js 14 (App Router), TypeScript, Tailwind CSS, Axios
- **Deployment**: Docker, Docker Compose

## Features
- **RBAC**: Admin and Student roles.
- **Profiles**: Personal information and customized Student ID Cards.
- **Courses**: Browse course catalogs and manage enrollments.
- **Enrollments**: Capacity constraints and real-time updates.

## Getting Started (Local Development)

### Backend
1. Ensure Java 17 and Maven are installed.
2. Ensure MySQL is running on `localhost:3306` with root (no password) or update `application.properties`.
3. Run backend:
   ```bash
   mvn spring-boot:run
   ```
4. Access Swagger UI at `http://localhost:8080/swagger-ui.html`.

### Frontend
1. Ensure Node.js and npm are installed.
2. Navigate to `/frontend` and install dependencies:
   ```bash
   cd frontend
   npm install
   ```
3. Run the development server:
   ```bash
   npm run dev
   ```
4. Open `http://localhost:3000`.

## Getting Started (Docker)

1. Make sure Docker and Docker Compose are installed.
2. Run the full stack:
   ```bash
   docker-compose up --build
   ```
3. The app is available at `http://localhost:3000` and the API at `http://localhost:8080/api`.

## Default Users (if populated)
- **Admin**: Create an admin manually or via API using the `/api/auth/register` endpoint with `"role": "ADMIN"`.
- **Student**: Register via the frontend.
