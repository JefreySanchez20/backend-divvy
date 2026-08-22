#!/usr/bin/env bash
# Carga .env al entorno del shell y arranca la app.
# Docker Compose lee .env por su cuenta (solo para el contenedor de Postgres);
# este script es lo que hace que el proceso de Spring Boot también vea esas mismas variables.
set -euo pipefail
cd "$(dirname "$0")"

if [ ! -f .env ]; then
  echo ".env no encontrado. Copia .env.example a .env y ajusta los valores." >&2
  exit 1
fi

set -a
source .env
set +a

./mvnw spring-boot:run
