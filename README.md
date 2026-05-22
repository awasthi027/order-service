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
- `GET /api/orders/search?query=<text>&status=<ORDER_STATUS>&page=0&size=10`

### Sample Create Payload
```json
{
  "customerName": "Ashish",
  "address": "Bangalore, India",
  "paymentType": "UPI",
  "products": [
    {
      "productId": "d3e4f262-6af5-4e8c-932f-b6c604f76295",
      "productName": "Keyboard",
      "price": 59.99
    },
    {
      "productId": "40ad8af8-2e8e-48e4-9d8e-2946f333f640",
      "productName": "Mouse",
      "price": 19.99
    }
  ]
}
```

`totalAmount` is calculated by the service from product prices.

Update rules:
- Order can be updated only when current status is `CREATED` or `PROCESSING`.
- Update request `status` accepts only `CREATED` or `PROCESSING`.
- Product list can be replaced during update while order is editable.

Search rules:
- `query` and `status` are optional.
- If both are absent/blank, API returns all orders in paginated format.

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

## Docker (optimized image)
This project includes a multi-stage `Dockerfile` to reduce image size and runtime memory usage.

Build image:
```bash
docker build -t order-service:latest .
```

Run container:
```bash
docker run --rm -p 8080:8080 \
  -e PORT=8080 \
  -e DATABASE_URL='postgresql://neondb_owner:npg_pWBbCX1w3OGf@ep-wild-breeze-aqmuxnv3-pooler.c-8.us-east-1.aws.neon.tech/neondb?sslmode=require&channel_binding=require' \
  order-service:latest
```

Optional Docker cleanup commands:
```bash
docker image prune -f
docker builder prune -f
docker system df
```

## Railway Deployment
Files added:
- `Dockerfile`
- `railway.json`
- `.dockerignore`

Set these variables in Railway:
- `DATABASE_URL` (Neon connection string)
- `PORT` (Railway usually injects automatically)

Railway uses the Dockerfile build path configured in `railway.json`.

