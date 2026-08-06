import { HttpErrorResponse } from '@angular/common/http';

/**
 * Construit un message affichable à l'utilisateur à partir d'une erreur HTTP.
 * Utilise le message du backend s'il en fournit un, sinon retombe sur un
 * message générique selon le code de statut (400 / 404 en particulier,
 * comme demandé dans le cahier des charges).
 */
export function getHttpErrorMessage(error: HttpErrorResponse): string {
  const backendMessage =
    typeof error.error === 'string' ? error.error : error.error?.message;

  if (backendMessage) {
    return backendMessage;
  }

  switch (error.status) {
    case 400:
      return 'Certaines informations sont invalides ou manquantes.';
    case 404:
      return 'Ressource introuvable.';
    case 0:
      return 'Impossible de contacter le serveur. Vérifie ta connexion.';
    default:
      return 'Une erreur inattendue est survenue.';
  }
}
