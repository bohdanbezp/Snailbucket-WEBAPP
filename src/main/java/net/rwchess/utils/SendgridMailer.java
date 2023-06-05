package net.rwchess.utils;

import org.apache.log4j.Logger;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessagePreparator;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by bodia on 10/17/14.
 */
public class SendgridMailer implements Mailer {

    static Logger log = Logger.getLogger(SendgridMailer.class.getName());

    private ExecutorService taskExecutor = Executors.newSingleThreadExecutor();

    private JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

    public SendgridMailer() {
        mailSender.setHost("localhost");
        mailSender.setPort(25);
    }

    @Override
    public void sendEmail(final String fromT, final String subject, final String contents, final String toT) {
                taskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try
                {
                    System.out.println("TO: " + toT + " Subj: " + subject);
                    System.out.println("contents: " + contents);
                    MimeMessagePreparator preparator = new MimeMessagePreparator() {
                        public void prepare(MimeMessage mimeMessage) throws MessagingException {
                            mimeMessage.setRecipient(Message.RecipientType.TO,
                                    new InternetAddress(toT));
                            mimeMessage.setFrom(new InternetAddress(fromT));
                            mimeMessage.setSubject(subject);
                            mimeMessage.setText(contents);
                        }
                    };

                    mailSender.send(preparator);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
