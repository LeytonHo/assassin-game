package edu.brown.cs.assassin.database;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.brown.cs.assassin.exception.DBAccessException;
import edu.brown.cs.assassin.game.Game;
import edu.brown.cs.assassin.game.User;
import edu.brown.cs.assassin.game.Player;
import edu.brown.cs.assassin.game.Team;
import edu.brown.cs.assassin.game.Message;
import edu.brown.cs.assassin.game.Identifiable;

/**
 * A utility class for various Assassin database operations.
 */
public final class DBMethods {
  private static final int CODE_WORD_LENGTH = 6;
  private static final int NUM_CODE_WORDS = 2;

  private DBMethods() {
  }

  /**
   * Connects database accessing classes to the database used for storing actual
   * user data.
   */
  public static void connectToMainDB() {
    Game.connectToMainDB();
    User.connectToMainDB();
    Player.connectToMainDB();
    Team.connectToMainDB();
    Message.connectToMainDB();
  }

  /**
   * Connects database accessing classes to the database used for storing test
   * data.
   */
  public static void connectToTestDB() {
    Game.connectToTestDB();
    User.connectToTestDB();
    Player.connectToTestDB();
    Team.connectToTestDB();
    Message.connectToTestDB();
  }

  /**
   * A short way to make a parameter list for DBProxy methods. Inputs any number
   * of objects and creates a list of strings based on those objects. Identifiable
   * objects are added as a string containing their ID, and everything else is
   * added based on its toString method.
   *
   * @param os the objects to make a list of strings from
   * @return a list of strings representing the input objects, usable as input to
   *         DBProxy methods
   */
  public static List<String> parameters(Object... os) {
    List<String> parameters = new ArrayList<>();
    for (Object o : os) {
      if (o instanceof Identifiable) {
        parameters.add(Integer.toString(((Identifiable) o).getID()));
      } else {
        parameters.add(o.toString());
      }
    }
    return parameters;
  }

  /**
   * Generates a random code. Used for game and team join codes and for kill
   * codes. Each code is formatted as a series of short strings which alternate
   * from consonants to vowels to make them easier to remember. The number and
   * size of the strings can be specified with CODE_WORD_LENGTH and
   * NUM_CODE_WORDS. Does not include q or x to make codes more pronounceable.
   *
   * @return the code
   */
  public static String generateCode() {
    Random r = new Random();
    char[] vowels = "aeiou".toCharArray();
    char[] consonants = "bcdfghjklmnprstvwyz".toCharArray();
    StringBuilder code = new StringBuilder();
    for (int i = 0; i < CODE_WORD_LENGTH * NUM_CODE_WORDS; i++) {
      if (i % 2 == 0) {
        code.append(consonants[r.nextInt(consonants.length)]);
      } else {
        code.append(vowels[r.nextInt(vowels.length)]);
      }
      if ((i + 1) % CODE_WORD_LENGTH == 0 && i != CODE_WORD_LENGTH * NUM_CODE_WORDS - 1) {
        code.append(' ');
      }
    }
    return code.toString();
  }

  /**
   * Generates a code that is guaranteed to be unique among all codes in a certain
   * database column.
   *
   * @param db     the database of the column
   * @param table  the table of the column
   * @param column the name of the column
   * @return a code that is different from all the other codes in that column of
   *         that table of that database
   * @throws DBAccessException if something goes wrong with the database
   */
  public static String generateUniqueCode(DBProxy db, String table, String column)
      throws DBAccessException {
    String code = null;
    boolean foundUniqueCode = false;
    while (!foundUniqueCode) {
      code = generateCode();
      foundUniqueCode = !db.hasKey("select * from " + table + " where " + column + " = ?",
          parameters(code));
    }
    return code;
  }

  /**
   * Returns the current Unix timestamp as a String. (We will probably want to
   * delete this eventually once we change how times are stored in the database.)
   *
   * @return the current Unix timestamp as a String
   */
  public static String timeString() {
    return Long.toString(Instant.now().getEpochSecond());
  }

  /**
   * Returns the Instant represented by a timestamp as a String.
   *
   * @param timeString a Unix timestamp as a String
   * @return the Instant of that timestamp
   */
  public static Instant convertTime(String timeString) {
    return Instant.ofEpochSecond(Long.parseLong(timeString));
  }
}
