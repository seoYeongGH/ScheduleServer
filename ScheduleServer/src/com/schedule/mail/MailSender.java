package com.schedule.mail;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailSender{
	private static MailSender instance;
	private MimeMessage mimeMessage;
	
	private MailSender() {
		Properties prop = System.getProperties();
		
		prop.put("mail.smtp.host","smtp.naver.com");
		prop.put("mail.smtp.port", 465);
		prop.put("mail.smtp.auth", "true");
		prop.put("mail.smtp.ssl.enable", "true");
		prop.put("mail.smtp.ssl.trust", "smtp.naver.com");
		
		Session session = Session.getDefaultInstance(prop, new javax.mail.Authenticator() {
			String id = "Naver Id";
			String pw = "Naver Password";
			
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(id,pw);
			}
		});
		
		session.setDebug(true);
		
		mimeMessage = new MimeMessage(session);
		try {
			mimeMessage.setFrom(new InternetAddress("Naver Email"));
		} catch (MessagingException e) {
			System.out.println("SEND MAIL CREATE ERR: "+e.toString());
		}
	}
	
	public static MailSender getInstance() {
		if(instance == null)
			instance = new MailSender();
		
		return instance;
	}
	
	public boolean sendMail(String recipient, String subject, String text) {
		boolean flag = true;
		
		try {
			mimeMessage.setRecipient(Message.RecipientType.TO,new InternetAddress(recipient));
			mimeMessage.setSubject(subject);
			mimeMessage.setText(text);
			
			Transport.send(mimeMessage);
		} catch (AddressException e) {
			flag = false;
			
			System.out.println("SEND MAIL ERR: "+e.toString());
		} catch (MessagingException e) {
			flag = false;
			
			System.out.println("SEND MAIL ERR: "+e.toString());
		}
		
		return flag;
	}
	
}
