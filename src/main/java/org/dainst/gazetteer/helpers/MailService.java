package org.dainst.gazetteer.helpers;

import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.*;

import org.springframework.beans.factory.annotation.Value;

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
	
	
	public void sendMail(String recipientMail, String subject, String content) throws MessagingException {
		
		Properties properties = new Properties();
		properties.setProperty("mail.smtp.host", smtpHost);
		properties.setProperty("mail.smtp.auth", "true");
		properties.setProperty("mail.smtp.port", smtpPort);
		Session session = Session.getInstance(properties);
		
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(senderMail));
		message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipientMail));
		message.setSubject(subject);
		message.setContent(content, "text/html; charset=utf-8");
		
	    Transport transport = session.getTransport("smtp");
	    try {
	        transport.connect(mailUsername, mailPassword);
	        transport.sendMessage(message, message.getAllRecipients());
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
