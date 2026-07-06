package school.hei.app.service.event;

import jakarta.mail.internet.InternetAddress;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.app.endpoint.event.model.SubscriptionRequested;
import school.hei.app.mail.Email;
import school.hei.app.mail.Mailer;

@Slf4j
@Service
@AllArgsConstructor
public class SubscriptionRequestedService implements Consumer<SubscriptionRequested> {

  private final Mailer mailer;

  @SneakyThrows
  @Override
  public void accept(SubscriptionRequested event) {
    log.info("Sending confirmation email to: {}", event.getUserEmail());

    String html =
        String.format(
            """
            <h2>Inscription confirmée</h2>
            <p>Bonjour <strong>%s</strong>,</p>
            <p>Vous êtes inscrit au cours :</p>
            <h3> %s</h3>
            <p><strong>Prix :</strong> %.2f €</p>
            <br>
            <p>Cordialement,<br>L'équipe de formation</p>
            """,
            event.getUserName(), event.getCourseTitle(), event.getCoursePrice());

    InternetAddress recipient = new InternetAddress(event.getUserEmail());
    Email email =
        new Email(
            recipient,
            List.of(),
            List.of(),
            "Inscription confirmée - " + event.getCourseTitle(),
            html,
            List.of());

    mailer.accept(email);
    log.info("Email sent to: {}", event.getUserEmail());
  }
}
