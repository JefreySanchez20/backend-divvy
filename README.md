<div align="center">

# Divvy — Backend

**API REST para dividir gastos compartidos entre grupos.**
Crear grupos, registrar gastos, dividirlos entre miembros y liquidar deudas con un algoritmo de simplificación — estilo Splitwise.

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![CI](https://github.com/JefreySanchez20/backend-divvy/actions/workflows/ci.yml/badge.svg)](https://github.com/JefreySanchez20/backend-divvy/actions/workflows/ci.yml)

**API en vivo:** [backend-divvy.onrender.com](https://backend-divvy.onrender.com) · **Swagger:** [backend-divvy.onrender.com/swagger-ui.html](https://backend-divvy.onrender.com/swagger-ui.html)

</div>

---

## Índice

- [Stack](#stack)
- [Arquitectura](#arquitectura)
- [Requisitos](#requisitos)
- [Puesta en marcha local](#puesta-en-marcha-local)
- [API](#api)
- [Testing](#testing)
- [Docker](#docker)
- [Despliegue](#despliegue)
- [CI](#ci)

---

## Stack

| Categoría | Tecnología |
|---|---|
| Lenguaje / Runtime | Java 21 |
| Framework | Spring Boot 4 (Spring Framework 7) |
| Base de datos | PostgreSQL + Flyway (migraciones versionadas) |
| Autenticación | Spring Security 7 + JWT stateless, con logout vía lista negra de tokens |
| Rate limiting | Bucket4j (login, registro, recuperación de contraseña) |
| Documentación API | springdoc-openapi (Swagger UI) |
| Testing | JUnit 5, Mockito, Testcontainers (integración contra Postgres real) |
| Empaquetado | Docker (imagen multi-stage) |

## Arquitectura

Domain-Driven Design con arquitectura hexagonal (puertos y adaptadores), organizado en **3 bounded contexts** independientes más un núcleo compartido. Ningún contexto importa clases Java de otro directamente: la comunicación entre ellos se hace mediante eventos de dominio o puertos definidos en `shared`.

```
com.divvy
│
├── autenticacion/    registro · login · logout (JWT blacklist) · recuperación de contraseña
├── grupos/           creación de grupos y gestión de miembros
├── gastos/           registro de gastos y su división entre miembros
├── liquidaciones/    cálculo de deudas y registro de pagos
│
└── shared/           Dinero (value object), eventos de dominio, seguridad, persistencia común
```

Cada contexto se organiza en las mismas tres capas:

```
<contexto>/
├── domain/           entidades, value objects, invariantes, eventos, puertos (interfaces)
├── application/      casos de uso — orquestan domain + puertos, sin Spring salvo @Transactional
└── infrastructure/   adaptadores — controllers REST, JPA, seguridad, email
```

**Decisiones de diseño relevantes**

- El dinero se maneja siempre como `BigDecimal` a través del value object `Dinero` — nunca `double`/`float`.
- La división de gastos distribuye los centavos de forma determinista para evitar descuadres por redondeo.
- Las liquidaciones usan un algoritmo greedy (*"el mayor deudor paga al mayor acreedor"*) para minimizar la cantidad de transacciones necesarias para saldar un grupo.
- La autenticación es 100% stateless. El logout se implementa con una lista negra de tokens (claim `jti` del JWT + tabla `revoked_tokens`), con limpieza automática programada de tokens ya expirados.

## Requisitos

- Java 21
- Docker (para Postgres local vía `docker-compose`, y para Testcontainers al correr los tests)

## Puesta en marcha local

```bash
cp .env.example .env
# los valores por defecto ya funcionan con docker-compose, ajustá si hace falta

docker compose up -d db   # levanta Postgres local
./run.sh                  # carga .env y arranca la app (mvnw spring-boot:run)
```

La API queda disponible en `http://localhost:8080`.
Variables de entorno documentadas en [`.env.example`](.env.example).

## API

Documentación interactiva (Swagger UI): en local **`http://localhost:8080/swagger-ui.html`**, en producción **[backend-divvy.onrender.com/swagger-ui.html](https://backend-divvy.onrender.com/swagger-ui.html)**.

| Método | Endpoint | Descripción | Auth |
|---|---|---|---|
| `POST` | `/api/auth/register` | Registrar usuario | — |
| `POST` | `/api/auth/login` | Login, devuelve JWT | — |
| `POST` | `/api/auth/logout` | Invalida el token actual | ✅ |
| `POST` | `/api/auth/forgot-password` | Solicita email de recuperación | — |
| `POST` | `/api/auth/reset-password` | Restablece contraseña con token | — |
| `GET` | `/api/users?email=` | Buscar usuario por email (para agregarlo a un grupo) | ✅ |
| `POST` | `/api/groups` | Crear grupo | ✅ |
| `GET` | `/api/groups` | Listar grupos del usuario | ✅ |
| `GET` | `/api/groups/{id}` | Detalle de un grupo | ✅ |
| `POST` | `/api/groups/{id}/members` | Agregar miembro | ✅ |
| `DELETE` | `/api/groups/{id}/members/{userId}` | Quitar miembro | ✅ |
| `PATCH` | `/api/groups/{id}/archive` | Archivar grupo | ✅ |
| `POST` | `/api/groups/{groupId}/expenses` | Registrar gasto | ✅ |
| `GET` | `/api/groups/{groupId}/expenses` | Listar gastos del grupo | ✅ |
| `GET` | `/api/groups/{groupId}/expenses/{id}` | Detalle de un gasto | ✅ |
| `PUT` | `/api/groups/{groupId}/expenses/{id}` | Editar gasto | ✅ |
| `DELETE` | `/api/groups/{groupId}/expenses/{id}` | Eliminar gasto | ✅ |
| `GET` | `/api/groups/{groupId}/settlements` | Deudas pendientes del grupo | ✅ |
| `GET` | `/api/groups/{groupId}/settlements/history` | Historial de pagos | ✅ |
| `POST` | `/api/settlements/{id}/debts/{debtId}/pay` | Registrar el pago de una deuda | ✅ |
| `GET` | `/actuator/health` | Health check | — |

`✅` = requiere header `Authorization: Bearer <token>`.

## Testing

```bash
./mvnw test
```

Los tests de integración levantan un contenedor Postgres real vía Testcontainers (requiere Docker corriendo).

> **176 tests · 0 fallos** (último run local)

## Docker

```bash
docker build -t divvy-backend .

docker run -p 8080:8080 \
  -e DB_HOST=host.docker.internal -e DB_PASSWORD=divvy_dev_password \
  -e JWT_SECRET=una-clave-de-al-menos-32-bytes \
  divvy-backend
```

## Despliegue

Desplegado en **Render** (aplicación, build vía Docker) + **Neon** (Postgres gestionado, con SSL):

- **API:** https://backend-divvy.onrender.com
- **Swagger:** https://backend-divvy.onrender.com/swagger-ui.html

La app soporta esto out-of-the-box:

- `server.port` respeta la variable `PORT` que inyecta Render.
- `DB_SSLMODE=require` habilita la conexión SSL que exige Neon.
- `/actuator/health` sirve como health check para Render.

> El plan de Render usado es el free tier: el servicio se "duerme" tras un rato sin tráfico, por lo que el primer request luego de inactividad puede tardar hasta 1-2 minutos (cold start).

## CI

GitHub Actions ([`.github/workflows/ci.yml`](.github/workflows/ci.yml)) corre `./mvnw test` en cada push/PR a `main`.
