# Python Job Matcher Service

A Python microservice that provides intelligent resume-job matching using local AI models, replacing Amazon Bedrock integration.

## Features

- **Skill Extraction**: Extracts technical skills, frameworks, and soft skills from text
- **Semantic Similarity**: Uses Sentence Transformers for intelligent text comparison
- **Experience Analysis**: Identifies experience requirements and gaps
- **Match Scoring**: Calculates comprehensive match scores
- **No External Dependencies**: All AI models run locally

## API Endpoints

### Health Check
```
GET /health
```

### Job Match Analysis
```
POST /analyze
Content-Type: application/json

{
  "resume_text": "Your resume content here...",
  "job_description": "Job description content here..."
}
```

**Response:**
```json
{
  "matchScore": 0.87,
  "matchedSkills": ["Spring Boot", "AWS", "Java"],
  "missingSkills": ["Docker", "Kubernetes"],
  "missingExperience": ["6+ years in Java", "3+ years in C#"],
  "otherMissing": []
}
```

### Skill Extraction
```
POST /skills
Content-Type: application/json

{
  "text": "Text content to extract skills from..."
}
```

## Setup

### Local Development

1. **Install Python 3.9+**
2. **Install dependencies:**
   ```bash
   pip install -r requirements.txt
   python -m spacy download en_core_web_sm
   ```
3. **Run the service:**
   ```bash
   python app.py
   ```

### Docker

1. **Build the image:**
   ```bash
   docker build -t python-job-matcher .
   ```

2. **Run the container:**
   ```bash
   docker run -p 5000:5000 python-job-matcher
   ```

## Integration with Spring Boot

Update your `JobMatchingService` to call this Python service instead of Amazon Bedrock:

```java
@Service
public class JobMatchingService {
    
    @Value("${python.matcher.url:http://localhost:5000}")
    private String pythonMatcherUrl;
    
    private final WebClient webClient;
    
    public JobMatchingResponse performJobMatching(String resumeText, User user, String jobDescription, String type) {
        try {
            // Call Python service
            Map<String, String> request = Map.of(
                "resume_text", resumeText,
                "job_description", jobDescription
            );
            
            PythonMatchResponse response = webClient.post()
                .uri(pythonMatcherUrl + "/analyze")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PythonMatchResponse.class)
                .block();
            
            // Convert response to your existing format
            return convertPythonResponse(response);
            
        } catch (Exception e) {
            log.error("Error calling Python matcher: {}", e.getMessage());
            return new JobMatchingResponse(false, "Error performing job matching", 0.0, List.of(), List.of(), null);
        }
    }
}
```

## AI Models Used

- **Sentence Transformers**: `all-MiniLM-L6-v2` for semantic similarity
- **spaCy**: `en_core_web_sm` for NLP tasks and entity extraction
- **Custom Regex Patterns**: For skill and experience extraction

## Performance

- **Response Time**: ~2-5 seconds for typical analysis
- **Memory Usage**: ~2GB RAM (mainly for AI models)
- **CPU**: Moderate usage during analysis

## Configuration

The service can be configured via environment variables:

- `PORT`: Service port (default: 5000)
- `WORKERS`: Number of Gunicorn workers (default: 2)
- `LOG_LEVEL`: Logging level (default: INFO)

## Monitoring

The service includes:
- Health check endpoint
- Structured logging
- Error handling and reporting
- Performance metrics

## Security

- CORS enabled for frontend integration
- Input validation and sanitization
- Error message sanitization
- No sensitive data logging
