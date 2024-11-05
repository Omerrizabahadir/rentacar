package com.autorent.rentacar.service;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService.setEmailFrom("no-reply@autorent.com"); // Test için e-posta adresi ayarlıyoruz
    }

    @Test
    void sendWelcomeMail_success() throws Exception {
        // Arrange
        String emailTo = "test@gmail.com";
        String firstName = "Test";
        String lastName = "User";

        MimeMessage message = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(message);

        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        doNothing().when(mailSender).send(message); // E-postanın gönderileceğini varsayıyoruz

        // Act
        emailService.sendWelcomeMail(emailTo, firstName, lastName);

        // Assert
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(message);
    }

    @Test
    void sendWelcomeMail_failure() throws Exception {
        // Arrange
        String emailTo = "test@gmail.com";
        String firstName = "Test";
        String lastName = "User";

        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("MimeMessage oluşturulamadı"));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            emailService.sendWelcomeMail(emailTo, firstName, lastName);
        });

        assertEquals("E-posta gönderilemedi", exception.getMessage());
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, never()).send(any(MimeMessage.class)); // E-posta gönderilmedi
    }

}
