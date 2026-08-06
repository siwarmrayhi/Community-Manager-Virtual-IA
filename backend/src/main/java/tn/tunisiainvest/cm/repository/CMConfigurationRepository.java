package tn.tunisiainvest.cm.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.tunisiainvest.cm.model.CMConfiguration;

public interface CMConfigurationRepository extends JpaRepository<CMConfiguration, Long> {

    Optional<CMConfiguration> findBySubscriptionId(Long subscriptionId);

    Optional<CMConfiguration> findFirstBySubscriptionUserEmailOrderByUpdatedAtDesc(String userEmail);
}
