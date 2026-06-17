# Interview Prep — Backend

A personal interview notes management system. Organize your interview preparation into sections, subsections, and questions with rich content support including code snippets, diagrams, and images.

**Frontend repository:** [NotesFrontend](https://github.com/Farhantigadi/NotesFrontend.git)

---

## Tech Stack

- **Java 21** + **Spring Boot 3.4**
- **Spring Security** + **JWT** (stateless authentication)
- **Spring Data JPA** + **Hibernate**
- **MySQL** (schema managed by Flyway)
- **MapStruct** (DTO mapping)
- **Swagger / OpenAPI** (API docs at `/swagger-ui.html`)

---

## Project Structure

```
src/main/java/com/interviewprep/
├── auth/               # Authentication — User entity, JWT, login API
├── section/            # Main sections
├── subsection/         # Sub sections
├── question/           # Questions (answer, code, images)
├── export/             # PDF export
├── common/             # Shared utilities, response wrapper, exception handler
└── config/             # Security, CORS, JPA, OpenAPI
```

---

## Prerequisites

- Java 21
- Maven 3.9+
- MySQL 8+

> If you prefer Docker, skip to the [Docker Setup](#docker-setup) section below.

---

## Running Without Docker

### 1. Clone the repository

```bash
git clone <your-repo-url>
cd InterviewPrep
```

### 2. Create the database

Log into MySQL and run:

```sql
CREATE DATABASE interviewprep;
```

### 3. Configure the application

Open `src/main/resources/application.yml` and update your database credentials:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/interviewprep?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: your_mysql_username
    password: your_mysql_password
```

### 4. Create your login user

After the app runs for the first time, Flyway will create all tables automatically. Then insert your user:

```sql
INSERT INTO users (username, password, created_at, updated_at)
VALUES ('admin', 'your_password', NOW(), NOW());
```

Or create a file `src/main/resources/db/migration/V5__seed_default_user.sql` before first run:

```sql
INSERT INTO users (username, password, created_at, updated_at)
VALUES ('admin', 'your_password', NOW(), NOW());
```

### 5. Run the application

```bash
./mvnw spring-boot:run
```

The server starts at `http://localhost:8080`.

---

## Docker Setup

### Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/)

### 1. Update `application-docker.yml`

Open `src/main/resources/application-docker.yml` and update your database name if needed. Everything else is already configured to connect to your local MySQL.

### 2. Build the image

```bash
docker build -t interview-backend .
```

### 3. Run the container

```bash
docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=docker interview-backend
```

That's it. The app will be available at `http://localhost:8080`.

---

## Authentication

This app uses JWT-based authentication. All `/api/**` endpoints (except `/api/auth/login`) require a valid token.

### Login

```
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "your_password"
}
```

Response:

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "<jwt_token>",
    "username": "admin"
  }
}
```

Use the token in subsequent requests:

```
Authorization: Bearer <jwt_token>
```

Token expires after **24 hours**.

---

## API Documentation

Swagger UI is available at:

```
http://localhost:8080/swagger-ui.html
```

---

## Database Schema

Flyway manages all schema changes automatically. Migrations are in `src/main/resources/db/migration/`.

| Migration | Description |
|---|---|
| V1 | Create `main_sections` table |
| V2 | Create `sub_sections` table |
| V3 | Create `questions` table |
| V4 | Create `users` table |

---

## Frontend

The frontend is a React + Vite + TailwindCSS application.

Repository: [https://github.com/Farhantigadi/NotesFrontend.git](https://github.com/Farhantigadi/NotesFrontend.git)

Follow the setup instructions in the frontend repository to connect it to this backend.
