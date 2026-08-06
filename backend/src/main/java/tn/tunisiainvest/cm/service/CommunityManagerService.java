package tn.tunisiainvest.cm.service;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tn.tunisiainvest.cm.dto.AlternativePostRequest;
import tn.tunisiainvest.cm.dto.CalendarEntryResponse;
import tn.tunisiainvest.cm.dto.ConfigurationRequest;
import tn.tunisiainvest.cm.dto.ConfigurationResponse;
import tn.tunisiainvest.cm.dto.DashboardResponse;
import tn.tunisiainvest.cm.dto.EditPostRequest;
import tn.tunisiainvest.cm.dto.GeneratedContent;
import tn.tunisiainvest.cm.dto.GeneratePostRequest;
import tn.tunisiainvest.cm.dto.GenerationContext;
import tn.tunisiainvest.cm.dto.NewListingEvent;
import tn.tunisiainvest.cm.dto.PostResponse;
import tn.tunisiainvest.cm.dto.ReusePostRequest;
import tn.tunisiainvest.cm.dto.ServiceDescriptionResponse;
import tn.tunisiainvest.cm.dto.StatisticsResponse;
import tn.tunisiainvest.cm.dto.SubscriptionResponse;
import tn.tunisiainvest.cm.dto.SuggestionResponse;
import tn.tunisiainvest.cm.model.CMConfiguration;
import tn.tunisiainvest.cm.model.ContentType;
import tn.tunisiainvest.cm.model.Frequency;
import tn.tunisiainvest.cm.model.Language;
import tn.tunisiainvest.cm.model.Network;
import tn.tunisiainvest.cm.model.Post;
import tn.tunisiainvest.cm.model.PostStatus;
import tn.tunisiainvest.cm.model.Subscription;
import tn.tunisiainvest.cm.model.SubscriptionStatus;
import tn.tunisiainvest.cm.model.Suggestion;
import tn.tunisiainvest.cm.model.SuggestionStatus;
import tn.tunisiainvest.cm.model.Tone;
import tn.tunisiainvest.cm.repository.CMConfigurationRepository;
import tn.tunisiainvest.cm.repository.PostRepository;
import tn.tunisiainvest.cm.repository.SubscriptionRepository;
import tn.tunisiainvest.cm.repository.SuggestionRepository;

@Service
public class CommunityManagerService {

    private static final BigDecimal MONTHLY_PRICE = BigDecimal.valueOf(99);

    private static final int WELCOME_POSTS_COUNT = 5;

    private final SubscriptionRepository subscriptionRepository;
    private final CMConfigurationRepository configurationRepository;
    private final PostRepository postRepository;
    private final SuggestionRepository suggestionRepository;
    private final GroqContentService groqContentService;
    private final PayPalService payPalService;

    public CommunityManagerService(
            SubscriptionRepository subscriptionRepository,
            CMConfigurationRepository configurationRepository,
            PostRepository postRepository,
            SuggestionRepository suggestionRepository,
            GroqContentService groqContentService,
            PayPalService payPalService
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.configurationRepository = configurationRepository;
        this.postRepository = postRepository;
        this.suggestionRepository = suggestionRepository;
        this.groqContentService = groqContentService;
        this.payPalService = payPalService;
    }

    public ServiceDescriptionResponse getServiceDescription() {
        return new ServiceDescriptionResponse(
                "Community Manager Virtuel IA",
                "Generation assistee par IA de contenus LinkedIn, Facebook et Instagram avec validation humaine obligatoire.",
                MONTHLY_PRICE,
                "EUR",
                enumNames(Network.values()),
                List.of("generated", "pending validation", "validate/edit/alternative/reject", "ready to publish"),
                enumNames(ContentType.values())
        );
    }

    @Transactional
    public SubscriptionResponse initiateSubscription(String userEmail) {
        PayPalService.PayPalSubscriptionResult result = payPalService.createSubscription(userEmail);

        Subscription subscription = new Subscription();
        subscription.setUserEmail(userEmail);
        subscription.setMonthlyPrice(MONTHLY_PRICE);
        subscription.setCurrency("EUR");
        subscription.setStatus(SubscriptionStatus.PENDING);
        subscription.setPaypalSubscriptionId(result.paypalSubscriptionId());
        Subscription saved = subscriptionRepository.save(subscription);
        return toSubscriptionResponse(saved, result.approvalUrl());
    }

    /**
     * Called once the partner comes back from the PayPal approval page.
     * Verifies the subscription status with PayPal (or the local sandbox
     * simulation), activates it, and - per CDC 8.4 - immediately generates
     * the 5 welcome posts across the configured networks.
     */
    @Transactional
    public SubscriptionResponse confirmSubscription(String userEmail, String paypalSubscriptionId) {
        Subscription subscription = subscriptionRepository.findFirstByUserEmailOrderByCreatedAtDesc(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found for " + userEmail));

        PayPalService.PayPalSubscriptionResult confirmed = payPalService.fetchSubscription(paypalSubscriptionId);
        if (!"ACTIVE".equalsIgnoreCase(confirmed.status()) && !"APPROVAL_PENDING".equalsIgnoreCase(confirmed.status())) {
            throw new IllegalStateException("Abonnement PayPal non actif (statut: " + confirmed.status() + ").");
        }

        boolean wasAlreadyActive = subscription.getStatus() == SubscriptionStatus.ACTIVE;
        subscription.setPaypalSubscriptionId(paypalSubscriptionId);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartedAt(Instant.now());
        Subscription saved = subscriptionRepository.save(subscription);

        if (!wasAlreadyActive) {
            generateWelcomePosts(saved);
        }

        return toSubscriptionResponse(saved, null);
    }

    @Transactional
    public void unsubscribe(String userEmail) {
        Subscription subscription = subscriptionRepository.findFirstByUserEmailAndStatusOrderByCreatedAtDesc(
                        userEmail,
                        SubscriptionStatus.ACTIVE
                )
                .orElseThrow(() -> new ResourceNotFoundException("Active subscription not found for " + userEmail));
        payPalService.cancelSubscription(subscription.getPaypalSubscriptionId(), "Desabonnement demande par le client");
        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setCancelledAt(Instant.now());
    }

    /**
     * CDC 8.4 "Premiere generation": at activation, 5 welcome posts are
     * generated immediately, spread across the partner's configured
     * networks and a mix of content types, planned starting today.
     */
    private void generateWelcomePosts(Subscription subscription) {
        configurationRepository.findBySubscriptionId(subscription.getId()).ifPresent(configuration -> {
            if (configuration.getTargetNetworks().isEmpty()) {
                return;
            }
            List<Network> networks = new ArrayList<>(configuration.getTargetNetworks());
            ContentType[] contentTypes = ContentType.values();
            LocalDate startDate = LocalDate.now();

            for (int i = 0; i < WELCOME_POSTS_COUNT; i++) {
                Network network = networks.get(i % networks.size());
                ContentType contentType = contentTypes[i % contentTypes.length];
                GeneratedContent content = groqContentService.generate(new GenerationContext(
                        network,
                        contentType,
                        configuration.getTone(),
                        configuration.getLanguage(),
                        configuration.getFrequency(),
                        configuration.getSector(),
                        configuration.getKeywords(),
                        configuration.getProfileDescription(),
                        "Post de bienvenue " + (i + 1) + " suite a l'activation de l'abonnement",
                        null
                ));
                Post post = buildPost(configuration, network, contentType, content, startDate.plusDays(i));
                postRepository.save(post);
            }
        });
    }

    @Transactional
    public ConfigurationResponse updateConfiguration(ConfigurationRequest request) {
        Subscription subscription = subscriptionRepository.findFirstByUserEmailOrderByCreatedAtDesc(request.userEmail())
                .orElseGet(() -> {
                    Subscription created = new Subscription();
                    created.setUserEmail(request.userEmail());
                    created.setStatus(SubscriptionStatus.PENDING);
                    created.setMonthlyPrice(MONTHLY_PRICE);
                    created.setCurrency("EUR");
                    return subscriptionRepository.save(created);
                });

        CMConfiguration configuration = configurationRepository.findBySubscriptionId(subscription.getId())
                .orElseGet(() -> {
                    CMConfiguration created = new CMConfiguration();
                    created.setSubscription(subscription);
                    return created;
                });

        configuration.setTargetNetworks(new HashSet<>(request.targetNetworks()));
        configuration.setTone(request.tone() == null ? Tone.PROFESSIONAL : request.tone());
        configuration.setLanguage(request.language() == null ? Language.FRENCH : request.language());
        configuration.setFrequency(request.frequency() == null ? Frequency.THREE_POSTS_PER_WEEK : request.frequency());
        configuration.setSector(request.sector());
        configuration.setKeywords(request.keywords() == null ? Set.of() : request.keywords());
        configuration.setProfileDescription(request.profileDescription());

        return toConfigurationResponse(configurationRepository.save(configuration));
    }

    private static final int CALENDAR_WINDOW_DAYS = 14;
    private static final int MONTHLY_GENERATION_DAYS = 30;

    /**
     * Editorial calendar view (CDC section 4): 2 weeks, day by day, with the
     * network/type/status of every planned post. Days that should carry a
     * post according to the configured frequency but have nothing generated
     * yet are still flagged (postingDay=true, posts=[]) so the "Generer le
     * contenu du mois" gap is visible before the button is used.
     */
    @Transactional(readOnly = true)
    public List<CalendarEntryResponse> getCalendar(String userEmail) {
        CMConfiguration configuration = findConfiguration(userEmail);
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(CALENDAR_WINDOW_DAYS - 1);
        Map<LocalDate, List<PostResponse>> postsByDate = postRepository
                .findByConfigurationAndPlannedDateBetweenOrderByPlannedDateAsc(configuration, start, end)
                .stream()
                .map(this::toPostResponse)
                .collect(Collectors.groupingBy(PostResponse::plannedDate));

        ContentType[] types = ContentType.values();
        List<CalendarEntryResponse> calendar = new ArrayList<>();
        int contentTypeCursor = 0;
        for (int i = 0; i < CALENDAR_WINDOW_DAYS; i++) {
            LocalDate date = start.plusDays(i);
            boolean postingDay = isPostingDay(configuration.getFrequency(), date);
            ContentType suggested = postingDay ? types[contentTypeCursor % types.length] : null;
            if (postingDay) {
                contentTypeCursor++;
            }
            calendar.add(new CalendarEntryResponse(date, postingDay, suggested, postsByDate.getOrDefault(date, List.of())));
        }
        return calendar;
    }

    /**
     * "Generer le contenu du mois" button: fills every posting day of the
     * next 30 days that has no post yet, respecting the configured
     * frequency, rotating across the configured networks and content types.
     * Existing posts (already generated, validated, or rejected) are left
     * untouched so re-running this never duplicates content.
     */
    @Transactional
    public List<PostResponse> generateMonthlyContent(String userEmail) {
        CMConfiguration configuration = findConfiguration(userEmail);
        if (configuration.getTargetNetworks().isEmpty()) {
            throw new IllegalStateException("Aucun reseau cible configure pour generer le calendrier.");
        }

        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(MONTHLY_GENERATION_DAYS - 1);
        Set<LocalDate> datesWithPosts = postRepository
                .findByConfigurationAndPlannedDateBetweenOrderByPlannedDateAsc(configuration, start, end)
                .stream()
                .map(Post::getPlannedDate)
                .collect(Collectors.toSet());

        List<Network> networks = new ArrayList<>(configuration.getTargetNetworks());
        ContentType[] contentTypes = ContentType.values();
        int slotsPerDay = postsPerPostingDay(configuration.getFrequency());

        List<PostResponse> created = new ArrayList<>();
        int cursor = 0;
        for (int i = 0; i < MONTHLY_GENERATION_DAYS; i++) {
            LocalDate date = start.plusDays(i);
            if (!isPostingDay(configuration.getFrequency(), date) || datesWithPosts.contains(date)) {
                continue;
            }
            for (int slot = 0; slot < slotsPerDay; slot++) {
                Network network = networks.get(cursor % networks.size());
                ContentType contentType = contentTypes[cursor % contentTypes.length];
                cursor++;

                GeneratedContent content = groqContentService.generate(new GenerationContext(
                        network,
                        contentType,
                        configuration.getTone(),
                        configuration.getLanguage(),
                        configuration.getFrequency(),
                        configuration.getSector(),
                        configuration.getKeywords(),
                        configuration.getProfileDescription(),
                        "Contenu du calendrier editorial pour le " + date,
                        null
                ));
                Post post = buildPost(configuration, network, contentType, content, date);
                created.add(toPostResponse(postRepository.save(post)));
            }
        }
        return created;
    }

    private boolean isPostingDay(Frequency frequency, LocalDate date) {
        return switch (frequency) {
            case ONE_POST_PER_DAY, TWO_POSTS_PER_DAY -> true;
            case THREE_POSTS_PER_WEEK -> {
                DayOfWeek day = date.getDayOfWeek();
                yield day == DayOfWeek.MONDAY || day == DayOfWeek.WEDNESDAY || day == DayOfWeek.FRIDAY;
            }
        };
    }

    private int postsPerPostingDay(Frequency frequency) {
        return frequency == Frequency.TWO_POSTS_PER_DAY ? 2 : 1;
    }

    @Transactional
    public List<PostResponse> generatePosts(GeneratePostRequest request) {
        CMConfiguration configuration = findConfiguration(request.userEmail());
        Set<Network> networks = request.networks() == null || request.networks().isEmpty()
                ? configuration.getTargetNetworks()
                : request.networks();
        ContentType contentType = request.contentType() == null ? ContentType.EXPERT_ADVICE : request.contentType();

        List<PostResponse> responses = new ArrayList<>();
        for (Network network : networks) {
            GeneratedContent content = groqContentService.generate(new GenerationContext(
                    network,
                    contentType,
                    configuration.getTone(),
                    configuration.getLanguage(),
                    configuration.getFrequency(),
                    configuration.getSector(),
                    configuration.getKeywords(),
                    configuration.getProfileDescription(),
                    request.topic(),
                    request.additionalInstructions()
            ));
            Post post = buildPost(configuration, network, contentType, content, request.plannedDate());
            responses.add(toPostResponse(postRepository.save(post)));
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public List<PostResponse> listPosts(
            String userEmail,
            ContentType contentType,
            Network network,
            LocalDate from,
            LocalDate to,
            PostStatus status
    ) {
        CMConfiguration configuration = findConfiguration(userEmail);
        Specification<Post> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(builder.equal(root.get("configuration"), configuration));
            if (contentType != null) {
                predicates.add(builder.equal(root.get("contentType"), contentType));
            }
            if (network != null) {
                predicates.add(builder.equal(root.get("network"), network));
            }
            if (status != null) {
                predicates.add(builder.equal(root.get("status"), status));
            }
            if (from != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("plannedDate"), from));
            }
            if (to != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("plannedDate"), to));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };

        return postRepository.findAll(specification).stream()
                .sorted(Comparator.comparing(Post::getPlannedDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Post::getCreatedAt).reversed())
                .map(this::toPostResponse)
                .toList();
    }

    @Transactional
    public PostResponse validatePost(Long postId, String userEmail) {
        Post post = findOwnedPost(postId, userEmail);
        post.setStatus(PostStatus.READY_TO_PUBLISH);
        post.setValidatedAt(Instant.now());
        return toPostResponse(post);
    }

    @Transactional
    public PostResponse editPost(Long postId, EditPostRequest request) {
        Post post = findOwnedPost(postId, request.userEmail());
        post.setText(request.text());
        post.setStatus(PostStatus.PENDING_VALIDATION);
        return toPostResponse(post);
    }

    @Transactional
    public PostResponse generateAlternative(Long postId, AlternativePostRequest request) {
        Post original = findOwnedPost(postId, request.userEmail());
        CMConfiguration configuration = original.getConfiguration();
        GeneratedContent content = groqContentService.generate(new GenerationContext(
                original.getNetwork(),
                original.getContentType(),
                configuration.getTone(),
                configuration.getLanguage(),
                configuration.getFrequency(),
                configuration.getSector(),
                configuration.getKeywords(),
                configuration.getProfileDescription(),
                "Alternative pour ce post: " + original.getText(),
                request.additionalInstructions()
        ));
        Post alternative = buildPost(
                configuration,
                original.getNetwork(),
                original.getContentType(),
                content,
                original.getPlannedDate()
        );
        return toPostResponse(postRepository.save(alternative));
    }

    /**
     * CDC section 7 - content library: takes any post already in the
     * library (validated, rejected or still pending) and uses it as the
     * basis to generate a brand new, similar post. The original post is
     * left untouched so the history stays intact.
     */
    @Transactional
    public PostResponse reusePost(Long postId, ReusePostRequest request) {
        Post original = findOwnedPost(postId, request.userEmail());
        CMConfiguration configuration = original.getConfiguration();
        Network network = request.network() == null ? original.getNetwork() : request.network();

        GeneratedContent content = groqContentService.generate(new GenerationContext(
                network,
                original.getContentType(),
                configuration.getTone(),
                configuration.getLanguage(),
                configuration.getFrequency(),
                configuration.getSector(),
                configuration.getKeywords(),
                configuration.getProfileDescription(),
                "Genere un nouveau post similaire, en t'inspirant de ce post existant: " + original.getText(),
                request.additionalInstructions()
        ));

        Post reused = buildPost(configuration, network, original.getContentType(), content, request.plannedDate());
        return toPostResponse(postRepository.save(reused));
    }

    @Transactional
    public void rejectPost(Long postId, String userEmail) {
        Post post = findOwnedPost(postId, userEmail);
        post.setStatus(PostStatus.REJECTED);
        post.setRejectedAt(Instant.now());
    }

    /**
     * CDC section 6: "L'IA surveille les nouvelles annonces et evenements
     * pour suggerer des sujets". Invoked by the listings module (binome 3)
     * whenever a new annonce is published: creates a LISTING_PROMO
     * suggestion (with sector hashtags pre-filled) for every active
     * subscription whose configuration matches, skipping subscriptions that
     * already received a suggestion for that same listing.
     */
    @Transactional
    public List<SuggestionResponse> notifyNewListing(NewListingEvent event) {
        List<Subscription> targets;
        if (StringUtils.hasText(event.targetUserEmail())) {
            targets = subscriptionRepository
                    .findFirstByUserEmailAndStatusOrderByCreatedAtDesc(event.targetUserEmail(), SubscriptionStatus.ACTIVE)
                    .map(List::of)
                    .orElse(List.of());
        } else {
            targets = subscriptionRepository.findByStatus(SubscriptionStatus.ACTIVE);
        }

        List<Suggestion> createdSuggestions = new ArrayList<>();
        for (Subscription subscription : targets) {
            configurationRepository.findBySubscriptionId(subscription.getId()).ifPresent(configuration -> {
                boolean sectorMatches = !StringUtils.hasText(event.sector())
                        || !StringUtils.hasText(configuration.getSector())
                        || configuration.getSector().equalsIgnoreCase(event.sector());
                if (!sectorMatches) {
                    return;
                }
                if (suggestionRepository.existsByConfigurationAndSourceReference(configuration, event.listingReference())) {
                    return;
                }

                Suggestion listingSuggestion = new Suggestion();
                listingSuggestion.setConfiguration(configuration);
                listingSuggestion.setContentType(ContentType.LISTING_PROMO);
                listingSuggestion.setTitle("Nouvelle annonce a promouvoir : " + event.title());
                listingSuggestion.setDescription(StringUtils.hasText(event.description())
                        ? event.description()
                        : "Une nouvelle opportunite vient d'etre publiee, pensez a la partager sur vos reseaux.");
                listingSuggestion.setSuggestedDate(LocalDate.now());
                listingSuggestion.setStatus(SuggestionStatus.NEW);
                listingSuggestion.setSourceReference(event.listingReference());
                listingSuggestion.setSuggestedHashtags(buildSectorHashtags(configuration, event.keywords()));
                createdSuggestions.add(suggestionRepository.save(listingSuggestion));
            });
        }
        return createdSuggestions.stream().map(this::toSuggestionResponse).toList();
    }

    private String buildSectorHashtags(CMConfiguration configuration, Set<String> extraKeywords) {
        Set<String> allKeywords = new java.util.LinkedHashSet<>();
        if (configuration.getKeywords() != null) {
            allKeywords.addAll(configuration.getKeywords());
        }
        if (extraKeywords != null) {
            allKeywords.addAll(extraKeywords);
        }
        if (StringUtils.hasText(configuration.getSector())) {
            allKeywords.add(configuration.getSector());
        }
        return allKeywords.stream()
                .filter(StringUtils::hasText)
                .map(keyword -> "#" + keyword.replaceAll("[^A-Za-z0-9]", ""))
                .filter(hashtag -> hashtag.length() > 1)
                .limit(6)
                .collect(Collectors.joining(" "));
    }

    @Transactional
    public List<SuggestionResponse> getSuggestions(String userEmail) {
        CMConfiguration configuration = findConfiguration(userEmail);
        List<Suggestion> suggestions = suggestionRepository
                .findByConfigurationAndStatusOrderBySuggestedDateAscCreatedAtDesc(configuration, SuggestionStatus.NEW);
        if (suggestions.isEmpty()) {
            suggestions = createDefaultSuggestions(configuration);
        }
        return suggestions.stream().map(this::toSuggestionResponse).toList();
    }

    @Transactional(readOnly = true)
    public StatisticsResponse getStatistics(String userEmail) {
        CMConfiguration configuration = findConfiguration(userEmail);
        List<Post> posts = postRepository.findByConfigurationOrderByPlannedDateAscCreatedAtDesc(configuration);
        long generated = posts.size();
        long validated = posts.stream().filter(post -> post.getStatus() == PostStatus.READY_TO_PUBLISH).count();
        double validationRate = generated == 0 ? 0 : (validated * 100.0) / generated;

        ContentType mostUsedType = mostUsed(posts, Post::getContentType);
        Network mostUsedNetwork = mostUsed(posts, Post::getNetwork);
        Map<String, Long> byStatus = posts.stream()
                .collect(Collectors.groupingBy(post -> post.getStatus().name(), Collectors.counting()));

        return new StatisticsResponse(generated, validated, validationRate, mostUsedType, mostUsedNetwork, byStatus);
    }

    private static final int DASHBOARD_CALENDAR_PREVIEW_DAYS = 5;

    /**
     * Aggregates everything the CM dashboard needs in a single call:
     * activation status, current configuration, how many posts/suggestions
     * are waiting on the partner, the next planned post, a compact calendar
     * preview and quick stats - plus the list of quick-access actions.
     */
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(String userEmail) {
        Subscription subscription = subscriptionRepository.findFirstByUserEmailOrderByCreatedAtDesc(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found for " + userEmail));
        CMConfiguration configuration = configurationRepository.findBySubscriptionId(subscription.getId())
                .orElse(null);

        ConfigurationResponse configurationResponse = configuration == null ? null : toConfigurationResponse(configuration);

        long pendingValidationCount = configuration == null
                ? 0
                : postRepository.countByConfigurationAndStatus(configuration, PostStatus.PENDING_VALIDATION);
        long newSuggestionsCount = configuration == null
                ? 0
                : suggestionRepository.countByConfigurationAndStatus(configuration, SuggestionStatus.NEW);

        PostResponse nextPlannedPost = configuration == null
                ? null
                : postRepository.findByConfigurationOrderByPlannedDateAscCreatedAtDesc(configuration).stream()
                .filter(post -> post.getStatus() != PostStatus.REJECTED)
                .filter(post -> post.getPlannedDate() == null || !post.getPlannedDate().isBefore(LocalDate.now()))
                .findFirst()
                .map(this::toPostResponse)
                .orElse(null);

        List<CalendarEntryResponse> upcomingCalendar = configuration == null
                ? List.of()
                : getCalendar(userEmail).stream().limit(DASHBOARD_CALENDAR_PREVIEW_DAYS).toList();

        StatisticsResponse statistics = configuration == null
                ? new StatisticsResponse(0, 0, 0, null, null, Map.of())
                : getStatistics(userEmail);

        List<String> quickActions = List.of(
                "generer_contenu",
                "voir_calendrier",
                "valider_posts_en_attente",
                "modifier_configuration",
                "acceder_bibliotheque"
        );

        return new DashboardResponse(
                subscription.getStatus(),
                configurationResponse,
                pendingValidationCount,
                newSuggestionsCount,
                nextPlannedPost,
                upcomingCalendar,
                statistics,
                quickActions
        );
    }

    private Post buildPost(
            CMConfiguration configuration,
            Network network,
            ContentType contentType,
            GeneratedContent content,
            LocalDate plannedDate
    ) {
        Post post = new Post();
        post.setConfiguration(configuration);
        post.setNetwork(network);
        post.setContentType(contentType);
        post.setStatus(PostStatus.PENDING_VALIDATION);
        post.setText(content.text());
        post.setImageDescription(content.imageDescription());
        post.setHashtags(content.hashtags());
        post.setCallToAction(content.callToAction());
        post.setPlannedDate(plannedDate);
        post.setGeneratedByFallback(content.fallback());
        post.setGenerationNotice(content.notice());
        return post;
    }

    private List<Suggestion> createDefaultSuggestions(CMConfiguration configuration) {
        LocalDate today = LocalDate.now();
        List<Suggestion> defaults = List.of(
                suggestion(configuration, ContentType.LISTING_PROMO, "Mettre en avant une nouvelle opportunite", "Transformer une annonce ou un listing recent en post multi-reseau.", today.plusDays(1)),
                suggestion(configuration, ContentType.EXPERT_ADVICE, "Partager un conseil sectoriel", "Publier une recommandation concrete liee au secteur " + configuration.getSector() + ".", today.plusDays(3)),
                suggestion(configuration, ContentType.ENGAGING_CONTENT, "Lancer une question ouverte", "Inviter la communaute a reagir sur une tendance ou un besoin client.", today.plusDays(5))
        );
        return suggestionRepository.saveAll(defaults);
    }

    private Suggestion suggestion(
            CMConfiguration configuration,
            ContentType contentType,
            String title,
            String description,
            LocalDate date
    ) {
        Suggestion suggestion = new Suggestion();
        suggestion.setConfiguration(configuration);
        suggestion.setContentType(contentType);
        suggestion.setTitle(title);
        suggestion.setDescription(description);
        suggestion.setSuggestedDate(date);
        suggestion.setStatus(SuggestionStatus.NEW);
        return suggestion;
    }

    private CMConfiguration findConfiguration(String userEmail) {
        if (!StringUtils.hasText(userEmail)) {
            throw new IllegalArgumentException("userEmail is required");
        }
        return configurationRepository.findFirstBySubscriptionUserEmailOrderByUpdatedAtDesc(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Configuration not found for " + userEmail));
    }

    private Post findOwnedPost(Long postId, String userEmail) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + postId));
        String owner = post.getConfiguration().getSubscription().getUserEmail();
        if (!owner.equals(userEmail)) {
            throw new ResourceNotFoundException("Post not found: " + postId);
        }
        return post;
    }

    private SubscriptionResponse toSubscriptionResponse(Subscription subscription, String approvalUrl) {
        return new SubscriptionResponse(
                subscription.getId(),
                subscription.getUserEmail(),
                subscription.getStatus(),
                subscription.getMonthlyPrice(),
                subscription.getCurrency(),
                subscription.getPaypalSubscriptionId(),
                approvalUrl,
                subscription.getStartedAt(),
                subscription.getCancelledAt()
        );
    }

    private ConfigurationResponse toConfigurationResponse(CMConfiguration configuration) {
        return new ConfigurationResponse(
                configuration.getId(),
                configuration.getSubscription().getId(),
                configuration.getTargetNetworks(),
                configuration.getTone(),
                configuration.getLanguage(),
                configuration.getFrequency(),
                configuration.getSector(),
                configuration.getKeywords(),
                configuration.getProfileDescription(),
                configuration.getCreatedAt(),
                configuration.getUpdatedAt()
        );
    }

    private PostResponse toPostResponse(Post post) {
        return new PostResponse(
                post.getId(),
                post.getNetwork(),
                post.getContentType(),
                post.getStatus(),
                post.getText(),
                post.getImageDescription(),
                post.getHashtags(),
                post.getCallToAction(),
                post.getPlannedDate(),
                post.isGeneratedByFallback(),
                post.getGenerationNotice(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getValidatedAt(),
                post.getRejectedAt()
        );
    }

    private SuggestionResponse toSuggestionResponse(Suggestion suggestion) {
        return new SuggestionResponse(
                suggestion.getId(),
                suggestion.getContentType(),
                suggestion.getTitle(),
                suggestion.getDescription(),
                suggestion.getSuggestedHashtags(),
                suggestion.getSourceReference(),
                suggestion.getSuggestedDate(),
                suggestion.getStatus(),
                suggestion.getCreatedAt()
        );
    }

    private <T> T mostUsed(List<Post> posts, Function<Post, T> mapper) {
        return posts.stream()
                .map(mapper)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private List<String> enumNames(Enum<?>[] values) {
        return Arrays.stream(values).map(Enum::name).toList();
    }
}
