import { HttpErrorResponse } from '@angular/common/http';

export function getHttpErrorMessage(error: HttpErrorResponse): string {
  const backendMessage =
    typeof error.error === 'string' ? error.error : error.error?.message;

  if (backendMessage) {
    return backendMessage;
  }

  switch (error.status) {
    case 400:
      return 'Certaines informations sont invalides ou manquantes.';
    case 403:
      return "Cette action n'est pas autorisée — vérifie le statut de ton abonnement.";
    case 404:
      return 'Ressource introuvable.';
    case 0:
      return 'Impossible de contacter le serveur. Vérifie ta connexion.';
    default:
      return 'Une erreur inattendue est survenue.';
  }
}
