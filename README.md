# FIAP Car Sales — Plataforma de Revenda de Veículos

> Cadastre veículos, autentique compradores e efetive compras online — com arquitetura hexagonal, identidade apartada (Keycloak) e microsserviços Spring Boot.

[Account CI](https://github.com/AndersonLorena/fiap-fase-4-system/actions/workflows/account-ci.yml)
[CD GHCR](https://github.com/AndersonLorena/fiap-fase-4-system/actions/workflows/cd.yml)

**Repositório:** [github.com/AndersonLorena/fiap-fase-4-system](https://github.com/AndersonLorena/fiap-fase-4-system)

Este repositório contém a `account-api` e a infraestrutura compartilhada (Nginx, Keycloak, Redis, Garage, Postgres da conta, Prometheus e Grafana). A `dealership-api` fica em [fiap-fase-4-dealership](https://github.com/AndersonLorena/fiap-fase-4-dealership) e entra na mesma rede Docker depois deste stack. O acesso externo das duas APIs continua pelo Nginx deste repo, em `http://localhost/api/...`.

---



## O que faz

O FIAP Car Sales atende ao desafio da **FIAP PósTech SOAT — Fase 4**: uma plataforma de revenda de veículos na internet. O time de UX cuida do frontend; este repositório entrega a **account-api** e a **infraestrutura** compartilhada. Catálogo, listagens e compra ficam na `dealership-api`.


| Capacidade                    | Como                                                                                     |
| ----------------------------- | ---------------------------------------------------------------------------------------- |
| Cadastro e edição de veículos | Catálogo normalizado (marca, modelo, ano, cor) + `Car` com preço                         |
| Compra online                 | JWT `CUSTOMER` com perfil `VALIDATED` (CPF). A compra reserva o veículo em `AWAITING_PAYMENT` e devolve o código de pagamento |
| Confirmação de pagamento      | Webhook `POST /payments/{paymentCode}` autenticado pelo client `payment-processor`. `PAID` efetiva a venda; `CANCELLED` devolve o veículo à venda |
| Listagem à venda / vendidos   | `GET /cars?status=AVAILABLE` ou `SOLD`, ordenação `sort=price,asc`. `AWAITING_PAYMENT` fica fora das duas listas |
| Identidade apartada           | Keycloak (senhas/tokens) + `account-api` (perfil) separados de `dealership-api` (vendas) |
| Fotos do veículo              | Garage (S3-compatível) com upload intent / complete                                      |
| Concorrência na compra        | Lock Redis por veículo                                                                   |
| Recuperação de senha          | Código OTP por e-mail (Resend; opcional no Compose local)                                |
| Persistência                  | PostgreSQL por serviço (`fiapf3_account`, `fiapf3_dealership`, `keycloak`)               |
| Observabilidade               | Actuator + Micrometer → Prometheus → Grafana                                             |
| Qualidade                     | Testes de integração REST Assured por endpoint                                           |


---



## Arquitetura em resumo

```text
┌─────────────────┐     HTTP/REST       ┌──────────────────────────────────────┐
│ Cliente HTTP    │ ──────────────────► │ Nginx (única porta no host: 80)      │
│ (curl / Postman │                     │  /api/account/*    → account-api     │
│  / IDE)         │                     │  /api/dealership/* → dealership-api  │
└─────────────────┘                     │  /fiapf3-vehicle-photos/* → Garage   │
                                        │  keycloak.* / grafana.* → KC/Grafana │
                                        └───────────┬──────────────────────────┘
                                                    │
          ┌─────────────────────────────────────────┼──────────────────────────┐
          │                                         │                          │
          ▼                                         ▼                          ▼
┌──────────────────┐                     ┌──────────────────┐         ┌────────────────┐
│   account-api    │ ◄── S2S buyer ───── │  dealership-api  │ ──────► │ Garage (S3)    │
│  (perfil/auth)   │     validation      │ (catálogo/venda) │  fotos  │ vehicle-photos │
└────┬─────────┬───┘                     └───┬──────────┬───┘         └────────────────┘
     │         │                             │          │
     │         │        locks                │          │ JDBC
     │         └──────────┬──────────────────┘          ▼
     │                    ▼                    ┌──────────────────┐
     │ OIDC / Admin ┌──────────┐               │   PostgreSQL     │
     │     API      │  Redis   │               │ fiapf3_* + KC    │
     ▼              └──────────┘               └──────────────────┘
┌──────────────────┐
│    Keycloak      │
│ fiap-car-sales   │
└──────────────────┘
```

**Estilo de design:** Arquitetura Hexagonal · DDD · Object Calisthenics · OAuth2 Resource Server (JWT Keycloak) · locks distribuídos (Redis) · integração síncrona HTTP (sem mensageria)

Documentação completa: `[docs/3-arquitetura-proposta-v1.md](./docs/3-arquitetura-proposta-v1.md)`


| Diagrama / doc       | Arquivo                                                                                      |
| -------------------- | -------------------------------------------------------------------------------------------- |
| Brief do desafio     | `[docs/0-fiap-fase-4.md](./docs/0-fiap-fase-4.md)`                                           |
| Rascunho inicial     | `[docs/1-rascunho-de-arquitetura-inicial.png](./docs/1-rascunho-de-arquitetura-inicial.png)` |
| Arquitetura final    | `[docs/2-documento-de-arquitetura-final.png](./docs/2-documento-de-arquitetura-final.png)`   |
| Arquitetura proposta | `[docs/3-arquitetura-proposta-v1.md](./docs/3-arquitetura-proposta-v1.md)`                   |


---



## Stack


| Camada          | Tecnologia                                                        |
| --------------- | ----------------------------------------------------------------- |
| APIs            | Java 21, Spring Boot 4.1, OAuth2 Resource Server, Flyway          |
| Identidade      | Keycloak 26 (realm `fiap-car-sales`, roles `ADMIN` / `CUSTOMER`)  |
| Bancos          | PostgreSQL 16 (`fiapf3_account`, `fiapf3_dealership`, `keycloak`) |
| Locks           | Redis 7 (por conta / veículo; não é cache de sessão)              |
| Object storage  | Garage (S3-compatível) — bucket `fiapf3-vehicle-photos`           |
| E-mail          | Resend (recuperação de senha)                                     |
| Entrada HTTP    | Nginx 1.30 (reverse proxy)                                        |
| Observabilidade | Actuator + Micrometer, Prometheus 3, Grafana 12                   |
| Runtime         | Docker Compose (`services.dev.yml` / `services.prod.yml`)         |
| CI              | GitHub Actions (`account-ci`; `dealership-ci` no outro repositório) |
| CD              | GitHub Actions (`cd.yml`) — publish de imagens no GHCR            |

### Versões LTS usadas

Tags e versões validadas para rodar o projeto, alinhadas a [`infra/compose/services.dev.yml`](./infra/compose/services.dev.yml) e [`infra/compose/services.prod.yml`](./infra/compose/services.prod.yml). Quando o produto não tem LTS oficial, a tabela registra a versão pinada.

| Serviço | Imagem | Versão usada para rodar o projeto |
|---|---|---|
| `postgresql` | `postgres:16-alpine` | LTS: PostgreSQL 16 (suporte da comunidade até nov/2028; a tag acompanha 16.x) |
| `redis` | `redis:7-alpine` | LTS: Redis 7 (Open Source 7.x; 7.2 e 7.4 são as linhas extended/LTS) |
| `garage` | `dxflrs/garage:v2.3.0` | Sem LTS oficial: Garage v2.3.0 |
| `keycloak` | `quay.io/keycloak/keycloak:26.7.1` | Sem LTS da comunidade: Keycloak 26.7.1 |
| `account` | `fiapf3-account:dev` / GHCR | LTS: Eclipse Temurin Java 21 (runtime do Dockerfile; suporte Temurin até pelo menos dez/2029) |
| `dealership` | `fiapf3-dealership:dev` / GHCR (repositório `fiap-fase-4-dealership`) | LTS: Eclipse Temurin Java 21 (runtime do Dockerfile; suporte Temurin até pelo menos dez/2029) |
| `nginx` | `nginx:1.30.4-alpine` (digest pinado) | nginx 1.30 Alpine (ramo estável par) |
| `prometheus` | `prom/prometheus:v3.2.1` | Sem LTS oficial: Prometheus v3.2.1 |
| `grafana` | `grafana/grafana:12.4.8` | Sem LTS oficial: Grafana 12.4.8 (último minor da série 12; patch até mai/2027) |


---



## Estrutura do repositório

```text
fiap-fase-4-system/
├── backends/
│   └── account-api/         # Conta, auth, recovery, validate buyer S2S
├── infra/
│   ├── compose/             # Compose dev/prod + .env.example / .env.prod.example
│   └── confs/               # Nginx, Postgres, Keycloak, Garage, Prometheus, Grafana
├── docs/                    # Brief, arquitetura, Postman collection
└── .github/workflows/       # account-ci e CD (imagem fiapf3-account)
```

---



## Pré-requisitos

- [Docker](https://docs.docker.com/get-docker/) + Docker Compose v2
- (Opcional, para rodar na IDE / testes) Java 21
- (Opcional) Chave de API do Resend para e-mails de recuperação de senha
- Para o fluxo completo de veículos, o repositório [fiap-fase-4-dealership](https://github.com/AndersonLorena/fiap-fase-4-dealership) sobe **depois** deste

---



## Dependências e ordem para rodar

1. Docker e Docker Compose v2. Java 21 só é necessário para testes na IDE.
2. **Primeiro** este repositório. O Compose cria a rede `fiapf3-dev-net` (produção: `fiapf3-prod-net`) e sobe Nginx (única porta **80**), `account-api`, Postgres (`fiapf3_account` e `keycloak`), Redis, Garage, Keycloak, Prometheus e Grafana.
3. **Depois** `fiap-fase-4-dealership`. Ele entra nessa rede e sobe só a `dealership-api` e o Postgres `fiapf3_dealership` (hostname `dealership-postgresql`). Sem a rede deste stack, o Compose da dealership não sobe.
4. Os `.env` dos dois repositórios usam os mesmos `REDIS_PASSWORD`, chaves do Garage e `KEYCLOAK_DEALERSHIP_CLIENT_SECRET`.
5. O acesso externo é só pelo Nginx: `http://localhost/api/account/...` e `http://localhost/api/dealership/...`. Sem a dealership no ar, `/api/dealership/*` responde 502.
6. Para desligar: dealership primeiro, este stack depois. A dealership usa a rede criada aqui.

---



## Início rápido (Docker Compose — dev)

O Compose **dev** (`[infra/compose/services.dev.yml](./infra/compose/services.dev.yml)`, projeto `fiapf3-dev`) faz **build local** da imagem `fiapf3-account:dev` e sobe Postgres (`fiapf3_account` e `keycloak`), Redis, Garage, Keycloak, `account-api`, Nginx, Prometheus e Grafana. Ele cria a rede `fiapf3-dev-net`.

A `dealership-api` não sobe aqui. No repositório `fiap-fase-4-dealership`, suba o Compose depois deste: ele entra em `fiapf3-dev-net` e o Nginx passa a encaminhar `/api/dealership/*`. Enquanto esse container estiver ausente, essa rota responde 502.

O **Nginx** é a única entrada HTTP das APIs no host (porta **80**). Os containers internos (`account`, `dealership`, etc.) **não** publicam porta no host.

```bash
# 1. Clone
git clone git@github.com:AndersonLorena/fiap-fase-4-system.git
cd fiap-fase-4-system

# 2. Ambiente
cp infra/compose/.env.example infra/compose/.env
# Ajuste os secrets se necessário. Defina RESEND_API_KEY para habilitar e-mail.
# Repita REDIS_PASSWORD, GARAGE_* e KEYCLOAK_DEALERSHIP_CLIENT_SECRET no .env da dealership.

# 3. Subir account + infra (build local)
docker compose -f infra/compose/services.dev.yml --env-file infra/compose/.env up -d --build

# 4. No repositório fiap-fase-4-dealership, subir a dealership na mesma rede
#    (veja o README de lá)

# 5. Remover este stack (pare a dealership antes: ela usa fiapf3-dev-net)
docker compose -f infra/compose/services.dev.yml --env-file infra/compose/.env down -v
```

Smoke (com o stack no ar):

```bash
curl -sS http://localhost/api/dealership/v1/brands
```

Collection Postman (todas as rotas): `[docs/fiapf4.postman_collection.json](./docs/fiapf4.postman_collection.json)`.

Swagger UI (Authorize com o access token do login):

- Account: [http://localhost/api/account/swagger](http://localhost/api/account/swagger)
- Dealership: [http://localhost/api/dealership/swagger](http://localhost/api/dealership/swagger)

### Endpoints


| Serviço                      | URL                                                                                  |
| ---------------------------- | ------------------------------------------------------------------------------------ |
| account-api                  | [http://localhost/api/account/v1/…](http://localhost/api/account/v1/…)               |
| account-api Swagger          | [http://localhost/api/account/swagger](http://localhost/api/account/swagger)         |
| dealership-api               | [http://localhost/api/dealership/v1/…](http://localhost/api/dealership/v1/…)         |
| dealership-api Swagger       | [http://localhost/api/dealership/swagger](http://localhost/api/dealership/swagger)   |
| Fotos Garage (pré-assinadas) | [http://localhost/fiapf3-vehicle-photos/…](http://localhost/fiapf3-vehicle-photos/…) |
| Keycloak Admin Console       | [http://keycloak.localhost](http://keycloak.localhost) (host `keycloak.*` via Nginx) |
| Grafana                      | [http://grafana.localhost](http://grafana.localhost) (`admin` / `fiapf3` por padrão) |


> **Única porta publicada no host:** Nginx **80**. Keycloak Admin e Grafana entram por hosts `keycloak.*` / `grafana.*` (no macOS/Linux, `*.localhost` resolve para `127.0.0.1`). Prometheus e as apps permanecem internos. Comunicação entre serviços usa hostnames Docker (`account:8080`, `dealership:8080`, etc.).

> **HTTP-only:** o stack Compose não termina TLS na borda; o realm Keycloak usa `sslRequired: none` para este ambiente acadêmico. Apps Distroless não expõem healthcheck no Compose — o Nginx sobe com `service_started` e o cliente deve retry em 502 no boot.



### Grafana — dashboard JVM (Micrometer)

O Compose provisiona o dashboard **[JVM (Micrometer)](https://grafana.com/grafana/dashboards/4701-jvm-micrometer/)** (ID `4701`). Os labels Prometheus `application=account` / `application=dealership` alimentam o seletor.

**Já provisionado (recomendado):**

1. Abra [http://grafana.localhost](http://grafana.localhost) e faça login (`admin` / `fiapf3` por padrão).
2. Em **Dashboards** → pasta **FIAP Car Sales** → abra **JVM (Micrometer)**.
3. Escolha `application` = `account` ou `dealership` (e a `instance` correspondente).

Se o dashboard não aparecer após um `up` (volume Grafana antigo), recrie o serviço:

```bash
docker compose -f infra/compose/services.dev.yml --env-file infra/compose/.env up -d --force-recreate grafana
```

**Importação manual por ID (Grafana.com):**

1. No Grafana: **Dashboards** → **New** → **Import**.
2. Informe o ID `4701` → **Load**.
3. Selecione o datasource **Prometheus** → **Import**.

**Importação manual por arquivo JSON:**

1. **Dashboards** → **New** → **Import** → **Upload dashboard JSON file**.
2. Envie `[infra/confs/grafana/provisioning/dashboards/json/jvm-micrometer-4701.json](./infra/confs/grafana/provisioning/dashboards/json/jvm-micrometer-4701.json)`.
3. Confirme o datasource **Prometheus** → **Import**.



### Primeiro uso (fluxo E2E via HTTP)

Todos os exemplos passam pelo Nginx no host (`localhost`).

1. **Criar conta de comprador**

```bash
curl -sS -X POST http://localhost/api/account/v1/accounts \
  -H 'Content-Type: application/json' \
  -d '{"email":"buyer@example.com","password":"Secret123!","fullName":"Buyer One"}'
```

1. **Login** (tokens emitidos pelo Keycloak via account-api)

```bash
curl -sS -X POST http://localhost/api/account/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"buyer@example.com","password":"Secret123!"}'
# Guarde accessToken → BUYER_TOKEN
```

1. **Completar perfil** (`PENDING` → `VALIDATED` — CPF válido e telefone; obrigatório para comprar)

```bash
curl -sS -X POST http://localhost/api/account/v1/accounts/me/profile \
  -H "Authorization: Bearer $BUYER_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"document":"52998224725","phone":"11999998888"}'
```

1. **Login como admin** (bootstrap no profile Docker: `ACCOUNT_BOOTSTRAP_ADMIN_`*)

```bash
curl -sS -X POST http://localhost/api/account/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@fiap-car-sales.com","password":"Fiapf3Admin!"}'
# Guarde accessToken → ADMIN_TOKEN
```

1. **Cadastrar catálogo + veículo** (role `ADMIN`)

IDs são **Snowflake** (`BIGINT`). Use sempre o valor retornado no JSON (`brandId`, `modelId`, …) — não use literais `1`.

```bash
# Brand / model / color / year
BRAND_ID=$(curl -sS -X POST http://localhost/api/dealership/v1/brands \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' \
  -d '{"name":"Chevrolet"}' | jq -r .brandId)

MODEL_ID=$(curl -sS -X POST http://localhost/api/dealership/v1/models \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' \
  -d "{\"brandId\":$BRAND_ID,\"name\":\"Onix\"}" | jq -r .modelId)

COLOR_ID=$(curl -sS -X POST http://localhost/api/dealership/v1/colors \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' \
  -d '{"name":"Prata"}' | jq -r .colorId)

YEAR_ID=$(curl -sS -X POST http://localhost/api/dealership/v1/years \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' \
  -d '{"year":2024}' | jq -r .yearId)

CAR_ID=$(curl -sS -X POST http://localhost/api/dealership/v1/cars \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' \
  -d "{\"brandId\":$BRAND_ID,\"modelId\":$MODEL_ID,\"colorId\":$COLOR_ID,\"yearId\":$YEAR_ID,\"price\":75990.00}" \
  | jq -r .carId)
```

1. **Fotos (opcional, só com o veículo `AVAILABLE`)** — upload intent + PUT pré-assinado + complete

```bash
INTENT=$(curl -sS -X POST "http://localhost/api/dealership/v1/cars/$CAR_ID/photos/upload-intent" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
UPLOAD_URL=$(echo "$INTENT" | jq -r .uploadUrl)
OBJECT_KEY=$(echo "$INTENT" | jq -r .objectKey)
INTENT_TOKEN=$(echo "$INTENT" | jq -r .intentToken)

curl -sS -X PUT "$UPLOAD_URL" -H 'Content-Type: image/jpeg' --data-binary @./photo.jpg

curl -sS -X POST "http://localhost/api/dealership/v1/cars/$CAR_ID/photos/complete" \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' \
  -d "{\"objectKey\":\"$OBJECT_KEY\",\"intentToken\":\"$INTENT_TOKEN\",\"sortOrder\":0}"
```

1. **Listar à venda, comprar e confirmar o pagamento**

A compra de um `CUSTOMER` com perfil ativo reserva o veículo (`AWAITING_PAYMENT`) e devolve `paymentCode`. A data da venda só entra quando o processador confirma o pagamento. O client `payment-processor` já existe no realm (`fiapf3-payment-client-secret` no Compose de desenvolvimento).

```bash
curl -sS 'http://localhost/api/dealership/v1/cars?status=AVAILABLE&sort=price,asc'

PURCHASE=$(curl -sS -X POST "http://localhost/api/dealership/v1/cars/$CAR_ID/purchase" \
  -H "Authorization: Bearer $BUYER_TOKEN")
PAYMENT_CODE=$(echo "$PURCHASE" | jq -r .paymentCode)

PAYMENT_TOKEN=$(curl -sS -X POST http://keycloak.localhost/realms/fiap-car-sales/protocol/openid-connect/token \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'grant_type=client_credentials' \
  -d 'client_id=payment-processor' \
  -d 'client_secret=fiapf3-payment-client-secret' | jq -r .access_token)

curl -sS -X POST "http://localhost/api/dealership/v1/payments/$PAYMENT_CODE" \
  -H "Authorization: Bearer $PAYMENT_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"status":"PAID"}'

curl -sS 'http://localhost/api/dealership/v1/cars?status=SOLD&sort=price,asc'
```

`CANCELLED` no lugar de `PAID` devolve o veículo para `AVAILABLE`. Repetir `PAID` no mesmo código não altera a venda já efetivada.



### Parar / resetar

```bash
docker compose -f infra/compose/services.dev.yml --env-file infra/compose/.env down
# Adicione -v para apagar volumes deste stack (Postgres da account, Redis, Garage, Keycloak, Grafana, Prometheus).
# Pare a dealership antes: o container dela está na rede fiapf3-dev-net.
```

---



## Variáveis de ambiente

**Dev** — copie de `[infra/compose/.env.example](./infra/compose/.env.example)`:


| Variável                                                            | Finalidade                                                                          |
| ------------------------------------------------------------------- | ----------------------------------------------------------------------------------- |
| `UPLOAD_INTENT_SECRET`                                              | Segredo dos tokens de intent de upload de fotos (dealership)                        |
| `POSTGRES_PASSWORD`                                                 | Senha do Postgres (default local: `fiapf3`)                                         |
| `REDIS_PASSWORD`                                                    | Autenticação Redis para locks distribuídos                                          |
| `KEYCLOAK_ADMIN` / `KEYCLOAK_ADMIN_PASSWORD`                        | Admin console do Keycloak                                                           |
| `KEYCLOAK_URL`                                                      | Issuer/token interno (`http://keycloak:8080` no Compose)                            |
| `KEYCLOAK_ADMIN_URL`                                                | URL pública do Admin Console (`http://keycloak.localhost` no dev)                   |
| `KEYCLOAK_REALM`                                                    | Realm OIDC (`fiap-car-sales`)                                                       |
| `ACCOUNT_BOOTSTRAP_ADMIN_*`                                         | Conta admin de produto criada pela account-api no profile Docker                    |
| `KEYCLOAK_ACCOUNT_CLIENT_ID` / `SECRET`                             | Client confidencial `account-api`                                                   |
| `KEYCLOAK_DEALERSHIP_CLIENT_ID` / `SECRET`                          | Client confidencial `dealership-api`                                                |
| `KEYCLOAK_PAYMENT_CLIENT_ID` / `SECRET`                             | Client confidencial `payment-processor` (webhook de pagamento)                      |
| `ACCOUNT_API_URL`                                                   | Base S2S dealership → account (`http://account:8080`)                               |
| `ACCOUNT_S2S_ALLOWED_CLIENT_IDS`                                    | Clients Keycloak autorizados no buyer-validation (`dealership-api`)                 |
| `DEALERSHIP_PAYMENT_ALLOWED_CLIENT_IDS`                             | Clients autorizados no webhook (`payment-processor`)                                |
| `FRONTEND_ORIGIN`                                                   | Origem CORS (ex.: `http://localhost`)                                               |
| `PASSWORD_RECOVERY_URL`                                             | Link de recuperação nos e-mails                                                     |
| `GARAGE_ENDPOINT`                                                   | Endpoint S3 interno (`http://garage:3900`)                                          |
| `GARAGE_PUBLIC_ENDPOINT`                                            | Origem pública nas URLs pré-assinadas (`http://localhost` no dev, via Nginx)        |
| `GARAGE_ACCESS_KEY` / `GARAGE_SECRET_KEY` / `GARAGE_BUCKET`         | Credenciais S3 e bucket                                                             |
| `GARAGE_RPC_SECRET` / `GARAGE_ADMIN_TOKEN` / `GARAGE_METRICS_TOKEN` | Secrets do daemon Garage                                                            |
| `GF_SECURITY_ADMIN_USER` / `GF_SECURITY_ADMIN_PASSWORD`             | Login do Grafana                                                                    |
| `GF_SERVER_ROOT_URL`                                                | URL pública do Grafana via Nginx (`http://grafana.localhost/` no dev)               |
| `RESEND_API_KEY`                                                    | Opcional — deixe vazio para pular e-mail de saída                                   |
| `RESEND_FROM`                                                       | Remetente das notificações de recovery                                              |


> **Auth:** JWTs são emitidos pelo **Keycloak**. As APIs validam o issuer OIDC; senhas não ficam no Postgres da account-api.

**Prod (VPS)** — copie de `[infra/compose/.env.prod.example](./infra/compose/.env.prod.example)`. Além das variáveis acima:


| Variável     | Finalidade                                          |
| ------------ | --------------------------------------------------- |
| `GHCR_OWNER` | Owner lowercase no GHCR (ex.: `andersonlorena`)     |
| `IMAGE_TAG`  | Tag da imagem (`latest` ou SHA do commit publicado) |


Em prod, troque `YOUR_PUBLIC_HOST` em `FRONTEND_ORIGIN`, `PASSWORD_RECOVERY_URL`, `GARAGE_PUBLIC_ENDPOINT`, `KEYCLOAK_ADMIN_URL` e `GF_SERVER_ROOT_URL`.

---



## Desenvolvimento local (IDE)

O caminho suportado para o stack completo é o **Docker Compose** (seção acima). Postgres, Redis, Keycloak e as apps **não** publicam portas no host — só o Nginx (`80`).

Para `./mvnw spring-boot:run` na IDE, é preciso expor Postgres/Redis/Keycloak no Compose (ou rodar esses serviços à parte) e definir `DB_*`, `REDIS_*`, `KEYCLOAK_*` / profile `docker` conforme `application-docker.properties`. Sem isso, use apenas os testes de integração (`./mvnw verify`), que sobem H2 e stubs.

### Convenções HTTP

- **Not-found / delete sem corpo:** `204 No Content` (não `404`).
- **Fotos:** `POST /cars/{id}/photos/upload-intent` → PUT na URL pré-assinada (Garage via Nginx) → `POST .../photos/complete` com `objectKey`, `intentToken`, `sortOrder`.

---


## Testes, CI e CD

```bash
cd backends/account-api && ./mvnw verify
```

Os testes da `dealership-api` ficam no repositório `fiap-fase-4-dealership`.

Os testes de Web API sobem a aplicação em porta aleatória e exercitam os endpoints com **REST Assured** (contrato HTTP, validação, auth e fluxos de domínio).

### CI

O GitHub Actions (`account-ci`) roda em pull request para a `main` e no merge nessa branch: `./mvnw verify`, build local `fiapf3-account:ci` e validação dos Composes dev/prod — **sem** publicar no registry. O `dealership-ci` roda no outro repositório.

### CD (GHCR)

O workflow `[cd.yml](./.github/workflows/cd.yml)` publica Continuous Delivery **mínimo**: imagens versionadas no GitHub Container Registry após merge na `main` (ou via `workflow_dispatch`). **Não** faz deploy automático em VPS.

Imagem publicada por este repositório:

- `ghcr.io/<owner>/fiapf3-account:latest` e `:<sha>`

`ghcr.io/<owner>/fiapf3-dealership` é publicada pelo repositório `fiap-fase-4-dealership`.

Se os pacotes forem privados, autentique o Docker no host que for fazer pull:

```bash
echo "$GITHUB_TOKEN" | docker login ghcr.io -u YOUR_GITHUB_USER --password-stdin
```

(Opcional) torne os packages públicos em GitHub → Packages → Package settings → Change visibility, para permitir pull anônimo.

### Ambiente prod (VPS)

Use `[services.prod.yml](./infra/compose/services.prod.yml)` (`name: fiapf3-prod`): infra local + `account-api` **somente** via `pull` do GHCR (sem `build`). O `up` sobe Nginx na porta **80**. `/api/account/…` responde com este stack; `/api/dealership/…` responde depois do Compose de produção do repositório `fiap-fase-4-dealership` entrar na rede `fiapf3-prod-net`.

```bash
cp infra/compose/.env.prod.example infra/compose/.env.prod
# Preencha secrets reais e GHCR_OWNER / IMAGE_TAG

docker compose -f infra/compose/services.prod.yml --env-file infra/compose/.env.prod pull
docker compose -f infra/compose/services.prod.yml --env-file infra/compose/.env.prod up -d

# Remover tudo
docker compose -f infra/compose/services.prod.yml --env-file infra/compose/.env.prod down -v
```

Para fixar uma versão publicada pelo CD, defina `IMAGE_TAG=<sha-do-commit>` no `.env.prod` e rode `pull` + `up -d` de novo.

Deploy automático via SSH a partir do Actions permanece como evolução.

---



## Modelos de status



### Conta (`account-api`)


| Status      | Significado                                                    |
| ----------- | -------------------------------------------------------------- |
| `PENDING`   | Conta criada; perfil incompleto — **não** elegível para compra |
| `VALIDATED` | Perfil completo com CPF válido e telefone — elegível para compra |




### Veículo (`dealership-api`)


| Status               | Significado                                                                                          |
| -------------------- | ---------------------------------------------------------------------------------------------------- |
| `AVAILABLE`          | À venda; editável, removível e com foto por `ADMIN`                                                  |
| `AWAITING_PAYMENT`   | Compra iniciada; fora das listagens de à venda e de vendidos. Edição, exclusão e foto bloqueadas     |
| `SOLD`               | Pagamento confirmado; data da venda gravada. Imutável, exceto leitura                                |


A **dealership-api** não armazena senha nem o cadastro do comprador. Na compra, copia `buyerAccountId` e o CPF devolvido pela account-api, gera `paymentCode` e só grava `soldAt` quando o webhook confirma `PAID`.

---



## Principais rotas

Prefixos **públicos via Nginx:** `/api/account/…` e `/api/dealership/…` (o Nginx remove o segmento do serviço e encaminha para `/api/…` de cada app).

### account-api (`/api/v1`)


| Método | Rota                                       | Auth                      |
| ------ | ------------------------------------------ | ------------------------- |
| `POST` | `/accounts`                                | Público (cria `CUSTOMER`) |
| `GET`  | `/accounts/me`                             | JWT                       |
| `POST` | `/accounts/me/profile`                     | JWT                       |
| `POST` | `/accounts/password`                       | JWT                       |
| `POST` | `/accounts/password-recovery`              | Público                   |
| `POST` | `/accounts/password-recovery/confirm`      | Público                   |
| `POST` | `/auth/login`                              | Público                   |
| `POST` | `/auth/refresh`                            | Público                   |
| `GET`  | `/internal/accounts/{id}/buyer-validation` | S2S (client-credentials)  |




### dealership-api (`/api/v1`)


| Método                    | Rota                                            | Auth                               |
| ------------------------- | ----------------------------------------------- | ---------------------------------- |
| CRUD                      | `/brands`, `/models`, `/colors`, `/years`       | Leitura pública; escrita `ADMIN`   |
| `GET`                     | `/cars` (`status`, `q`, `page`, `size`, `sort`) | Público                            |
| `POST` / `PUT` / `DELETE` | `/cars`, `/cars/{id}`                           | `ADMIN`                            |
| `POST`                    | `/cars/{id}/purchase`                           | `CUSTOMER` + comprador `VALIDATED` |
| `POST`                    | `/payments/{paymentCode}`                       | Client `payment-processor`         |
| `POST`                    | `/cars/{id}/photos/upload-intent`               | `ADMIN`                            |
| `POST`                    | `/cars/{id}/photos/complete`                    | `ADMIN`                            |


---



## Banco de dados e recursos

O schema é de cada serviço via Flyway (`ddl-auto=validate`):

- Migrações account — `backends/account-api/src/main/resources/db/migration/`
- Migrações dealership — repositório `fiap-fase-4-dealership`, `backends/dealership-api/src/main/resources/db/migration/`
- Bootstrap do Postgres deste repo — `infra/confs/postgresql/initdb.d/` (`fiapf3_account` e `keycloak`)
- Realm Keycloak — `infra/confs/keycloak/import/fiap-car-sales-realm.json`
- Bootstrap do Garage — Compose (`--single-node --default-bucket` + `GARAGE_*` no `.env`)

Bancos lógicos: `fiapf3_account` e `keycloak` neste Postgres. `fiapf3_dealership` fica no Postgres do outro repositório.

---



## Checklist do desafio

Os itens de [docs/0-fiap-fase-4.md](./docs/0-fiap-fase-4.md) estão atendidos.


| Requisito | Status |
| --- | --- |
| Cadastrar veículo (marca, modelo, ano, cor, preço) | Atendido (`dealership-api`) |
| Editar dados do veículo | Atendido (`PUT /cars/{id}` se `AVAILABLE`) |
| Compra com CPF e data de venda | Atendido (perfil `VALIDATED` + compra em `AWAITING_PAYMENT`; `soldAt` no webhook `PAID`) |
| Webhook de pagamento (`PAID` / `CANCELLED`) | Atendido (`POST /payments/{paymentCode}` com client `payment-processor`) |
| Listagem à venda por preço (barato → caro) | Atendido (`status=AVAILABLE&sort=price,asc`) |
| Listagem vendidos por preço | Atendido (`status=SOLD&sort=price,asc`) |
| Serviço de venda isolado, com banco próprio | Atendido (repositório `fiap-fase-4-dealership`, Postgres `fiapf3_dealership`) |
| Demais funções em outro repositório e outro banco | Atendido (este repo: `account-api`, Postgres `fiapf3_account` + `keycloak`) |
| Comunicação só por HTTP, cada serviço no seu limite | Atendido (dealership chama account no validate buyer; a account não chama a dealership nem abre `fiapf3_dealership`) |
| Escalar a API de dealership conforme a carga | Atendido (serviço, imagem, Compose e banco isolados; ver abaixo) |
| CI/CD por repositório, gatilho no merge da `main` | Atendido (CI `./mvnw verify`; CD publica a imagem no GHCR). O `up` na VPS é o Compose de produção |
| Testes automatizados | Atendido (`./mvnw verify` + REST Assured no CI de cada API) |
| `README.md` (o que é, como usar, como testar) | Atendido (este arquivo) |
| CI/CD e deploy automatizado                                    | CI + CD (publish GHCR) atendidos; deploy na VPS é **manual** via `services.prod.yml` (sem SSH no Actions) |
| Campos e funcionalidades necessárias para atender os requisito | Atendido (campos e funcionalidades tanto necessários como incrementais foram cuidadosamente implementados) |
| Documentação de arquitetura                                    | `[docs/3-arquitetura-proposta-v1.md](./docs/3-arquitetura-proposta-v1.md)

Listagem e compra ficam na `dealership-api`, com Postgres próprio, imagem `fiapf3-dealership` e Compose próprio. A API é stateless (JWT e lock no Redis), então dá para subir mais réplicas de `fiapf3-dealership` na rede `fiapf3-dev-net` / `fiapf3-prod-net` sem escalar a account. O Nginx já encaminha `/api/dealership/*` para o hostname `dealership`. O Compose atual sobe uma réplica; o isolamento é o que permite crescer esse serviço à parte.


---



## Licença e contexto acadêmico

Projeto acadêmico da **FIAP — PósTech Software Architecture (SOAT), Fase 4** (plataforma de revenda de veículos).

Construído com Java 21, Spring Boot 4, Keycloak, PostgreSQL, Redis, Garage, Prometheus e Grafana (integração síncrona HTTP entre os serviços).