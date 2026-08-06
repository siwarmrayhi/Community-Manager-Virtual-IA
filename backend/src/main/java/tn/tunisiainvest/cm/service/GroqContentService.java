package tn.tunisiainvest.cm.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import tn.tunisiainvest.cm.config.GroqProperties;
import tn.tunisiainvest.cm.dto.GeneratedContent;
import tn.tunisiainvest.cm.dto.GenerationContext;
import tn.tunisiainvest.cm.model.Language;
import tn.tunisiainvest.cm.model.Network;
import tn.tunisiainvest.cm.model.Tone;

@Service
public class GroqContentService {

    private static final String FALLBACK_NOTICE =
            "Generation IA indisponible. Un modele de secours a ete utilise.";

    private final WebClient groqWebClient;
    private final GroqProperties properties;
    private final ObjectMapper objectMapper;

    public GroqContentService(WebClient groqWebClient, GroqProperties properties, ObjectMapper objectMapper) {
        this.groqWebClient = groqWebClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public GeneratedContent generate(GenerationContext context) {
        if (!StringUtils.hasText(properties.getApiKey())) {
            return fallback(context, "Cle Groq absente. " + FALLBACK_NOTICE);
        }

        String prompt = buildPrompt(context);
        GroqChatRequest request = new GroqChatRequest(
                properties.getModel(),
                List.of(
                        new GroqMessage("system", systemPrompt()),
                        new GroqMessage("user", prompt)
                ),
                0.7,
                1200
        );

        try {
            GroqChatResponse response = groqWebClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey())
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(GroqChatResponse.class)
                    .timeout(Duration.ofSeconds(30))
                    .onErrorResume(error -> Mono.empty())
                    .block();

            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                return fallback(context, FALLBACK_NOTICE);
            }

            String rawContent = response.choices().get(0).message().content();
            return parseGeneratedContent(rawContent, context);
        } catch (RuntimeException exception) {
            return fallback(context, FALLBACK_NOTICE);
        }
    }

    String buildPrompt(GenerationContext context) {
        Network network = context.network();
        return """
                Genere un post social media pour Tunisia Invest / Community Manager Virtuel IA.

                Configuration client:
                - Reseau cible: %s
                - Type de contenu: %s
                - Ton: %s
                - Langue: %s
                - Frequence editoriale: %s
                - Secteur: %s
                - Mots-cles: %s
                - Description du profil: %s
                - Sujet ou donnees du formulaire: %s
                - Instructions additionnelles: %s

                Contraintes reseau:
                %s

                Regles importantes:
                - Le contenu doit etre pret a etre relu, modifie et valide par l'utilisateur.
                - Ne jamais dire que le post est publie automatiquement.
                - Adapter le style au reseau cible, au secteur et a la langue demandee.
                - Eviter les promesses non verifiables et les donnees inventees.
                - Pour les histoires client, anonymiser tout detail personnel.

                Reponds uniquement en JSON valide avec les champs:
                {
                  "text": "texte du post",
                  "imageDescription": "description visuelle optionnelle",
                  "hashtags": "hashtags separes par espaces",
                  "callToAction": "appel a l'action"
                }
                """.formatted(
                network,
                context.contentType(),
                context.tone(),
                context.language(),
                context.frequency(),
                valueOrDefault(context.sector(), "Non precise"),
                formatKeywords(context.keywords()),
                valueOrDefault(context.profileDescription(), "Non precisee"),
                valueOrDefault(context.topic(), "Non precise"),
                valueOrDefault(context.additionalInstructions(), "Aucune"),
                networkConstraints(network)
        );
    }

    private String systemPrompt() {
        return """
                Tu es un assistant senior en communication digitale pour des experts, partenaires et acteurs
                de l'investissement en Tunisie. Tu produis des contenus professionnels, clairs et adaptés
                a LinkedIn, Facebook ou Instagram. Tu respectes strictement le format JSON demande.
                """;
    }

    private GeneratedContent parseGeneratedContent(String rawContent, GenerationContext context) {
        if (!StringUtils.hasText(rawContent)) {
            return fallback(context, FALLBACK_NOTICE);
        }

        try {
            JsonNode node = objectMapper.readTree(extractJson(rawContent));
            String text = textValue(node, "text");
            if (!StringUtils.hasText(text)) {
                return fallback(context, FALLBACK_NOTICE);
            }

            return new GeneratedContent(
                    text,
                    textValue(node, "imageDescription"),
                    textValue(node, "hashtags"),
                    textValue(node, "callToAction"),
                    false,
                    "Contenu genere avec Groq."
            );
        } catch (JsonProcessingException exception) {
            return new GeneratedContent(
                    rawContent.trim(),
                    defaultImageDescription(context),
                    defaultHashtags(context),
                    defaultCallToAction(context),
                    false,
                    "Contenu Groq recu hors format JSON, enrichi par le backend."
            );
        }
    }

    private GeneratedContent fallback(GenerationContext context, String notice) {
        String topic = valueOrDefault(context.topic(), "votre actualite");
        String sector = valueOrDefault(context.sector(), "votre secteur");
        String text = switch (context.network()) {
            case LINKEDIN -> """
                    %s

                    Dans le secteur %s, la regularite et la clarte font la difference. Voici une piste de contenu a valider: %s.

                    Notre recommandation: mettre en avant l'expertise, expliquer la valeur concrete pour votre audience et terminer par une invitation a l'echange.
                    """.formatted(opening(context), sector, topic).trim();
            case FACEBOOK -> """
                    %s

                    Une idee simple a partager avec votre communaute: %s.

                    Objectif: informer, creer de la proximite et inviter vos contacts a reagir.
                    """.formatted(opening(context), topic).trim();
            case INSTAGRAM -> "%s %s. %s".formatted(opening(context), topic, defaultHashtags(context));
        };

        return new GeneratedContent(
                text,
                defaultImageDescription(context),
                defaultHashtags(context),
                defaultCallToAction(context),
                true,
                notice
        );
    }

    private String opening(GenerationContext context) {
        if (context.language() == Language.ENGLISH) {
            return "New content idea to review before publishing:";
        }
        if (context.language() == Language.ARABIC) {
            return "فكرة محتوى جديدة للمراجعة قبل النشر:";
        }
        return "Nouvelle idee de contenu a valider avant publication :";
    }

    private String networkConstraints(Network network) {
        return switch (network) {
            case LINKEDIN -> """
                    - Environ 1500 caracteres.
                    - Ton professionnel et utile.
                    - Inclure hashtags sectoriels limites.
                    - Inclure un appel a l'action clair.
                    """;
            case FACEBOOK -> """
                    - Environ 500 caracteres.
                    - Ton accessible et proche.
                    - Quelques emojis pertinents sont acceptes.
                    - Favoriser l'engagement et les commentaires.
                    """;
            case INSTAGRAM -> """
                    - Environ 150 caracteres pour la legende principale.
                    - Plusieurs hashtags pertinents.
                    - Inclure une description d'image exploitable.
                    """;
        };
    }

    private String defaultImageDescription(GenerationContext context) {
        return switch (context.network()) {
            case LINKEDIN -> "Visuel professionnel lie au secteur " + valueOrDefault(context.sector(), "du client");
            case FACEBOOK -> "Image chaleureuse illustrant le sujet: " + valueOrDefault(context.topic(), "actualite");
            case INSTAGRAM -> "Visuel vertical clair, moderne, avec un element central lie au sujet.";
        };
    }

    private String defaultHashtags(GenerationContext context) {
        String base = switch (context.network()) {
            case LINKEDIN -> "#TunisiaInvest #Expertise #Business";
            case FACEBOOK -> "#Tunisie #Business #Communaute";
            case INSTAGRAM -> "#TunisiaInvest #Tunisie #Business #Entrepreneur #Investissement #Expertise";
        };

        if (context.keywords() == null || context.keywords().isEmpty()) {
            return base;
        }

        return base + " " + context.keywords().stream()
                .filter(StringUtils::hasText)
                .map(keyword -> "#" + keyword.replaceAll("[^A-Za-z0-9]", ""))
                .filter(hashtag -> hashtag.length() > 1)
                .limit(4)
                .reduce("", (left, right) -> left + " " + right)
                .trim();
    }

    private String defaultCallToAction(GenerationContext context) {
        return switch (context.tone() == null ? Tone.PROFESSIONAL : context.tone()) {
            case APPROACHABLE -> "Dites-nous ce que vous en pensez en commentaire.";
            case DYNAMIC -> "Contactez-nous pour passer a l'action.";
            case INSTITUTIONAL -> "Contactez notre equipe pour en savoir plus.";
            case PROFESSIONAL -> "Echangeons sur vos objectifs.";
        };
    }

    private String extractJson(String rawContent) {
        int start = rawContent.indexOf('{');
        int end = rawContent.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return rawContent.substring(start, end + 1);
        }
        return rawContent;
    }

    private String textValue(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        return value == null || value.isNull() ? "" : value.asText();
    }

    private String formatKeywords(Set<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return "Aucun";
        }
        return String.join(", ", keywords);
    }

    private String valueOrDefault(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GroqChatResponse(List<GroqChoice> choices) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GroqChoice(GroqMessage message) {
    }

    private record GroqChatRequest(
            String model,
            List<GroqMessage> messages,
            double temperature,
            int max_tokens
    ) {
    }

    private record GroqMessage(String role, String content) {
    }
}
