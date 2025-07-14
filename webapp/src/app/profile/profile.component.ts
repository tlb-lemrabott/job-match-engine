import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Subject, takeUntil } from 'rxjs';

import { ResumeService, Resume } from '../services/resume.service';
import { JobMatchingService, JobMatchingRequest, JobMatchingResponse } from '../services/job-matching.service';
import { FileUploadService, FileValidationResult } from '../services/file-upload.service';
import { AuthServiceService } from '../services/auth.service.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent implements OnInit, OnDestroy {
  // Resume management properties
  userResume: Resume | null = null;
  selectedFile: File | null = null;
  isUploading = false;
  uploadProgress = 0;
  uploadError = '';

  // Job matching properties
  jobMatchingForm: FormGroup;
  isMatching = false;
  matchingProgress = 0;
  matchingResult: JobMatchingResponse | null = null;
  matchingError = '';

  // UI state properties
  activeTab: 'resume' | 'matching' = 'resume';
  user: any = null;

  private destroy$ = new Subject<void>();

  constructor(
    private resumeService: ResumeService,
    private jobMatchingService: JobMatchingService,
    private fileUploadService: FileUploadService,
    private authService: AuthServiceService,
    private fb: FormBuilder
  ) {
    this.jobMatchingForm = this.fb.group({
      jobDescription: ['', [Validators.required, Validators.minLength(50)]],
      analysisType: ['full-job', Validators.required]
    });
  }

  ngOnInit(): void {
    this.user = this.authService.getUser();
    this.loadUserResume();
    
    // For debugging: simulate what might be returned from backend
    setTimeout(() => {
      console.log('=== DEBUGGING: Simulating backend response ===');
      const mockResume = {
        id: '550e8400-e29b-41d4-a716-446655440000',
        fileName: 'test-resume.pdf',
        fileSize: 1024000,
        fileType: 'application/pdf',
        fileUrl: 'http://localhost:8080/files/test-resume.pdf',
        uploadDate: {
          year: 2024,
          monthValue: 1,
          dayOfMonth: 15,
          hour: 10,
          minute: 30,
          second: 0,
          toString: () => '2024-01-15T10:30:00'
        }
      };
      console.log('Mock backend response:', mockResume);
      const processed = this.processResumeData(mockResume);
      console.log('After processing:', processed);
    }, 2000);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // Resume Management Methods
  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      const validation = this.fileUploadService.validateFile(file);
      if (validation.isValid) {
        this.selectedFile = file;
        this.uploadError = '';
      } else {
        this.uploadError = validation.error || 'Invalid file';
        this.selectedFile = null;
      }
    }
  }

  uploadResume(): void {
    if (!this.selectedFile) {
      this.uploadError = 'Please select a file to upload';
      return;
    }

    this.isUploading = true;
    this.uploadProgress = 0;
    this.uploadError = '';

    // Simulate upload progress
    const progressInterval = setInterval(() => {
      this.uploadProgress += 10;
      if (this.uploadProgress >= 90) {
        clearInterval(progressInterval);
      }
    }, 200);

    this.resumeService.uploadResume(this.selectedFile)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          console.log('Upload response:', response);
          clearInterval(progressInterval);
          this.uploadProgress = 100;
          
          setTimeout(() => {
            this.isUploading = false;
            this.uploadProgress = 0;
            this.selectedFile = null;
            this.loadUserResume();
          }, 500);
        },
        error: (error) => {
          clearInterval(progressInterval);
          this.isUploading = false;
          this.uploadProgress = 0;
          this.uploadError = error.message;
        }
      });
  }

  deleteResume(): void {
    if (confirm('Are you sure you want to delete your resume? This action cannot be undone.')) {
      this.resumeService.deleteResume()
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => {
            this.userResume = null;
          },
          error: (error) => {
            console.error('Delete resume error:', error);
          }
        });
    }
  }

  private loadUserResume(): void {
    this.resumeService.getUserResume()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (resume) => {
          console.log('=== RESUME DATA DEBUG ===');
          console.log('Raw resume data from API:', resume);
          console.log('Resume type:', typeof resume);
          console.log('Resume keys:', Object.keys(resume));
          console.log('Resume JSON:', JSON.stringify(resume, null, 2));
          
          // Debug each property
          console.log('fileName:', resume.fileName, 'type:', typeof resume.fileName);
          console.log('fileSize:', resume.fileSize, 'type:', typeof resume.fileSize);
          console.log('fileType:', resume.fileType, 'type:', typeof resume.fileType);
          console.log('uploadDate:', resume.uploadDate, 'type:', typeof resume.uploadDate);
          console.log('uploadDate JSON:', JSON.stringify(resume.uploadDate, null, 2));
          
          // Handle potential data type issues and backend format differences
          const processedResume = this.processResumeData(resume);
          this.userResume = processedResume;
          console.log('Processed userResume:', this.userResume);
          console.log('=== END RESUME DATA DEBUG ===');
        },
        error: (error) => {
          // User might not have a resume yet, which is fine
          console.log('No resume found for user');
          console.error('Resume fetch error:', error);
        }
      });
  }

  // Job Matching Methods
  checkJobMatch(): void {
    if (this.jobMatchingForm.invalid) {
      this.markFormGroupTouched();
      return;
    }

    if (!this.userResume) {
      this.matchingError = 'Please upload your resume first before checking job matches.';
      return;
    }

    this.isMatching = true;
    this.matchingProgress = 0;
    this.matchingError = '';
    this.matchingResult = null;

    // Simulate matching progress
    const progressInterval = setInterval(() => {
      this.matchingProgress += 5;
      if (this.matchingProgress >= 90) {
        clearInterval(progressInterval);
      }
    }, 300);

    const request: JobMatchingRequest = {
      resume: this.userResume.id, // Assuming the resume ID is sent
      type: this.jobMatchingForm.get('analysisType')?.value,
      'text-area': this.jobMatchingForm.get('jobDescription')?.value
    };

    this.jobMatchingService.checkJobMatch(request)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          clearInterval(progressInterval);
          this.matchingProgress = 100;
          
          setTimeout(() => {
            this.isMatching = false;
            this.matchingProgress = 0;
            this.matchingResult = response;
          }, 500);
        },
        error: (error) => {
          clearInterval(progressInterval);
          this.isMatching = false;
          this.matchingProgress = 0;
          this.matchingError = error.message;
        }
      });
  }

  private markFormGroupTouched(): void {
    Object.keys(this.jobMatchingForm.controls).forEach(key => {
      const control = this.jobMatchingForm.get(key);
      control?.markAsTouched();
    });
  }

  // Utility Methods
  getFileTypeDisplayName(fileType: string): string {
    if (!fileType) {
      console.warn('getFileTypeDisplayName called with empty fileType:', fileType);
      return 'Unknown';
    }
    return this.fileUploadService.getFileTypeDisplayName(fileType);
  }

  formatFileSize(bytes: number): string {
    if (!bytes || isNaN(bytes)) {
      console.warn('formatFileSize called with invalid bytes:', bytes);
      return '0 Bytes';
    }
    return this.fileUploadService.formatFileSize(bytes);
  }

  setActiveTab(tab: 'resume' | 'matching'): void {
    this.activeTab = tab;
  }

  clearMatchingResult(): void {
    this.matchingResult = null;
    this.matchingError = '';
  }

  // Data processing methods
  private processResumeData(resume: any): Resume {
    console.log('Processing resume data:', resume);
    
    // Backend entity properties match frontend interface
    const processedResume: Resume = {
      id: resume.id || '',
      fileName: resume.fileName || '',
      fileSize: this.parseFileSize(resume.fileSize),
      fileType: resume.fileType || '',
      uploadDate: this.parseUploadDate(resume.uploadDate),
      fileUrl: resume.fileUrl
    };
    
    console.log('Processed resume:', processedResume);
    return processedResume;
  }

  private parseFileSize(size: any): number {
    if (typeof size === 'number') return size;
    if (typeof size === 'string') {
      const parsed = parseInt(size, 10);
      return isNaN(parsed) ? 0 : parsed;
    }
    // Handle Java Long type (might be serialized as string)
    if (size && typeof size === 'object' && size.toString) {
      const parsed = parseInt(size.toString(), 10);
      return isNaN(parsed) ? 0 : parsed;
    }
    return 0;
  }

  private parseUploadDate(date: any): Date {
    if (date instanceof Date) return date;
    if (typeof date === 'string') {
      const parsed = new Date(date);
      return isNaN(parsed.getTime()) ? new Date() : parsed;
    }
    // Handle Java LocalDateTime object (might have specific properties)
    if (date && typeof date === 'object') {
      // If it's a Java LocalDateTime, it might have year, month, day, etc.
      if (date.year && date.monthValue && date.dayOfMonth) {
        return new Date(date.year, date.monthValue - 1, date.dayOfMonth, 
                       date.hour || 0, date.minute || 0, date.second || 0);
      }
      // If it has a toString method, try parsing it
      if (date.toString) {
        const parsed = new Date(date.toString());
        return isNaN(parsed.getTime()) ? new Date() : parsed;
      }
    }
    return new Date();
  }

  // Score utility methods
  getScoreColor(score: number): string {
    if (score >= 80) return 'linear-gradient(135deg, #4CAF50, #45a049)';
    if (score >= 60) return 'linear-gradient(135deg, #FF9800, #F57C00)';
    return 'linear-gradient(135deg, #F44336, #D32F2F)';
  }

  getScoreDescription(score: number): string {
    if (score >= 80) return 'Excellent match! You have most of the required skills.';
    if (score >= 60) return 'Good match. Consider developing some missing skills.';
    if (score >= 40) return 'Fair match. Focus on developing key missing skills.';
    return 'Poor match. Consider if this role aligns with your career goals.';
  }
}
