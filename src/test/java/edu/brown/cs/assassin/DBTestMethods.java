package edu.brown.cs.assassin;

import edu.brown.cs.assassin.exception.DBAccessException;
import edu.brown.cs.assassin.database.DBMethods;
import edu.brown.cs.assassin.exception.InvalidAccessException;
import edu.brown.cs.assassin.exception.InvalidActionException;
import edu.brown.cs.assassin.game.Game;
import edu.brown.cs.assassin.game.Player;
import edu.brown.cs.assassin.game.Team;
import edu.brown.cs.assassin.game.User;

import java.sql.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import static edu.brown.cs.assassin.main.AssassinConstants.GAME_TEST_DATA_PATH;
import static edu.brown.cs.assassin.main.AssassinConstants.LOGIN_TEST_DATA_PATH;

/**
 * Contains helper methods useful for testing database methods.
 */
public final class DBTestMethods {
  /**
   * Removes every entry from a given database.
   * @param path the path to the database
   * @throws ClassNotFoundException if database classes could not be found
   * @throws SQLException if there was a SQL error
   */
  public static void clearDatabase(String path) throws ClassNotFoundException, SQLException {
    Class.forName("org.sqlite.JDBC");
    String urlToDB = "jdbc:sqlite:" + path;
    Connection conn = DriverManager.getConnection(urlToDB);
    PreparedStatement tables = conn.prepareStatement("SELECT tbl_name FROM sqlite_master");
    ResultSet rs = tables.executeQuery();
    while (rs.next()) {
      String tableName = rs.getString(1);
      PreparedStatement delete = conn.prepareStatement("DELETE FROM " + tableName);
      delete.execute();
    }
  }

  /**
   * Generates a string of a given length that alternates between consonants and vowels, with the
   * first letter capitalized. Uses a given random seed so the results are predictable.
   *
   * @param seed   the random seed
   * @param length the length of the string to create
   * @return a random string
   */
  public static String generateString(int seed, int length) {
    String consonants = "bcdfghjklmnpqrstvwxyz";
    String vowels = "aeiou";
    int numConsonants = consonants.length();
    int numVowels = vowels.length();
    StringBuilder name = new StringBuilder();
    Random r = new Random(seed);
    for (int i = 0; i < length; i++) {
      char c;
      if (i % 2 == 0) {
        c = consonants.charAt(r.nextInt(numConsonants));
      } else {
        c = vowels.charAt(r.nextInt(numVowels));
      }
      if (i == 0) {
        c = Character.toUpperCase(c);
      }
      name.append(c);
    }
    return name.toString();
  }

  /**
   * Adds a given number of pseudorandom users to the users database. Generates strings in
   * predictable ways to allow for testing.
   *
   * @param users the number of users to add
   * @throws DBAccessException if something goes wrong with the database
   * @throws InvalidActionException if users could not be added
   */
  public static void addUsers(int users) throws DBAccessException, InvalidActionException {
    for (int i = 0; i < users; i++) {
      String email = String.format("user_%d@email.com", i);
      String name = generateString(i, 8);
      String password = generateString(i + 100000, 12);
      User.addUser(email, name, password);
    }
  }

  /**
   * Sets up the databases for testing: clears test databases and connects all the database classes
   * to the test databases.
   *
   * @throws DBAccessException if something goes wrong with a database
   */
  public static void setUp() throws DBAccessException {
    try {
      clearDatabase(GAME_TEST_DATA_PATH);
      clearDatabase(LOGIN_TEST_DATA_PATH);
      DBMethods.connectToTestDB();
    } catch (SQLException | ClassNotFoundException e) {
      throw new DBAccessException("Error clearing databases");
    }
  }

  /**
   * Connects database classes back to main database after testing.
   */
  public static void tearDown() {
    DBMethods.connectToMainDB();
  }
}
