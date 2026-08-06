# Community Manager Virtuel IA API

Spring Boot backend for the Tunisia Invest Community Manager Virtuel IA frontend.

## Project Structure

```text
backend/
  pom.xml
  src/main/java/tn/tunisiainvest/cm/
    CommunityManagerApiApplication.java
    config/        # CORS, WebClient, app configuration
    controller/    # REST controllers for /api/cm/*
    dto/           # request/response payloads
    model/         # JPA entities and enums
    repository/    # Spring Data JPA repositories
    service/       # business logic, Groq integration, subscription flow
  src/main/resources/
    application.yml
```

## Domain Model

- `Subscription`: monthly PayPal subscription state for a partner or expert.
- `CMConfiguration`: activation settings, including networks, tone, language, frequency, sector, keywords, and profile description.
- `Post`: generated social content awaiting validation, ready to publish, or rejected.
- `Suggestion`: content ideas based on listings, events, or other business inputs.

## Groq Integration

- `GroqContentService` builds a network-specific prompt from the user configuration and generation context.
- Groq calls use `WebClient` against `/chat/completions` with the `llama3-8b-8192` model by default.
- If the API key is missing, Groq is unavailable, or the response cannot be used, the service returns a structured fallback template with a user-facing notice.

## REST API

All endpoints are exposed under `/api/cm`. Until authentication is connected, user-owned operations accept `userEmail` either in the request body or query string.

- `GET /api/cm/service`
- `POST /api/cm/abonnement`
- `POST /api/cm/abonnement/confirmer`
- `DELETE /api/cm/abonnement?userEmail=...`
- `PUT /api/cm/configuration`
- `GET /api/cm/calendrier?userEmail=...`
- `POST /api/cm/generer`
- `GET /api/cm/posts?userEmail=...&type=...&network=...&from=...&to=...&status=...`
- `PATCH /api/cm/posts/{id}/valider?userEmail=...`
- `PATCH /api/cm/posts/{id}/modifier`
- `POST /api/cm/posts/{id}/alternative`
- `DELETE /api/cm/posts/{id}?userEmail=...`
- `GET /api/cm/suggestions?userEmail=...`
- `GET /api/cm/statistiques?userEmail=...`

## Configuration

The Groq key is read from the environment and must never be committed:

```powershell
$env:GROQ_API_KEY="your-groq-key"
```

By default the API runs on `http://localhost:8080` and accepts Vite frontend calls
from `http://localhost:5173` and `http://127.0.0.1:5173`.

If another process is already using `8080`, run the backend on a dedicated API port:

```powershell
$env:SERVER_PORT="8082"
mvn spring-boot:run
```

Then set the frontend API URL to `http://localhost:8082/api/cm`.
