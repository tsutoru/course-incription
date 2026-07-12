package school.hei.app.service.event;

import jakarta.mail.internet.InternetAddress;
import java.io.File;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import school.hei.app.endpoint.event.model.SubscriptionRequested;
import school.hei.app.mail.Email;
import school.hei.app.mail.Mailer;
import school.hei.app.model.Course;
import school.hei.app.model.User;
import school.hei.app.repository.CourseRepository;
import school.hei.app.repository.UserRepository;
import school.hei.app.service.InvoicePdfGenerator;

@Service
@AllArgsConstructor
public class SubscriptionRequestedService implements Consumer<SubscriptionRequested> {
  private final Mailer mailer;
  private final UserRepository userRepository;
  private final CourseRepository courseRepository;
  private final InvoicePdfGenerator invoicePdfGenerator;

  @SneakyThrows
  @Override
  public void accept(SubscriptionRequested event) {
    User user = userRepository.findById(event.getUserId()).orElseThrow();
    Course course = courseRepository.findById(event.getCourseId()).orElseThrow();

    File invoicePdf = invoicePdfGenerator.generate(user, course);

    InternetAddress recipientAddress = new InternetAddress(event.getUserEmail());
    var email =
            new Email(
                    recipientAddress,
                    List.of(),
                    List.of(),
                    "Inscription confirmée : " + event.getCourseTitle(),
                    "<p>Bonjour " + user.getName() + ",</p>"
                            + "<p>Vous êtes bien inscrit(e) au cours <b>"
                            + event.getCourseTitle()
                            + "</b>.</p>"
                            + "<p>Vous trouverez votre facture en pièce jointe.</p>"
                            + "<p>Merci !</p>",
                    List.of(invoicePdf));

    mailer.accept(email);
  }
}