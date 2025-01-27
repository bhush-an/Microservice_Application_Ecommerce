# Microservice_Application_Ecommerce

This project leverages Docker, Resilience4j, and microservices to create a scalable and fault-tolerant system:

1. **Database Management:**
   - **Docker Compose** is used to configure and manage a MySQL database container.

2. **Microservices:**
   - **Inventory Service:** Handles CRUD operations for inventory data.
   - **Product Service:** Manages CRUD operations for product data.
   - **Order Service:** Facilitates CRUD operations for order management.
   - **Payment Service:** Integrates with the **Stripe payment gateway** for secure payments.
   - **Notification Service:** Utilizes **Kafka** for high-throughput event-driven notifications.
   - **User Service:** Manages user registration and authentication.

3. **Resilience and Communication:**
   - **Circuit Breaker:** Implemented using **Resilience4j** to handle failures gracefully.

4. **Service Discovery:**
   - **Eureka Server:** Enables dynamic service discovery for seamless inter-service communication.

5. **API Gateway:**
   - Centralized gateway for:
     - User authentication.
     - Logging and monitoring.
     - Routing and fallback mechanisms.
