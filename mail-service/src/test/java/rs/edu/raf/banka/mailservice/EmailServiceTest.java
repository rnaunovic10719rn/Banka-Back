package rs.edu.raf.banka.mailservice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import rs.edu.raf.banka.mailservice.smtp.EmailServiceImpl;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @InjectMocks
    private EmailServiceImpl emailService;

    @Mock
    private JavaMailSender emailSender;


    @Test
    void testSendHtmlMessage() throws MessagingException {
        String to = "example@gmail.com";
        String subject = "Subject";
        String htmlBody = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<body>\n" +
                "\n" +
                "<h1>Test</h1>\n" +
                "\n" +
                "<p>Email</p>\n" +
                "\n" +
                "</body>\n" +
                "</html>\n";
        JavaMailSender javaMailSender = new JavaMailSenderImpl();
        MimeMessage message = javaMailSender.createMimeMessage();
        when(emailSender.createMimeMessage()).thenReturn(message);
        emailService.setAddress("banka@gmail.com");
        emailService.sendHtmlMessage(to, subject, htmlBody);
        Mockito.verify(emailSender).send(message);
    }
}
