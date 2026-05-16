import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login';
import { RegistroComponent } from './features/auth/registro/registro';
import { HomeComponent } from './features/home/home';
import { AdminPanelComponent } from './features/admin/admin-panel/admin-panel';
import { AdminGuard } from './core/guards/admin-guard';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'registro', component: RegistroComponent },
  { path: 'admin', component: AdminPanelComponent, canActivate: [AdminGuard] },
  { path: '**', redirectTo: '' }
];