package tn.tunisiainvest.cm.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "cm_configurations")
public class CMConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subscription_id", nullable = false, unique = true)
    private Subscription subscription;

    @ElementCollection(targetClass = Network.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "cm_configuration_networks", joinColumns = @JoinColumn(name = "configuration_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "network", nullable = false)
    private Set<Network> targetNetworks = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tone tone = Tone.PROFESSIONAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Language language = Language.FRENCH;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Frequency frequency = Frequency.THREE_POSTS_PER_WEEK;

    @Column(nullable = false)
    private String sector;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "cm_configuration_keywords", joinColumns = @JoinColumn(name = "configuration_id"))
    @Column(name = "keyword", nullable = false)
    private Set<String> keywords = new HashSet<>();

    @Column(length = 4000)
    private String profileDescription;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "configuration")
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "configuration")
    private List<Suggestion> suggestions = new ArrayList<>();

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }

    public Set<Network> getTargetNetworks() {
        return targetNetworks;
    }

    public void setTargetNetworks(Set<Network> targetNetworks) {
        this.targetNetworks = targetNetworks;
    }

    public Tone getTone() {
        return tone;
    }

    public void setTone(Tone tone) {
        this.tone = tone;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public Frequency getFrequency() {
        return frequency;
    }

    public void setFrequency(Frequency frequency) {
        this.frequency = frequency;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public Set<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(Set<String> keywords) {
        this.keywords = keywords;
    }

    public String getProfileDescription() {
        return profileDescription;
    }

    public void setProfileDescription(String profileDescription) {
        this.profileDescription = profileDescription;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<Post> getPosts() {
        return posts;
    }

    public List<Suggestion> getSuggestions() {
        return suggestions;
    }
}
