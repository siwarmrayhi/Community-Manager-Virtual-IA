package tn.tunisiainvest.cm.controller;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tn.tunisiainvest.cm.dto.AlternativePostRequest;
import tn.tunisiainvest.cm.dto.CalendarEntryResponse;
import tn.tunisiainvest.cm.dto.ConfirmSubscriptionRequest;
import tn.tunisiainvest.cm.dto.ConfigurationRequest;
import tn.tunisiainvest.cm.dto.ConfigurationResponse;
import tn.tunisiainvest.cm.dto.DashboardResponse;
import tn.tunisiainvest.cm.dto.EditPostRequest;
import tn.tunisiainvest.cm.dto.GenerateMonthlyContentRequest;
import tn.tunisiainvest.cm.dto.GeneratePostRequest;
import tn.tunisiainvest.cm.dto.NewListingEvent;
import tn.tunisiainvest.cm.dto.PostResponse;
import tn.tunisiainvest.cm.dto.ReusePostRequest;
import tn.tunisiainvest.cm.dto.ServiceDescriptionResponse;
import tn.tunisiainvest.cm.dto.StatisticsResponse;
import tn.tunisiainvest.cm.dto.SubscriptionRequest;
import tn.tunisiainvest.cm.dto.SubscriptionResponse;
import tn.tunisiainvest.cm.dto.SuggestionResponse;
import tn.tunisiainvest.cm.model.ContentType;
import tn.tunisiainvest.cm.model.Network;
import tn.tunisiainvest.cm.model.PostStatus;
import tn.tunisiainvest.cm.service.CommunityManagerService;

@RestController
@RequestMapping("/api/cm")
public class CommunityManagerController {

    private final CommunityManagerService communityManagerService;

    public CommunityManagerController(CommunityManagerService communityManagerService) {
        this.communityManagerService = communityManagerService;
    }

    @GetMapping("/service")
    public ServiceDescriptionResponse getService() {
        return communityManagerService.getServiceDescription();
    }

    @PostMapping("/abonnement")
    @ResponseStatus(HttpStatus.CREATED)
    public SubscriptionResponse initiateSubscription(@Valid @RequestBody SubscriptionRequest request) {
        return communityManagerService.initiateSubscription(request.userEmail());
    }

    @PostMapping("/abonnement/confirmer")
    public SubscriptionResponse confirmSubscription(@Valid @RequestBody ConfirmSubscriptionRequest request) {
        return communityManagerService.confirmSubscription(request.userEmail(), request.paypalSubscriptionId());
    }

    @DeleteMapping("/abonnement")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unsubscribe(@RequestParam String userEmail) {
        communityManagerService.unsubscribe(userEmail);
    }

    @PutMapping("/configuration")
    public ConfigurationResponse updateConfiguration(@Valid @RequestBody ConfigurationRequest request) {
        return communityManagerService.updateConfiguration(request);
    }

    @GetMapping("/calendrier")
    public List<CalendarEntryResponse> getCalendar(@RequestParam String userEmail) {
        return communityManagerService.getCalendar(userEmail);
    }

    @PostMapping("/calendrier/generer")
    @ResponseStatus(HttpStatus.CREATED)
    public List<PostResponse> generateMonthlyCalendar(@Valid @RequestBody GenerateMonthlyContentRequest request) {
        return communityManagerService.generateMonthlyContent(request.userEmail());
    }

    @PostMapping("/generer")
    @ResponseStatus(HttpStatus.CREATED)
    public List<PostResponse> generate(@Valid @RequestBody GeneratePostRequest request) {
        return communityManagerService.generatePosts(request);
    }

    @GetMapping("/posts")
    public List<PostResponse> listPosts(
            @RequestParam String userEmail,
            @RequestParam(required = false) ContentType type,
            @RequestParam(required = false) Network network,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) PostStatus status
    ) {
        return communityManagerService.listPosts(userEmail, type, network, from, to, status);
    }

    @PatchMapping("/posts/{id}/valider")
    public PostResponse validatePost(@PathVariable Long id, @RequestParam String userEmail) {
        return communityManagerService.validatePost(id, userEmail);
    }

    @PatchMapping("/posts/{id}/modifier")
    public PostResponse editPost(@PathVariable Long id, @Valid @RequestBody EditPostRequest request) {
        return communityManagerService.editPost(id, request);
    }

    @PostMapping("/posts/{id}/alternative")
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponse generateAlternative(
            @PathVariable Long id,
            @Valid @RequestBody AlternativePostRequest request
    ) {
        return communityManagerService.generateAlternative(id, request);
    }

    @PostMapping("/posts/{id}/reutiliser")
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponse reusePost(
            @PathVariable Long id,
            @Valid @RequestBody ReusePostRequest request
    ) {
        return communityManagerService.reusePost(id, request);
    }

    @DeleteMapping("/posts/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rejectPost(@PathVariable Long id, @RequestParam String userEmail) {
        communityManagerService.rejectPost(id, userEmail);
    }

    @GetMapping("/suggestions")
    public List<SuggestionResponse> getSuggestions(@RequestParam String userEmail) {
        return communityManagerService.getSuggestions(userEmail);
    }

    /**
     * Webhook called by the listings module (binome 3) whenever a new
     * annonce is published, so the CM can automatically suggest a
     * promotional post (CDC section 6). Server-to-server call.
     */
    @PostMapping("/suggestions/annonce")
    @ResponseStatus(HttpStatus.CREATED)
    public List<SuggestionResponse> notifyNewListing(@Valid @RequestBody NewListingEvent event) {
        return communityManagerService.notifyNewListing(event);
    }

    @GetMapping("/statistiques")
    public StatisticsResponse getStatistics(@RequestParam String userEmail) {
        return communityManagerService.getStatistics(userEmail);
    }

    @GetMapping("/dashboard")
    public DashboardResponse getDashboard(@RequestParam String userEmail) {
        return communityManagerService.getDashboard(userEmail);
    }
}
