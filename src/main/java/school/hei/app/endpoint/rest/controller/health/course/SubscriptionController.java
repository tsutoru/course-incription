package school.hei.app.endpoint.rest.controller.health.course;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.app.endpoint.event.EventProducer;
import school.hei.app.endpoint.event.model.SubscriptionRequested;
import school.hei.app.model.Course;
import school.hei.app.model.Subscription;
import school.hei.app.model.User;
import school.hei.app.repository.CourseRepository;
import school.hei.app.repository.SubscriptionRepository;
import school.hei.app.repository.UserRepository;

@Slf4j
@RestController
@AllArgsConstructor
public class SubscriptionController {

  private final UserRepository userRepository;
  private final CourseRepository courseRepository;
  private final SubscriptionRepository subscriptionRepository;
  private final EventProducer<SubscriptionRequested> eventProducer;

  @PostMapping("/courses/{courseId}/subscribe")
  public ResponseEntity<Void> subscribe(
      @PathVariable String courseId, @RequestParam String userId) {

    try {
      log.info("Subscription request - courseId: {}, userId: {}", courseId, userId);

      User user =
          userRepository
              .findById(userId)
              .orElseThrow(
                  () -> {
                    log.error("User not found: {}", userId);
                    return new IllegalArgumentException("User not found: " + userId);
                  });

      log.info(
          "User found: {} (name: {}, email: {})", user.getId(), user.getName(), user.getEmail());
      Course course =
          courseRepository
              .findById(courseId)
              .orElseThrow(
                  () -> {
                    log.error("Course not found: {}", courseId);
                    return new IllegalArgumentException("Course not found: " + courseId);
                  });

      log.info(
          "Course found: {} (title: {}, price: {})",
          course.getId(),
          course.getTitle(),
          course.getPrice());

      Subscription subscription =
          Subscription.builder().user(user).course(course).subscribedAt(Instant.now()).build();

      subscriptionRepository.save(subscription);
      log.info("Subscription saved successfully");

      SubscriptionRequested event =
          SubscriptionRequested.builder()
              .userId(userId)
              .courseId(courseId)
              .userEmail(user.getEmail())
              .userName(user.getName())
              .courseTitle(course.getTitle())
              .courseDescription(course.getDescription())
              .coursePrice(course.getPrice())
              .build();

      log.info("Event created: {}", event);

      eventProducer.accept(List.of(event));
      log.info("Event sent successfully");

      return ResponseEntity.ok().build();

    } catch (IllegalArgumentException e) {
      log.error("Validation error: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      log.error("Unexpected error: ", e);
      return ResponseEntity.internalServerError().build();
    }
  }
}
