# Banking System API

REST API for a simple banking system, built with Spring Boot as a portfolio project, with one SOAP web service for recipient verification.
Clients register, open accounts and send transfers. Bank staff (`ADMIN`) handle cash deposits and withdrawals, block accounts and manage customers.

## Tech stack

Java 21 · Spring Boot 4 · Spring Security + JWT · Spring Data JPA / Hibernate · Apache CXF (SOAP, JAX-WS) · PostgreSQL 16 · Docker Compose · JUnit 6 · Mockito · Testcontainers · SpringDoc OpenAPI (Swagger UI)

## Features

- Registration and login with JWT, roles `USER` and `ADMIN`
- Clients open accounts for themselves and see only their own accounts and transaction history
- Transfers between accounts, with balance and blocked-account checks
- Recipient verification: the client sends an account number and the recipient's name and gets back only `MATCH` or `NO_MATCH`, over REST or SOAP
- Cash deposits and withdrawals, done only by bank staff
- Admin: customer management, blocking and unblocking accounts, bank statistics

## Running locally

Requirements: JDK 21 and Docker.

1. Create a `.env` file in the project root (it is in `.gitignore`):

   ```properties
   DB_PASSWORD=choose_a_password
   JWT_SECRET=any_random_string_of_at_least_32_characters
   ```

2. Start PostgreSQL (it runs on port **5433**):

   ```bash
   docker compose up -d
   ```

3. Start the application:

   ```bash
   ./mvnw spring-boot:run
   ```

   On Windows use `mvnw.cmd spring-boot:run`.

4. Open Swagger UI: http://localhost:8080/swagger-ui.html

Registration always creates a `USER`. To get an admin account, register a user and then change the role in the database:

```bash
docker exec -it bankdb psql -U postgres -d bankdb -c "UPDATE users SET role = 'ADMIN' WHERE username = 'your_username';"
```

## Main endpoints

| Method | Endpoint                                | Access                      | Description                                 |
|--------|-----------------------------------------|-----------------------------|---------------------------------------------|
| POST   | `/api/auth/register`                    | public                      | Create a user and customer                  |
| POST   | `/api/auth/login`                       | public                      | Returns a JWT                               |
| GET    | `/api/accounts/my`                      | logged in                   | The client's own accounts                   |
| POST   | `/api/accounts`                         | logged in                   | Open an account for the logged-in client    |
| GET    | `/api/accounts/{id}/transactions`       | account owner               | Transaction history                         |
| PUT    | `/api/accounts/transfer`                | owner of the source account | Transfer money                              |
| POST   | `/api/accounts/verify-recipient`        | logged in                   | Check recipient name against account number |
| PUT    | `/api/accounts/{id}/deposit`, `/withdraw` | ADMIN                       | Cash operations                             |
| PUT    | `/api/accounts/{id}/block`, `/unblock`  | ADMIN                       | Block or unblock an account                 |
| *      | `/api/customers/**`                     | ADMIN                       | Customer management (except `/me`)          |
| GET    | `/api/transactions`                     | ADMIN                       | All transactions in the bank                |
| GET    | `/api/stats`, `/api/stats/sum`          | ADMIN                       | Bank statistics and total balance           |
| POST   | `/ws/payee-verification`                | logged in                   | Recipient verification over SOAP            |

The full REST list is in Swagger UI. The SOAP service is described by its WSDL at `/ws/payee-verification?wsdl` (it also needs a JWT).

## Design decisions

- **Recipient verification instead of account lookup.** The old `GET /api/accounts/number/{number}` returned the balance and owner of any account. It was replaced with a check that returns only `MATCH` or `NO_MATCH`, similar to the EU Verification of Payee. A non-existent account also returns `NO_MATCH`, so the endpoint can't be used to find out which account numbers exist. Names are compared ignoring case and extra spaces.
- **IDOR fixes.** Account operations check that the account belongs to the logged-in user. When a client opens an account, the customer is taken from the JWT, not from the request, so nobody can open an account for someone else.
- **Cash operations are staff-only.** In a real bank, deposits and withdrawals happen at a branch or a cash machine, so the client API doesn't allow them.
- **Money is `BigDecimal`**, never `double`.
- **Enums are stored as `STRING`.** I found a bug where a field was stored with `ORDINAL`: changing the order of enum values would silently change the meaning of existing rows. I fixed the mapping and migrated the existing data.
- **Invalid JWT returns 401, not 500.** Expired or malformed tokens are handled in the filter and return a JSON 401 response.
- **DTOs instead of entities.** Entities are never sent to the client: responses are built from DTO records, and JSON request bodies are records validated with Bean Validation.
- **Package by feature:** `account`, `customer`, `transaction`, `user`, `security`.
- **One service, two entry points.** Recipient verification is available over REST and SOAP (Apache CXF, code-first). The REST controller and the SOAP endpoint only translate the input and call the same `AccountService.verifyRecipient()`, so the business logic doesn't depend on the protocol.
- **SOAP errors.** Bean Validation runs only for REST, so the SOAP endpoint checks blank fields itself and returns a SOAP Fault with the code `Client`. A missing or invalid JWT is rejected before the request reaches CXF, so it gets the same JSON 401 as REST. This is a deliberate simplification: a real partner integration would use WS-Security or mutual TLS.

## Tests

- Unit tests for services and the SOAP endpoint (JUnit 6, Mockito)
- Web layer tests with `@WebMvcTest`, including security rules (401 and 403 for each role)
- Integration tests on a real PostgreSQL with Testcontainers, with a shared base class so the Spring context is cached between test classes

Run all tests (Docker must be running):

```bash
./mvnw test
```

## Status and roadmap

The project is in active development. Next steps:
- [ ] Integration test for the SOAP endpoint with a client generated from the WSDL
- [ ] Concurrent transfers: integration test that reproduces a lost balance update (in progress), then optimistic locking (`@Version`) to fix it
- [ ] Account numbers that can't be guessed (currently based on a timestamp)
- [ ] CI with GitHub Actions