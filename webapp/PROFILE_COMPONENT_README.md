# User Profile Component - Resume Management & Job Matching

## Overview

The User Profile Component provides comprehensive resume management and job matching functionality with advanced AI-powered analysis. The component follows Angular best practices with proper separation of concerns and single responsibility principles.

## Features

### Resume Management
- **File Upload**: Support for PDF, DOC, and DOCX formats (max 10MB)
- **File Validation**: Client-side validation for file type and size
- **Resume Storage**: Secure storage with user authentication
- **Resume Display**: Current resume information with metadata
- **Resume Deletion**: Safe deletion with confirmation dialog
- **Progress Tracking**: Real-time upload progress with visual feedback

### Job Matching Analysis
- **Dual Analysis Modes**:
  - Full Job Description Analysis
  - Skills Section Only Analysis
- **Comprehensive Matching**: AI-powered skill matching with confidence scores
- **Missing Skills Detection**: Identifies gaps in user's skill set
- **Visual Results**: Color-coded matching scores and skill cards
- **Recommendations**: AI-generated improvement suggestions

## Component Architecture

### Services (Separation of Concerns)

#### 1. ResumeService (`src/app/services/resume.service.ts`)
- **Responsibility**: Handle all resume-related API operations
- **Methods**:
  - `uploadResume(file: File)`: Upload new resume
  - `getUserResume()`: Fetch current user's resume
  - `deleteResume()`: Delete user's resume

#### 2. JobMatchingService (`src/app/services/job-matching.service.ts`)
- **Responsibility**: Handle job matching API calls
- **Methods**:
  - `checkJobMatch(request: JobMatchingRequest)`: Submit job matching request

#### 3. FileUploadService (`src/app/services/file-upload.service.ts`)
- **Responsibility**: File validation and processing utilities
- **Methods**:
  - `validateFile(file: File)`: Validate file type and size
  - `formatFileSize(bytes: number)`: Format file size for display
  - `getFileTypeDisplayName(fileType: string)`: Get human-readable file type
  - `fileToBase64(file: File)`: Convert file to base64 (if needed)

### Main Component (`src/app/profile/profile.component.ts`)
- **Responsibility**: Orchestrate UI interactions and data flow
- **Features**:
  - Tab-based navigation (Resume Management / Job Matching)
  - Form handling with validation
  - Progress tracking for uploads and analysis
  - Error handling and user feedback

## API Integration

### Resume Management Endpoints
```
POST /resume/upload - Upload new resume
GET /resume/user - Get current user's resume
DELETE /resume/user - Delete user's resume
```

### Job Matching Endpoint
```
POST /api - Job matching analysis
```

**Request Format:**
```json
{
  "resume": "resume_id",
  "type": "full-job" | "skills-section",
  "text-area": "job_description_text"
}
```

**Response Format:**
```json
{
  "success": true,
  "message": "Analysis completed",
  "matchingScore": 85,
  "matchedSkills": [
    {
      "skill": "JavaScript",
      "confidence": 95,
      "category": "Programming"
    }
  ],
  "missingSkills": [
    {
      "skill": "React",
      "importance": 80,
      "category": "Frontend"
    }
  ],
  "analysis": {
    "overallMatch": "Excellent match with strong technical skills",
    "recommendations": [
      "Consider learning React to improve frontend skills",
      "Focus on cloud technologies for better market positioning"
    ]
  }
}
```

## UI/UX Features

### Modern Design
- **Responsive Layout**: Mobile-first design with breakpoints
- **Tab Navigation**: Clean tab-based interface
- **Progress Indicators**: Visual feedback for all operations
- **Color-coded Results**: Intuitive score visualization
- **Hover Effects**: Interactive elements with smooth transitions

### User Experience
- **Form Validation**: Real-time validation with helpful error messages
- **Loading States**: Clear indication of processing status
- **Error Handling**: User-friendly error messages
- **Confirmation Dialogs**: Safe deletion with confirmation
- **File Preview**: Selected file information display

## Security Features

- **Authentication**: All API calls require valid JWT token
- **File Validation**: Client and server-side file validation
- **Error Handling**: Secure error messages without exposing internals
- **Token Management**: Automatic token refresh and logout on expiration

## File Support

### Supported Formats
- **PDF** (.pdf) - Portable Document Format
- **DOC** (.doc) - Microsoft Word Document
- **DOCX** (.docx) - Microsoft Word Open XML Document

### File Restrictions
- **Maximum Size**: 10MB
- **Validation**: MIME type and extension validation
- **Security**: Malware scanning (backend implementation)

## Usage Instructions

### For Users
1. **Upload Resume**: Navigate to Profile → Resume Management tab
2. **Select File**: Choose PDF, DOC, or DOCX file (max 10MB)
3. **Upload**: Click upload button and wait for completion
4. **Job Matching**: Switch to Job Matching tab
5. **Enter Job Description**: Paste full job description or skills section
6. **Select Analysis Type**: Choose between full job or skills-only analysis
7. **Analyze**: Click "Check Match" to get comprehensive results

### For Developers
1. **Service Integration**: Inject required services in component constructor
2. **Error Handling**: Implement proper error handling for all API calls
3. **Loading States**: Use provided loading indicators for better UX
4. **Validation**: Leverage built-in form validation and file validation
5. **Styling**: Use provided CSS classes for consistent styling

## Technical Implementation

### Dependencies
- Angular 17+ (Standalone Components)
- Reactive Forms for form handling
- RxJS for reactive programming
- Font Awesome for icons
- HTTP Client for API communication

### Key Patterns
- **Observer Pattern**: RxJS observables for async operations
- **Service Pattern**: Separation of business logic
- **Component Pattern**: Reusable UI components
- **Form Pattern**: Reactive forms with validation

## Future Enhancements

1. **Drag & Drop**: File upload with drag and drop interface
2. **Multiple Resumes**: Support for multiple resume versions
3. **Resume Templates**: Pre-built resume templates
4. **Export Results**: PDF export of matching results
5. **Skill Recommendations**: Personalized learning recommendations
6. **Integration**: LinkedIn and other professional network integration

## Error Handling

The component implements comprehensive error handling:
- **Network Errors**: Connection timeout and server errors
- **Validation Errors**: Form and file validation errors
- **Authentication Errors**: Token expiration and invalid credentials
- **File Errors**: Upload failures and format issues

All errors are displayed to users with actionable messages and recovery options. 