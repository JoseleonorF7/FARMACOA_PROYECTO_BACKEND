package Package.PHARMACY_PROJECT.PRUEBACORREOS;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender; // Correcto: usar el atributo mailSender

    public void sendEmailWithAttachment(String to, String subject, String body, byte[] attachment, String attachmentFilename) {
        try {
            // Crear un mensaje MIME
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            // Configurar detalles del correo
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);
            helper.addAttachment(attachmentFilename, new ByteArrayResource(attachment));

            // Enviar el correo
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new MailSendException("Error al enviar el correo con el archivo adjunto", e);
        }
    }
}
