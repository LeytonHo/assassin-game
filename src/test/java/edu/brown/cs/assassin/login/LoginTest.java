package edu.brown.cs.assassin.login;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import edu.brown.cs.assassin.main.AssassinConstants;

public class LoginTest {

  @Test
  public void testSimpleEncryption() {
    String unencrypted = "Obama 2";
    String encrypted = "j86VAJ8a7fTmdpn4qG3bVA==";
    assertEquals(encrypted, Login.encryptInfo(unencrypted, AssassinConstants.KEY));
    assertEquals(unencrypted, Login.decrypt(encrypted, AssassinConstants.KEY));
  }

  @Test
  public void weirdEncryption() {
    String unencrypted = " Nose15 -?. !";
    String encrypted = "6K25RCeTvPPhHSpwKZUF0Q==";
    assertEquals(encrypted, Login.encryptInfo(unencrypted, AssassinConstants.KEY));
    assertEquals(unencrypted, Login.decrypt(encrypted, AssassinConstants.KEY));
  }

}
