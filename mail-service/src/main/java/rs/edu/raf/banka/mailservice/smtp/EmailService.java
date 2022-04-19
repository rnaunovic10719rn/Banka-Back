package rs.edu.raf.banka.mailservice.smtp;

import javax.mail.MessagingException;

public interface EmailService {

    void sendHtmlMessage(String to, String subject, String htmlBody) throws MessagingException;
}
