package edu.brown.cs.assassin.login;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import edu.brown.cs.assassin.main.AssassinConstants;

/**
 * Code pulled from :
 * https://howtodoinjava.com/security/java-aes-encryption-example/
 *
 * Login class to handle encryption of passwords and user data safety.
 */
public final class Login {

  private static SecretKeySpec secretKey;
  private static byte[] key;

  private Login() {
  }

  /**
   * Sets the key for the encryption process.
   *
   * @param myKey the string used to encrypt a string.
   */
  private static void setKey(String myKey) {
    MessageDigest sha = null;
    try {
      key = myKey.getBytes("UTF-8");
      sha = MessageDigest.getInstance("SHA-1");
      key = sha.digest(key);
      key = Arrays.copyOf(key, AssassinConstants.KEY_COPY);
      secretKey = new SecretKeySpec(key, "AES");
    } catch (Exception e) {
      // e.printStackTrace();
    }
  }

  /**
   * Encrypts an encrypted string using secret.
   *
   * @param strToEncrypt the string to encrypt.
   * @param secret       the key used to encode the string.
   * @return the encrypted string.
   */
  public static String encryptInfo(String strToEncrypt, String secret) {
    try {
      setKey(secret);
      Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
      cipher.init(Cipher.ENCRYPT_MODE, secretKey);
      return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
    } catch (Exception e) {
      System.out.println("Error while encrypting.");
    }
    return null;
  }

  /**
   * Decrypts an encrypted string using secret.
   *
   * @param strToDecrypt the string to decrypt.
   * @param secret       the key used to decode the string.
   * @return the decrypted string.
   */
  public static String decrypt(String strToDecrypt, String secret) {
    try {
      setKey(secret);
      Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
      cipher.init(Cipher.DECRYPT_MODE, secretKey);
      return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
    } catch (Exception e) {
      System.out.println("Error while decrypting.");
    }
    return null;
  }
}
