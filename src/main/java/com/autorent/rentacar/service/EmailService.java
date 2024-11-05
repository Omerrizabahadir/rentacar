package com.autorent.rentacar.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String emailFrom;

    // Email adresini ayarlamak için setter metodu
    public void setEmailFrom(String emailFrom) {
        this.emailFrom = emailFrom;
    }

    public void sendWelcomeMail(String emailTo, String firstName, String lastName) {
        MimeMessage message;
        try {
            message = mailSender.createMimeMessage();
        } catch (RuntimeException e) {
            log.error("E-posta {} adresine gönderilemedi: {}", emailTo, e.getMessage());
            throw new RuntimeException("E-posta gönderilemedi", e); // Burada hata mesajını "E-posta gönderilemedi" olarak ayarlıyoruz.
        }

        MimeMessageHelper helper;
        try {
            helper = new MimeMessageHelper(message, true);
            helper.setFrom(emailFrom, "AutoRent");
            helper.setTo(emailTo);
            helper.setSubject("Merhaba " + firstName + " " + lastName + ", Hoşgeldiniz");

            String content = "<p>Merhaba " + firstName + "</p><p>AutoRent araç kiralama sitesine üyeliğiniz başarıyla gerçekleştirildi</p>";
            helper.setText(content, true);

            mailSender.send(message);
            log.info("E-posta {} adresine gönderildi", emailTo);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("E-posta {} adresine gönderilemedi: {}", emailTo, e.getMessage());
            throw new RuntimeException("E-posta gönderilemedi", e); // Burada da hata mesajını ayarlıyoruz.
        }
    }

}
