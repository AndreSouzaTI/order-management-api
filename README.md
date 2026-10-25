# Order Management API

![CI](https://github.com/AndreSouzaTI/order-management-api/actions/workflows/ci.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-6DB33F?logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-336791?logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-ready-2496ED?logo=docker)

API REST para gestão de **produtos e pedidos**, com autenticação **JWT**, controle de estoque, ciclo de vida do pedido e perfis de acesso (cliente e administrador).

O foco do projeto é mostrar como estruturo um back-end Java de forma profissional: regras de negócio no domínio, banco versionado com migrations, tratamento de erros padronizado, documentação OpenAPI e testes de integração contra um PostgreSQL real.

## Funcionalidades

- Cadastro e login de usuários com senha criptografada (BCrypt) e token JWT (HS256)
- Perfis `USER` e `ADMIN` com autorização por método (`@PreAuthorize`)
- CRUD de produtos com busca por nome, paginação e exclusão lógica
- Criação de pedidos com baixa automática de estoque e preço congelado no momento da compra
- Máquina de estados do pedido: `CREATED → PAID → SHIPPED → DELIVERED`, com cancelamento que devolve os itens ao estoque
- Lock otimista (`@Version`) para evitar vender o mesmo estoque em pedidos simultâneos
- Erros no padrão **RFC 9457 (Problem Details)**, com detalhe por campo nas validações
- Documentação interativa com **Swagger UI**
- Health check com **Spring Boot Actuator**

## Stack

| Camada | Tecnologias |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 4.1, Spring Web MVC, Spring Data JPA, Spring Security (OAuth2 Resource Server) |
| Persistência | Hibernate, PostgreSQL 17, Flyway |
| Documentação | springdoc-openapi (Swagger UI) |
| Testes | JUnit 5, Mockito, AssertJ, MockMvc, Testcontainers |
| Infra | Docker (multi-stage), Docker Compose, GitHub Actions |

## Arquitetura

```mermaid
flowchart LR
    C[Cliente HTTP] -->|JWT| F[Spring Security<br/>Resource Server]
    F --> W[Controllers REST]
    W --> S[Services<br/>transações]
    S --> D[Domínio<br/>Order, Product]
    S --> R[Repositories<br/>Spring Data JPA]
    R --> DB[(PostgreSQL)]
    M[Flyway] --> DB
```

```
src/main/java/com/andresouza/orders
├── config/       # OpenAPI e criação do usuário admin
├── domain/       # Entidades e regras de negócio (estoque, transições de status)
├── exception/    # Exceções de negócio e handler global (Problem Details)
├── repository/   # Interfaces Spring Data JPA com EntityGraph (sem N+1)
├── security/     # Configuração JWT, emissão de token, usuário autenticado
├── service/      # Casos de uso transacionais
└── web/          # Controllers REST e DTOs (records)
```

## Como executar

### Com Docker (recomendado)

```bash
docker compose up --build
```

A API sobe em `http://localhost:8080` junto com o PostgreSQL. Um administrador de demonstração é criado automaticamente (`admin@demo.com` / `admin12345`, definido no `docker-compose.yml`).

- Swagger UI: http://localhost:8080/swagger-ui.html
- Health check: http://localhost:8080/actuator/health

### Localmente

Pré-requisitos: Java 21 e um PostgreSQL rodando (ou `docker compose up postgres`).

```bash
./mvnw spring-boot:run
```

Variáveis de ambiente aceitas: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION`, `ADMIN_EMAIL`, `ADMIN_PASSWORD`.

## Exemplos de uso

```bash
# Cadastro e login
curl -X POST localhost:8080/api/auth/register -H "Content-Type: application/json" \
  -d '{"name":"Maria","email":"maria@email.com","password":"senha-segura"}'

TOKEN=$(curl -s -X POST localhost:8080/api/auth/login -H "Content-Type: application/json" \
  -d '{"email":"maria@email.com","password":"senha-segura"}' | jq -r .accessToken)

# Criar pedido
curl -X POST localhost:8080/api/orders -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"items":[{"productId":1,"quantity":1},{"productId":3,"quantity":2}]}'
```

## Endpoints

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| POST | `/api/auth/register` | público | Cadastra cliente |
| POST | `/api/auth/login` | público | Retorna token JWT |
| GET | `/api/products?name=&page=&size=` | público | Lista produtos ativos |
| GET | `/api/products/{id}` | público | Detalha produto |
| POST | `/api/products` | ADMIN | Cadastra produto |
| PUT | `/api/products/{id}` | ADMIN | Atualiza produto |
| DELETE | `/api/products/{id}` | ADMIN | Desativa produto |
| POST | `/api/orders` | autenticado | Cria pedido |
| GET | `/api/orders/me` | autenticado | Pedidos do usuário |
| GET | `/api/orders/{id}` | dono ou ADMIN | Detalha pedido |
| POST | `/api/orders/{id}/cancel` | dono ou ADMIN | Cancela pedido |
| GET | `/api/orders?status=` | ADMIN | Lista todos os pedidos |
| PATCH | `/api/orders/{id}/status` | ADMIN | Altera status |

## Testes

```bash
./mvnw verify
```

- **Unitários**: regras do domínio (`OrderTest`) e do serviço com Mockito (`OrderServiceTest`)
- **Integração**: `OrderFlowIntegrationTest` sobe a aplicação inteira contra um PostgreSQL em container (Testcontainers), aplica as migrations e percorre o fluxo completo pela API: cadastro, login, criação de produto, pedido, pagamento, cancelamento e devolução de estoque, além de cenários de 401, 403, 400 e 422

O pipeline de CI no GitHub Actions executa todos os testes e o build da imagem Docker a cada push.

## Autor

**André Souza** · Desenvolvedor Java Back-end
[LinkedIn](https://www.linkedin.com/in/andre-souza-ti) · [GitHub](https://github.com/AndreSouzaTI)
