# Frontend Unit Testing Guide

## Overview
This document outlines the comprehensive unit test suite for the WanderWise frontend application.

## Test Structure

### Test Files Created:
1. **Components Tests**
   - `TripDetailsModal.test.jsx` - Tests for trip details modal functionality
   - `TripImage.test.jsx` - Tests for trip image component
   - `NotificationCenter.test.jsx` - Tests for notification center (already existed)

2. **Pages Tests**
   - `Home.test.jsx` - Tests for home page functionality
   - `MyTrips.test.jsx` - Tests for my trips page
   - `CreateTrip.test.jsx` - Tests for create trip page
   - `Login.test.jsx` - Tests for login page

3. **Context Tests**
   - `AuthContext.test.jsx` - Tests for authentication context

4. **Utility Tests**
   - `helpers.test.js` - Tests for utility functions
   - `api.test.js` - Tests for API service

5. **Setup Files**
   - `setupTests.js` - Global test configuration

## Test Coverage

### Components (4 test files)
- **TripDetailsModal**: 15 test cases
  - Rendering with different props
  - Modal open/close functionality
  - Data display and formatting
  - Error handling

- **TripImage**: 9 test cases  
  - Image loading and fallback handling
  - Props validation
  - Different fallback types

- **NotificationCenter**: 2 test cases (existing)
  - Notification display
  - User interaction

### Pages (4 test files)
- **Home**: 15 test cases
  - User authentication states
  - API data fetching
  - Navigation functionality
  - Error handling

- **MyTrips**: 18 test cases
  - Trip listing and management
  - Modal interactions
  - Delete functionality
  - Loading states

- **CreateTrip**: 15 test cases
  - Form validation
  - API integration
  - User input handling
  - Error states

- **Login**: 15 test cases
  - Form validation
  - Authentication flow
  - Error handling
  - User interaction

### Context (1 test file)
- **AuthContext**: 12 test cases
  - Authentication methods
  - State management
  - Error handling
  - Local storage integration

### Services & Utils (2 test files)
- **API Service**: 8 test cases
  - Axios configuration
  - Request/response interceptors
  - Error handling
  - Token management

- **Utility Functions**: 12 test cases
  - Email validation
  - Currency formatting
  - Date formatting
  - Helper functions

## Total Test Coverage
- **8 test files**
- **123+ individual test cases**
- **Comprehensive coverage of core functionality**

## Running Tests

### Run All Tests
```bash
npm test
```

### Run Tests in Watch Mode
```bash
npm test -- --watch
```

### Run Tests with Coverage
```bash
npm test -- --coverage
```

### Run Specific Test File
```bash
npm test -- TripDetailsModal.test.jsx
```

### Run Tests for Specific Pattern
```bash
npm test -- --testNamePattern="renders"
```

## Test Features

### Mocking Strategy
- **API calls**: Mocked using Jest
- **React Router**: Navigation functions mocked
- **Local Storage**: Fully mocked with Jest
- **External Components**: Mocked to isolate units
- **Browser APIs**: Mocked for consistent testing

### Test Types
- **Unit Tests**: Individual component/function testing
- **Integration Tests**: Component interaction testing
- **Error Handling**: Exception and edge case testing
- **User Interaction**: Event handling and user flow testing

### Assertions
- **Rendering**: Component presence and content
- **State Changes**: Component state updates
- **API Calls**: Correct API endpoints and parameters
- **Navigation**: Router navigation calls
- **Error States**: Error message display
- **Loading States**: Loading indicator behavior

## Best Practices Implemented

1. **Test Isolation**: Each test is independent
2. **Descriptive Names**: Clear test case descriptions
3. **Mock Reset**: Mocks cleared between tests
4. **Error Scenarios**: Both success and failure cases tested
5. **User Perspective**: Tests written from user's viewpoint
6. **Edge Cases**: Null, undefined, and invalid inputs handled
7. **Async Testing**: Proper handling of async operations

## Test Environment Setup

### Dependencies Required
- `@testing-library/react`
- `@testing-library/jest-dom`
- `@testing-library/user-event`
- `jest`
- `react-scripts` (includes Jest configuration)

### Configuration
- Tests use `setupTests.js` for global configuration
- Browser APIs are mocked for consistent testing
- Console methods are mocked to reduce noise
- LocalStorage and SessionStorage are mocked

## Maintenance

### Adding New Tests
1. Create test file in appropriate `__tests__` directory
2. Follow existing naming convention: `ComponentName.test.jsx`
3. Include comprehensive test cases for all functionality
4. Mock external dependencies
5. Test both success and error scenarios

### Updating Tests
1. Update tests when component functionality changes
2. Ensure mocks reflect actual API behavior
3. Keep test descriptions updated
4. Maintain consistent test structure

This comprehensive test suite ensures the WanderWise frontend application is well-tested, maintainable, and reliable.
