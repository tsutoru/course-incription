package school.hei.app.service.event;

import school.hei.app.endpoint.event.model.SendEmailRequested;
import school.hei.app.mail.Email;
import school.hei.app.mail.Mailer;
import jakarta.mail.internet.InternetAddress;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SendEmailRequestedService implements Consumer<SendEmailRequested> {
    private final Mailer mailer;

    @SneakyThrows
    @Override
    public void accept(SendEmailRequested sendEmailRequested) {
        InternetAddress recipientAddress = new InternetAddress(sendEmailRequested.getTo());
        mailer.accept(new Email(recipientAddress, List.of(), List.of(), "", "... world!", List.of()));
    }
}