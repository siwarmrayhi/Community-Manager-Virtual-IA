import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

/**
 * Log centralisé de toutes les erreurs HTTP (utile en dev). Ne redirige
 * jamais et ne modifie pas l'erreur : chaque écran reste responsable de sa
 * propre réaction à un 400 ou un 404 via `getHttpErrorMessage`.
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      console.error(`[HTTP ${error.status}] ${req.method} ${req.url}`, error);
      return throwError(() => error);
    })
  );
};
