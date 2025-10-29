"# Event Service

A comprehensive Spring Boot microservice for managing environmental sustainability events, including event lifecycle management, attendee registration, QR code-based check-ins, and analytics.

## Features

- **Event Management**: Full CRUD operations for events (admin-only)
- **Event Discovery**: Public APIs for browsing and searching events
- **Attendee Management**: Registration, deregistration, and attendance tracking
- **QR Code Integration**: Generate and scan QR codes for event check-ins using ZXing
- **User-Event Relationships**: Track upcoming and past events per user
- **Analytics**: Event statistics (open events, upcoming events, participant counts)
- **Event Types**: Support for multiple event categories (Workshop, Tree Planting, Beach Cleanup, etc.)

## API Documentation

### Swagger/OpenAPI

Access the interactive API documentation:
- **Development**: http://localhost:8083/swagger-ui.html
- **API Docs JSON**: http://localhost:8083/v3/api-docs

## Test Coverage

Generate and view test coverage reports:

```bash
# Run tests with coverage
mvn clean test

# Generate Jacoco coverage report
mvn jacoco:report

# View report
# Open: target/site/jacoco/index.html
```

## Getting Started

### Prerequisites

- **Java 21** or higher
- **Maven 3.8+** or use included wrapper (`mvnw`)
- **PostgreSQL** database (Supabase recommended)

### Configuration

1. Copy `.env.example` to `.env`:
   ```bash
   cp .env.example .env
   ```

2. Update `.env` with your database credentials:
   ```properties
   DATABASE_URL=jdbc:postgresql://your-db-host:5432/your-database-name
   DATABASE_USERNAME=your-username
   DATABASE_PASSWORD=your-password
   ```

### Running the Service

```bash
# Using Maven wrapper
./mvnw spring-boot:run

# Or using installed Maven
mvn spring-boot:run
```

The service will start on port **8083**.

## Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=EventServiceTest

# Run tests with coverage
mvn clean test jacoco:report
```

## API Endpoints

### Event Query Endpoints (Public/User)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/events` | Get all events |
| GET | `/api/events/{id}` | Get event by ID |
| GET | `/api/events/upcoming/joined` | Get upcoming events for authenticated user |
| GET | `/api/events/past` | Get past events for authenticated user |
| GET | `/api/events/types` | Get all event types |

### Event Analytics Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/events/stats/open/total` | Total count of open events |
| GET | `/api/events/stats/upcoming/30days` | Upcoming events in next 30 days |
| GET | `/api/events/stats/open/participants` | Total participants in open events |

### Event Management Endpoints (Admin-Only)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/events` | Create new event |
| PUT | `/api/events/{id}` | Update event |
| DELETE | `/api/events/{id}` | Delete event |

### Attendee Management Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/events/{eventId}/attendees` | Register for event |
| GET | `/api/events/{eventId}/attendees` | Get all event attendees |
| GET | `/api/events/{eventId}/attendees/{userId}` | Get specific attendee |
| DELETE | `/api/events/{eventId}/attendees/{userId}` | Deregister from event |

### QR Code & Attendance Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/events/{eventId}/qr` | Generate/retrieve QR code |
| POST | `/api/events/scan` | Mark attendance via QR scan |

## Integration with Other Services

This service integrates with:
- **Gateway Service**: Receives authenticated user information via headers (`X-User-ID`, `X-User-Email`, `X-User-Role`)
- **User Service**: Validates user existence during registration
- **Gamification Service**: Triggers coin rewards when attendance is marked

## Technology Stack

- **Framework**: Spring Boot 3.5.6
- **Java Version**: 21
- **Build Tool**: Maven 3.9+
- **Database**: PostgreSQL (via Supabase)
- **ORM**: Hibernate/JPA
- **QR Code Generation**: ZXing 3.5.3
- **Testing**: JUnit 5, Mockito, AssertJ
- **Code Coverage**: Jacoco 0.8.12
- **API Documentation**: SpringDoc OpenAPI 2.7.0
- **Code Quality**: SpotBugs 4.8.6, Checkstyle 3.2.2

## Static Analysis

```bash
# Run SpotBugs analysis
mvn spotbugs:check

# Run Checkstyle (Google Java Style)
mvn checkstyle:check

# Generate Javadoc
mvn javadoc:javadoc
# View: target/site/apidocs/index.html
```

## Database Schema

The service uses schema `event_service` with tables:
- `events`: Event details (name, capacity, dates, QR tokens, etc.)
- `event_attendees`: Attendee registrations and attendance records

Key relationships:
- One-to-Many: Event → EventAttendees
- Cascade operations maintain referential integrity

## Architecture Notes

- **Service Layer Design**: EventService contains both command (create/update/delete) and query (get/list/analytics) operations
  - Previously split into EventService and EventQueryService, now merged for simpler API structure
  - Maintains separation of concerns at the repository layer
- **QR Code Strategy**: Unique tokens generated per event, refreshable by admins
- **Attendance Workflow**: Register → Event goes ONGOING → Scan QR → Attendance marked → Coins awarded
- **Connection Pooling**: HikariCP optimized for Supabase free-tier limits (max 2 connections)

## Contributing

1. Follow Google Java Style Guide
2. Add unit tests for new features
3. Ensure Jacoco coverage remains above 80%
4. Run `mvn clean verify` before committing
5. Add comprehensive Javadoc for public methods

## License

© 2024 GreenLoop Team. All rights reserved.
" 
