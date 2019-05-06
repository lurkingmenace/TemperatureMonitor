package com.jdivirgilio.temperature;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

class TimePumpIsOnTask extends Thread
{
	private Boolean finished = false;
	private GregorianCalendar time = new GregorianCalendar();

	public static void sendMail(TimeBreakDown elapsedTime) {
		String sender = "lurkingmenace@gmail.com";
		String senderPwd = "shjifewirpcbqhca";

		Properties props = new Properties() {{
			put("mail.smtp.auth", "true");
			put("mail.smtp.host", "smtp.gmail.com");
			put("mail.smtp.port", "587");
			put("mail.smtp.starttls.enable", "true");
			put("mail.debug", "true");
		}};
		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(sender, senderPwd);
			}
		});

		try {
			// Creates email message
			MimeMessage message = new MimeMessage(session);
			message.setSentDate(new Date());
			message.setFrom(new InternetAddress(sender));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse("2146327287@msg.fi.google.com"));
			message.setSubject("Change Ice");
			String msgBody = String.format("The pump has been on for %d Days %02d:%02d:%02d\n",
					elapsedTime.getDays(), elapsedTime.getHours(), 
					elapsedTime.getMinutes(), elapsedTime.getSeconds());
			message.setText(msgBody);

			// Send a message
			Transport.send(message);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
	
	public void shutdown() {
		synchronized (finished) {
			finished = true;
			interrupt();
		}
	}

	@Override
	public void run() {
		System.out.println("Entered Alter thread. Will report after 2 hours");
		while (!finished) {
			try {
				sleep(60 * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			TimeBreakDown elapsedTime = new TimeBreakDown(System.currentTimeMillis() - this.time.getTimeInMillis());
			if (elapsedTime.getDays() > 0 || elapsedTime.getHours() >= 2) {
				// Send an email/txt notification
				System.out.println("Reporting pump on too long");
				TimePumpIsOnTask.sendMail(elapsedTime);
				try {
					sleep(30 * 60 * 1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			synchronized (finished) {
				if (finished)
					break;
			}
		}
	}
}