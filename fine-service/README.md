# Bibliotech Fine Service

Spring Boot 3.3.5 / Java 17 microservice for calculating and tracking late-return fines.

## Setup
1. Create the PostgreSQL database: `CREATE DATABASE fine_db;`
2. In `src/main/resources/application.properties`, set `DB_PASSWORD` to your PostgreSQL password, or provide it as an environment variable.
3. Import as **Maven > Existing Maven Projects** in STS.
4. Start Eureka on port 8761, then run `FineServiceApplication`.
5. Fine Service runs on port 8084 and registers as `FINE-SERVICE`.

## Calculate fine
`POST http://localhost:8084/fines/calculate`
```json
{
  "rentalId": 1,
  "userId": 1,
  "bookId": 1,
  "dueDate": "2026-10-20",
  "returnDate": "2026-10-24",
  "dailyRate": 5.00
}
```
This example creates a fine of 20.00 (4 overdue days × 5.00 per day). Dates are supplied by the caller; Rental Service integration is a separate step.

## Endpoints
- `POST /fines/calculate` - calculate and save a fine (one per rental)
- `GET /fines` - list all fines
- `GET /fines/{id}` - get fine by ID
- `GET /fines/rental/{rentalId}` - find a fine by rental ID
- `PATCH /fines/{id}/pay` - mark a fine as paid
- `DELETE /fines/{id}` - delete a fine

## Gateway
Add a route to the API Gateway:
```properties
spring.cloud.gateway.routes[3].id=fine-service
spring.cloud.gateway.routes[3].uri=lb://FINE-SERVICE
spring.cloud.gateway.routes[3].predicates[0]=Path=/fines/**
```
If route indexes 2 or 3 are already used, choose the next unused index.

## Notes
The calculation uses `max(0, days between due date and return date) * daily rate`. Authentication and role-based authorization should be enforced through the existing gateway/security configuration before production deployment. The service currently exposes its own REST API and is not yet automatically called by Rental Service on return.
