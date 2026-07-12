package school.hei.app.conf.Subscription;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import school.hei.app.endpoint.event.model.SubscriptionRequested;
import school.hei.app.mail.Email;
import school.hei.app.mail.Mailer;
import school.hei.app.model.Course;
import school.hei.app.model.User;
import school.hei.app.repository.CourseRepository;
import school.hei.app.repository.UserRepository;
import school.hei.app.service.InvoicePdfGenerator;
import school.hei.app.service.event.S3Service;
import school.hei.app.service.event.SubscriptionRequestedService;

class SubscriptionRequestedServiceTest {

    private Mailer mailer;
    private UserRepository userRepository;
    private CourseRepository courseRepository;
    private InvoicePdfGenerator invoicePdfGenerator;
    private S3Service s3Service;
    private SubscriptionRequestedService service;

    private static final String USER_ID = "user-1";
    private static final String COURSE_ID = "course-1";
    private static final String PRESIGNED_URL =
            "https://bucket.s3.amazonaws.com/certificats/user-1/facture.pdf?X-Amz-Signature=abc";

    @BeforeEach
    void setUp() {
        mailer = mock(Mailer.class);
        userRepository = mock(UserRepository.class);
        courseRepository = mock(CourseRepository.class);
        invoicePdfGenerator = mock(InvoicePdfGenerator.class);
        s3Service = mock(S3Service.class);
        service =
                new SubscriptionRequestedService(
                        mailer, userRepository, courseRepository, invoicePdfGenerator, s3Service);
    }

    @Test
    void accept_uploadsPdfToS3AndSendsEmailWithLink() throws Exception {
        User user = User.builder().id(USER_ID).name("Alice").email("alice@example.com").build();
        Course course = Course.builder().id(COURSE_ID).title("Java 101").price(100.0).build();
        byte[] fakePdfBytes = new byte[] {1, 2, 3};

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));
        when(invoicePdfGenerator.generate(user, course)).thenReturn(fakePdfBytes);
        when(s3Service.uploadPdfAndGenerateUrl(any(byte[].class), anyString(), any()))
                .thenReturn(PRESIGNED_URL);

        SubscriptionRequested event =
                SubscriptionRequested.builder()
                        .userId(USER_ID)
                        .courseId(COURSE_ID)
                        .userEmail(user.getEmail())
                        .courseTitle(course.getTitle())
                        .build();

        service.accept(event);

        // Vérifie que le PDF généré est bien celui uploadé sur S3
        verify(s3Service, times(1)).uploadPdfAndGenerateUrl(fakePdfBytes, "facture_Java_101.pdf", USER_ID);

        // Vérifie le contenu de l'email envoyé
        ArgumentCaptor<Email> emailCaptor = ArgumentCaptor.forClass(Email.class);
        verify(mailer, times(1)).accept(emailCaptor.capture());

        Email sentEmail = emailCaptor.getValue();
        assertThat(sentEmail.to().getAddress()).isEqualTo("alice@example.com");
        assertThat(sentEmail.subject()).contains("Java 101");
        assertThat(sentEmail.htmlBody()).contains(PRESIGNED_URL);
        assertThat(sentEmail.htmlBody()).contains("Alice");
        assertThat(sentEmail.attachments()).isEmpty(); // plus de pièce jointe, juste le lien
    }

    @Test
    void accept_userNotFound_throwsAndDoesNotUploadOrSendEmail() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        SubscriptionRequested event =
                SubscriptionRequested.builder()
                        .userId(USER_ID)
                        .courseId(COURSE_ID)
                        .userEmail("alice@example.com")
                        .courseTitle("Java 101")
                        .build();

        org.junit.jupiter.api.Assertions.assertThrows(
                java.util.NoSuchElementException.class, () -> service.accept(event));

        verify(s3Service, times(0)).uploadPdfAndGenerateUrl(any(), any(), any());
        verify(mailer, times(0)).accept(any());
    }

    @Test
    void accept_courseNotFound_throwsAndDoesNotUploadOrSendEmail() {
        User user = User.builder().id(USER_ID).name("Alice").email("alice@example.com").build();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.empty());

        SubscriptionRequested event =
                SubscriptionRequested.builder()
                        .userId(USER_ID)
                        .courseId(COURSE_ID)
                        .userEmail("alice@example.com")
                        .courseTitle("Java 101")
                        .build();

        org.junit.jupiter.api.Assertions.assertThrows(
                java.util.NoSuchElementException.class, () -> service.accept(event));

        verify(s3Service, times(0)).uploadPdfAndGenerateUrl(any(), any(), any());
        verify(mailer, times(0)).accept(any());
    }

    @Test
    void accept_s3UploadFails_propagatesExceptionAndDoesNotSendEmail() {
        User user = User.builder().id(USER_ID).name("Alice").email("alice@example.com").build();
        Course course = Course.builder().id(COURSE_ID).title("Java 101").price(100.0).build();
        byte[] fakePdfBytes = new byte[] {1, 2, 3};

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));
        when(invoicePdfGenerator.generate(user, course)).thenReturn(fakePdfBytes);
        when(s3Service.uploadPdfAndGenerateUrl(any(byte[].class), anyString(), any()))
                .thenThrow(new RuntimeException("Failed to upload PDF to S3"));

        SubscriptionRequested event =
                SubscriptionRequested.builder()
                        .userId(USER_ID)
                        .courseId(COURSE_ID)
                        .userEmail(user.getEmail())
                        .courseTitle(course.getTitle())
                        .build();

        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> service.accept(event));

        verify(mailer, times(0)).accept(any());
    }
}