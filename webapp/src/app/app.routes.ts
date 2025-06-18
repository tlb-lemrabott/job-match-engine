import { Routes } from '@angular/router';
import { SignupComponent } from './pages/signup/signup.component';
import { LoginComponent } from './pages/login/login.component';
import { UploadResumeComponent } from './pages/upload-resume/upload-resume.component';
import { MatchCheckerComponent } from './pages/match-checker/match-checker.component';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'signup', component: SignupComponent },
  { path: 'login', component: LoginComponent },
  { path: 'upload-resume', component: UploadResumeComponent },
  { path: 'check-match', component: MatchCheckerComponent }
];
