package edu.brown.cs.assassin.game;

import static edu.brown.cs.assassin.main.AssassinConstants.GAME_DATA_PATH;
import static edu.brown.cs.assassin.main.AssassinConstants.GAME_TEST_DATA_PATH;
import static edu.brown.cs.assassin.main.AssassinConstants.LOGIN_DATA_PATH;
import static edu.brown.cs.assassin.main.AssassinConstants.LOGIN_TEST_DATA_PATH;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import edu.brown.cs.assassin.database.DBMethods;
import edu.brown.cs.assassin.exception.DBAccessException;
import edu.brown.cs.assassin.database.DBProxy;
import edu.brown.cs.assassin.exception.InvalidAccessException;
import edu.brown.cs.assassin.exception.InvalidActionException;
import edu.brown.cs.assassin.login.Login;
import edu.brown.cs.assassin.main.AssassinConstants;

/**
 * Represents a user account.
 */
public class User implements Identifiable {

  private static DBProxy userDB;
  private static DBProxy gameDB;
  private int id;
  private String name;
  private String email;

  // DATABASE CONNECTIONS ======================================================

  /**
   * Connects to the database used for storing actual user data.
   */
  public static void connectToMainDB() {
    try {
      userDB = new DBProxy(LOGIN_DATA_PATH);
      gameDB = new DBProxy(GAME_DATA_PATH);
    } catch (DBAccessException e) {
      System.err.println("Error connecting to database: " + e.getMessage());
    }
  }

  /**
   * Connects to the database used for storing test user data.
   */
  public static void connectToTestDB() {
    try {
      userDB = new DBProxy(LOGIN_TEST_DATA_PATH);
      gameDB = new DBProxy(GAME_TEST_DATA_PATH);
    } catch (DBAccessException e) {
      System.err.println("Error connecting to database: " + e.getMessage());
    }
  }

  // CONSTRUCTORS ==============================================================

  /**
   * Constructor given just user ID.
   *
   * @param id User ID
   */
  public User(int id) {
    this.id = id;
  }

  /**
   * Constructor given full account details.
   *
   * @param id    User ID
   * @param email User email
   * @param name  Username
   */
  public User(int id, String email, String name) {
    this(id);
    this.name = name;
    this.email = email;
  }

  // GETTER METHODS ============================================================

  /**
   * @return ID of this user
   */
  @Override
  public int getID() {
    return id;
  }

  /**
   * @return Username of this user
   * @throws DBAccessException if something goes wrong with the database
   */
  public String getName() throws DBAccessException {
    if (name == null) {
      name = userDB.executeQuery("select name from login where id = ?", DBMethods.parameters(id))
              .get(0).get(0);
    }
    return name;
  }

  /**
   * @return Email address of this user
   * @throws DBAccessException if something goes wrong with the database
   */
  public String getEmail() throws DBAccessException {
    if (email == null) {
      email = userDB.executeQuery("select email from login where id = ?", DBMethods.parameters(id))
              .get(0).get(0);
    }
    return email;
  }

  /**
   * @return Players this user is playing as
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if the database contains an invalid player ID
   */
  public List<Player> getPlayers() throws DBAccessException, InvalidAccessException {
    List<List<String>> playerStrings = gameDB.executeQuery("select id from player where user = ?",
            DBMethods.parameters(id));
    List<Player> players = new ArrayList<>();
    for (List<String> playerString : playerStrings) {
      players.add(Player.fromID(Integer.parseInt(playerString.get(0))));
    }
    return players;
  }

  /**
   * @return Games this user is playing
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if the database contains an invalid ID
   */
  public List<Game> getPlayingGames() throws DBAccessException, InvalidAccessException {
//    List<List<String>> gameStrings = gameDB.executeQuery(
//            "select team.game from player, team where player.user = ? and player.team = team.id",
//            DBMethods.parameters(id));
    List<List<String>> gameStrings = gameDB.executeQuery(
            "select game.id from player, team, game where player.user = ? "
                    + "and player.team = team.id and team.game = game.id "
                    + "order by game.created desc",
            DBMethods.parameters(id));
    List<Game> games = new ArrayList<>();
    for (List<String> gameString : gameStrings) {
      games.add(Game.fromID(Integer.parseInt(gameString.get(0))));
    }
    return games;
  }

  /**
   * @return Games this user is admin for
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if the database contains an invalid ID
   */
  public List<Game> getAdminGames() throws DBAccessException, InvalidAccessException {
    List<List<String>> gameStrings = gameDB.executeQuery("select game.id from admin, game where "
                    + "admin.user = ? and admin.game = game.id order by game.created desc",
            DBMethods.parameters(id));
    List<Game> games = new ArrayList<>();
    for (List<String> gameString : gameStrings) {
      games.add(Game.fromID(Integer.parseInt(gameString.get(0))));
    }
    return games;
  }

  // JOINING GAMES/TEAMS =======================================================

  /**
   * Represents information whether a user can join a team or game.
   */
  public class JoinResult {
    private Team team;
    private Game game;
    private String errorMessage;

    /**
     * Creates a new JoinResult indicating that the user can join a game.
     *
     * @param game the game the user is attempting to join
     */
    public JoinResult(Game game) {
      this.game = game;
      this.team = null;
      this.errorMessage = null;
    }

    /**
     * Creates a new JoinResult indicating that the user can join a team.
     *
     * @param game the game of the team
     * @param team the team the user is attempting to join
     */
    public JoinResult(Game game, Team team) {
      this.game = game;
      this.team = team;
      this.errorMessage = null;
    }

    /**
     * Creates a new JoinResult indicating that the user cannot join the game or team they were
     * attempting to join.
     *
     * @param errorMessage the reason why they cannot join this game or team
     */
    public JoinResult(String errorMessage) {
      this.game = null;
      this.team = null;
      this.errorMessage = errorMessage;
    }

    /**
     * @return whether this user can (or did) join the game
     */
    public boolean joinGame() {
      return game != null;
    }

    /**
     * @return whether this user can (or did) join the team
     */
    public boolean joinTeam() {
      return team != null;
    }

    /**
     * @return the team the user joined, or null if they did not join a team
     */
    public Team getTeam() {
      return team;
    }

    /**
     * @return the game the user joined, or null if they did not join a game
     */
    public Game getGame() {
      return game;
    }

    /**
     * @return the reason why the user cannot (or did not) join a game or team
     */
    public String getError() {
      return errorMessage;
    }
  }

  /**
   * Checks whether this user can join the input game. Return a JoinResult either indicating that
   * they did join the game or indicating why they couldn't.
   *
   * @param game Game to add this user to
   * @return a JoinResult indicating whether the user can join the game, and if not, why
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if the database contains an invalid ID
   */
  public JoinResult checkJoinGame(Game game) throws DBAccessException, InvalidAccessException {
    if (game.getStatus() == GameStatus.PLAYING) {
      return new JoinResult("This game has already started.");
    } else if (game.getStatus() == GameStatus.DONE) {
      return new JoinResult("This game has finished.");
    } else if (getPlayingGames().contains(game)) {
      return new JoinResult("You are already playing this game.");
    } else if (getAdminGames().contains(game)) {
      return new JoinResult("You are already an admin of this game.");
    }
    return new JoinResult(game);
  }

  /**
   * Checks whether this user can join the input team. Return a JoinResult either indicating that
   * they did join the team or indicating why they couldn't.
   *
   * @param team Team to add this user to
   * @return a JoinResult indicating whether the user can join the team, and if not, why
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if the database contains an invalid ID
   */
  public JoinResult checkJoinTeam(Team team) throws DBAccessException, InvalidAccessException {
    Game game = team.getGame();
    JoinResult gameResult = checkJoinGame(game);
    if (gameResult.joinGame()) {
      if (team.getPlayers().size() >= game.getMaxTeamSize()) {
        return new JoinResult("This team is already at the maximum size.");
      } else {
        return new JoinResult(game, team);
      }
    } else {
      return gameResult;
    }
  }

  /**
   * Adds this user to a game as part of a new team.
   *
   * @param game     Game to join
   * @param codename Codename of the new team
   * @return a JoinResult indicating whether the user joined the game in a new team, and if not, why
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if the database contains an invalid ID
   * @throws InvalidActionException if
   */
  public JoinResult formNewTeam(Game game, String codename)
          throws DBAccessException, InvalidAccessException, InvalidActionException {
    JoinResult gameResult = checkJoinGame(game);
    if (gameResult.joinGame()) {
      Team newTeam = Team.addTeam(codename, game);
      Player.addPlayer(this, newTeam);
      return new JoinResult(game, newTeam);
    } else {
      return gameResult;
    }
  }

  /**
   * Adds this user to a game as part of an existing team.
   *
   * @param team Team to join
   * @return a JoinResult indicating whether the user can join the team, and if not, why
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if the database contains an invalid ID
   * @throws InvalidActionException if this user could not join this team
   */
  public JoinResult joinExistingTeam(Team team)
          throws DBAccessException, InvalidAccessException, InvalidActionException {
    JoinResult teamResult = checkJoinTeam(team);
    if (teamResult.joinTeam()) {
      Player.addPlayer(this, team);
    }
    return teamResult;
  }

  /**
   * Adds this user to a game as part of an existing team, given the join code of
   * the team.
   *
   * @param code Join code of the team to join
   * @return a JoinResult indicating whether the user can join the team, and if not, why
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if there is no team with this code or if the
   *                                database contains an invalid ID
   * @throws InvalidActionException if this user could not join this team
   */
  public JoinResult joinTeamFromCode(String code)
          throws DBAccessException, InvalidAccessException, InvalidActionException {
    Team team = Team.fromJoinCode(code);
    return joinExistingTeam(team);
  }

  // CREATING/FINDING USERS ====================================================

  /**
   * Adds a new user to database. NOTE: password should be unencrypted.
   *
   * @param email    Email address of the user
   * @param name     Username of the user
   * @param password Password of the user
   * @return User that was created
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidActionException if the user could not be added given this info
   */
  public static User addUser(String email, String name, String password)
          throws DBAccessException, InvalidActionException {
    // Check if user email is valid (must be unique identifier)
    if (userDB.hasKey("SELECT * FROM login WHERE email = ?", DBMethods.parameters(email))) {
      throw new InvalidActionException("Email already in use");
    } else {
      // Update the user database with new user information
      userDB.updateDatabase(
              "INSERT INTO login (email, password, name) " + "VALUES (?, ?, ?)",
              DBMethods.parameters(
                      email, Login.encryptInfo(password, AssassinConstants.KEY), name));

      String keyString = userDB.executeQuery("select last_insert_rowid() from login").get(0).get(0);
      int key = Integer.parseInt(keyString);

      // Update the game database with new user ID
      gameDB.updateDatabase("insert into user (id) values (?)", DBMethods.parameters(key));

      // Return a User object for convenience
      return new User(key, email, name);
    }
  }

  /**
   * Gets the User with a given ID.
   *
   * @param id The ID
   * @return User with this ID
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if there is no user with this ID
   */
  public static User fromID(int id) throws DBAccessException, InvalidAccessException {
    String query = "SELECT email, name FROM login WHERE id = ?";
    List<String> parameters = DBMethods.parameters(id);
    List<List<String>> userStrings = userDB.executeQuery(query, parameters);
    if (userStrings.isEmpty()) {
      throw new InvalidAccessException(String.format("Invalid user id '%s'", id));
    } else {
      List<String> resRow = userStrings.get(0);
      return new User(id, resRow.get(0), resRow.get(1));
    }
  }

  // LOGIN =====================================================================

  /**
   * Attempts to log in with a given email and password.
   *
   * @param email    Email input
   * @param password Password input
   * @return User with this email and password, if they are correct
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if the input is not a correct email/password
   *                                pair
   */
  public static User attemptLogin(String email, String password)
          throws DBAccessException, InvalidAccessException {

    // Query database for the given account
    List<List<String>> databaseOutput = userDB.executeQuery(
            "SELECT password, name, id FROM login WHERE email = ?", DBMethods.parameters(email));
    if (databaseOutput.size() == 0) {
      throw new InvalidAccessException("No account found with that email");
    }

    // Check if passwords match
    List<String> res = databaseOutput.get(0);
    String passwordEntered = Login.encryptInfo(password, AssassinConstants.KEY);
    if (!(res.get(0).equals(passwordEntered))) {
      throw new InvalidAccessException("Incorrect password");
    }

    // If details are valid, return the requested account
    try {
      int userID = Integer.parseInt(res.get(2));
      return new User(userID, email, res.get(1));
    } catch (NumberFormatException nfe) {
      throw new DBAccessException("Invalid value for user id (database may be malformed)");
    }
  }

  /**
   * Update password reset in database.
   *
   * @param id      Player ID
   * @param newPass New password
   * @throws DBAccessException if something goes wrong with the database
   */
  public static void resetPassword(int id, String newPass) throws DBAccessException {
    String encrypted = Login.encryptInfo(newPass, AssassinConstants.KEY);
    String idString = String.valueOf(id);
    List<String> parameters = Arrays.asList(encrypted, idString);
    userDB.updateDatabase("update login set password = ? where id = ?", parameters);
  }

  // OVERRIDE METHODS ==========================================================

  @Override
  public String toString() {
    return "User{id=" + id + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    User user = (User) o;
    return id == user.id;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
