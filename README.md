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

### Option A — Run only the app in Docker (use your local MySQL)

**1. Build the image:**

```bash
docker build -t interview-prep .
```

**2. Run the container:**

```bash
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://host.docker.internal:3306/interviewprep?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true" \
  -e SPRING_DATASOURCE_USERNAME=your_mysql_username \
  -e SPRING_DATASOURCE_PASSWORD=your_mysql_password \
  interview-prep
```

> `host.docker.internal` lets the container connect to MySQL running on your machine.
> On Linux, replace it with your machine's local IP (e.g. `192.168.1.x`) or use `--network=host`.

**3. Insert your user** (same as above, run directly in MySQL).

### Option B — Run app + MySQL together with Docker Compose

Create a `docker-compose.yml` in the project root:

```yaml
services:
  db:
    image: mysql:8
    environment:
      MYSQL_DATABASE: interviewprep
      MYSQL_ROOT_PASSWORD: rootpassword
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://db:3306/interviewprep?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: rootpassword
    depends_on:
      - db

volumes:
  mysql_data:
```

Then run:

```bash
docker compose up --build
```

> Flyway will run migrations automatically on startup. Manually insert your user into MySQL after the containers are up.

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

## Environment Variables (Docker overrides)

| Variable | Description |
|---|---|
| `SPRING_DATASOURCE_URL` | Full JDBC connection URL |
| `SPRING_DATASOURCE_USERNAME` | MySQL username |
| `SPRING_DATASOURCE_PASSWORD` | MySQL password |
| `APP_JWT_SECRET` | JWT signing secret (optional override) |

---

## Frontend

The frontend is a React + Vite + TailwindCSS application.

Repository: [https://github.com/Farhantigadi/NotesFrontend.git](https://github.com/Farhantigadi/NotesFrontend.git)

Follow the setup instructions in the frontend repository to connect it to this backend.
