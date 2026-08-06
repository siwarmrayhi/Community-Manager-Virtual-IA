package tn.tunisiainvest.cm.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import tn.tunisiainvest.cm.model.CMConfiguration;
import tn.tunisiainvest.cm.model.Post;
import tn.tunisiainvest.cm.model.PostStatus;

public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {

    List<Post> findByConfigurationOrderByPlannedDateAscCreatedAtDesc(CMConfiguration configuration);

    List<Post> findByConfigurationAndPlannedDateBetweenOrderByPlannedDateAsc(
            CMConfiguration configuration,
            LocalDate startDate,
            LocalDate endDate
    );

    long countByConfigurationAndStatus(CMConfiguration configuration, PostStatus status);
}
