# saasxl-qa-service

A Spring Boot 3.2 (Java 17) client service using Spring Reactive `WebClient` to authenticate against the `saasxl-backend`, automatically inject the JWT Authorization token (`Bearer <token>`) into every request header, parse API definitions from a Google Sheet / CSV, and make REST calls with sample DTOs.

---

## Key Features

1. **Automated Authentication & Session Management**:
   - Performs login via `POST /idp/api/v1/user/v2/login` with `LoginRequestDTO` (email and password).
   - Extracts the JWT `accessToken` and `refreshToken` from `AuthResponseDTO`.
   - Caches the access token and automatically attaches `Authorization: Bearer <token>` to all outgoing REST requests.
   - Automatically intercepts HTTP `401 Unauthorized` responses and rotates tokens via `POST /idp/api/v1/user/v2/refresh` with zero downtime.

2. **Spreadsheet & CSV Endpoint Ingestion**:
   - Ingests endpoint specifications directly from a Google Sheet export URL or a local CSV (such as `Backend APIs Not Hit - MAIN-6624 - Untitled.csv` or `combined_api_hits_by_method_and_url.csv`).
   - Extracts `Method`, `Route`, `Package`, `Controller.method`, and `Action`.

3. **Dynamic & Type-Safe REST Clients**:
   - `UserApiClient`: calls `GET /idp/api/v1/user`
   - `WorkSpaceApiClient`: calls `GET /noCo/api/v2/workspaces`, `GET /noCo/api/v2/workspaces/{id}/tables`, and `POST /noCo/api/v2/workspaces/{id}/tables/{id}/cursor`
   - `DynamicApiClient`: dynamically replaces path parameters (e.g. `{workspaceId}`, `{tableId}`) and calls any HTTP method (`GET`, `POST`, `PUT`, `DELETE`).

---

## Configuration (`application.yml`)

```yaml
backend:
  base-url: http://localhost:8989
  auth:
    email: admin@example.com
    password: Password123!
    login-path: /idp/api/v1/user/v2/login
    refresh-path: /idp/api/v1/user/v2/refresh

sheet:
  # Export URL for Google Sheets:
  google-sheet-url: https://docs.google.com/spreadsheets/d/12qElRJY2SSPpivc2ulK4zz9WCr1eWQGOBgukIq2rWx0/export?format=csv&gid=1684616346
  # Path to local downloaded CSV:
  local-csv-path: /Users/mickey/Downloads/Backend APIs Not Hit - MAIN-6624 - Untitled.csv

runner:
  enabled: true
  max-endpoints-to-test: 20
```

---

## Building and Running

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v17)
mvn clean install
mvn spring-boot:run
```
