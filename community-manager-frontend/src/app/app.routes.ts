import { Routes } from '@angular/router';
import { Landing } from './features/landing/landing';
import { Login } from './features/auth/login/login';
import { Signup } from './features/auth/signup/signup';
import { ForgotPassword } from './features/auth/forgot-password/forgot-password';
import { SubscribeConfig } from './features/subscribe/subscribe-config/subscribe-config';
import { SubscribeConfirm } from './features/subscribe/subscribe-confirm/subscribe-confirm';
import { Shell } from './layout/shell/shell';
import { Dashboard } from './features/dashboard/dashboard';
import { Calendar } from './features/calendar/calendar';
import { Library } from './features/library/library';
import { Suggestions } from './features/suggestions/suggestions';
import { Configuration as ConfigurationPage } from './features/configuration/configuration';
import { Settings } from './features/settings/settings';
import { Statistics } from './features/statistics/statistics';
import { authGuard } from './core/guards/auth-guard';
import { SubscribePayment } from './features/subscribe/subscribe-payment/subscribe-payment';

export const routes: Routes = [
  { path: '', component: Landing },
  { path: 'login', component: Login },
  { path: 'signup', component: Signup },
  { path: 'forgot-password', component: ForgotPassword },
  { path: 'subscribe', component: SubscribeConfig, canActivate: [authGuard] },
  { path: 'subscribe/confirm', component: SubscribeConfirm, canActivate: [authGuard] },
  {
    path: 'app',
    component: Shell,
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: Dashboard },
      { path: 'calendrier', component: Calendar },
      { path: 'posts', component: Library },
      { path: 'suggestions', component: Suggestions },
      { path: 'configuration', component: ConfigurationPage },
      { path: 'abonnement', component: Settings },
      { path: 'statistiques', component: Statistics },
      { path: 'subscribe/annule', component: SubscribePayment, canActivate: [authGuard] },
    ],
  },
];
