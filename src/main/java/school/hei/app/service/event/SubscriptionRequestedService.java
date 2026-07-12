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
  private final PdfGeneratorService pdfGenerator;
  private final S3Service s3Service;

  @SneakyThrows
  @Override
  public void accept(SubscriptionRequested event) {
    log.info("📧 Processing subscription for: {}", event.getUserEmail());

    try {
      byte[] pdfContent =
          pdfGenerator.generateCertificate(
              event.getUserName(), event.getCourseTitle(), event.getCoursePrice());

      String fileName = "certificat_" + event.getCourseTitle().replaceAll(" ", "_");
      String pdfUrl = s3Service.uploadPdfAndGenerateUrl(pdfContent, fileName, event.getUserId());

      log.info("PDF uploaded to S3: {}", pdfUrl);

      String html =
          String.format(
              """
<div style="font-family: Arial, sans-serif; max-width: 500px; padding: 20px;">
    <div style="background: #4CAF50; color: white; padding: 15px; text-align: center; border-radius: 5px 5px 0 0;">
        <h2 style="margin: 0;">Inscription confirmée</h2>
    </div>
    <div style="background: #f9f9f9; padding: 20px; border-radius: 0 0 5px 5px;">
        <p>Bonjour <strong>%s</strong>,</p>
        <p>Vous êtes inscrit au cours :</p>
        <div style="background: white; padding: 15px; border-radius: 5px; margin: 10px 0;">
            <h3 style="margin: 0; color: #333;">%s</h3>
            <p style="margin: 5px 0; font-size: 18px; color: #4CAF50;">
                <strong>%.2f €</strong>
            </p>
        </div>
        <div style="background: #e8f4fd; padding: 15px; border-radius: 5px; margin: 15px 0;">
            <p style="margin: 0;">
                <a href="%s" style="color: #2196F3; text-decoration: none; font-weight: bold;">
                    Télécharger votre certificat
                </a>
            </p>
            <p style="margin: 5px 0; font-size: 12px; color: #666;">
                Ce lien expirera dans 24 heures
            </p>
        </div>
        <p style="color: #666; font-size: 14px;">
            Cordialement,<br>L'équipe de formation
        </p>
    </div>
</div>
""",
              event.getUserName(),
              event.getCourseTitle(),
              event.getCoursePrice() != null ? event.getCoursePrice() : 0.0,
              pdfUrl);

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
      log.info("Email sent to: {} avec lien PDF S3", event.getUserEmail());

    } catch (Exception e) {
      log.error("Failed to process subscription: ", e);
      throw new RuntimeException("Subscription processing failed", e);
    }
  }
}
