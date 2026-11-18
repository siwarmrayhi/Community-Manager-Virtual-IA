# Community Manager Virtuel IA

Application web d'**assistant community manager** propulsé par l'IA, conçue pour la plateforme **Tunisia Invest**. Elle génère automatiquement des publications pour les réseaux sociaux (LinkedIn, Facebook, Instagram) à partir du profil, du ton et des objectifs de l'utilisateur, puis les soumet à validation avant publication.

L'accès au service se fait par abonnement mensuel, payé via PayPal.

## 🎥 Démo vidéo

▶️ **[Regarder la démo sur Google Drive](REMPLACER_PAR_LE_LIEN_DRIVE)**

## Sommaire

- [Fonctionnalités](#fonctionnalités)
- [Architecture](#architecture)
- [Structure du projet](#structure-du-projet)
- [Modèle de données](#modèle-de-données)
- [Génération de contenu (Groq)](#génération-de-contenu-groq)
- [API REST](#api-rest)

- [Technologies utilisées](#technologies-utilisées)

## Fonctionnalités

- **Inscription et connexion** sécurisées (JWT), avec pages de connexion, d'inscription et de mot de passe oublié.
- **Abonnement mensuel via PayPal** : souscription, confirmation et résiliation.
- **Configuration du community manager** : réseaux ciblés, ton, langue, fréquence de publication, secteur d'activité, mots-clés et description du profil.
- **Génération de publications par IA**, adaptées à chaque réseau social.
- **Calendrier éditorial** des publications planifiées.
- **Bibliothèque de posts** avec filtres (type, réseau, période, statut).
- **Validation, modification et rejet** des posts, avec génération d'une version alternative.
- **Suggestions de contenu** basées sur des annonces, des événements ou d'autres informations métier.
- **Tableau de bord et statistiques** d'activité.

### Options de configuration

| Paramètre | Valeurs possibles |
|---|---|
| Réseaux | LinkedIn, Facebook, Instagram |
| Ton | Professionnel, Accessible, Dynamique, Institutionnel |
| Langue | Français, Anglais, Arabe, Multilingue |
| Fréquence | 3 posts par semaine, 1 post par jour, 2 posts par jour |
| Types de contenu | Promotion d'annonce, Conseil d'expert, Commentaire d'actualité sectorielle, Témoignage client, Contenu engageant, Promotion d'événement |
| Statuts d'un post | Généré, En attente de validation, Prêt à publier, Rejeté |

## Architecture

```
┌──────────────────────┐      REST / JWT      ┌─────────────────────┐
│  Frontend Angular 21 │ ───────────────────► │  Backend Spring Boot │
│  (localhost:4200)    │                      │  (localhost:8080)    │
└──────────────────────┘                      └──────────┬──────────┘
                                                         │
                          ┌──────────────────────────────┼───────────────┐
                          ▼                              ▼               ▼
                    API Groq (LLM)                 API PayPal      Base de données
                                                                   (H2 / PostgreSQL)
```

## Structure du projet

```
Community-Manager-Virtual-IA/
├── backend/                              API Spring Boot (Java 17)
│   ├── pom.xml
│   └── src/main/java/tn/tunisiainvest/cm/
│       ├── config/                       CORS, clients Groq et PayPal, propriétés
│       ├── controller/                   Contrôleurs REST (/api/auth, /api/cm)
│       ├── dto/                          Objets de requête et de réponse
│       ├── model/                        Entités JPA et énumérations
│       ├── repository/                   Repositories Spring Data JPA
│       ├── security/                     Authentification JWT et configuration de sécurité
│       └── service/                      Logique métier, intégration Groq et PayPal
└── community-manager-frontend/           Application Angular 21
    └── src/app/
        ├── core/                         Services, guards, intercepteurs, modèles
        ├── features/                     Pages (landing, auth, dashboard, calendrier,
        │                                 posts, suggestions, configuration, abonnement,
        │                                 statistiques)
        ├── layout/                       Structure principale de l'application
        └── shared/                       Composants réutilisables
```

## Modèle de données

- **User** : compte utilisateur.
- **Subscription** : état de l'abonnement mensuel PayPal.
- **CMConfiguration** : paramètres d'activation (réseaux, ton, langue, fréquence, secteur, mots-clés, description du profil).
- **Post** : contenu généré, en attente de validation, prêt à publier ou rejeté.
- **Suggestion** : idée de contenu issue d'une annonce, d'un événement ou d'un autre besoin métier.

## Génération de contenu (Groq)

- `GroqContentService` construit un prompt adapté à chaque réseau à partir de la configuration de l'utilisateur et du contexte de génération.
- Les appels utilisent `WebClient` vers l'endpoint `/chat/completions` de l'API Groq (modèle par défaut : `llama-3.3-70b-versatile`).
- Si la clé API est absente, si Groq est indisponible ou si la réponse est inutilisable, le service renvoie un modèle de publication de secours accompagné d'un message d'information pour l'utilisateur.

## API REST

### Authentification (`/api/auth`)

| Méthode | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Création de compte |
| POST | `/api/auth/login` | Connexion (retourne un JWT) |

### Community manager (`/api/cm`)

| Méthode | Endpoint | Description |
|---|---|---|
| GET | `/api/cm/service` | Description du service |
| POST | `/api/cm/abonnement` | Initier un abonnement |
| POST | `/api/cm/abonnement/confirmer` | Confirmer l'abonnement PayPal |
| DELETE | `/api/cm/abonnement` | Résilier l'abonnement |
| PUT | `/api/cm/configuration` | Mettre à jour la configuration |
| GET | `/api/cm/calendrier` | Calendrier éditorial |
| POST | `/api/cm/generer` | Générer du contenu |
| GET | `/api/cm/posts` | Lister les posts (filtres : type, réseau, dates, statut) |
| PATCH | `/api/cm/posts/{id}/valider` | Valider un post |
| PATCH | `/api/cm/posts/{id}/modifier` | Modifier un post |
| POST | `/api/cm/posts/{id}/alternative` | Générer une version alternative |
| DELETE | `/api/cm/posts/{id}` | Supprimer un post |
| GET | `/api/cm/suggestions` | Suggestions de contenu |
| GET | `/api/cm/statistiques` | Statistiques |



## Technologies utilisées

**Backend**
- Spring Boot 3.3 (Web, WebFlux, Data JPA, Validation, Security)
- JWT (jjwt 0.12)
- H2 (développement) et PostgreSQL
- Maven, Java 17

**Frontend**
- Angular 21 et Angular Material
- RxJS, TypeScript
- Vitest (tests)

**Services externes**
- API Groq (génération de contenu par LLM)
- PayPal (abonnements)
