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
  isUpdating = false;
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
      this.uploadError = this.userResume ? 'Please select a file to update' : 'Please select a file to upload';
      return;
    }

    // If user has an existing resume, use update instead of upload
    if (this.userResume) {
      this.updateResume();
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
          clearInterval(progressInterval);
          this.uploadProgress = 100;
          
          setTimeout(() => {
            this.isUploading = false;
            this.uploadProgress = 0;
            this.selectedFile = null;
            this.loadUserResume();
            alert('Resume uploaded successfully!');
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

  updateResume(): void {
    if (!this.selectedFile) {
      this.uploadError = 'Please select a file to update';
      return;
    }

    this.isUpdating = true;
    this.uploadProgress = 0;
    this.uploadError = '';

    // Simulate update progress
    const progressInterval = setInterval(() => {
      this.uploadProgress += 10;
      if (this.uploadProgress >= 90) {
        clearInterval(progressInterval);
      }
    }, 200);

    this.resumeService.updateResume(this.selectedFile)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          clearInterval(progressInterval);
          this.uploadProgress = 100;
          
          setTimeout(() => {
            this.isUpdating = false;
            this.uploadProgress = 0;
            this.selectedFile = null;
            this.loadUserResume();
            alert('Resume updated successfully!');
          }, 500);
        },
        error: (error) => {
          clearInterval(progressInterval);
          this.isUpdating = false;
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
          next: (response) => {
            console.log('Delete response:', response);
            if (response.success) {
              // Clear the resume from UI
              this.userResume = null;
              // Show success message
              alert('Resume deleted successfully!');
              // Don't reload resume data since we just deleted it
            } else {
              alert('Failed to delete resume: ' + response.message);
            }
          },
          error: (error) => {
            console.error('Delete error:', error);
            
            // Check if it's a 404 (not found) error, which is expected after deletion
            if (error.status === 404) {
              this.userResume = null;
              alert('Resume deleted successfully!');
            } else {
              // Show generic error message
              alert('Failed to delete resume: ' + error.message);
            }
          }
        });
    }
  }

  private loadUserResume(): void {
    this.resumeService.getUserResume()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (resume) => {
          this.userResume = resume;
        },
        error: (error) => {
          this.userResume = null;
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
      return 'Unknown';
    }
    return this.fileUploadService.getFileTypeDisplayName(fileType);
  }

  formatFileSize(bytes: number): string {
    if (!bytes || isNaN(bytes)) {
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

  // Force refresh resume data (useful for debugging)
  refreshResumeData(): void {
    console.log('Force refreshing resume data...');
    this.loadUserResume();
  }

  // Clear resume data (useful for debugging corrupted records)
  clearResumeData(): void {
    console.log('Clearing resume data from UI...');
    this.userResume = null;
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

  triggerUpdateFileInput(fileInput: HTMLInputElement): void {
    if (!this.isUploading && !this.isUpdating) {
      fileInput.value = '';
      fileInput.click();
    }
  }

  onUpdateFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      const validation = this.fileUploadService.validateFile(file);
      if (validation.isValid) {
        this.selectedFile = file;
        this.uploadError = '';
        this.updateResume();
      } else {
        this.uploadError = validation.error || 'Invalid file';
        this.selectedFile = null;
      }
    }
  }
}
