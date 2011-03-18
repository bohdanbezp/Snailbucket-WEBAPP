package net.rwchess.site.utils;

import java.io.IOException;
import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.google.appengine.api.datastore.Text;

public class Mailer {
	private static Message prepareMessage(String uname) throws AddressException,
			MessagingException, IOException {
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);

		Message msg = new MimeMessage(session);
		msg.setFrom(new InternetAddress("bvk256@gmail.com", "RW Notify"));

		if (!uname.equals("pchesso")) {
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(
					"psotar@web.de", "pchesso"));
		}
		if (!uname.equals("Bodia") && !uname.equals("reg")) {
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(
					"bvk256@gmail.com", "Bodia"));
		}
		
		return msg;
	}
	
	public static void emailSignup(String msgBody) throws IOException {		
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);

		Message msg = new MimeMessage(session);
		try {
			msg.setFrom(new InternetAddress("rwnotify@yandex.ru", "RW Notify"));
			msg.addRecipient(Message.RecipientType.BCC, new InternetAddress(
					"rwnotify@yandex.ru"));
			msg.setSubject("Swiss 2011 registration");
			Address ad[] = {new InternetAddress("rwchesstds@googlegroups.com")};
			msg.setReplyTo(ad);
			msg.setText(msgBody + "\n\n Your RWebsite notificator");
			Transport.send(msg);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void emailWikipageCreation(String msgBody) throws IOException {
		try {
			Message msg = prepareMessage("");
			msg.setSubject("RWiki page creation");
			msg.setText(msgBody + "\n\n Your RWebsite notificator");
			Transport.send(msg);

		} catch (AddressException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	public static void forumPost(String msgBody, String top) throws IOException {
		try {
			Message msg = prepareMessage("");
			msg.setSubject(top);
			msg.setText(msgBody + "\n\n Your RWebsite notificator");
			Transport.send(msg);

		} catch (AddressException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
	
	public static void swissGuestReg(String username, String email) throws IOException {
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);

		Message msg = new MimeMessage(session);
		try {
			msg.setFrom(new InternetAddress("rwnotify@yandex.ru", "RW Notify"));
			msg.addRecipient(Message.RecipientType.BCC, new InternetAddress(
					"rwnotify@yandex.ru"));
			msg.setSubject("Swiss 2011 registration");
			Address ad[] = {new InternetAddress("rwchesstds@googlegroups.com")};
			msg.setReplyTo(ad);
			msg.setText(username + " ("+email+") has signed up for RW Swiss 2011. Information on his/her approval can be" +
					" seen by TD on http://rwchess.appspot.com/swiss2011/approve" + "\n\n Your RWebsite notificator");
			Transport.send(msg);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}
	
	public static void editedWiki(String msgBody, Text original, Text after,
			String uname) throws IOException {
		try {
			Message msg = prepareMessage(uname);
			msg.setSubject("RWiki has been edited");

			Multipart mp = new MimeMultipart();

			MimeBodyPart htmlPart = new MimeBodyPart();
			htmlPart.setContent(msgBody + "\n\n Your RWebsite notificator",
					"text/plain");
			mp.addBodyPart(htmlPart);

			MimeBodyPart attachment1 = new MimeBodyPart();
			attachment1.setFileName("original.txt");
			attachment1.setContent(original.getValue(), "text/plain");
			mp.addBodyPart(attachment1);

			MimeBodyPart attachment2 = new MimeBodyPart();
			attachment2.setFileName("changed.txt");
			attachment2.setContent(after.getValue(), "text/plain");
			mp.addBodyPart(attachment2);

			msg.setContent(mp);
			Transport.send(msg);

		} catch (AddressException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
}
