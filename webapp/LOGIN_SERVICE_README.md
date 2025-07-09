# Login Service Implementation

This document describes the login service implementation for the JobFitEngine AI webapp.

## Overview

The login service has been implemented to handle user authentication by making HTTP requests to the backend API endpoint.

## API Endpoint

- **URL**: `http://localhost:8080/auth/login`
- **Method**: POST
- **Content-Type**: application/json

### Request Body
```json
{
  "email": "user@example.com",
  "password": "userpassword"
}
```

### Expected Response
```json
{
  "success": true,
  "message": "Login successful",
  "token": "jwt-token-here",
  "user": {
    "id": "user-id",
    "email": "user@example.com",
    "name": "User Name"
  }
}
```

## Implementation Details

### AuthServiceService (`src/app/services/auth.service.service.ts`)

The main authentication service that handles:

- **login(email, password)**: Sends POST request to login endpoint
- **logout()**: Clears stored authentication data
- **isAuthenticated()**: Checks if user is currently authenticated
- **getToken()**: Retrieves stored JWT token
- **getUser()**: Retrieves stored user information
- **isTokenExpired()**: Checks if JWT token has expired

### LoginComponent (`src/app/auth/login/login.component.ts`)

The login component that:

- Manages the login form with validation
- Handles form submission
- Displays loading states and error messages
- Navigates to profile page on successful login

### AuthInterceptor (`src/app/services/auth.interceptor.ts`)

HTTP interceptor that:

- Automatically adds Authorization header with JWT token to requests
- Handles 401 Unauthorized responses by logging out user
- Redirects to login page when authentication fails

## Features

1. **Form Validation**: Email and password validation with error messages
2. **Loading States**: Visual feedback during login process
3. **Error Handling**: Comprehensive error handling for different scenarios
4. **Token Management**: Automatic storage and retrieval of JWT tokens
5. **Authentication State**: Reactive authentication state management
6. **Security**: Automatic token expiration checking

## Usage

### Basic Login
```typescript
// In a component
constructor(private authService: AuthServiceService) {}

login(email: string, password: string) {
  this.authService.login(email, password).subscribe({
    next: (response) => {
      if (response.success) {
        // Login successful
        console.log('User logged in:', response.user);
      }
    },
    error: (error) => {
      // Handle login error
      console.error('Login failed:', error);
    }
  });
}
```

### Check Authentication Status
```typescript
// Check if user is authenticated
if (this.authService.isAuthenticated()) {
  // User is logged in
}

// Subscribe to authentication changes
this.authService.isAuthenticated$.subscribe(isAuth => {
  // Handle authentication state changes
});
```

### Logout
```typescript
// Logout user
this.authService.logout();
```

## Error Handling

The service handles various error scenarios:

- **401 Unauthorized**: Invalid credentials
- **0 Network Error**: Connection issues
- **5xx Server Errors**: Backend server issues
- **Validation Errors**: Form validation failures

## Security Considerations

1. JWT tokens are stored in localStorage
2. Automatic token expiration checking
3. Automatic logout on 401 responses
4. Secure HTTP headers for API requests

## Testing

To test the login functionality:

1. Start the backend server on `http://localhost:8080`
2. Navigate to the login page
3. Enter valid credentials
4. Verify successful login and navigation to profile page

## Dependencies

- Angular HttpClient
- Angular Reactive Forms
- Angular Router
- RxJS for reactive programming 