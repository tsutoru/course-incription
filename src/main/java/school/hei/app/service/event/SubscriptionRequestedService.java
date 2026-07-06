package school.hei.app.service.event;

import jakarta.mail.internet.InternetAddress;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import school.hei.app.endpoint.event.model.SubscriptionRequested;

@Service
@AllArgsConstructor
public class SubscriptionRequestedService implements Consumer<SubscriptionRequested> {
  private final school.hei.app.mail.Mailer mailer;

  @SneakyThrows
  @Override
  public void accept(SubscriptionRequested event) {
    InternetAddress recipientAddress = new InternetAddress(event.getUserEmail());
    var email =
        new school.hei.app.mail.Email(
            recipientAddress,
            List.of(),
            List.of(),
            "Inscription confirmée",
            "Vous êtes inscrit au cours : " + event.getCourseTitle(),
            List.of());
    mailer.accept(email);
  }
}
