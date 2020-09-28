package edu.brown.cs.assassin.exception;

/**
 * Indicates that a method is trying to access something that does not exist.
 */
public class InvalidAccessException extends Exception {

  private static final long serialVersionUID = -4929016782548962967L;

  /**
   * Constructor that specifies the message for this exception.
   *
   * @param message the details specific to this exception when thrown.
   */
  public InvalidAccessException(String message) {
    super(message);
  }
}
