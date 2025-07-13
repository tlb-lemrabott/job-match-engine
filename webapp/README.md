# JobFitEngineAI 🚀

An AI-powered web application that helps job seekers evaluate how well their resume matches a given job description. Get instant insights on job compatibility and improve your chances of landing your dream role.

## 🌟 Features

### Core Functionality
- **Resume Upload**: Support for multiple file formats (PDF, DOC, DOCX, TXT)
- **Job Description Analysis**: Paste any job description for instant analysis
- **AI-Powered Matching**: Advanced algorithms to analyze resume-job compatibility
- **Match Percentage**: Get a detailed compatibility score
- **Skill Gap Analysis**: Identify missing skills and qualifications
- **Improvement Suggestions**: Receive actionable recommendations to improve your resume
- **User Dashboard**: Track your analysis history and progress

### User Experience
- **Modern UI/UX**: Clean, responsive design built with Tailwind CSS
- **Real-time Analysis**: Instant results with progress indicators
- **Secure Authentication**: JWT-based authentication system
- **Profile Management**: Personal dashboard with analysis history
- **Mobile Responsive**: Optimized for all device sizes

## 🛠️ Tech Stack

### Frontend
- **Angular 19**: Latest version with standalone components
- **TypeScript**: Type-safe development
- **Tailwind CSS 4.1**: Utility-first CSS framework for modern design
- **RxJS**: Reactive programming for state management
- **Angular Forms**: Reactive forms with validation
- **Angular Router**: Client-side routing

### Backend
- **Java 17+**: Modern Java with latest features
- **Spring Boot 3.x**: Rapid application development framework
- **Spring Security**: Authentication and authorization
- **Spring Data JPA**: Data access layer
- **Spring Web**: RESTful web services
- **JWT**: JSON Web Tokens for secure authentication

### Database
- **PostgreSQL**: Robust, open-source relational database
- **JPA/Hibernate**: Object-relational mapping

### Development Tools
- **Angular CLI**: Command-line interface for Angular
- **PostCSS**: CSS processing
- **Autoprefixer**: CSS vendor prefixing
- **TypeScript**: Static type checking
- **Jasmine/Karma**: Testing framework

## 🏗️ Architecture Design

### System Architecture
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   Backend       │    │   Database      │
│   (Angular)     │◄──►│   (Spring Boot) │◄──►│   (PostgreSQL)  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### Frontend Architecture
```
src/
├── app/
│   ├── auth/                 # Authentication components
│   │   ├── login/           # Login component
│   │   └── signup/          # Signup component
│   ├── profile/             # User profile and dashboard
│   ├── services/            # Shared services
│   │   └── auth.service.ts  # Authentication service
│   ├── app.component.*      # Main app component
│   ├── app.config.ts        # App configuration
│   └── app.routes.ts        # Routing configuration
├── styles.css               # Global styles
└── main.ts                  # Application entry point
```

### Backend Architecture
```
backend/
├── src/main/java/
│   └── com/jobfitengine/
│       ├── config/          # Configuration classes
│       ├── controller/      # REST controllers
│       ├── service/         # Business logic
│       ├── repository/      # Data access layer
│       ├── model/           # Entity classes
│       ├── dto/             # Data transfer objects
│       ├── security/        # Security configuration
│       └── util/            # Utility classes
├── resources/
│   └── application.yml      # Application properties
└── pom.xml                  # Maven dependencies
```

### Data Flow
1. **User Authentication**: JWT-based authentication with secure token storage
2. **Resume Upload**: File upload with validation and processing
3. **Job Description Input**: Text processing and analysis
4. **AI Analysis**: Machine learning algorithms for matching
5. **Results Display**: Real-time feedback and recommendations

## 🚀 Getting Started

### Prerequisites
- Node.js 18+ and npm
- Java 17+
- PostgreSQL 13+
- Maven 3.6+

### Frontend Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd webapp
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Start development server**
   ```bash
   npm start
   ```

4. **Open your browser**
   Navigate to `http://localhost:4200`

### Backend Setup

1. **Navigate to backend directory**
   ```bash
   cd ../backend
   ```

2. **Configure database**
   - Create PostgreSQL database
   - Update `application.yml` with database credentials

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

4. **API will be available at**
   `http://localhost:8080`

### Environment Configuration

Create `.env` file in the frontend directory:
```env
API_BASE_URL=http://localhost:8080/api
```

Update `application.yml` in backend:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/jobfitengine
    username: your_username
    password: your_password
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

jwt:
  secret: your_jwt_secret_key
  expiration: 86400000
```

## 📱 Usage

### For Job Seekers
1. **Sign Up**: Create a new account with email and password
2. **Login**: Access your personal dashboard
3. **Upload Resume**: Upload your resume in supported formats
4. **Paste Job Description**: Copy and paste the job description you want to analyze
5. **Get Analysis**: Click "Check Match" to receive instant results
6. **Review Results**: View match percentage, skill gaps, and improvement suggestions

### Features Overview
- **Dashboard**: View your analysis history and recent activities
- **Profile Management**: Update personal information and preferences
- **Analysis History**: Track all your previous resume analyses
- **Export Results**: Download analysis reports in PDF format

## 🔧 Development

### Code Structure
- **Components**: Reusable UI components with TypeScript
- **Services**: Business logic and API communication
- **Models**: TypeScript interfaces and data models
- **Guards**: Route protection and authentication
- **Interceptors**: HTTP request/response handling

### Testing
```bash
# Frontend tests
npm test

# Backend tests
mvn test
```

### Building for Production
```bash
# Frontend build
npm run build

# Backend build
mvn clean package
```

## 🔒 Security Features

- **JWT Authentication**: Secure token-based authentication
- **Password Hashing**: BCrypt password encryption
- **CORS Configuration**: Cross-origin resource sharing setup
- **Input Validation**: Server-side validation for all inputs
- **SQL Injection Prevention**: Parameterized queries
- **XSS Protection**: Content Security Policy headers

## 📊 Performance Optimization

- **Lazy Loading**: Angular modules loaded on demand
- **Image Optimization**: Compressed and optimized images
- **Caching**: Browser and server-side caching strategies
- **Database Indexing**: Optimized database queries
- **CDN Integration**: Content delivery network for static assets

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

- **Documentation**: [Wiki](link-to-wiki)
- **Issues**: [GitHub Issues](link-to-issues)
- **Email**: support@jobfitengine.ai

## 🙏 Acknowledgments

- Angular team for the amazing framework
- Spring Boot community for robust backend solutions
- Tailwind CSS for beautiful utility-first CSS
- PostgreSQL for reliable database management

---

**Made with ❤️ for job seekers worldwide**
