package tn.tunisiainvest.cm.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.tunisiainvest.cm.model.Subscription;
import tn.tunisiainvest.cm.model.SubscriptionStatus;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findFirstByUserEmailOrderByCreatedAtDesc(String userEmail);

    Optional<Subscription> findFirstByUserEmailAndStatusOrderByCreatedAtDesc(
            String userEmail,
            SubscriptionStatus status
    );

    List<Subscription> findByStatus(SubscriptionStatus status);
}
