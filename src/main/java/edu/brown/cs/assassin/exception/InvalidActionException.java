package edu.brown.cs.assassin.exception;

/**
 * Indicates that some action could not be accomplished.
 */
public class InvalidActionException extends Exception {

  private static final long serialVersionUID = 1978618825903215195L;

  /**
   * Constructor that specifies the message for this exception.
   *
   * @param message the details specific to this exception when thrown.
   */
  public InvalidActionException(String message) {
    super(message);
  }
}
