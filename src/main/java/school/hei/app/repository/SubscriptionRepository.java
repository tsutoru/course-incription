package school.hei.app.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.app.model.Subscription;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, String> {

  Optional<Subscription> findByUserIdAndCourseId(String userId, String courseId);

  List<Subscription> findByUserId(String userId);
}
