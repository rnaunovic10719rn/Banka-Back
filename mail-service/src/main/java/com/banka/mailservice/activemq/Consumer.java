package com.banka.mailservice.activemq;

import com.banka.mailservice.smtp.EmailService;
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
        String email = spl[0];
        String subject = spl[1];
        String message = spl[2];
        emailService.sendHtmlMessage(email, subject, message);
    }
}
