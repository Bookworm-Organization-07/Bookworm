package com.example.Services;

import com.example.models.Transaction;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String mailUsername;

    public EmailService(JavaMailSender mailSender,
                        @Value("${spring.mail.username:}") String mailUsername) {
        this.mailSender = mailSender;
        this.mailUsername = mailUsername;
    }

    /**
     * Best-effort: a checkout that has already been paid for must not be
     * rolled back because an SMTP server was unreachable. With no
     * spring.mail.username configured this returns immediately rather
     * than making the reader wait out a connection timeout.
     */
    public void sendTransactionSuccessEmail(String toEmail,
                                            Transaction transaction,
                                            byte[] invoicePdf) {
        if (mailUsername == null || mailUsername.isBlank()) {
            log.info("Mail is not configured - skipping invoice email for transaction {}",
                    transaction.getTransactionId());
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject("Bookworm - Transaction Successful");
            helper.setText(
                    "Hello " + transaction.getUser().getUserName() + ",\n\n" +
                    "Your transaction was successful.\n\n" +
                    "Transaction ID: " + transaction.getTransactionId() + "\n" +
                    "Amount: Rs " + transaction.getTotalAmount() + "\n\n" +
                    "Thank you for shopping with Bookworm!",
                    false
            );
            helper.addAttachment(
                    "invoice_" + transaction.getTransactionId() + ".pdf",
                    new ByteArrayResource(invoicePdf)
            );

            mailSender.send(message);
        } catch (Exception e) {
            log.warn("Could not send the invoice email for transaction {}: {}",
                    transaction.getTransactionId(), e.getMessage());
        }
    }
}
