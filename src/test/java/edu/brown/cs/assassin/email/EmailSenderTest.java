package edu.brown.cs.assassin.email;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class EmailSenderTest {

  @Test
  public void testSendingEmail() {
    List<String> recipients = Arrays.asList("haridandapani@gmail.com");
    String subject = "A subject";
    String body = "<b>BODY !</b>";
    // Sometimes we reach our daily limit of emails sent, so just having this here
    // to show that it doesn't error when that happens, even if the assertion isn't
    // always true or false

    EmailSender.send(recipients, subject, body);
  }

}
