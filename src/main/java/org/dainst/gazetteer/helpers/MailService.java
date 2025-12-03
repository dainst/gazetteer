package org.dainst.gazetteer.helpers;

import java.util.Properties;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MailService {

	@Value("${senderMail}")
	private String senderMail;
	
	@Value("${smtpHost}")
	private String smtpHost;
	
	@Value("${smtpPort}")
	private String smtpPort;
	
	@Value("${mailUsername}")
	private String mailUsername;
	
	@Value("${mailPassword}")
	private String mailPassword;
	
	private static Logger logger = LoggerFactory.getLogger(MailService.class);
	
	public void sendMail(String recipientMail, String subject, String content) throws MessagingException {
		
		sendMail(recipientMail, subject, content, null);
	}
	
	public void sendMail(String recipientMail, String subject, String content, String replyTo) throws MessagingException {
		
		if (senderMail.isEmpty() || smtpHost.isEmpty() || smtpPort.isEmpty() || mailUsername.isEmpty() || mailPassword.isEmpty()) {
			logger.warn("Could not send mail: Mail properties not set");
			return;
		}
		
		Properties properties = new Properties();
		properties.setProperty("mail.smtp.host", smtpHost);
		properties.setProperty("mail.smtp.auth", "true");
		properties.setProperty("mail.smtp.port", smtpPort);
		properties.setProperty("mail.smtp.starttls.enable", "true");
		Session session = Session.getInstance(properties);
		
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(senderMail));
		message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipientMail));
		if (replyTo != null) message.setReplyTo(new InternetAddress[] {new InternetAddress(replyTo)});
		message.setSubject(subject);
		message.setContent(content, "text/html; charset=utf-8");
		
	    Transport transport = session.getTransport("smtp");
	    try {
	        transport.connect(mailUsername, mailPassword);
	        transport.sendMessage(message, message.getAllRecipients());
	    } catch (MessagingException e) {
	    	logger.warn("Could not send mail to: " + recipientMail, e);
	    } finally {
	    	transport.close();
	    }
	}

	public String getSenderMail() {
		return senderMail;
	}

	public void setSenderMail(String senderMail) {
		this.senderMail = senderMail;
	}

	public String getSmtpHost() {
		return smtpHost;
	}

	public void setSmtpHost(String smtpHost) {
		this.smtpHost = smtpHost;
	}

	public String getSmtpPort() {
		return smtpPort;
	}

	public void setSmtpPort(String smtpPort) {
		this.smtpPort = smtpPort;
	}
}
