package com.tienda.ecommerce.pago.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${smartcart.mail.from:no-reply@smartcart.com}")
    private String remitente;

    @Value("${smartcart.mail.nombre-tienda:SmartCart}")
    private String nombreTienda;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void enviarReciboCompra(String destinatario,
                                   String nombreCliente,
                                   Long numeroRecibo,
                                   byte[] pdf) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mensaje, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

            helper.setFrom(remitente, nombreTienda);
            helper.setTo(destinatario);
            helper.setSubject("Tu recibo de compra en " + nombreTienda + " - N° " + numeroRecibo);
            helper.setText(construirCuerpoHtml(nombreCliente, numeroRecibo), true);

            String nombreArchivo = "recibo-" + numeroRecibo + ".pdf";
            helper.addAttachment(nombreArchivo, new ByteArrayResource(pdf));

            mailSender.send(mensaje);
            log.info("Recibo enviado a {} (recibo N° {})", destinatario, numeroRecibo);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("No se pudo enviar el recibo a {}: {}", destinatario, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error inesperado enviando recibo a {}: {}", destinatario, e.getMessage(), e);
        }
    }

    private String construirCuerpoHtml(String nombreCliente, Long numeroRecibo) {
        return "<!DOCTYPE html><html><body style=\"font-family: Arial, sans-serif; color:#222;\">" +
                "<h2 style=\"color:#212529;\">¡Gracias por tu compra, " + nombreCliente + "!</h2>" +
                "<p>Hemos recibido tu pago correctamente. Adjunto encontrarás tu recibo " +
                "en formato PDF correspondiente a la compra <b>N° " + numeroRecibo + "</b>.</p>" +
                "<p>Si tienes alguna duda sobre tu pedido, responde a este correo y te ayudaremos.</p>" +
                "<br><p>Saludos,<br><b>El equipo de " + nombreTienda + "</b></p>" +
                "</body></html>";
    }
}
