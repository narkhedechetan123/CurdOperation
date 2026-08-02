# JWT CRUD Demo (Spring Boot + MySQL)

Register / Login with JWT, then use the token to access a protected `Product` CRUD API.

## Stack
- Spring Boot 3.3.2 (Java 17)
- Spring Security (stateless, JWT filter)
- Spring Data JPA + MySQL
- jjwt 0.11.5
- Lombok

## Project layout
```
src/main/java/com/example/jwtcrud
 ├── config/SecurityConfig.java        # security rules, permits /api/auth/**
 ├── security/JwtUtil.java             # generate/validate tokens
 ├── security/JwtAuthenticationFilter.java  # reads Bearer token on every request
 ├── security/CustomUserDetailsService.java
 ├── model/User.java, Product.java
 ├── repository/UserRepository.java, ProductRepository.java
 ├── dto/                              # request/response payloads
 ├── controller/AuthController.java    # /api/auth/register, /api/auth/login
 ├── controller/ProductController.java # /api/products (JWT required)
 ├── service/ProductService.java
 └── exception/GlobalExceptionHandler.java
```

## Environment variables (all optional — falls back to defaults if unset)

| Variable        | Default                                                   | Purpose                     |
|-----------------|------------------------------------------------------------|------------------------------|
| `DB_URL`        | `jdbc:mysql://localhost:3306/jwtcruddb?createDatabaseIfNotExist=true` | MySQL connection URL |
| `DB_USERNAME`   | `root`                                                     | MySQL username               |
| `DB_PASSWORD`   | `root`                                                     | MySQL password                |
| `JWT_SECRET`    | dev default (32+ chars)                                    | HMAC signing key for JWT     |
| `JWT_EXPIRATION`| `86400000` (24h, in ms)                                    | Token lifetime               |
| `SERVER_PORT`   | `8080`                                                     | App port                      |
| `DDL_AUTO`      | `update`                                                   | Hibernate schema strategy    |
| `SHOW_SQL`      | `true`                                                     | Log SQL statements           |

This works via Spring's built-in placeholder syntax in `application.properties`:
```properties
spring.datasource.password=${DB_PASSWORD:root}
```
If `DB_PASSWORD` is set in the environment, Spring uses that value. If it's not set, it uses `root`. No code changes needed — set what you want to override, leave the rest alone.

## Run

**1. Without setting any env vars** (uses all defaults — assumes local MySQL root/root):
```bash
mvn spring-boot:run
```

**2. With env vars (recommended for anything beyond local testing):**

Linux/macOS:
```bash
export DB_URL="jdbc:mysql://localhost:3306/jwtcruddb?createDatabaseIfNotExist=true"
export DB_USERNAME=root
export DB_PASSWORD=your_mysql_password
export JWT_SECRET=$(openssl rand -base64 32)
export JWT_EXPIRATION=86400000

mvn spring-boot:run
```

Windows PowerShell:
```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/jwtcruddb?createDatabaseIfNotExist=true"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_mysql_password"
$env:JWT_SECRET="a-long-random-secret-at-least-32-characters"
$env:JWT_EXPIRATION="86400000"

mvn spring-boot:run
```

Or run the packaged jar:
```bash
mvn clean package -DskipTests
java -jar target/jwt-crud-demo-0.0.1-SNAPSHOT.jar
```

You only need to set the variables you want to override — anything left unset just uses its default.

## API usage

### 1. Register
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"ram","email":"ram@example.com","password":"secret123"}'
```

### 2. Login → get JWT
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"ram","password":"secret123"}'
```
Response:
```json
{ "token": "eyJhbGciOi...", "type": "Bearer", "username": "ram" }
```

### 3. Access protected CRUD API with the token
```bash
TOKEN="eyJhbGciOi..."

# Create
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Keyboard","description":"Mechanical","price":49.99,"quantity":10}'

# Read all
curl http://localhost:8080/api/products -H "Authorization: Bearer $TOKEN"

# Read one
curl http://localhost:8080/api/products/1 -H "Authorization: Bearer $TOKEN"

# Update
curl -X PUT http://localhost:8080/api/products/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Keyboard","description":"Mechanical RGB","price":59.99,"quantity":8}'

# Delete
curl -X DELETE http://localhost:8080/api/products/1 -H "Authorization: Bearer $TOKEN"
```

Without a valid token, any `/api/products/**` call returns `401/403`.

## Notes / next steps
- Passwords are hashed with BCrypt before being stored.
- `jwt.secret` must be at least 32 characters (HS256 requirement) — the default is fine for local dev, but always override `JWT_SECRET` in any real deployment.
- `spring.jpa.hibernate.ddl-auto=update` auto-creates the `users` and `products` tables on first run — fine for dev, use migrations (Flyway/Liquibase) for production.
- To add role-based access (e.g. ADMIN-only delete), extend `SecurityConfig`'s `authorizeHttpRequests` with `.requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")`.
