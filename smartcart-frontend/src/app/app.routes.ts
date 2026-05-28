import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login';
import { RegistroComponent } from './features/auth/registro/registro';
import { HomeComponent } from './features/home/home';
import { AdminPanelComponent } from './features/admin/admin-panel/admin-panel';
import { AdminGuard } from './core/guards/admin-guard';
import { CatalogoComponent } from './features/productos/catalogo/catalogo';
import { MiCuentaComponent } from './features/cuenta/mi-cuenta/mi-cuenta';
import { RecomendadosComponent } from './features/recomendados/recomendados/recomendados';
import { DetalleComponent } from './features/productos/detalle/detalle';
import { CarritoComponent } from './features/carrito/carrito/carrito';


export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'registro', component: RegistroComponent },
  { path: 'admin', component: AdminPanelComponent, canActivate: [AdminGuard] },
  { path: 'catalogo', component: CatalogoComponent },
  { path: 'mi-cuenta', component: MiCuentaComponent },
  { path: 'recomendados', component: RecomendadosComponent },
  { path: 'producto/:id', component: DetalleComponent },
  { path: 'carrito', component: CarritoComponent },
  { path: '**', redirectTo: '' }
];