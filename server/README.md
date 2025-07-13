# JobFitEngine AI Backend

A comprehensive Spring Boot backend application for intelligent job matching using AI and AWS services. The application analyzes resumes against job descriptions to provide detailed matching scores and recommendations.

## 🚀 Features

- **User Authentication**: JWT-based authentication with secure password hashing
- **Resume Management**: Upload, store, and manage user resumes (PDF, DOC, DOCX)
- **Text Extraction**: Apache Tika for extracting text from various document formats
- **AI-Powered Analysis**: AWS Comprehend for skill extraction and entity recognition
- **Semantic Matching**: Amazon Bedrock embeddings for semantic similarity scoring
- **Intelligent Matching**: Advanced algorithms for skill matching and gap analysis
- **RESTful API**: Complete REST API with proper error handling and validation

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.5.0
- **Database**: PostgreSQL
- **Security**: Spring Security with JWT
- **Document Processing**: Apache Tika
- **AI Services**: AWS Comprehend, Amazon Bedrock
- **Build Tool**: Maven
- **Language**: Java 17

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+
- AWS Account with access to:
  - AWS Comprehend
  - Amazon Bedrock
  - AWS S3 (optional for file storage)

## 🔧 Setup Instructions

### 1. Database Setup

Create a PostgreSQL database:

```sql
CREATE DATABASE jobfitengine;
CREATE USER postgres WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE jobfitengine TO postgres;
```

### 2. AWS Configuration

Set up your AWS credentials as environment variables:

```bash
export AWS_ACCESS_KEY_ID=your_access_key
export AWS_SECRET_ACCESS_KEY=your_secret_key
export AWS_REGION=us-east-1
```

### 3. Application Configuration

Update `src/main/resources/application.properties` with your specific configurations:

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/jobfitengine
spring.datasource.username=postgres
spring.datasource.password=your_password

# JWT Configuration
jwt.secret=your-super-secret-jwt-key-that-should-be-at-least-256-bits-long

# AWS Configuration
aws.region=us-east-1
aws.s3.bucket-name=your-s3-bucket-name
```

### 4. Build and Run

```bash
# Build the application
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## 📚 API Documentation

### Authentication Endpoints

#### POST `/auth/login`
User login endpoint.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "token": "jwt_token_here",
  "user": {
    "id": "uuid",
    "email": "user@example.com",
    "name": "John Doe"
  }
}
```

#### POST `/auth/signup`
User registration endpoint.

**Request Body:**
```json
{
  "name": "John Doe",
  "email": "user@example.com",
  "password": "password123",
  "phone": "+1234567890"
}
```

### Resume Management Endpoints

#### POST `/resume/upload`
Upload user resume (requires authentication).

**Headers:** `Authorization: Bearer <token>`
**Content-Type:** `multipart/form-data`

**Form Data:**
- `resume`: File (PDF, DOC, DOCX, max 10MB)

#### GET `/resume/user`
Get current user's resume (requires authentication).

**Headers:** `Authorization: Bearer <token>`

#### DELETE `/resume/user`
Delete current user's resume (requires authentication).

**Headers:** `Authorization: Bearer <token>`

### Job Matching Endpoint

#### POST `/api`
Perform job matching analysis (requires authentication).

**Headers:** 
- `Authorization: Bearer <token>`
- `Content-Type: application/json`

**Request Body:**
```json
{
  "resume": "resume-uuid",
  "type": "full-job",
  "text-area": "Job description text here..."
}
```

**Response:**
```json
{
  "success": true,
  "message": "Job matching analysis completed successfully",
  "matchingScore": 87.5,
  "matchedSkills": [
    {
      "skill": "Java",
      "confidence": 0.9,
      "category": "Technical"
    }
  ],
  "missingSkills": [
    {
      "skill": "Docker",
      "importance": 0.8,
      "category": "Technical"
    }
  ],
  "analysis": {
    "overallMatch": "Excellent match! Your profile strongly aligns with the job requirements.",
    "recommendations": [
      "Highlight your matched skills prominently in your application",
      "Prepare to discuss your experience with the identified technologies"
    ]
  }
}
```

## 🔐 Security Features

- **JWT Authentication**: Secure token-based authentication
- **Password Hashing**: BCrypt password encryption
- **CORS Configuration**: Configured for Angular frontend
- **Input Validation**: Comprehensive request validation
- **Error Handling**: Secure error responses without sensitive information

## 🏗️ Architecture

The application follows a clean architecture pattern with clear separation of concerns:

```
src/main/java/com/jobfitengine/code/
├── config/          # Configuration classes
├── controller/      # REST API controllers
├── dto/            # Data Transfer Objects
├── entity/         # JPA entities
├── exception/      # Global exception handling
├── repository/     # Data access layer
├── security/       # Security configuration and filters
└── service/        # Business logic layer
```

### Service Layer Breakdown

- **UserService**: User management and authentication
- **ResumeService**: Resume upload and management
- **DocumentTextExtractionService**: Text extraction using Apache Tika
- **AwsComprehendService**: Skill and entity extraction
- **BedrockEmbeddingService**: Semantic similarity calculations
- **JobMatchingService**: Orchestrates the entire matching process

## 🧪 Testing

Run the test suite:

```bash
mvn test
```

## 📝 Environment Variables

| Variable | Description | Required |
|----------|-------------|----------|
| `AWS_ACCESS_KEY_ID` | AWS access key | Yes |
| `AWS_SECRET_ACCESS_KEY` | AWS secret key | Yes |
| `AWS_REGION` | AWS region | Yes |

## 🚀 Deployment

### Docker Deployment

```bash
# Build Docker image
docker build -t jobfitengine-backend .

# Run container
docker run -p 8080:8080 \
  -e AWS_ACCESS_KEY_ID=your_key \
  -e AWS_SECRET_ACCESS_KEY=your_secret \
  -e AWS_REGION=us-east-1 \
  jobfitengine-backend
```

### Production Considerations

1. **Database**: Use managed PostgreSQL service (AWS RDS, Google Cloud SQL)
2. **File Storage**: Use AWS S3 for resume storage
3. **Security**: Use AWS Secrets Manager for sensitive configuration
4. **Monitoring**: Implement logging and monitoring (CloudWatch, ELK Stack)
5. **SSL**: Configure HTTPS with proper certificates

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License.

## 🆘 Support

For support and questions, please contact the development team or create an issue in the repository. 