import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { MatchCheckerService } from '../../services/match-checker.service';

@Component({
  selector: 'app-match-checker',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './match-checker.component.html',
})
export class MatchCheckerComponent {
  jobDescription = '';
  skills = '';
  matchPercentage: number | null = null;
  loading = false;

  constructor(private matchService: MatchCheckerService) {}

  onSubmit() {
    if (!this.jobDescription || !this.skills) {
      alert('Please fill in both fields.');
      return;
    }

    this.loading = true;
    this.matchService.checkMatch({
      jobDescription: this.jobDescription,
      skills: this.skills
    }).subscribe({
      next: (res) => {
        this.matchPercentage = res.matchPercentage;
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        alert('Error checking match.');
        this.loading = false;
      }
    });
  }
}
