package rs.edu.raf.banka.mailservice.activemq;

import rs.edu.raf.banka.mailservice.exceptions.BadMessage;
import rs.edu.raf.banka.mailservice.smtp.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;

@Component
public class Consumer {

    @Autowired
    public EmailService emailService;

    @JmsListener(destination = "mail.queue")
    public void consume(String req) throws MessagingException {
        String[] spl = req.split("###");
        if(spl.length != 3){
            throw new BadMessage();
        }
        String email = spl[0];
        String subject = spl[1];
        String message = spl[2];
        emailService.sendHtmlMessage(email, subject, message);
    }
}
