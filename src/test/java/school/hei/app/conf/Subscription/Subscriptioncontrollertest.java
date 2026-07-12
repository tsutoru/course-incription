package school.hei.app.conf.Subscription;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import school.hei.app.endpoint.event.EventProducer;
import school.hei.app.endpoint.event.model.SubscriptionRequested;
import school.hei.app.endpoint.rest.controller.health.course.SubscriptionController;
import school.hei.app.model.Course;
import school.hei.app.model.Subscription;
import school.hei.app.model.User;
import school.hei.app.repository.CourseRepository;
import school.hei.app.repository.SubscriptionRepository;
import school.hei.app.repository.UserRepository;

/**
 * Teste SubscriptionController de manière isolée (sans contexte Spring), en mockant les
 * repositories et l'EventProducer.
 */
class SubscriptionControllerTest {

    private UserRepository userRepository;
    private CourseRepository courseRepository;
    private SubscriptionRepository subscriptionRepository;
    private EventProducer<SubscriptionRequested> eventProducer;
    private SubscriptionController controller;

    private static final String USER_ID = "user-1";
    private static final String COURSE_ID = "course-1";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userRepository = org.mockito.Mockito.mock(UserRepository.class);
        courseRepository = org.mockito.Mockito.mock(CourseRepository.class);
        subscriptionRepository = org.mockito.Mockito.mock(SubscriptionRepository.class);
        eventProducer = org.mockito.Mockito.mock(EventProducer.class);
        controller =
                new SubscriptionController(userRepository, courseRepository, subscriptionRepository, eventProducer);
    }

    @Test
    void subscribe_success_savesAndPublishesEvent() {
        User user = User.builder().id(USER_ID).name("Alice").email("alice@example.com").build();
        Course course = Course.builder().id(COURSE_ID).title("Java 101").price(100.0).build();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));

        ResponseEntity<Void> response = controller.subscribe(COURSE_ID, USER_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(subscriptionRepository, times(1)).save(any(Subscription.class));
        verify(eventProducer, times(1)).accept(any(List.class));
    }

    @Test
    void subscribe_userNotFound_returnsBadRequest() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        ResponseEntity<Void> response = controller.subscribe(COURSE_ID, USER_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(subscriptionRepository, never()).save(any());
        verify(eventProducer, never()).accept(any());
    }

    @Test
    void subscribe_courseNotFound_returnsBadRequest() {
        User user = User.builder().id(USER_ID).name("Alice").email("alice@example.com").build();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.empty());

        ResponseEntity<Void> response = controller.subscribe(COURSE_ID, USER_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(subscriptionRepository, never()).save(any());
        verify(eventProducer, never()).accept(any());
    }

    @Test
    void subscribe_unexpectedError_returnsInternalServerError() {
        when(userRepository.findById(USER_ID))
                .thenThrow(new RuntimeException("DB connection lost"));

        ResponseEntity<Void> response = controller.subscribe(COURSE_ID, USER_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        verify(eventProducer, never()).accept(any());
    }

    /**
     * NB: ce test suppose que la vérification anti-doublon (recommandée dans le README) a été
     * ajoutée au controller via subscriptionRepository.findByUserIdAndCourseId(...). Si ce n'est
     * pas encore fait, ce test échouera — c'est volontaire, ça sert de rappel/garde-fou.
     */
    @Test
    void subscribe_alreadySubscribed_doesNotDuplicateOrReturnsConflict() {
        User user = User.builder().id(USER_ID).name("Alice").email("alice@example.com").build();
        Course course = Course.builder().id(COURSE_ID).title("Java 101").price(100.0).build();
        Subscription existing = Subscription.builder().id("sub-1").user(user).course(course).build();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));
        when(subscriptionRepository.findByUserIdAndCourseId(USER_ID, COURSE_ID))
                .thenReturn(Optional.of(existing));

        ResponseEntity<Void> response = controller.subscribe(COURSE_ID, USER_ID);

        // Si la vérification anti-doublon n'existe pas encore, retire ce test ou adapte-le.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        verify(eventProducer, never()).accept(any());
    }
}