# Bibliotech Rental Service

Spring Boot 3.3.5 / Java 17 service for creating rentals, preventing repeat active rentals by the same user/book, tracking issue and due dates, returning books, and updating Book Service inventory through Eureka + Spring Cloud LoadBalancer.

## Before running
1. Create PostgreSQL database `rental_db`.
2. Set `DB_PASSWORD` to your local PostgreSQL password (or change the default in `application.properties`).
3. Start Eureka on port 8761, Book Service, and then Rental Service.
4. Add the `RENTAL-SERVICE` route to API Gateway: `lb://RENTAL-SERVICE`, predicate `Path=/rentals/**`.
5. Ensure Auth/Gateway's `X-User-Id`, `X-Username`, and `X-Role` headers are added only after JWT validation. Do not call Rental Service directly from an untrusted client because these headers are trusted internally.

Default port: 8083. A second instance can be run on 8084 with `--server.port=8084`; both instances must use the same `rental_db`.

## API examples (through API Gateway)
- `POST http://localhost:8080/rentals/borrow` with Bearer token and JSON `{"bookId":1,"dueDate":"2026-11-10"}`
- `GET http://localhost:8080/rentals`
- `GET http://localhost:8080/rentals/{id}`
- `PUT http://localhost:8080/rentals/{id}/return`
- `PATCH http://localhost:8080/rentals/{id}/due-date` (ADMIN/LIBRARIAN) JSON `{"dueDate":"2026-11-15"}`
- `DELETE http://localhost:8080/rentals/{id}` (ADMIN/LIBRARIAN; returned rentals only)

Note: Fine Service integration and scheduled overdue notifications are the next stage. For production-grade consistency, borrowing/returning across databases should use retries/idempotency or a saga/outbox pattern; this starter performs a compensating inventory update if rental persistence fails.
