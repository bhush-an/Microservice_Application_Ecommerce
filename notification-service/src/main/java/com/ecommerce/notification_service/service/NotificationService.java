package com.ecommerce.notification_service.service;

import com.ecommerce.order_service.event.OrderPlacedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private JavaMailSender javaMailSender;

    @KafkaListener(topics = "order-placed")
    public void listen(OrderPlacedEvent orderPlacedEvent) {
        MimeMessagePreparator message = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);

            messageHelper.setFrom("mobiles@crystal.com");
            messageHelper.setTo(orderPlacedEvent.getEmail());
            messageHelper.setSubject("Order placed successfully: " + orderPlacedEvent.getOrderId());

            // Use HTML format for the email body
            String htmlContent = String.format("""
                <html>
                    <body>
                        <p>Hi,</p>
                        <p>Your order with Order ID: <strong>%s</strong> is placed successfully!</p>
                        <p>Please complete payment by clicking the link below:</p>
                        <a href="%s" target="_blank">Complete Payment</a>
                        <br/><br/>
                        <p>Regards,</p>
                        <p>Crystal Electronics</p>
                    </body>
                </html>
                """, orderPlacedEvent.getOrderId(), orderPlacedEvent.getPaymentUrl());

            messageHelper.setText(htmlContent, true);
        };
        try {
            javaMailSender.send(message);
            log.info("Order notification sent successfully to: {}", orderPlacedEvent.getEmail());
        } catch (MailException e) {
            log.info("Exception occurred while sending email: ", e);
        }
    }
}
