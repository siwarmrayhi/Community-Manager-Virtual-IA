package tn.tunisiainvest.cm.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.tunisiainvest.cm.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
