package edu.brown.cs.assassin.graph;

/**
 * Represents assassin/target pair.
 *
 * @param <U> Player type
 */
public class Target<U> {
  private U assassin;
  private U target;

  /**
   * Target constructor.
   *
   * @param a Assassin
   * @param t Target
   */
  public Target(U a, U t) {
    assassin = a;
    target = t;
  }

  /**
   * Getter method for Assassin.
   *
   * @return Assassin
   */
  public U getAssassin() {
    return assassin;
  }

  /**
   * Getter method for Target.
   *
   * @return Target
   */
  public U getTarget() {
    return target;
  }
}
