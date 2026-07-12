package school.hei.app.conf.pdf;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import school.hei.app.model.Course;
import school.hei.app.model.User;
import school.hei.app.service.InvoicePdfGenerator;

class InvoicePdfGeneratorTest {

  private final InvoicePdfGenerator generator = new InvoicePdfGenerator();

  @Test
  void generate_producesNonEmptyValidPdfBytes() {
    User user = User.builder().id("u1").name("Alice Dupont").email("alice@example.com").build();
    Course course =
        Course.builder()
            .id("c1")
            .title("Java 101")
            .description("Introduction à Java")
            .price(150.0)
            .build();

    byte[] pdf = generator.generate(user, course);

    assertThat(pdf).isNotEmpty();
    // Magic bytes "%PDF" en tête de tout fichier PDF valide
    assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");
  }

  @Test
  void generate_handlesNullDescriptionGracefully() {
    User user = User.builder().id("u2").name("Bob").email("bob@example.com").build();
    Course course =
        Course.builder().id("c2").title("Python 101").description(null).price(80.0).build();

    byte[] pdf = generator.generate(user, course);

    assertThat(pdf).isNotEmpty();
    assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");
  }

  @Test
  void generate_differentCoursesProduceDifferentContent() {
    User user = User.builder().id("u3").name("Chloé").email("chloe@example.com").build();
    Course course1 = Course.builder().id("c3").title("Java 101").price(100.0).build();
    Course course2 = Course.builder().id("c4").title("Rust 101").price(200.0).build();

    byte[] pdf1 = generator.generate(user, course1);
    byte[] pdf2 = generator.generate(user, course2);

    assertThat(pdf1).isNotEqualTo(pdf2);
  }
}
