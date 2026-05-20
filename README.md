# Order Service

Spring Boot based Order Service with APIs for:
- Create order
- Update order
- Track order status
- Delete order
- Get all orders
- Search orders

It is configured for Neon PostgreSQL and includes Railway deployment files.

## Project Structure
```
src/main/java/com/ashi/orderservice/
├── config/              # Database and application configuration
├── controller/          # REST API endpoints
├── dto/                 # Request and Response DTOs
├── entity/              # JPA entities (Order, OrderStatus)
├── exception/           # Exception handlers and custom exceptions
├── repository/          # Spring Data JPA repositories
├── service/             # Business logic (interface + implementation)
└── OrderServiceApplication.java
```

## Tech Stack
- Java 17
- Spring Boot 3
- Spring Web + Spring Data JPA
- PostgreSQL (Neon)
- JUnit 5 + Mockito + MockMvc

## API Endpoints
- `POST /api/orders`
- `PUT /api/orders/{id}`
- `GET /api/orders/{id}/status`
- `DELETE /api/orders/{id}`
- `GET /api/orders`
- `GET /api/orders/search?query=<text>&status=<ORDER_STATUS>`

### Sample Create Payload
```json
{
  "customerName": "Ashish",
  "productName": "Keyboard",
  "quantity": 1,
  "totalAmount": 59.99
}
```

## Neon Setup
Use this connection string as `DATABASE_URL`:

`postgresql://neondb_owner:npg_pWBbCX1w3OGf@ep-wild-breeze-aqmuxnv3-pooler.c-8.us-east-1.aws.neon.tech/neondb?sslmode=require&channel_binding=require`

The app auto-parses `DATABASE_URL` and builds the JDBC datasource.

## Local Run
```bash
cd /Users/ashisha2/Desktop/backend-learning/order-service
export DATABASE_URL='postgresql://neondb_owner:npg_pWBbCX1w3OGf@ep-wild-breeze-aqmuxnv3-pooler.c-8.us-east-1.aws.neon.tech/neondb?sslmode=require&channel_binding=require'
mvn spring-boot:run
```

## Test
```bash
mvn test
```

## Railway Deployment
Files added:
- `Procfile`
- `railway.json`

Set these variables in Railway:
- `DATABASE_URL` (Neon connection string)
- `PORT` (Railway usually injects automatically)

Railway build command (default Nixpacks):
```bash
mvn -DskipTests clean package
```

Start command:
```bash
java -Dserver.port=$PORT -jar target/order-service-0.0.1-SNAPSHOT.jar
```

