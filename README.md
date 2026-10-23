# Flight Booking API

A Spring Boot REST API for managing flights and seats, selecting seats through a Kafka-serialized booking flow, and paying via the iyzipay sandbox.

Built as a take-home case study (2024).

## Features

- Flight CRUD: create, update, delete, get by id, and list all flights with their seats and available seat count.
- Seat CRUD: create, update, delete and get seats belonging to a flight.
- Seat selection: a buyer reserves a seat before paying; concurrent requests for the same seat are serialized through Kafka.
- Payment: pays for the selected seats through the iyzipay sandbox and releases them again if the payment fails.
- Global exception handler that maps domain exceptions (not found, seat not available, payment not accepted) to JSON error responses with matching HTTP status codes.

## Architecture

Seat selection requests are not written to the database directly by the controller. Instead, the controller publishes each request to the `seat-topic` Kafka topic, which has a single partition. A single consumer (`KafkaListeners`, with `spring.kafka.listener.concurrency: 1`) reads the messages in order and performs the availability check and the update. As a result, simultaneous requests for the same seat are processed one after another, so only the first one succeeds and the others are rejected as "seat not available".

Code layout: `controller` (REST endpoints), `service` (business logic and the iyzipay integration), `repository` (Spring Data JPA), `entity` / `dto` / `converter` (persistence model and manual mapping), `exception` (custom exceptions and the global handler).

## Tech stack

- Java 11
- Spring Boot 2.7.3: Spring Web, Spring Data JPA (Hibernate 5.6.10)
- Spring Kafka 2.8.8 with kafka-clients 3.1.1
- Apache Kafka 4.3.1 (official `apache/kafka` image, KRaft mode) via Docker Compose
- H2 2.1.214 (file-based database)
- iyzipay-java 2.0.119
- JUnit 4.12 (run on the JUnit Platform through the JUnit Vintage engine), Mockito 4.5.1
- Maven 3.9.9 (via the Maven wrapper)

> **Note:** Spring Boot 2.7 and Java 11 are no longer current (Spring Boot 2.7 is past the end of its open-source support). The project intentionally stays on the versions it was written with.

## Running locally

Prerequisites: a JDK (11 or newer), Docker, and an [iyzico sandbox](https://sandbox-merchant.iyzipay.com/) account.

1. Start Kafka:

   ```bash
   docker compose up -d
   ```

   The application creates `seat-topic` itself on startup (`KafkaTopicConfig`), so no manual topic setup is needed.

2. Provide your iyzico sandbox credentials. Use the API key and secret key from your own sandbox merchant account. Credentials are not stored in the repository.

   ```bash
   # bash / zsh
   export IYZIPAY_API_KEY=<your-sandbox-api-key>
   export IYZIPAY_SECRET_KEY=<your-sandbox-secret-key>
   # optional, defaults to https://sandbox-api.iyzipay.com
   export IYZIPAY_BASE_URL=https://sandbox-api.iyzipay.com
   ```

   ```powershell
   # PowerShell
   $env:IYZIPAY_API_KEY = "<your-sandbox-api-key>"
   $env:IYZIPAY_SECRET_KEY = "<your-sandbox-secret-key>"
   ```

   The application does not start if `IYZIPAY_API_KEY` or `IYZIPAY_SECRET_KEY` is missing.

3. Run the application (on Windows use `.\mvnw.cmd`):

   ```bash
   ./mvnw spring-boot:run
   ```

   The API listens on `http://localhost:8080`.

4. H2 console: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:file:./data/demo`, user `root`, password `pass`). The schema is recreated on every start (`ddl-auto: create-drop`).

## API

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/flight/af` | Create a flight (`name`, `description`, `price`) |
| POST | `/api/v1/flight/uf/{id}` | Update a flight |
| DELETE | `/api/v1/flight/df/{id}` | Delete a flight |
| GET | `/api/v1/flight/gf/{id}` | Get a flight with its seats |
| GET | `/api/v1/flight/gaf` | List all flights with seats and available seat count |
| POST | `/api/v1/seat/as` | Create a seat (`flight_id`, `seatNumber`) |
| POST | `/api/v1/seat/us/{id}` | Update a seat |
| DELETE | `/api/v1/seat/ds/{id}` | Delete a seat |
| GET | `/api/v1/seat/gs/{id}` | Get a seat |
| POST | `/api/v1/main/ss` | Select a seat for a buyer (`seat_id`, `buyer_id`), processed through Kafka |
| POST | `/api/v1/main/mp` | Pay for selected seats via iyzipay (`seat_ids`, `buyer`, `address`, `paymentCard`, `baskedId`) |

Create a flight and a seat:

```bash
curl -X POST http://localhost:8080/api/v1/flight/af \
  -H "Content-Type: application/json" \
  -d '{"name":"FB101","description":"Istanbul to Ankara","price":100}'

curl -X POST http://localhost:8080/api/v1/seat/as \
  -H "Content-Type: application/json" \
  -d '{"flight_id":1,"seatNumber":1}'
```

Select the seat:

```bash
curl -X POST http://localhost:8080/api/v1/main/ss \
  -H "Content-Type: application/json" \
  -d '{"seat_id":1,"buyer_id":"buyer1"}'
```

Pay for it with an iyzico sandbox test card:

```bash
curl -X POST http://localhost:8080/api/v1/main/mp \
  -H "Content-Type: application/json" \
  -d '{
    "seat_ids": [1],
    "baskedId": "B123",
    "buyer": {"id": "buyer1", "name": "John", "surname": "Doe", "gsmNumber": "+905350000000",
              "email": "john.doe@example.com", "identityNumber": "74300864791",
              "registrationAddress": "Nidakule Goztepe, Merdivenkoy Mah. Bora Sok. No:1",
              "ip": "85.34.78.112", "city": "Istanbul", "country": "Turkey", "zipCode": "34732"},
    "address": {"contactName": "John Doe", "city": "Istanbul", "country": "Turkey",
                "address": "Nidakule Goztepe, Merdivenkoy Mah. Bora Sok. No:1", "zipCode": "34742"},
    "paymentCard": {"cardHolderName": "John Doe", "cardNumber": "5528790000000008",
                    "expireMonth": "12", "expireYear": "2030", "cvc": "123", "registerCard": 0}
  }'
```

## Tests

- `FlightServiceTest` (7 tests) and `SeatServiceTest` (8 tests): unit tests for the service layer, with mocked repositories and converters (Mockito). They cover saving, lookup, deletion, available seat counting, seat selection and releasing seats.
- `IyzipayPaymentServiceTest` (4 tests): integration tests that start the full Spring context. Each test creates its own flight and seats.
  - Concurrent selection: two buyers request the same seat through Kafka. The test waits for the listener to process both messages (a `CountDownLatch` with a timeout) and checks that the seat is taken by one of them and the other got a `SeatNotAvailableException`. `SeatService` is a `@SpyBean`, which records the outcome of each request.
  - Payments against the iyzipay sandbox: a successful payment keeps the seats for the buyer, a payment by a buyer who does not hold the seats is rejected and leaves them as they were, and a payment that iyzipay rejects (a required buyer field is missing) releases the seats again.

Run them with Kafka running and the iyzipay environment variables set as described above:

```bash
docker compose up -d
./mvnw test
```

Result: 19 tests, all passing. The integration tests need Kafka and the iyzipay variables, and the payment tests call the iyzipay sandbox over the network.

## Known limitations

- Seat selection is serialized through a single Kafka consumer. There is no database-level locking (no optimistic locking and no unique constraint), so correctness depends on running exactly one consumer instance.
- The booking result is not returned to the HTTP caller synchronously. The listener stores the outcome in shared state, which the controller reads right after publishing the message, so a response can reflect the outcome of an earlier request.
- Endpoint paths are abbreviated and not resource-oriented (for example `/api/v1/flight/af`, and `POST` for updates). A production API would use resource paths with `PUT`/`PATCH`.
- A production version would use optimistic locking (`@Version`) and return the booking result synchronously.
