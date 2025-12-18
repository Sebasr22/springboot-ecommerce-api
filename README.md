# Farmatodo Backend Challenge

![Java](https://img.shields.io/badge/Java-21-orange?style=flat&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.0-brightgreen?style=flat&logo=spring)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=flat&logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=flat&logo=docker)
![Tests](https://img.shields.io/badge/Tests-123%20Passing-success?style=flat)
![Coverage](https://img.shields.io/badge/Coverage-%3E70%25-green?style=flat)

## 📋 Descripción

Sistema backend empresarial desarrollado con **Arquitectura Hexagonal (Clean Architecture)** que implementa un sistema completo de e-commerce con tokenización de tarjetas de crédito, gestión de pedidos, carrito de compras y notificaciones por email.

### Características Principales

- **Arquitectura Hexagonal Estricta**: Separación total entre Domain (lógica de negocio), Application (casos de uso) e Infrastructure (adaptadores)
- **Principios SOLID**: Código mantenible, extensible y testeable
- **Domain-Driven Design**: Value Objects inmutables, Aggregate Roots, y lógica de negocio encapsulada
- **Seguridad**: API Key authentication, encriptación AES-256-GCM para tokens, configuración externa de credenciales
- **Observabilidad**: Trace IDs en todos los requests, logging estructurado, MDC (Mapped Diagnostic Context)
- **Testing Robusto**: 123 tests (90 unitarios + 33 de integración web), >70% de cobertura
- **Production-Ready**: Docker Compose, health checks, retry logic, async processing

## 🛠️ Tecnologías

| Categoría | Tecnologías |
|-----------|-------------|
| **Backend** | Java 21, Spring Boot 3.4.0, Spring Data JPA |
| **Base de Datos** | PostgreSQL 16 (Docker) |
| **Mapeo de Datos** | MapStruct (obligatorio para todas las conversiones) |
| **Productividad** | Lombok (@Builder, @Data, @Value, @RequiredArgsConstructor) |
| **Seguridad** | AES-256-GCM encryption, API Key filter |
| **Testing** | JUnit 5, Mockito, AssertJ, Testcontainers, MockMvc |
| **Documentación** | SpringDoc OpenAPI 3 (Swagger UI) |
| **Email** | Spring Mail + MailHog (SMTP testing) |
| **Build** | Maven 3.9+, JaCoCo (cobertura) |
| **Deployment** | Docker & Docker Compose |

## 🚀 Guía de Inicio Rápido

### Prerrequisitos

- **Docker** y **Docker Compose** instalados
- Puerto `8080` (API), `5433` (PostgreSQL), `8025` (MailHog UI), `1025` (SMTP) disponibles

### Paso 1: Configurar Variables de Entorno

Copia el archivo de ejemplo y ajusta las credenciales:

```bash
# Windows
copy .env.example .env

# Linux/Mac
cp .env.example .env
```

**Opcional**: Edita el archivo `.env` si necesitas cambiar las credenciales por defecto:

```bash
# Ejemplo de valores (ya configurados por defecto)
DB_PASSWORD=password
DB_USER=postgres
DB_NAME=farmatodo_db
ENCRYPTION_KEY=esta_es_una_super_clave_secreta_de_32_caracteres_minimo
API_KEY=production_api_key_123
```

> ⚠️ **IMPORTANTE**: El archivo `.env` contiene credenciales sensibles y **NO debe ser commitado** a Git (ya está en `.gitignore`).

### Paso 2: Levantar la Aplicación

Ejecuta el comando mágico que construye y levanta todo el stack (PostgreSQL + MailHog + App):

```bash
docker compose up --build
```

**Espera a ver estos logs**:
```
farmatodo-app       | Started RetoTecnicoApplication in X.XXX seconds
farmatodo-postgres  | database system is ready to accept connections
farmatodo-mailhog   | [HTTP] Binding to address: 0.0.0.0:8025
```

La aplicación estará lista cuando veas el mensaje de Spring Boot startup completo.

### Paso 3: Verificar que Funciona

Abre tu navegador y verifica el health check:

```
http://localhost:8080/ping
```

Deberías ver:
```json
{
  "status": "UP",
  "timestamp": "2025-12-17T...",
  "application": "ft-backend",
  "version": "0.0.1-SNAPSHOT"
}
```

## 📍 Endpoints Principales

### Recursos de la API

| Endpoint | Descripción | Puerto |
|----------|-------------|--------|
| **API REST** | http://localhost:8080/api/v1 | 8080 |
| **Swagger UI** | http://localhost:8080/swagger-ui.html | 8080 |
| **Health Check** | http://localhost:8080/ping | 8080 |
| **OpenAPI JSON** | http://localhost:8080/v3/api-docs | 8080 |
| **MailHog UI** | http://localhost:8025 | 8025 |

### Autenticación

Todos los endpoints (excepto `/ping`) requieren el header de autenticación:

```http
X-API-KEY: production_api_key_123
```

### Ejemplos de Uso con cURL

#### 1. Registrar un Cliente

```bash
curl -X POST http://localhost:8080/api/v1/customers \
  -H "Content-Type: application/json" \
  -H "X-API-KEY: production_api_key_123" \
  -d '{
    "name": "Juan Pérez García",
    "email": "juan.perez@example.com",
    "phone": "573001234567",
    "address": "Calle 123 #45-67, Bogotá"
  }'
```

#### 2. Buscar Productos

```bash
curl -X GET "http://localhost:8080/api/v1/products/search?query=paracetamol" \
  -H "X-API-KEY: production_api_key_123"
```

#### 3. Agregar Producto al Carrito

```bash
curl -X POST http://localhost:8080/api/v1/cart/items \
  -H "Content-Type: application/json" \
  -H "X-API-KEY: production_api_key_123" \
  -d '{
    "customerId": "UUID-del-cliente",
    "productId": "UUID-del-producto",
    "quantity": 2
  }'
```

#### 4. Crear Orden desde el Carrito

```bash
curl -X POST http://localhost:8080/api/v1/cart/checkout/UUID-del-cliente \
  -H "X-API-KEY: production_api_key_123"
```

#### 5. Procesar Pago con Tarjeta Nueva

```bash
curl -X POST http://localhost:8080/api/v1/payments/orders/UUID-de-orden \
  -H "Content-Type: application/json" \
  -H "X-API-KEY: production_api_key_123" \
  -d '{
    "creditCard": {
      "cardNumber": "4111111111111111",
      "cardHolderName": "Juan Pérez",
      "expirationMonth": "12",
      "expirationYear": "2025",
      "cvv": "123"
    }
  }'
```

#### 6. Ver Emails Enviados

Abre en tu navegador: **http://localhost:8025**

Verás todos los emails simulados de confirmación de pago (éxito/fallo).

### Exploración Completa

Para explorar todos los endpoints disponibles, visita:

🔗 **Swagger UI**: http://localhost:8080/swagger-ui.html

Ahí encontrarás documentación interactiva con ejemplos, schemas y la posibilidad de ejecutar requests directamente desde el navegador.

## 🏗️ Arquitectura

### ¿Por Qué Arquitectura Hexagonal?

Este proyecto sigue los principios de **Clean Architecture / Hexagonal Architecture** para lograr:

- **Independencia de Frameworks**: El dominio no conoce Spring, JPA ni ningún framework
- **Testabilidad**: Tests unitarios puros sin necesidad de cargar el contexto de Spring
- **Separación de Responsabilidades**: Lógica de negocio aislada de detalles de infraestructura
- **Inversión de Dependencias**: Las capas externas dependen de las internas, nunca al revés
- **Facilidad de Cambio**: Podemos cambiar la base de datos, el framework web o cualquier detalle técnico sin tocar el dominio

### Estructura de Capas

```
com.farmatodo.reto_tecnico/
│
├── domain/                          # ⭐ NÚCLEO - Lógica de Negocio Pura
│   ├── model/                       # Entidades y Value Objects
│   │   ├── valueobjects/            # Email, Phone, Money, CardNumber (Records inmutables)
│   │   ├── Customer.java            # Aggregate Root
│   │   ├── Product.java             # Con lógica de stock
│   │   ├── Order.java               # Máquina de estados (PENDING → CONFIRMED → COMPLETED)
│   │   ├── CreditCard.java          # Con tokenización y validación Luhn
│   │   └── Cart.java                # Gestión de carrito temporal
│   ├── port/
│   │   ├── in/                      # Interfaces de Casos de Uso (QUÉ hace el sistema)
│   │   │   ├── CreateOrderUseCase
│   │   │   ├── ProcessPaymentUseCase
│   │   │   └── TokenizeCardUseCase
│   │   └── out/                     # Interfaces de Repositorios/Gateways (CÓMO persiste)
│   │       ├── CustomerRepositoryPort
│   │       ├── ProductRepositoryPort
│   │       └── EmailPort
│   └── exception/                   # Excepciones de dominio
│
├── application/                     # ⚙️ ORQUESTACIÓN - Casos de Uso
│   ├── service/                     # Implementaciones de los Use Cases
│   │   ├── OrderServiceImpl         # Validación stock + creación orden
│   │   ├── PaymentService           # Lógica de reintentos (configurable)
│   │   ├── TokenizationServiceImpl  # Simulación probabilística
│   │   └── CartService              # Gestión de carrito temporal
│   └── config/
│       └── FarmatodoProperties      # @ConfigurationProperties (type-safe)
│
└── infrastructure/                  # 🔌 ADAPTADORES - Detalles Técnicos
    ├── adapter/
    │   ├── in/rest/                 # Adaptadores de Entrada
    │   │   ├── controller/          # REST Controllers (@RestController)
    │   │   ├── dto/                 # Request/Response DTOs
    │   │   └── mapper/              # MapStruct mappers (DTO ↔ Domain)
    │   └── out/                     # Adaptadores de Salida
    │       ├── persistence/         # JPA (Entities, Repositories, Adapters)
    │       │   ├── entity/          # @Entity classes
    │       │   ├── repository/      # Spring Data JPA
    │       │   ├── adapter/         # Implementan RepositoryPort
    │       │   └── mapper/          # MapStruct (Domain ↔ Entity)
    │       └── email/
    │           └── JavaMailEmailAdapter  # Implementa EmailPort
    ├── security/
    │   └── filter/                  # ApiKeyAuthenticationFilter, TraceIdFilter
    └── config/                      # Configuración de Spring (CORS, Async, etc.)
```

### Flujo de Datos (Ejemplo: Crear Orden)

```
1. HTTP POST /api/v1/orders
   ↓
2. OrderController (REST adapter)
   ↓ (convierte DTO → Domain con MapStruct)
3. CreateOrderUseCase (interface en domain/port/in)
   ↓
4. OrderServiceImpl (application layer)
   ↓ (valida stock, aplica lógica de negocio)
5. OrderRepositoryPort (interface en domain/port/out)
   ↓
6. OrderRepositoryAdapter (infrastructure)
   ↓ (convierte Domain → Entity con MapStruct)
7. OrderJpaRepository (Spring Data JPA)
   ↓
8. PostgreSQL
```

**Clave**: El dominio **nunca** conoce detalles como JPA, HTTP o bases de datos. Solo define **interfaces** (ports) que la infraestructura implementa.

### Patrones Aplicados

- **Ports and Adapters**: Interfaces en dominio, implementaciones en infraestructura
- **Aggregate Root**: Order, Customer, Cart gestionan sus propios items
- **Value Objects**: Email, Phone, Money con validación en constructor
- **Repository Pattern**: Abstracción del acceso a datos
- **Mapper Pattern**: MapStruct para conversiones automáticas
- **Strategy Pattern**: Diferentes flujos de pago (con token vs con tarjeta)
- **Retry Pattern**: Reintentos configurables en procesamiento de pagos

## 🧪 Testing

### Ejecutar Tests

```bash
# Todos los tests (123 tests)
./mvnw test

# Con reporte de cobertura JaCoCo
./mvnw clean verify

# Ver reporte de cobertura en navegador
open target/site/jacoco/index.html  # Mac/Linux
start target/site/jacoco/index.html # Windows
```

### Cobertura Actual

| Capa | Cobertura | Tests |
|------|-----------|-------|
| **Application (Services)** | ~72% | 43 tests unitarios |
| **Domain (Models)** | ~54% | 51 tests unitarios |
| **Infrastructure (Controllers)** | 100% | 33 tests de integración web |
| **TOTAL** | >70% | **123 tests passing** |

### Estrategia de Testing

1. **Tests Unitarios (90 tests)**:
   - Sin Spring (`@ExtendWith(MockitoExtension.class)`)
   - Rápidos (<8 segundos)
   - Mockito + AssertJ
   - Cubren lógica de negocio en servicios y dominio

2. **Tests de Integración Web (33 tests)**:
   - Con Spring (`@WebMvcTest`)
   - MockMvc para simular requests HTTP
   - Validan contratos de API, códigos HTTP, validaciones `@Valid`
   - Cubren GlobalExceptionHandler

3. **Principios**:
   - Arrange-Act-Assert (AAA)
   - Given-When-Then (BDD style)
   - Tests aislados (no comparten estado)
   - Nombres descriptivos (`shouldThrowExceptionWhenStockInsufficient`)

### Ejemplo de Test Unitario

```java
@Test
void shouldThrowExceptionWhenStockInsufficient() {
    // Arrange
    Product product = Product.builder().id(UUID.randomUUID())
        .stock(5).build();
    when(productRepository.findById(productId)).thenReturn(Optional.of(product));

    // Act & Assert
    assertThatThrownBy(() -> orderService.createOrder(customerId, orderItems))
        .isInstanceOf(InsufficientStockException.class)
        .hasMessageContaining("Insufficient stock for product");
}
```

## 📊 Requisitos Funcionales Implementados

| # | Requisito | Estado | Implementación |
|---|-----------|--------|----------------|
| 1 | Gestión de productos | ✅ Completo | CRUD + búsqueda con paginación + logging asíncrono |
| 2 | Creación de pedidos | ✅ Completo | Validación atómica de stock con `UPDATE WHERE stock >= qty` |
| 3 | Registro de clientes | ✅ Completo | Validación de email Y teléfono únicos |
| 4 | Almacenar búsquedas | ✅ Completo | Persistencia en BD con traceId para correlación |
| 5 | Carrito de compras | ✅ Completo | Agregar, ver, checkout → Order |
| 6 | Notificaciones email | ✅ Completo | Templates HTML para éxito/fallo + MailHog |
| 7 | Tokenización de tarjetas | ✅ Completo | AES-256-GCM + simulación + reintentos configurables |
| 8 | Trazabilidad (TraceID) | ✅ Completo | MDC + header `X-Trace-Id` en responses |
| 9 | Tests + Cobertura | ✅ Completo | 123 tests, >70% cobertura, JaCoCo |

**Resultado**: **9/9 requisitos funcionales completados** ✅

## 🔐 Seguridad

### Medidas Implementadas

1. **API Key Authentication**:
   - Header `X-API-KEY` obligatorio en todos los endpoints (excepto `/ping`)
   - Filtro custom `ApiKeyAuthenticationFilter`
   - Configurable vía variable de entorno `API_KEY`

2. **Encriptación de Datos Sensibles**:
   - Tokens de pago encriptados con **AES-256-GCM**
   - IV aleatorio de 12 bytes por registro
   - Clave configurable vía `ENCRYPTION_KEY` (mínimo 32 caracteres)
   - JPA `@Convert` con `CryptoConverter` automático

3. **Configuración Externa**:
   - Credenciales en archivo `.env` (gitignored)
   - Nunca hardcodeadas en código
   - Rotación de claves sin recompilar

4. **Validación de Entrada**:
   - Jakarta Validation en DTOs (`@Valid`, `@NotBlank`, `@Email`, `@Pattern`)
   - Validación de algoritmo de Luhn para tarjetas
   - Sanitización de queries

5. **CORS Configurado**:
   - Actualmente permisivo para facilitar evaluación
   - Listo para restringir en producción

## 🤖 Uso de Inteligencia Artificial

### Asistente de Desarrollo

Este proyecto fue desarrollado con el apoyo de **Claude Code (Claude Sonnet 4.5)** actuando como:

- **Arquitecto de Software Senior**: Diseño de arquitectura hexagonal, elección de patrones
- **Code Reviewer**: Detección de race conditions, mejores prácticas, code smells
- **Desarrollador Senior**: Implementación de capas siguiendo estrictamente Clean Architecture
- **QA Engineer**: Estrategia de testing, casos de prueba, validaciones

### Transparencia y Documentación

Como **requisito del reto técnico**, todo el uso de IA está documentado:

📄 **Ver archivo**: [`PROMPTS.md`](PROMPTS.md)

Este archivo contiene:
- **20 prompts completos** enviados a la IA
- **Contexto de cada fase** (Planificación, Desarrollo, Testing, Deployment, QA)
- **Resultado generado** por cada prompt (archivos creados, decisiones técnicas)
- **Historial cronológico** completo del desarrollo (2025-12-16 a 2025-12-17)

### Ejemplos de Prompts Documentados

| Prompt | Fase | Descripción |
|--------|------|-------------|
| #1 | Setup | Diseño de arquitectura hexagonal y estructura inicial |
| #4 | Code Review | Detección de race condition TOCTOU en validación de stock |
| #8 | Testing | Generación de 90 tests unitarios con JUnit 5 + Mockito |
| #16 | QA | Bugfix de validación en CreateOrderRequest (flujo dual) |
| #19 | Corrección | Implementación de requisitos faltantes (phone único + persistencia búsquedas) |
| #20 | Seguridad | Externalización de credenciales a .env |

### Filosofía de Desarrollo

- **IA como Herramienta**: La arquitectura, decisiones y validación final fueron humanas
- **Aprendizaje**: Cada prompt fue diseñado para profundizar en conceptos (hexagonal, DDD, SOLID)
- **Trazabilidad**: Todo cambio justificado y documentado en PROMPTS.md
- **Responsabilidad**: El código final fue revisado, validado y comprendido completamente

## 📂 Archivos de Configuración

| Archivo | Descripción | Versionado |
|---------|-------------|------------|
| `.env` | Credenciales reales (DB, API keys) | ❌ NO (gitignored) |
| `.env.example` | Template para desarrolladores | ✅ SÍ |
| `docker-compose.yml` | Orquestación de servicios | ✅ SÍ |
| `application.properties` | Config de Spring Boot | ✅ SÍ |
| `pom.xml` | Dependencias Maven | ✅ SÍ |

## 🚢 Deployment

### Docker Compose (Recomendado)

```bash
# Desarrollo
docker compose up --build

# Producción (detached mode)
docker compose up -d --build

# Ver logs
docker logs farmatodo-app -f

# Detener todo
docker compose down

# Limpiar volúmenes (⚠️ borra datos de BD)
docker compose down -v
```

### Healthchecks Implementados

Todos los servicios tienen healthchecks configurados:

- **PostgreSQL**: `pg_isready -U postgres -d farmatodo_db`
- **MailHog**: `wget --spider http://localhost:8025`
- **App**: `wget --spider http://localhost:8080/ping`

La aplicación solo arranca cuando PostgreSQL y MailHog están saludables (`depends_on: service_healthy`).

## 📖 Documentación Adicional

| Archivo | Contenido |
|---------|-----------|
| [`PROMPTS.md`](PROMPTS.md) | Historial completo de prompts de IA (requisito del reto) |
| [`CLAUDE.md`](CLAUDE.md) | Guía de arquitectura y convenciones para Claude Code |
| [`PROJECT_STATUS.md`](PROJECT_STATUS.md) | Estado del proyecto, requisitos completados |
| [`TECHNICAL_DEBT_CLEANUP.md`](TECHNICAL_DEBT_CLEANUP.md) | Reporte de limpieza de deuda técnica |
| [`Instrucciones.md`](Instrucciones.md) | Requisitos originales del reto técnico |

## 👨‍💻 Comandos Útiles para Desarrollo

```bash
# Compilar sin tests
./mvnw clean compile

# Empaquetar (genera JAR)
./mvnw clean package -DskipTests

# Ejecutar localmente (sin Docker)
./mvnw spring-boot:run

# Verificar estilo de código
./mvnw checkstyle:check

# Ver dependencias
./mvnw dependency:tree

# Actualizar dependencias
./mvnw versions:display-dependency-updates
```

## 🔍 Troubleshooting

### La aplicación no arranca

**Problema**: Puertos ocupados
```bash
# Verificar qué usa el puerto 8080
netstat -ano | findstr :8080  # Windows
lsof -i :8080                 # Linux/Mac

# Cambiar puerto en .env
APP_PORT=8081
```

**Problema**: No encuentra `.env`
```bash
# Crear desde template
cp .env.example .env
```

### Tests fallan

**Problema**: Falta Maven wrapper
```bash
# Reinstalar wrapper
mvn wrapper:wrapper
```

**Problema**: Testcontainers no puede arrancar
```bash
# Verificar Docker está corriendo
docker ps

# Dar permisos (Linux)
sudo usermod -aG docker $USER
```

### Base de datos no responde

```bash
# Ver logs de PostgreSQL
docker logs farmatodo-postgres

# Conectarse manualmente
docker exec -it farmatodo-postgres psql -U postgres -d farmatodo_db

# Recrear volúmenes
docker compose down -v
docker compose up --build
```

## 📞 Soporte y Contacto

- **Issues**: Reportar problemas en GitHub Issues
- **Email**: [Tu email de contacto]
- **Documentación IA**: Ver `PROMPTS.md` para entender decisiones de diseño

---

**Desarrollado con**:
- ☕ Java 21 + Spring Boot 3.4.0
- 🏛️ Arquitectura Hexagonal (Clean Architecture)
- 🤖 Asistido por Claude Code (Anthropic)
- 📐 Principios SOLID y DDD
- ✅ 123 Tests Passing

**Licencia**: Este proyecto es parte de un reto técnico para Farmatodo.
