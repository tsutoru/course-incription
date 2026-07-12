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
import school.hei.app.model.Course;
import school.hei.app.model.User;
import school.hei.app.repository.CourseRepository;
import school.hei.app.repository.UserRepository;
import school.hei.app.service.InvoicePdfGenerator;

@Slf4j
@Service
@AllArgsConstructor
public class SubscriptionRequestedService implements Consumer<SubscriptionRequested> {
  private final Mailer mailer;
  private final UserRepository userRepository;
  private final CourseRepository courseRepository;
  private final InvoicePdfGenerator invoicePdfGenerator;
  private final S3Service s3Service;

  @SneakyThrows
  @Override
  public void accept(SubscriptionRequested event) {
    log.info("Processing subscription for: {}", event.getUserEmail());

    User user = userRepository.findById(event.getUserId()).orElseThrow();
    Course course = courseRepository.findById(event.getCourseId()).orElseThrow();

    byte[] pdfContent = invoicePdfGenerator.generate(user, course);
    String fileName = "facture_" + course.getTitle().replaceAll(" ", "_") + ".pdf";
    String pdfUrl = s3Service.uploadPdfAndGenerateUrl(pdfContent, fileName, user.getId());

    log.info("PDF uploaded to S3, presigned URL generated");

    String html =
        "<div style=\"font-family: Arial, sans-serif; max-width: 500px; padding: 20px;\">"
            + "<p>Bonjour <strong>"
            + user.getName()
            + "</strong>,</p><p>Vous êtes bien inscrit(e) au cours :</p><div style=\"background:"
            + " #f9f9f9; padding: 15px; border-radius: 5px; margin: 10px 0;\"><h3 style=\"margin:"
            + " 0;\">"
            + course.getTitle()
            + "</h3>"
            + "<p style=\"margin: 5px 0; color: #4CAF50;\"><strong>"
            + course.getPrice()
            + " €</strong></p>"
            + "</div>"
            + "<p><a href=\""
            + pdfUrl
            + "\" style=\"color: #2196F3; font-weight: bold;\">"
            + "Télécharger votre facture</a></p>"
            + "<p style=\"font-size: 12px; color: #666;\">Ce lien expire dans 24 heures.</p>"
            + "</div>";

    InternetAddress recipientAddress = new InternetAddress(event.getUserEmail());
    var email =
        new Email(
            recipientAddress,
            List.of(),
            List.of(),
            "Inscription confirmée : " + course.getTitle(),
            html,
            List.of());

    mailer.accept(email);
    log.info("Email sent to: {}", event.getUserEmail());
  }
}
