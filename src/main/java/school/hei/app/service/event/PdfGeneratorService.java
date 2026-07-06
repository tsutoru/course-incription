package school.hei.app.service.event;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PdfGeneratorService {

  public byte[] generateCertificate(String userName, String courseTitle, Double coursePrice) {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      Document document = new Document();
      PdfWriter.getInstance(document, out);
      document.open();

      Font titleFont = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD);
      document.add(new Paragraph(" Certificat d'inscription", titleFont));
      document.add(new Paragraph(" "));
      document.add(new Paragraph(" "));

      document.add(new Paragraph("Ce certificat atteste que :"));
      document.add(new Paragraph(" "));
      document.add(
          new Paragraph("   " + userName, new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD)));
      document.add(new Paragraph(" "));
      document.add(new Paragraph("est inscrit au cours :"));
      document.add(new Paragraph(" "));
      document.add(
          new Paragraph("   " + courseTitle, new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD)));
      document.add(new Paragraph(" "));
      document.add(new Paragraph("   Prix : " + String.format("%.2f €", coursePrice)));
      document.add(new Paragraph(" "));
      document.add(new Paragraph(" "));
      document.add(new Paragraph("Date d'inscription : " + Instant.now()));

      document.close();
      return out.toByteArray();

    } catch (Exception e) {
      log.error("PDF generation error: ", e);
      throw new RuntimeException("Failed to generate PDF", e);
    }
  }
}
