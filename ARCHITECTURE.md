# System Architecture & Authentication Flow

This document provides a visual and technical overview of the Library Application's architecture, including container interactions and the secure JWT authentication flow.

## 1. Container Architecture (Docker Compose)

The application is composed of several Docker containers that interact within a private network. The **API Gateway** acts as the single entry point for the Frontend.

```mermaid
graph TD
    User((User))
    
    subgraph DockerNetwork [Docker Network]
        Frontend["Frontend Application\n(Angular / Nginx)"]
        Gateway["API Gateway\n(Spring Cloud Gateway)"]
        
        AuthService["Auth Service\n(Spring Boot)"]
        LibraryService["Library Service\n(Spring Boot)"]
        
        AuthDB[("Auth DB\n(PostgreSQL)")]
        LibraryDB[("Library DB\n(PostgreSQL)")]
        MailHog["MailHog\n(SMTP Testing)"]
    end

    User -- "Browser (HTTP/HTTPS)" --> Frontend
    Frontend -- "API Requests (/api/...)" --> Gateway
    
    Gateway -- "/api/v1/auth/*" --> AuthService
    Gateway -- "/api/v1/library/*" --> LibraryService
    
    AuthService -- "Read/Write" --> AuthDB
    LibraryService -- "Read/Write" --> LibraryDB
    AuthService -- "Send Emails" --> MailHog
```

### Key Components:
*   **Frontend**: Serves the Angular application.
*   **API Gateway**: Routes requests to appropriate microservices. Handles CORS and centralizes routing.
*   **Auth Service**: Manages user registration, login, and token issuance (JWT).
*   **Library Service**: Manages books and loans. Protected by JWT validation.
*   **Databases**: Dedicated PostgreSQL instances for isolation.

---

## 2. JWT Authentication Flow

The application uses a **stateless** authentication mechanism with **HttpOnly Cookies** for maximum security.

### Login & Token Issuance

```mermaid
sequenceDiagram
    participant U as User
    participant F as Frontend
    participant G as API Gateway
    participant A as Auth Service
    participant D as Database

    U->>F: Enters Credentials
    F->>G: POST /auth/authenticate
    G->>A: Forward Request
    A->>D: Validate Credentials
    D-->>A: User Valid
    A->>A: Generate Access Token (15min)
    A->>A: Generate Refresh Token (7days)
    A-->>G: Response + Set-Cookie (Refresh Token, HttpOnly)
    G-->>F: JSON (Access Token) + Cookie
    F->>F: Store Access Token in Memory
```

### Accessing Protected Resources (e.g., Borrowing a Book)

```mermaid
sequenceDiagram
    participant U as User
    participant F as Frontend
    participant G as API Gateway
    participant L as Library Service

    U->>F: Click "Borrow Book"
    F->>G: POST /library/loans<br>Authorization: Bearer [Access Token]
    G->>L: Forward Request
    L->>L: Validate JWT Signature
    L->>L: Extract User Role & Email
    alt Token Valid
        L-->>G: 200 OK (Loan Created)
        G-->>F: 200 OK
        F-->>U: Show Success Dialog
    else Token Expired
        L-->>G: 401 Unauthorized
        G-->>F: 401 Unauthorized
        F->>F: Trigger Refresh Flow
    end
```

---

## 3. API Endpoints Overview

### Auth Service (`/api/v1/auth`)

| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `POST` | `/register` | Register a new user | No |
| `POST` | `/authenticate` | Login and receive tokens | No |
| `POST` | `/refresh-token` | Get new access token using cookie | No (Cookie req) |
| `POST` | `/forgot-password` | Request password reset email | No |

### Library Service (`/api/v1/books`, `/api/v1/loans`)

| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `GET` | `/books` | List paginated books | Yes |
| `GET` | `/books/{id}` | Get book details | Yes |
| `POST` | `/loans` | Borrow a book | Yes |
| `GET` | `/loans` | Get current user's loans | Yes |
| `POST` | `/loans/{id}/return` | Return a book (Admin/User) | Yes |

---

## 4. Key Security Features implemented

1.  **HttpOnly Cookies**: The Refresh Token is stored in a cookie that JavaScript cannot access, preventing XSS attacks from stealing the long-lived token.
2.  **Short-lived Access Tokens**: The Access Token (used for API calls) expires quickly (e.g., 15 mins), minimizing the window of opportunity if stolen.
3.  **Role-Based Access Control (RBAC)**: The JWT contains the user's role (`ADMIN` or `USER`), allowing the backend to enforce permissions securely.
