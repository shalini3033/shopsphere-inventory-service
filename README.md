# ShopSphere — Inventory Service

A Spring Boot microservice that manages product inventory, stock updates, and stock reservations for the ShopSphere e-commerce application.

## Features

- Create inventory records for products.
- Retrieve inventory for a specific product or list all inventory records.
- Update available stock quantities.
- Reserve stock by moving quantities from available stock to reserved stock.
- Validate stock quantities and reject reservations when stock is insufficient.
- Use PostgreSQL for persistent inventory storage.
- Use pessimistic database locking to protect stock reservations from concurrent updates.
- Handle errors through a centralized exception handler.
- Include automated unit, controller, application-context, and concurrency tests.
- Run the service using Docker and Docker Compose.
- Use GitHub Actions for automated build and test verification.

## Tech Stack

- Java 17
- Spring Boot
- Spring Data JPA and Hibernate
- PostgreSQL
- Maven
- JUnit and Mockito
- Docker and Docker Compose
- GitHub Actions

## API Endpoints

Base URL: `http://localhost:8082` when using the configured Docker port mapping.

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/inventory` | Create inventory for a product |
| GET | `/api/inventory` | Retrieve all inventory records |
| GET | `/api/inventory/{productId}` | Retrieve inventory for a product |
| PUT | `/api/inventory/{productId}/stock?quantity=20` | Update available stock |
| POST | `/api/inventory/{productId}/reserve?quantity=2` | Reserve stock |

### Example: Create Inventory

**Request**

`POST /api/inventory`

```json
{
  "productId": 101,
  "availableQuantity": 20
}
```

The service initializes the reserved quantity to zero.

### Example: Reserve Stock

`POST /api/inventory/101/reserve?quantity=2`

A successful reservation decreases available stock by two and increases reserved stock by two.

For example:

| Field | Before | After |
|---|---:|---:|
| Available quantity | 20 | 18 |
| Reserved quantity | 0 | 2 |

## Concurrency Handling

The service uses pessimistic database locking when updating stock and reserving inventory.

When multiple requests attempt to reserve stock for the same product simultaneously, the database lock serializes access to that inventory row. Each transaction checks the current available quantity before completing the reservation.

This helps prevent overselling due to concurrent reservations.

A concurrency test verifies that, with five units available, two simultaneous requests to reserve four units each cannot both succeed.

## Running the Project

### Prerequisites

- Java 17
- Maven, or the project's Maven Wrapper if available
- PostgreSQL for local execution, or Docker and Docker Compose
- Git

### Run with Docker Compose

From the project root, run:

```bash
docker compose up --build
```

Use the API base URL:

`http://localhost:8082`

This assumes the configured Compose file maps host port `8082` to the application's container port `8081`.

Stop the containers with:

```bash
docker compose down
```

### Run Locally

Configure the PostgreSQL connection in `src/main/resources/application.properties`, ensuring the database exists and the credentials match your local setup.

Build and test the project:

```bash
mvn clean verify
```

Run the application from your IDE, or execute the generated Spring Boot JAR:

```bash
java -jar target/inventory_service-0.0.1-SNAPSHOT.jar
```

When running directly on your machine, the configured application port is `8081`.

## Testing

Run the complete test suite with:

```bash
mvn clean verify
```

The tests cover inventory service behavior, controller endpoints, application startup, and concurrent stock reservations.

## Continuous Integration

GitHub Actions automatically builds and tests the project on configured pushes and pull requests to the `main` and `master` branches.

The CI workflow provisions PostgreSQL for database-dependent tests and runs the Maven verification lifecycle.

## Project Goals

This service is part of ShopSphere, a learning project focused on building an e-commerce application using microservices and production-oriented backend engineering practices.

The project explores REST APIs, database integration, automated testing, concurrency control, containerization, and continuous integration.

## Author

Developed as part of the ShopSphere microservices project.
