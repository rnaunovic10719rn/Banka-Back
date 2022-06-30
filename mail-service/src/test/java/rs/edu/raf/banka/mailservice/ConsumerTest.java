package rs.edu.raf.banka.mailservice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.edu.raf.banka.mailservice.activemq.Consumer;
import rs.edu.raf.banka.mailservice.exceptions.BadMessage;
import rs.edu.raf.banka.mailservice.smtp.EmailServiceImpl;

import javax.mail.MessagingException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class ConsumerTest {

    @InjectMocks
    Consumer consumer;

    @Mock
    EmailServiceImpl emailService;

    @Test
    void testConsume() throws MessagingException {
        String req = "example@gmail.com###Subject###htmlBody";
        consumer.consume(req);
        String[] spl = req.split("###");
        String email = spl[0];
        String subject = spl[1];
        String message = spl[2];
        Mockito.verify(emailService).sendHtmlMessage(email, subject, message);
    }

    @Test
    void testConsumeBadMessage(){
        Throwable exception = assertThrows(BadMessage.class, () -> consumer.consume("example@gmail.comSubject###htmlBody"));
        assertEquals("Bad message.", exception.getMessage());
    }

}
