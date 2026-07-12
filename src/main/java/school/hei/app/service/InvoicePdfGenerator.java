package school.hei.app.service;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import school.hei.app.model.Course;
import school.hei.app.model.User;

@Service
public class InvoicePdfGenerator {

    @SneakyThrows
    public File generate(User user, Course course) {
        File file = File.createTempFile("facture-" + course.getId() + "-", ".pdf");

        Document document = new Document();
        try (FileOutputStream fos = new FileOutputStream(file)) {
            PdfWriter.getInstance(document, fos);
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
                    new Paragraph(
                            "Date : " + DateTimeFormatter.ISO_INSTANT.format(Instant.now()), normal));

            document.close();
        }

        return file;
    }
}