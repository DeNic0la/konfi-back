# Testing Guide

This document describes the comprehensive test suite for the Konfi application and how to run the tests.

## Test Architecture

The test suite follows a layered testing approach:

### 1. Unit Tests

- **BrunchServiceTest**: Tests the core business logic for DTO/Entity conversion and data mapping
- **AuthenticationServiceTest**: Tests the authentication service for brunch access control
- **BrunchTest**: Tests the entity model behavior

### 2. Integration Tests

- **BrunchRepositoryTest**: Tests JPA repository operations and database interactions
- **BrunchControllerTest**: Tests REST API endpoints with mocked services

### 3. End-to-End Tests

- **BrunchWorkflowIntegrationTest**: Tests complete workflows from HTTP request to database storage

## Test Configuration

### Test Profiles

- **test**: Uses H2 in-memory database for fast, isolated testing
- Test configuration in `src/test/resources/application-test.yaml`

### Test Dependencies

- **Spring Boot Test**: Comprehensive testing framework
- **JUnit 5**: Test execution framework
- **Mockito**: Mocking framework for isolation
- **AssertJ**: Fluent assertions for readable tests
- **Spring Security Test**: Security testing utilities
- **TestContainers**: Optional integration testing with real databases

### Test Utilities

- **TestDataFactory**: Builder pattern for creating test data
- **TestWebConfig**: Test-specific web configuration for CORS handling

## Running Tests

### All Tests

```bash
# Run all tests (skips JaCoCo coverage due to Java 24 compatibility)
./mvnw test -Djacoco.skip=true
```

### Specific Test Classes

```bash
# Run unit tests
./mvnw test -Djacoco.skip=true -Dtest=BrunchServiceTest
./mvnw test -Djacoco.skip=true -Dtest=AuthenticationServiceTest
./mvnw test -Djacoco.skip=true -Dtest=BrunchTest

# Run integration tests
./mvnw test -Djacoco.skip=true -Dtest=BrunchRepositoryTest
./mvnw test -Djacoco.skip=true -Dtest=BrunchControllerTest

# Run end-to-end tests
./mvnw test -Djacoco.skip=true -Dtest=BrunchWorkflowIntegrationTest

# Run basic application context test
./mvnw test -Djacoco.skip=true -Dtest=KonfiApplicationTests
```

### Quick Test Validation

```bash
# Run just the basic context loading test
./mvnw test -Djacoco.skip=true -Dtest=KonfiApplicationTests#contextLoads
```

## Test Data Management

### Test Data Factory

The `TestDataFactory` class provides builder patterns for creating test objects:

```java
// Creating test brunches
Brunch brunch = TestDataFactory.brunch()
    .withId("test-brunch")
    .withTitle("Test Brunch")
    .withEmailRequirement(true, ".*@company\\.com")
    .build();

// Creating test DTOs
BrunchCreateDTO dto = TestDataFactory.brunchCreateDTO()
    .withId("dto-brunch")
    .withPasswords("admin123", "vote123")
    .build();
```

### Database Cleanup

Tests use `@Transactional` and H2 in-memory database to ensure isolation:
- Each test gets a fresh database state
- No cleanup required between tests
- Fast execution with automatic rollback

## Test Best Practices Implemented

### 1. Independence

- Tests can run in any order
- Each test is isolated with its own data
- No shared state between tests

### 2. Descriptive Names

- Test methods use `@DisplayName` annotations
- Method names describe the behavior being tested
- Nested test classes group related scenarios

### 3. Comprehensive Coverage

- Happy path scenarios
- Error conditions and edge cases
- Boundary value testing
- Integration between components

### 4. Maintainable Structure

- Clear separation between Arrange, Act, Assert
- Test utilities for common operations
- Logical grouping with nested test classes

### 5. Fast and Reliable

- In-memory database for speed
- Minimal external dependencies
- Deterministic test outcomes

## Troubleshooting

### Common Issues

1. **JaCoCo Compatibility**: Use `-Djacoco.skip=true` for Java 24
2. **CORS Errors**: Integration tests use `TestWebConfig` for CORS handling
3. **Database Conflicts**: Tests use H2 in-memory DB to avoid conflicts

### Performance

- Individual test classes run in 3-6 seconds
- Full test suite completes in under 30 seconds
- Database operations are optimized with proper indexing

### Debugging

- Tests use `@SpringBootTest` for full context loading when needed
- Database queries are logged in test profile
- Detailed error messages with AssertJ assertions

