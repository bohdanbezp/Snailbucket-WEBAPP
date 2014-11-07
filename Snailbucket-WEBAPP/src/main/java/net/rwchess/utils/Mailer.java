package net.rwchess.utils;


public interface Mailer {
    public void sendEmail(String from, String subject, String contents, String to);
}
