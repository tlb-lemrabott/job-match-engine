import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { SignupService, SignupPayload } from '../../services/signup.service';


@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './signup.component.html',
  styleUrl: './signup.component.css'
})
export class SignupComponent {
  user: SignupPayload = {
    name: '',
    email: '',
    password: ''
  };

  constructor(private signupService: SignupService, private router: Router) { }

  onSubmit() {
    if (!this.user.name || !this.user.email || !this.user.password) {
      alert('All fields are required');
      return;
    }

    this.signupService.signup(this.user).subscribe({
      next: () => {
        alert('Signup successful!');
        this.router.navigate(['/login']);
      },
      error: (error) => {
        console.error(error);
        alert('Signup failed');
      }
    });
  }
}

