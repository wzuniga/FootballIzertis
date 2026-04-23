# Football Confederation API

REST API built with **Java 21 + Spring Boot 3** that allows a Football Confederation to manage club registrations and their federated players.

> **Database:** This project uses **H2 in-memory** to simplify setup and testing — no external database required. Since it uses standard JPA/Hibernate, it is compatible with any relational database (PostgreSQL, MySQL, etc.) with minimal configuration changes. See the sections below for execution details.

---

## Table of Contents

- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [API Endpoints](#api-endpoints)
- [Security Model](#security-model)
- [Running Locally (H2 – zero config)](#running-locally-h2--zero-config)
- [Running with Docker (H2)](#running-with-docker-h2)
- [Running Tests](#running-tests)
- [Execute Options](#execute-options)
- [Environment Variables](#environment-variables)
- [Swagger / OpenAPI](#swagger--openapi)

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.2 |
| Security | Spring Security 6 + JWT (jjwt 0.12) |
| Persistence | Spring Data JPA + Hibernate |
| Database | H2 in-memory |
| Validation | Jakarta Bean Validation |
| Documentation | SpringDoc OpenAPI 3 / Swagger UI |
| Build | Maven 3.9+ |
| Containerisation | Docker |

---

## Project Structure

```
src/main/java/com/izertis/football/
├── FootballConfederationApplication.java   # Entry point
├── config/
│   ├── SecurityConfig.java                 # Spring Security + JWT filter chain
│   └── OpenApiConfig.java                  # Swagger / OpenAPI bean
├── controller/
│   ├── AuthController.java                 # POST /login
│   ├── ClubController.java                 # POST|GET|PUT /club
│   └── PlayerController.java              # POST|GET|PUT|DELETE /club/{id}/player
├── domain/
│   ├── Club.java                           # JPA entity
│   └── Player.java                         # JPA entity
├── dto/
│   ├── request/                            # Incoming JSON payloads
│   └── response/                           # Outgoing JSON payloads
├── exception/                              # Business exceptions + global handler
├── mapper/                                 # Entity ↔ DTO conversions
├── repository/                             # Spring Data JPA interfaces
├── security/                               # JWT provider, filter, entry point
└── service/                                # Business logic
```

---

## API Endpoints

### Authentication

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/club` | ❌ Public | Register a new club |
| POST | `/login` | ❌ Public | Obtain a JWT token |

### Clubs *(require Bearer token)*

| Method | Path | Description |
|--------|------|-------------|
| GET | `/club` | List all **public** clubs (no password/playerCount) |
| GET | `/club/{clubId}` | Club detail + player count (private clubs: owner only) |
| PUT | `/club/{clubId}` | Update club details (owner only) |

### Players *(require Bearer token)*

| Method | Path | Description |
|--------|------|-------------|
| POST | `/club/{clubId}/player` | Create a player (owner only) |
| GET | `/club/{clubId}/player` | List players – returns id, givenName, familyName only |
| GET | `/club/{clubId}/player/{playerId}` | Full player details |
| PUT | `/club/{clubId}/player/{playerId}` | Update player (owner only) |
| DELETE | `/club/{clubId}/player/{playerId}` | Delete player (owner only) |

---

## Security Model

- Passwords are **BCrypt-hashed** before storage. Plain-text passwords are never persisted.
- Authentication is **stateless** – no server-side session. A JWT is issued on login and must be supplied as `Authorization: Bearer <token>` on every protected request.
- **UUID identifiers** – All resource IDs (clubs and players) use UUID v4 instead of sequential integers. This prevents **IDOR (Insecure Direct Object Reference)** attacks: since UUIDs are randomly generated and non-sequential, no club can guess or enumerate the IDs of other clubs or their players. This follows the [OWASP A01:2021 – Broken Access Control](https://owasp.org/Top10/A01_2021-Broken_Access_Control/) recommendation.
- **Visibility rules**:
  - `public=true` clubs and their players are readable by any authenticated user.
  - `public=false` clubs and their players are only readable by the **owning club**.
  - Write operations (create/update/delete) are always restricted to the **owning club**.
- Clubs **cannot delete their own account**.

---

## Running Locally (H2 – zero config)

### Prerequisites

- Java 21
- Maven 3.9+

### Steps

```bash
# Clone the repository
git clone https://github.com/wzuniga/FootballIzertis.git
cd football-confederation

# Run (H2 in-memory, zero config)
mvn spring-boot:run
```

The API starts on **http://localhost:8080**.

> **H2 Console** is available at http://localhost:8080/h2-console  
> **Important:** the JDBC URL field defaults to `jdbc:h2:~/test` — change it manually to `jdbc:h2:mem:footballdb`  
> User: `sa` | Password: *(empty)*

---

## Running with Docker (H2)

> **No local Java or Maven required.** The Dockerfile uses a multi-stage build:
> the first stage compiles the project inside a Maven + JDK 21 container, and the
> second stage produces a minimal JRE runtime image. This works on **Linux, macOS,
> and Windows** as long as Docker Desktop (or Docker Engine) is installed.

### Prerequisites

- Docker Desktop (or Docker Engine) — no Java, no Maven needed

### Steps

```bash
# Build the image
docker build -t football-api .

# Run the container
docker run -p 8080:8080 football-api
```

The API starts on **http://localhost:8080**.
Swagger UI on: **http://localhost:8080/swagger-ui.html**

To stop the container:

```bash
docker stop $(docker ps -q --filter ancestor=football-api)
```

---

## Running Tests

```bash
# Run all tests (unit + integration, uses H2 test profile)
mvn test
```

Test coverage includes:
- **Unit tests** for `ClubService`, `PlayerService`, and `JwtTokenProvider`
- **Integration tests** for the full API flow (22 ordered scenarios) using `MockMvc` + H2

---

## Execute Options

### Option A – Plain JAR on a server

```bash
# Build
mvn clean package -DskipTests

# Run
java -jar target/football-confederation-1.0.0.jar \
  --app.jwt.secret=<your-secret-min-32-chars>
```

### Option B – Container registry

```bash
# Build image (compilation happens inside Docker — no local Maven needed)
docker build -t football-confederation:latest .

# Push to your registry and deploy with your orchestrator (K8s, ECS, etc.)
```

---

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `APP_JWT_SECRET` | HMAC-SHA256 signing secret (**min 32 chars**) | weak default – **change in prod** |
| `APP_JWT_EXPIRATION_MS` | Token validity in milliseconds | `10800000` (3 h) |

---

## Swagger / OpenAPI

Interactive API documentation is available while the application is running:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

**How to authenticate in Swagger UI:**

1. Call `POST /login` and copy the `token` value from the response.
2. Click the **Authorize 🔒** button at the top of the page.
3. Enter `Bearer <your-token>` and click **Authorize**.
4. All subsequent calls will include the token automatically.
