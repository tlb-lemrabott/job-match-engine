import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';


@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './signup.component.html',
  styleUrl: './signup.component.css'
})
export class SignupComponent {
  user = {
    name: '',
    email: '',
    password: ''
  };

  constructor() { }

  onSubmit() {
    if (!this.user.name || !this.user.email || !this.user.password) {
      alert('All fields are required');
      return;
    }
    console.log(this.user);
  }
}

