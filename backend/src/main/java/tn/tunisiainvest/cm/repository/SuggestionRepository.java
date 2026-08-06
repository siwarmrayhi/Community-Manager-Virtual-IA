package tn.tunisiainvest.cm.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.tunisiainvest.cm.model.CMConfiguration;
import tn.tunisiainvest.cm.model.Suggestion;
import tn.tunisiainvest.cm.model.SuggestionStatus;

public interface SuggestionRepository extends JpaRepository<Suggestion, Long> {

    List<Suggestion> findByConfigurationAndStatusOrderBySuggestedDateAscCreatedAtDesc(
            CMConfiguration configuration,
            SuggestionStatus status
    );

    boolean existsByConfigurationAndSourceReference(CMConfiguration configuration, String sourceReference);

    long countByConfigurationAndStatus(CMConfiguration configuration, SuggestionStatus status);
}
