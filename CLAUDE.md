# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**ft-backend** is a Java 21 + Spring Boot 3.4.0 backend system that implements a credit card tokenization, customer management, and e-commerce order processing system following Clean Architecture/Hexagonal Architecture principles.

**Key Technical Stack:**
- Java 21
- Spring Boot 3.4.0
- PostgreSQL 16 (Docker)
- Maven
- Lombok (extensive use: @Builder, @Data, @Value)
- MapStruct (OBLIGATORY for all layer mappings)
- SpringDoc OpenAPI (Swagger)
- JaCoCo (80%+ test coverage requirement)
- Testcontainers

## Build & Run Commands

### Build
```bash
./mvnw clean compile
./mvnw clean package
```

### Run Application
```bash
./mvnw spring-boot:run
```

### Database (Docker Compose)
```bash
# Start PostgreSQL
docker compose up -d

# Stop PostgreSQL
docker compose down

# View logs
docker logs farmatodo-postgres
```

**Database Connection:**
- Host: localhost:5433 (mapped to 5432 in container)
- Database: farmatodo_db
- User: postgres
- Password: password

### Testing
```bash
# Run all tests
./mvnw test

# Run with coverage report
./mvnw clean verify

# View coverage report
open target/site/jacoco/index.html

# Run specific test
./mvnw test -Dtest=ClassName#methodName
```

### API Documentation
Once running, access:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- Health Check: http://localhost:8080/ping
- Actuator: http://localhost:8080/actuator/health

## Architecture

This project follows **Clean Architecture / Hexagonal Architecture** with strict layer separation:

### Layer Structure

```
com.farmatodo.reto_tecnico/
├── domain/                    # Pure business logic (NO Spring dependencies)
│   ├── model/                # Domain entities and value objects
│   │   ├── valueobjects/    # Email, Phone, Money, CardNumber (Records)
│   │   └── *.java           # Customer, Product, Order, CreditCard
│   ├── port/
│   │   ├── in/              # Use case interfaces (what the system does)
│   │   └── out/             # Repository/Gateway interfaces (how it persists/integrates)
│   └── exception/           # Domain exceptions
│
├── application/              # Use case orchestration (Spring @Service allowed)
│   ├── service/             # Service implementations
│   └── config/              # @ConfigurationProperties
│
└── infrastructure/           # Framework & external integrations
    ├── adapter/
    │   ├── in/rest/         # Controllers, DTOs, Mappers
    │   └── out/             # JPA Entities, Repositories, Gateways
    ├── security/            # API Key filter
    └── config/              # Infrastructure configuration
```

### Critical Architecture Rules

1. **Domain Layer is Framework-Free**
   - NO `@Entity`, `@Table`, `@Service`, `@Autowired` in `domain/` package
   - Only Java + Lombok + Jakarta Validation
   - Business logic lives here

2. **MapStruct is OBLIGATORY**
   - ALL mappings between layers MUST use MapStruct
   - DTO ↔ Domain ↔ Entity conversions
   - Never expose `@Entity` in controllers

3. **Value Objects Over Primitives**
   - Use `Email` instead of `String`
   - Use `Phone` instead of `String`
   - Use `Money` instead of `BigDecimal`
   - Use `CardNumber` instead of `String`

4. **Ports and Adapters Pattern**
   - Input Ports (domain/port/in): Define use cases
   - Output Ports (domain/port/out): Define persistence/integration contracts
   - Adapters (infrastructure/adapter): Implement ports

5. **Constructor Injection Only**
   - Use `@RequiredArgsConstructor` from Lombok
   - Never use field injection

## Key Business Logic

### Payment Processing Flow
1. Tokenize credit card (configurable failure rate)
2. Assign token to order
3. Process payment with retry logic (configurable max retries)
4. Update order status (PAYMENT_CONFIRMED or PAYMENT_FAILED)

**Retry Configuration** (application.properties):
- `farmatodo.payment.max-retries=3`
- `farmatodo.payment.retry-delay-millis=1000`
- `farmatodo.payment.rejection-probability=20` (0-100)

### Order Creation Flow
1. Validate customer exists (create if not)
2. Validate stock availability for all items
3. Create order with domain logic
4. Reduce stock atomically
5. Persist order with @Transactional

### Product Search with Async Logging
- `ProductServiceImpl.searchByName()` performs search
- `ProductServiceImpl.logSearchAsync()` logs asynchronously with `@Async`
- Non-blocking, runs in separate thread pool

### Stock Management
- `Product.reduceStock(quantity)` - Domain validation
- `Product.hasSufficientStock(quantity)` - Domain logic
- Stock threshold: `farmatodo.product.min-stock-threshold=1`

## Configuration Properties

Centralized in `FarmatodoProperties.java` using `@ConfigurationProperties`:

```properties
# Tokenization
farmatodo.tokenization.rejection-probability=10

# Payment
farmatodo.payment.rejection-probability=20
farmatodo.payment.max-retries=3
farmatodo.payment.retry-delay-millis=1000

# Product
farmatodo.product.min-stock-threshold=1

# Encryption
farmatodo.encryption.algorithm=AES
farmatodo.encryption.key=${ENCRYPTION_KEY:default-encryption-key-change-in-production}
```

## Domain Model Key Entities

**Order (Aggregate Root)**
- Manages OrderItems collection
- State machine: PENDING → PAYMENT_PROCESSING → PAYMENT_CONFIRMED/FAILED → COMPLETED/CANCELLED
- Methods: `confirmPayment()`, `failPayment()`, `cancel()`

**Product**
- Stock management with domain validation
- Methods: `reduceStock()`, `increaseStock()`, `hasSufficientStock()`

**CreditCard**
- Tokenization support: `assignToken()`, `isTokenized()`
- Security: `clearSensitiveData()` after tokenization
- Validation: `isExpired()`, Luhn check via `CardNumber`

**Value Objects (Records)**
- Immutable by design
- Self-validating in constructor
- `Email`, `Phone`, `Money`, `CardNumber`

## Exception Handling

All exceptions extend `DomainException`:
- `InsufficientStockException` - Stock validation failures
- `PaymentFailedException` - Payment processing failures
- `TokenizationFailedException` - Card tokenization failures
- `CustomerNotFoundException` - Customer lookup failures
- `OrderNotFoundException` - Order lookup failures
- `ProductNotFoundException` - Product lookup failures

Global exception handling via `@RestControllerAdvice` in `GlobalExceptionHandler`.

## Testing Strategy

**Required Coverage: 80%+ (enforced by JaCoCo)**

- Unit tests for domain logic (no Spring)
- Integration tests with Testcontainers for repositories
- Controller tests with MockMvc
- Service tests with mocked dependencies

## Code Style & Patterns

- **Code Language:** English
- **Documentation Language:** Spanish (for PROMPTS.md, comments)
- **Lombok Usage:** Extensive (@Builder, @Data, @Value, @RequiredArgsConstructor)
- **Logging:** SLF4J with `@Slf4j`
- **Validation:** Jakarta Validation annotations in domain and DTOs
- **Transactions:** `@Transactional` on service methods that modify state

## AI Documentation Protocol (MANDATORY)

**⚠️ CRITICAL REQUIREMENT - MUST BE FOLLOWED ON EVERY INTERACTION:**

Before performing any task, your **FIRST ACTION** must be to document the user's prompt in `PROMPTS.md`. This is a project requirement for documenting AI assistance.

### Documentation Steps:
1. **Read** the user's prompt/request
2. **Append** to `PROMPTS.md` (never delete existing content) following this format:
   ```markdown
   ## Prompt #N: [Descriptive Title]
   **Fecha**: YYYY-MM-DD
   **Fase**: [Planificación/Desarrollo/Testing/Deployment/Dockerización]

   ### Contexto
   [Brief context and project state]

   ### Prompt Completo
   ```
   [Full user prompt here]
   ```

   ### Resultado Generado
   [What was generated, decisions made, files created/modified]
   ```
3. **Then** proceed with the actual task
4. **Update** the "Resultado Generado" section after completing the task

**Why This Matters:**
- Project requirement: Document all AI usage
- Provides audit trail for technical decisions
- Helps future developers understand AI-assisted development choices

### Current Status:
- 10 prompts documented (as of 2025-12-17)
- See `PROMPTS.md` for complete history

---

## Special Notes

1. **PROMPTS.md**: Document all AI prompts used during development (project requirement) - See "AI Documentation Protocol" section above

2. **Async Configuration**: Enabled with `@EnableAsync` on main class
   - Thread pool: 5-10 threads
   - Used for search logging (ProductServiceImpl)

3. **Transaction Management**: Enabled with `@EnableTransactionManagement`
   - Used for order creation, payment processing

4. **Simulation**: Tokenization and payment failures are simulated probabilistically for testing

5. **Security**: Credit card data encryption (AES-GCM) and API Key authentication to be implemented in infrastructure layer

## Common Pitfalls to Avoid

- ❌ Don't expose JPA @Entity in REST controllers
- ❌ Don't use Spring annotations in domain package
- ❌ Don't skip MapStruct for layer conversions
- ❌ Don't use field injection (@Autowired on fields)
- ❌ Don't create repositories/services without port interfaces first
- ❌ Don't modify domain entities after they're created (use builder pattern)
