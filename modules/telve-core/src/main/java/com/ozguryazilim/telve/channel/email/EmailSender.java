package com.ozguryazilim.telve.channel.email;

import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.deltaspike.core.api.config.ConfigResolver;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.ozguryazilim.telve.audit.AuditLogger;

/**
 * Java EE Mail Session üzerinden e-posta atmak için API.
 *
 * Kullanım için java:/TelveMail kaynağında Mail Session tanımları yapılmış
 * olması gerekir.
 *
 * @author Hakan Uygun
 */
@Stateless
@Named
public class EmailSender {

    @Resource(mappedName = "java:/TelveMail")
    private Session mailSession;

    @Inject
    private AuditLogger auditLogger;

    /**
     * Verilen bilgilerle sunucu üzerine tanımlanmış ayarlar ile e-posta atar.
     *
     * @param from
     * @param to
     * @param subject
     * @param body
     * @throws MessagingException
     */
    public void send(String from, String to, String subject, String body) throws MessagingException {
        Message message = new MimeMessage(mailSession);
        message.setFrom(new InternetAddress(from));
        Address toAddress = new InternetAddress(to);
        message.addRecipient(Message.RecipientType.TO, toAddress);
        message.setSubject(subject);
        message.setContent(body, "text/plain; charset=UTF-8");
        Transport.send(message);

        auditLogger.actionLog("NOTIFICATION", 0l, to, "EMAIL", "SEND", "SYSTEM", subject);
    }

    /**
     * Attachment'ları ekleyerek multipart e-posta atar.
     *
     * @param from
     * @param to
     * @param subject
     * @param body
     * @param attachments
     * @throws MessagingException
     */
    public void send(String from, String to, String subject, String body, Map<String, DataHandler> attachments) throws MessagingException {
        Message message = new MimeMessage(mailSession);
        message.setFrom(new InternetAddress(from));
        Address toAddress = new InternetAddress(to);
        message.addRecipient(Message.RecipientType.TO, toAddress);
        message.setSubject(subject);

        // Create the message part
        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText(body);

        // Create a multipar message
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);

        // Attachments
        for (Map.Entry<String, DataHandler> attachment : attachments.entrySet()) {

            messageBodyPart = new MimeBodyPart();

            messageBodyPart.setDataHandler(attachment.getValue());
            messageBodyPart.setFileName(attachment.getKey());
            multipart.addBodyPart(messageBodyPart);
        }

        message.setContent(multipart);

        Transport.send(message);

        auditLogger.actionLog("NOTIFICATION", 0l, to, "EMAIL", "SEND", "SYSTEM", subject);
    }



    /**
     * Attachment'ları ekleyerek multipart e-posta atar.
     *
     * @param from
     * @param to Ad Soyad <adres@domain.com>, ... formatında
     * @param cc
     * @param bcc
     * @param subject
     * @param body
     * @param attachments
     * @throws MessagingException
     */
    public void send(String from, String to, String cc, String bcc, String subject, String body, Map<String, DataHandler> attachments) throws MessagingException {
        Message message = new MimeMessage(mailSession);
        message.setFrom(new InternetAddress(from));

        //To addresses
        List<String> toAddrs = Splitter.on(',').trimResults().trimResults().splitToList(to);
        for( String a : toAddrs ){
            Address toAddress = new InternetAddress(a);
            message.addRecipient(Message.RecipientType.TO, toAddress);
        }

        //CC addresses
        if(!Strings.isNullOrEmpty(cc)) {
            List<String> ccAddrs = Splitter.on(',').trimResults().trimResults().splitToList(cc);
            for( String cca : ccAddrs ){
        	if(!Strings.isNullOrEmpty(cca)) {
        	    Address ccAddress = new InternetAddress(cca);
        	    message.addRecipient(Message.RecipientType.CC, ccAddress);
        	}
            }
        }

        //BCC addresses
        if(!Strings.isNullOrEmpty(bcc)) {
            List<String> bccAddrs = Splitter.on(',').trimResults().trimResults().splitToList(bcc);
            for( String bcca : bccAddrs ){
        	if(!Strings.isNullOrEmpty(bcca)) {
        	    Address bccAddress = new InternetAddress(bcca);
        	    message.addRecipient(Message.RecipientType.BCC, bccAddress);
        	}
            }
        }

        message.setSubject(subject);

        // Create the message part
        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText(body);

        // Create a multipar message
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);

        // Attachments
        if( attachments !=  null ){
            for (Map.Entry<String, DataHandler> attachment : attachments.entrySet()) {

                messageBodyPart = new MimeBodyPart();

                messageBodyPart.setDataHandler(attachment.getValue());
                messageBodyPart.setFileName(attachment.getKey());
                multipart.addBodyPart(messageBodyPart);
            }
        }

        message.setContent(multipart);

        Transport.send(message);

        auditLogger.actionLog("NOTIFICATION", 0l, to, "EMAIL", "SEND", "SYSTEM", subject);
    }

    /**
     * From bilgisini ayarlardan okur.
     *
     * @param to
     * @param cc
     * @param bcc
     * @param subject
     * @param body
     * @param attachments
     * @throws MessagingException
     */
    public void send(String to, String cc, String bcc, String subject, String body, Map<String, DataHandler> attachments) throws MessagingException {
        String from = ConfigResolver.getProjectStageAwarePropertyValue("app.email.from");
        send(from, to, cc, bcc, subject, body, attachments);
    }

    /**
     * From bilgisini ayarlardan okuyarak e-posta atar.
     *
     * @param to
     * @param subject
     * @param body
     * @throws MessagingException
     */
    public void send(String to, String subject, String body) throws MessagingException {
        String from = ConfigResolver.getProjectStageAwarePropertyValue("app.email.from");
        send(from, to, subject, body);
    }

    /**
     * From bilgisini ayarlardan okuyarak e-posta atar.
     *
     * @param to
     * @param subject
     * @param body
     * @param attachments
     * @throws MessagingException
     */
    public void send(String to, String subject, String body, Map<String, DataHandler> attachments) throws MessagingException {
        String from = ConfigResolver.getProjectStageAwarePropertyValue("app.email.from");
        send(from, to, subject, body, attachments);
    }
}
