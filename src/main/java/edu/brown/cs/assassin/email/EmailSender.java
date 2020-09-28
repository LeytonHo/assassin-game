package edu.brown.cs.assassin.email;

import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import edu.brown.cs.assassin.login.Login;
import edu.brown.cs.assassin.main.AssassinConstants;

/**
 * EmailSender class used to send emails from the login associated with the
 * Assassin game.
 */
public final class EmailSender {

  private EmailSender() {
  }

  /**
   * Sends an email to recipients with the specified subject and text in the
   * email.
   *
   * @param recipients a list of people to receive the email.
   * @param subject    the subject of the email.
   * @param text       the body of the email.
   * @return whether the email was sent successfully.
   */
  public static boolean send(List<String> recipients, String subject, String text) {

    String host = "smtp.gmail.com";

    // Get the session object
    Properties properties = System.getProperties();
    properties.setProperty("mail.smtp.host", host);
    properties.setProperty("mail.smtp.starttls.enable", "true");
    properties.setProperty("mail.smtp.port", "587");

    Session session = Session.getDefaultInstance(properties);

    // compose the message
    try {
      MimeMessage message = new MimeMessage(session);
      message.setFrom(new InternetAddress(AssassinConstants.USERNAME));

      for (String to : recipients) {
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
      }
      message.setSubject(subject);
      // Can format message with html
      message.setContent(text, "text/html");

      // Send message
      Transport.send(message, AssassinConstants.USERNAME,
          Login.decrypt(AssassinConstants.PASSWORD, AssassinConstants.KEY));
      return true;

    } catch (MessagingException mex) {
      return false;
    }
  }
}
