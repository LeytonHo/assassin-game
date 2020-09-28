package edu.brown.cs.assassin.game;

import static edu.brown.cs.assassin.main.AssassinConstants.GAME_DATA_PATH;
import static edu.brown.cs.assassin.main.AssassinConstants.GAME_TEST_DATA_PATH;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import edu.brown.cs.assassin.database.DBMethods;
import edu.brown.cs.assassin.database.DBProxy;
import edu.brown.cs.assassin.email.EmailSender;
import edu.brown.cs.assassin.exception.DBAccessException;
import edu.brown.cs.assassin.exception.InvalidAccessException;
import edu.brown.cs.assassin.exception.InvalidActionException;
import edu.brown.cs.assassin.graph.Graph;
import edu.brown.cs.assassin.graph.Target;

/**
 * Represents a game of Assassin.
 */
public class Game implements Identifiable {
  private static DBProxy db;
  private int id;

  static {
    try {
      db = new DBProxy(GAME_DATA_PATH);
    } catch (DBAccessException e) {
      System.err.println("Error connecting to database: " + e.getMessage());
    }
  }

  /**
   * Creates a game object with given ID.
   *
   * @param id game ID
   */
  public Game(int id) {
    this.id = id;
  }

  // DATABASE CONNECTIONS ======================================================

  /**
   * Connects to the database used for storing actual game data.
   */
  public static void connectToMainDB() {
    try {
      db = new DBProxy(GAME_DATA_PATH);
    } catch (DBAccessException e) {
      System.err.println("Error connecting to database: " + e.getMessage());
    }
  }

  /**
   * Connects to the database used for storing test game data.
   */
  public static void connectToTestDB() {
    try {
      db = new DBProxy(GAME_TEST_DATA_PATH);
    } catch (DBAccessException e) {
      System.err.println("Error connecting to database: " + e.getMessage());
    }
  }

  // GETTER METHODS ============================================================

  /**
   * @return game ID
   */
  @Override
  public int getID() {
    return id;
  }

  /**
   * @return Game status (forming, playing, done)
   * @throws DBAccessException if something goes wrong with the database
   */
  public GameStatus getStatus() throws DBAccessException {
    String statusString = db
        .executeQuery("select status from game where id = ?", DBMethods.parameters(id)).get(0)
        .get(0);
    if (statusString.equals("0")) {
      return GameStatus.FORMING;
    } else if (statusString.equals("1")) {
      return GameStatus.PLAYING;
    } else {
      return GameStatus.DONE;
    }
  }

  /**
   * @return Game name
   * @throws DBAccessException if something goes wrong with the database
   */
  public String getName() throws DBAccessException {
    return db.executeQuery("select name from game where id = ?", DBMethods.parameters(id)).get(0)
        .get(0);
  }

  /**
   * @return Game rules
   * @throws DBAccessException if something goes wrong with the database
   */
  public String getRules() throws DBAccessException {
    return db.executeQuery("select rules from game where id = ?", DBMethods.parameters(id)).get(0)
        .get(0);
  }

  /**
   * @return Maximum size of teams in this game
   * @throws DBAccessException if something goes wrong with the database
   */
  public int getMaxTeamSize() throws DBAccessException {
    String maxSizeString = db
        .executeQuery("select max_team_size from game where id = ?", DBMethods.parameters(id))
        .get(0).get(0);
    return Integer.parseInt(maxSizeString);
  }

  /**
   * @return Number of targets each team has in this game
   * @throws DBAccessException if something goes wrong with the database
   */
  public int getNumTargets() throws DBAccessException {
    String numTargetsString = db
        .executeQuery("select num_targets from game where id = ?", DBMethods.parameters(id)).get(0)
        .get(0);
    return Integer.parseInt(numTargetsString);
  }

  /**
   * @return Time this game was created
   * @throws DBAccessException if something goes wrong with the database
   */
  public Instant getCreatedTime() throws DBAccessException {
    String createdString = db
        .executeQuery("select created from game where id = ?", DBMethods.parameters(id)).get(0)
        .get(0);
    return DBMethods.convertTime(createdString);
  }

  /**
   * @return Whether this game is anonymous (true) or whether players can see
   *         which players are in their target teams (false)
   * @throws DBAccessException if something goes wrong with the database
   */
  public boolean isAnonymous() throws DBAccessException {
    String anonymousString = db
        .executeQuery("select anonymous from game where id = ?", DBMethods.parameters(id)).get(0)
        .get(0);
    return anonymousString.equals("1");
  }

  /**
   * @return Game join code
   * @throws DBAccessException if something goes wrong with the database
   */
  public String getJoinCode() throws DBAccessException {
    return db.executeQuery("select join_code from game where id = ?", DBMethods.parameters(id))
        .get(0).get(0);
  }

  /**
   * @return Teams playing this game
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if the database contains an invalid team ID
   */
  public List<Team> getTeams() throws DBAccessException, InvalidAccessException {
    List<List<String>> teamStrings = db.executeQuery("select id from team where game = ?",
        DBMethods.parameters(id));
    List<Team> teams = new ArrayList<>();
    for (List<String> teamString : teamStrings) {
      teams.add(Team.fromID(Integer.parseInt(teamString.get(0))));
    }
    return teams;
  }

  /**
   * @return Teams playing this game who are still alive
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if the database contains an invalid team ID
   */
  public List<Team> getAliveTeams() throws DBAccessException, InvalidAccessException {
    List<List<String>> teamStrings = db
        .executeQuery("select id from team where game = ? and alive = 1", DBMethods.parameters(id));
    List<Team> teams = new ArrayList<>();
    for (List<String> teamString : teamStrings) {
      teams.add(Team.fromID(Integer.parseInt(teamString.get(0))));
    }
    return teams;
  }

  /**
   * @return Administrators of this game
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if the database contains an invalid user ID
   */
  public List<User> getAdmin() throws DBAccessException, InvalidAccessException {
    List<List<String>> adminStrings = db.executeQuery("select user from admin where game = ?",
        DBMethods.parameters(id));
    List<User> admin = new ArrayList<>();
    for (List<String> adminString : adminStrings) {
      admin.add(User.fromID(Integer.parseInt(adminString.get(0))));
    }
    return admin;
  }

  /**
   * Get the last n messages that were sent in this game, most recent first.
   *
   * @param numMessages Number of recent messages to get
   * @return The messages
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if the database contains an invalid message ID
   */
  public List<Message> getMessages(int numMessages)
      throws DBAccessException, InvalidAccessException {
    String query = "select id from message where game = ? order by id desc " + "limit ?";
    List<String> parameters = DBMethods.parameters(id, numMessages);
    List<List<String>> messageStrings = db.executeQuery(query, parameters);
    List<Message> messages = new ArrayList<>();
    for (List<String> messageString : messageStrings) {
      messages.add(Message.fromID(Integer.parseInt(messageString.get(0))));
    }
    return messages;
  }

  // GAME ACTIONS ==============================================================

  /**
   * Records in the database that the game has started.
   *
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidActionException if the game has already started
   */
  public void markStarted() throws DBAccessException, InvalidActionException {
    if (getStatus() != GameStatus.FORMING) {
      throw new InvalidActionException("This game has already started.");
    }
    db.updateDatabase("update game set status = 1 where id = ?", DBMethods.parameters(id));
  }

  /**
   * Records in the database that the game has ended.
   *
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidActionException if the game is not currently playing
   */
  public void markEnded() throws DBAccessException, InvalidActionException {
    if (getStatus() != GameStatus.PLAYING) {
      throw new InvalidActionException(
          "This game is not currently playing, so it cannot be ended.");
    }
    db.updateDatabase("update game set status = 2 where id = ?", DBMethods.parameters(id));
  }

  /**
   * Starts the game.
   *
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if the database contains an invalid ID
   * @throws InvalidActionException if the game has already started
   */
  public void start() throws DBAccessException, InvalidAccessException, InvalidActionException {
    generateNewTargets(); // Could throw exception if number of teams is insufficient
    markStarted();
    Message.addMessage(this, MessageType.START);

    String emailSubject = String.format("The game %s has started!", getName());
    String emailText = "Log in to see your team’s targets.";
    emailBlast(emailSubject, emailText);
  }

  /**
   * Ends the game.
   *
   * @param winner Team that won the game
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if the database contains an invalid user ID
   * @throws InvalidActionException if the game is not currently playing
   */
  public void end(Team winner)
      throws DBAccessException, InvalidAccessException, InvalidActionException {
    markEnded();
    Message.addMessage(this, MessageType.WIN, winner.getCodename());

    String emailSubject = String.format("The game %s has ended!", getName());
    String emailText = String.format("Congratulations to %s for winning!", winner.getCodename());
    emailBlast(emailSubject, emailText);
  }

  /**
   * Ends the game without recording a winner.
   *
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if the database contains an invalid user ID
   * @throws InvalidActionException if the game is not currently playing
   */
  public void endWithNoWinner()
      throws DBAccessException, InvalidAccessException, InvalidActionException {
    markEnded();
    Message.addMessage(this, MessageType.END);

    String emailSubject = String.format("The game %s has ended!", getName());
    StringBuilder emailText = new StringBuilder("The remaining teams are:<br>");
    for (Team team : getAliveTeams()) {
      emailText.append(team.getCodename());
      emailText.append("<br>");
    }
    Message.addMessage(this, MessageType.END);
    emailBlast(emailSubject, emailText.toString());
  }

  /**
   * Sets the name of the game to a new string.
   *
   * @param name New game name.
   * @throws DBAccessException if something goes wrong with the database
   */
  public void changeName(String name) throws DBAccessException {
    db.updateDatabase("update game set name = ? where id = ?", DBMethods.parameters(name, id));
    Message.addMessage(this, MessageType.CHANGE_NAME, name);
  }

  /**
   * Sets the rules of the game to a new string.
   *
   * @param rules New rules
   * @throws DBAccessException if something goes wrong with the database
   */
  public void changeRules(String rules) throws DBAccessException {
    db.updateDatabase("update game set rules = ? where id = ?", DBMethods.parameters(rules, id));
    Message.addMessage(this, MessageType.CHANGE_RULES, rules);
  }

  /**
   * Changes whether the game is anonymous.
   *
   * @param anon the new anonymity setting
   * @throws DBAccessException if something goes wrong with the database
   */
  public void changeAnonymity(boolean anon) throws DBAccessException {
    String anonString = anon ? "1" : "0";
    db.updateDatabase("update game set anonymous = ? where id = ?",
        DBMethods.parameters(anonString, id));
    if (anon) {
      Message.addMessage(this, MessageType.CHANGE_ANON);
    } else {
      Message.addMessage(this, MessageType.CHANGE_NOT_ANON);
    }
  }

  /**
   * Changes the number of targets allowed in a game.
   *
   * @param targets the new number of targets
   * @throws DBAccessException if something goes wrong with the database
   * @throws InvalidActionException if the input is not positive
   */
  public void changeNumTargets(int targets) throws DBAccessException, InvalidActionException {
    if (targets < 1) {
      throw new InvalidActionException("Number of targets must be positive.");
    }
    db.updateDatabase("update game set num_targets = ? where id = ?",
        DBMethods.parameters(targets, id));
    Message.addMessage(this, MessageType.CHANGE_NUM_TARGETS, Integer.toString(targets));
  }

  /**
   * Sends an email containing subject and text to all the players in the game.
   *
   * @param subject the subject of the email.
   * @param text    the body of the email, which can be formatted like html.
   * @return whether the blast sent properly.
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if the database contains an invalid user ID
   */
  public boolean emailBlast(String subject, String text)
      throws DBAccessException, InvalidAccessException {
    List<String> recipients = new ArrayList<>();
    List<List<String>> playerIds = db.executeQuery(
        "SELECT player.user FROM player JOIN team ON player.team = team.id WHERE team.game = ?",
        DBMethods.parameters(id));
    playerIds
        .addAll(db.executeQuery("SELECT user FROM admin WHERE game = ?", DBMethods.parameters(id)));
    for (List<String> playerID : playerIds) {
      User u = User.fromID(Integer.parseInt(playerID.get(0)));
      recipients.add(u.getEmail());
    }
    String subjectWithGame = String.format("[Assassin Game: %s] %s", getName(), subject);
    return EmailSender.send(recipients, subjectWithGame, text);
  }

  /**
   * Clears every team's targets.
   *
   * @throws DBAccessException if something goes wrong with the database
   */
  public void clearTargets() throws DBAccessException {
    String command = "DELETE FROM target WHERE killer IN "
        + "(SELECT target.killer FROM target INNER JOIN team "
        + "ON target.killer = team.id AND team.game = ?);";
    List<String> parameters = DBMethods.parameters(id);
    db.updateDatabase(command, parameters);
  }

  /**
   * Generates a new target graph for the teams in this game.
   *
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if the database contains an invalid ID
   * @throws InvalidActionException if a target could not be added
   */
  public void generateNewTargets()
      throws DBAccessException, InvalidAccessException, InvalidActionException {
    clearTargets();

    // Retrieve list of teams
    List<Team> teamList = getAliveTeams();
    if (teamList.size() < 2) {
      throw new InvalidActionException(
          "Not enough teams in this game to generate new targets (Number of teams: "
              + teamList.size() + ")");
    }

    // Retrieve num targets per team
    int numTargets = getNumTargets();

    // Create graph
    Graph<Team> g = new Graph<Team>(teamList, numTargets);

    // Save targets generated by graph into database
    for (Target<Team> t : g.getTargets()) {
      t.getAssassin().addTarget(t.getTarget());
    }

    // Message and email
    Message.addMessage(this, MessageType.NEW_TARGETS);
    String emailSubject = "You have new targets";
    String emailText = "Log into the game to check which teams you are now targeting.";
    emailBlast(emailSubject, emailText);
  }

  /**
   * In this game, sets all dead players on living teams to alive and generates new
   * kill codes for them. (A living team is one that has at least one living
   * player.)
   *
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if the team id for a team is invalid.
   */
  public void revivePlayersOnLivingTeams() throws DBAccessException, InvalidAccessException {
    String query = "select id from player where alive = 0 and exists ( "
        + "select * from team, game where player.team = team.id and team.game = ? "
        + "and team.alive = 1);";
    List<String> parameters = DBMethods.parameters(id);
    List<List<String>> playerQueryResults = db.executeQuery(query, parameters);
    for (List<String> result : playerQueryResults) {
      String idString = result.get(0);
      String newKillCode = DBMethods.generateUniqueCode(db, "player", "kill_code");
      String updateKillCodeCommand = "update player set kill_code = ? where id = ?";
      List<String> updateKillCodeParameters = DBMethods.parameters(newKillCode, idString);
      db.updateDatabase(updateKillCodeCommand, updateKillCodeParameters);
    }

    String command = "update player set alive = 1 where exists ( "
        + "select * from team, game where player.team = team.id and team.game = ? "
        + "and team.alive = 1);";

    db.updateDatabase(command, parameters);

    // Message and email
    Message.addMessage(this, MessageType.REVIVE);
    String emailSubject = "Your team is revived";
    for (Team team : getAliveTeams()) {
      String emailText = String.format("All players on team %s are now alive.", team.getCodename());
      team.emailEveryone(emailSubject, emailText);
    }
  }

  // CREATING/FINDING GAMES ====================================================

  /**
   * Adds a new game to the database.
   *
   * @param name        the name of the game
   * @param rules       the rules of the game
   * @param maxTeamSize the maximum size of teams in the game
   * @param admin       the administrators of the game
   * @param numTargets  the number of targets each team has in the game
   * @param anon        whether the teams should see only each other's names
   *                    (true) or also the players (false)
   * @return the Game that was created
   * @throws DBAccessException if something goes wrong with the database
   */
  public static Game addGame(String name, String rules, int maxTeamSize, List<User> admin,
      int numTargets, boolean anon) throws DBAccessException {
    String gameCommand = "insert into game"
        + "(name, rules, max_team_size, created, join_code, num_targets, anonymous) "
        + "values (?, ?, ?, ?, ?, ?, ?)";
    String code = DBMethods.generateUniqueCode(db, "game", "join_code");
    String anonString = anon ? "1" : "0";
    List<String> gameParameters = DBMethods.parameters(name, rules, maxTeamSize,
        DBMethods.timeString(), code, numTargets, anonString);
    db.updateDatabase(gameCommand, gameParameters);

    List<List<String>> addedGameQueryResult = db
        .executeQuery("select last_insert_rowid() from game");
    String addedGameIDString = addedGameQueryResult.get(0).get(0);
    int addedGameID = Integer.parseInt(addedGameIDString);
    String adminCommand = "insert into admin (user, game) values (?, ?)";
    for (User a : admin) {
      List<String> adminParameters = DBMethods.parameters(a, addedGameIDString);
      db.updateDatabase(adminCommand, adminParameters);
    }
    return new Game(addedGameID);
  }

  /**
   * Gets the Game with a given ID.
   *
   * @param id The ID
   * @return Game with this ID
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if there is no game with this ID
   */
  public static Game fromID(int id) throws DBAccessException, InvalidAccessException {
    if (db.hasKey("select * from game where id = ?", DBMethods.parameters(id))) {
      return new Game(id);
    } else {
      throw new InvalidAccessException("There is no game with ID " + id);
    }
  }

  /**
   * Gets the Game with a given join code.
   *
   * @param code Join code
   * @return Game with this join code
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if there is no game with this join code
   */
  public static Game fromJoinCode(String code) throws DBAccessException, InvalidAccessException {
    List<List<String>> gameQueryResult = db.executeQuery(
        "select id from game where status = 0 AND join_code = ?", DBMethods.parameters(code));
    if (gameQueryResult.isEmpty()) {
      throw new InvalidAccessException("There is no game with that join code.");
    } else {
      int gameID = Integer.parseInt(gameQueryResult.get(0).get(0));
      return new Game(gameID);
    }
  }

  // OVERRIDE METHODS ==========================================================

  @Override
  public String toString() {
    return "Game{" + "id=" + this.id + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Game gamer = (Game) o;
    return id == gamer.getID();
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
