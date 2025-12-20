# Farmatodo - Sistema Backend E-commerce

Aplicacion Backend para un sistema de E-commerce, desarrollado como reto tecnico para Farmatodo.

## 1. Descripcion del Sistema y Componentes

Este proyecto es una **API Backend** para una plataforma de E-commerce que gestiona clientes, catalogo de productos, carrito de compras, tokenizacion de tarjetas de credito y procesamiento de ordenes con simulacion de pagos.

### Arquitectura

![Arquitectura Hexagonal](resources/architecture/architecture_diagram.png)

La aplicacion sigue el patron de **Arquitectura Hexagonal (Ports and Adapters)** con una separacion estricta de capas:

```
com.farmatodo.reto_tecnico/
├── domain/                    # Logica de negocio pura (SIN dependencias de framework)
│   ├── model/                # Entidades de dominio y value objects
│   ├── port/
│   │   ├── in/              # Interfaces de casos de uso (input ports)
│   │   └── out/             # Interfaces de repositorios/gateways (output ports)
│   └── exception/           # Excepciones de dominio
│
├── application/              # Orquestacion de casos de uso
│   └── service/             # Implementaciones de servicios
│
└── infrastructure/           # Integraciones con frameworks externos
    ├── adapter/
    │   ├── in/rest/         # Controllers, DTOs, Mappers
    │   └── out/             # JPA Entities, Repositories, Adapters
    └── config/              # Configuracion de infraestructura
```

### Stack Tecnologico

| Categoria | Tecnologia |
|-----------|------------|
| Lenguaje | Java 21 |
| Framework | Spring Boot 3.3.5 |
| Base de Datos | PostgreSQL 16 |
| Contenedores | Docker & Docker Compose |
| Build Tool | Maven |
| Generacion de Codigo | Lombok, MapStruct |
| Documentacion API | SpringDoc OpenAPI (Swagger) |
| Testing | JUnit 5, Mockito, Testcontainers |
| Cobertura de Codigo | JaCoCo (requisito minimo 80%) |
| Testing de Email | MailHog |

---

## 2. Instrucciones para Ejecutar Localmente

### Prerequisitos

- **Java 21** (JDK)
- **Docker** y **Docker Compose**
- **Maven 3.8+** (o usar el Maven Wrapper incluido `./mvnw`)

### Paso 1: Clonar el Repositorio

```bash
git clone <repository-url>
cd ft-backend
```

### Paso 2: Configurar Variables de Entorno

Copiar el archivo de entorno de ejemplo:

```bash
cp .env.example .env
```

Editar el archivo `.env` con los valores deseados:

```properties
# Configuracion de Base de Datos
DB_PASSWORD=your_secure_password
DB_USER=postgres
DB_NAME=farmatodo_db
DB_PORT=5432
DB_HOST=farmatodo-postgres

# Seguridad de la Aplicacion
ENCRYPTION_KEY=change_this_to_a_secure_random_key_minimum_32_characters
API_KEY=change_this_to_a_secure_api_key

# Configuracion SMTP (MailHog)
SPRING_MAIL_HOST=mailhog
SPRING_MAIL_PORT=1025

# Puerto de la Aplicacion
APP_PORT=8080
```

**Notas Importantes de Configuracion:**

| Escenario | Valor de `DB_HOST` | Valor de `SPRING_MAIL_HOST` |
|-----------|--------------------|-----------------------------|
| Ejecutando **dentro de Docker** (stack completo) | `farmatodo-postgres` | `mailhog` |
| Ejecutando **fuera de Docker** (desarrollo local/tests) | `localhost` | `localhost` |

### Paso 3: Iniciar la Aplicacion

Ejecutar todos los servicios con Docker Compose:

```bash
docker compose up -d --build
```

Este comando realizara lo siguiente:
1. Construir la imagen de la aplicacion Spring Boot
2. Iniciar la base de datos PostgreSQL 16
3. Iniciar el servidor de email MailHog
4. Iniciar el contenedor de la aplicacion

### Paso 4: Verificar los Servicios

| Servicio | URL/Puerto | Descripcion |
|----------|------------|-------------|
| **API** | `http://localhost:8080` | Aplicacion principal |
| **Swagger UI** | `http://localhost:8080/swagger-ui.html` | Documentacion de la API |
| **Health Check** | `http://localhost:8080/ping` | Estado de la aplicacion |
| **MailHog UI** | `http://localhost:8025` | Interfaz de testing de emails |
| **PostgreSQL** | `localhost:5432` | Base de datos (puerto externo) |

### Comandos Utiles

```bash
# Ver logs
docker compose logs -f app

# Detener todos los servicios
docker compose down

# Detener y eliminar volumenes (limpiar base de datos)
docker compose down -v

# Reconstruir solo la aplicacion
docker compose up -d --build app
```
---

## 3. Despliegue en GCP y CI/CD (Live Demo)

La aplicación está desplegada en Google Cloud Platform (Cloud Run) y dispone de un pipeline de Integración Continua / Despliegue Continuo (CI/CD).

### Enlaces del Entorno Productivo

| Servicio | URL |
|----------|-----|
| Aplicación (Cloud Run) | [AGREGAR_URL_APP] |
| Documentación API (Swagger) | [AGREGAR_URL_SWAGGER] |
| Servidor de Correos (MailHog) | [AGREGAR_URL_MAILHOG_SI_LA_TIENES] |
| Pipeline CI/CD (GitHub Actions) | https://github.com/<owner>/<repo>/actions |

### Pipeline de CI/CD (resumen)

El proyecto incluye un workflow automatizado en .github/workflows que asegura la calidad y el despliegue continuo:

1. Build & Test: Ejecuta mvn clean verify para correr pruebas unitarias y de integracion en cada Push.

2. Docker Build: Construye la imagen Docker optimizada.

3. Artifact Registry: Sube la imagen al registro privado de Google Cloud.

4. Deploy: Despliega la nueva version en Cloud Run conectandose a Cloud SQL.

---

## 4. Como Ejecutar los Tests

El proyecto incluye **Tests Unitarios** y **Tests de Integracion** con Testcontainers.

### Ejecutar Todos los Tests

```bash
# Usando Maven Wrapper (recomendado)
./mvnw clean test

# En Windows
mvnw.cmd clean test
```

### Ejecutar Tests con Reporte de Cobertura

```bash
./mvnw clean verify
```

El reporte de cobertura se generara en: `target/site/jacoco/index.html`

**Nota:** El proyecto exige un minimo de **80% de cobertura de codigo** mediante JaCoCo.

### Ejecutar Tests Especificos

```bash
# Ejecutar una clase de test especifica
./mvnw test -Dtest=CustomerServiceImplTest

# Ejecutar un metodo de test especifico
./mvnw test -Dtest=CustomerServiceImplTest#shouldRegisterCustomerSuccessfully
```

### Categorias de Tests

| Tipo | Descripcion | Ubicacion |
|------|-------------|-----------|
| Tests Unitarios | Logica de dominio sin contexto de Spring | `src/test/java/**/domain/**` |
| Tests de Servicios | Servicios de aplicacion con dependencias mockeadas | `src/test/java/**/application/**` |
| Tests de Integracion | Stack completo con Testcontainers | `src/test/java/**/infrastructure/**` |
| Tests de Controllers | Endpoints REST con MockMvc | `src/test/java/**/rest/**` |

---
## 5. Pruebas y Documentacion API (Postman)

Todos los recursos necesarios para probar la API se encuentran organizados en la carpeta resources/postman.

### Configuracion Inicial

1. Importar Environment: Cargar el archivo resources/postman/environments/Farmatodo_Env.postman_environment.json.

2. Seleccionar Environment: Asegurarse de tener seleccionado "Farmatodo" (o "dev") en Postman antes de ejecutar cualquier peticion.

### Colecciones Disponibles

Hemos incluido 2 colecciones especializadas en la carpeta resources/postman/collections:

## A. Farmatodo - Data-Driven Tests

- Enfoque: Pruebas de validación masiva con datos externos.

- Como ejecutar:

1. Abrir el Collection Runner en Postman.

2. Seleccionar la carpeta/request deseada (Marcadas con (Done)).

3. Cargar el archivo CSV correspondiente desde resources/postman/data/.

4. Ejecutar.

- Archivos CSV Disponibles:

-- order_tests.csv: Validaciones de creación de ordenes.

-- cart_tests.csv: Validaciones de limites y errores del carrito.

-- customer_tests.csv: Validaciones de registro de clientes.

-- cards_tokenization_tests.csv: Validaciones de tokenización de tarjetas.

## B. Farmatodo - E2E Automatic Flows

- Enfoque: Flujos "End-to-End" completos y autónomos.

- Descripcion: Esta coleccion NO requiere archivos CSV. Utiliza scripts internos (Pre-request Scripts) para generar datos aleatorios (Emails únicos, Telefonos, Tarjetas) en cada ejecución.

- Uso ideal: Validar rápidamente que todo el sistema funciona (Happy Path) sin configurar datos manualmente. Solo dale al botón "Run".

## Resumen de Endpoints de la API

Para documentacion completa de la API, visitar [Swagger UI](https://ft-api.srodriguez-tech.com/swagger-ui/index.html)

---
