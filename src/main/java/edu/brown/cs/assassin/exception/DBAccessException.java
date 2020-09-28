package edu.brown.cs.assassin.exception;

/**
 * DBAccessException class used to handle errors that occur when pulling
 * information from a database.
 */
public class DBAccessException extends Exception {

  private static final long serialVersionUID = 8594229227623160899L;

  /**
   * Constructor that specifies the message for this exception.
   *
   * @param message the details specific to this exception when thrown.
   */
  public DBAccessException(String message) {
    super(message);
  }
}
