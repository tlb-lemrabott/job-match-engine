import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UploadResumeService } from '../../services/upload-resume.service';
import { HttpEvent, HttpEventType } from '@angular/common/http';

@Component({
  selector: 'app-upload-resume',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './upload-resume.component.html',
  styleUrl: './upload-resume.component.css'
})
export class UploadResumeComponent {
  selectedFile: File | null = null;
  uploadProgress: number | null = null;
  uploadComplete: boolean = false;

  constructor(private uploadService: UploadResumeService) { }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files?.length) {
      this.selectedFile = input.files[0];
      this.uploadProgress = null;
      this.uploadComplete = false;
    }
  }

  upload() {
    if (!this.selectedFile) {
      alert('Please select a file.');
      return;
    }

    this.uploadService.uploadResume(this.selectedFile).subscribe({
      next: (event: HttpEvent<any>) => {
        if (event.type === HttpEventType.UploadProgress && event.total) {
          this.uploadProgress = Math.round((event.loaded / event.total) * 100);
        } else if (event.type === HttpEventType.Response) {
          alert('Resume uploaded successfully!');
          this.uploadComplete = true;
        }
      },
      error: (err) => {
        console.error(err);
        alert('Upload failed.');
      }
    });
  }
}
