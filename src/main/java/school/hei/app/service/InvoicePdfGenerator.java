package school.hei.app.service;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import school.hei.app.model.Course;
import school.hei.app.model.User;

@Service
public class InvoicePdfGenerator {

  @SneakyThrows
  public byte[] generate(User user, Course course) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Document document = new Document();
    PdfWriter.getInstance(document, out);
    document.open();

    Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
    Font normal = FontFactory.getFont(FontFactory.HELVETICA, 12);

    document.add(new Paragraph("Facture d'inscription", title));
    document.add(new Paragraph(" "));
    document.add(new Paragraph("Élève : " + user.getName(), normal));
    document.add(new Paragraph("Email : " + user.getEmail(), normal));
    document.add(new Paragraph("Cours : " + course.getTitle(), normal));
    if (course.getDescription() != null) {
      document.add(new Paragraph("Description : " + course.getDescription(), normal));
    }
    document.add(new Paragraph("Prix : " + course.getPrice() + " €", normal));
    document.add(
        new Paragraph("Date : " + DateTimeFormatter.ISO_INSTANT.format(Instant.now()), normal));

    document.close();
    return out.toByteArray();
  }
}
