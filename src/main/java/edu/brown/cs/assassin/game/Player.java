package edu.brown.cs.assassin.game;

import static edu.brown.cs.assassin.main.AssassinConstants.GAME_DATA_PATH;
import static edu.brown.cs.assassin.main.AssassinConstants.GAME_TEST_DATA_PATH;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import edu.brown.cs.assassin.database.DBMethods;
import edu.brown.cs.assassin.database.DBProxy;
import edu.brown.cs.assassin.email.EmailSender;
import edu.brown.cs.assassin.exception.DBAccessException;
import edu.brown.cs.assassin.exception.InvalidAccessException;
import edu.brown.cs.assassin.exception.InvalidActionException;

/**
 * Represents a player within the context of a single game.
 */
public class Player implements Identifiable {
  private static DBProxy db;
  private int id;

  // DATABASE CONNECTIONS ======================================================

  /**
   * Connects to the database used for storing actual player data.
   */
  public static void connectToMainDB() {
    try {
      db = new DBProxy(GAME_DATA_PATH);
    } catch (DBAccessException e) {
      System.err.println("Error connecting to database: " + e.getMessage());
    }
  }

  /**
   * Connects to the database used for storing test player data.
   */
  public static void connectToTestDB() {
    try {
      db = new DBProxy(GAME_TEST_DATA_PATH);
    } catch (DBAccessException e) {
      System.err.println("Error connecting to database: " + e.getMessage());
    }
  }

  // CONSTRUCTOR ===============================================================

  /**
   * Player constructor.
   *
   * @param id player ID
   */
  public Player(int id) {
    this.id = id;
  }

  // GETTER METHODS ============================================================

  /**
   * @return ID of this player
   */
  @Override
  public int getID() {
    return id;
  }

  /**
   * @return User who is playing as this player
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if the database contains an invalid user ID
   */
  public User getUser() throws DBAccessException, InvalidAccessException {
    String userIDString = db
        .executeQuery("select user from player where id = ?", DBMethods.parameters(id)).get(0)
        .get(0);
    int userID = Integer.parseInt(userIDString);
    return User.fromID(userID);
  }

  /**
   * @return Team this player is on
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if the database contains an invalid team ID
   */
  public Team getTeam() throws DBAccessException, InvalidAccessException {
    String userIDString = db
        .executeQuery("select team from player where id = ?", DBMethods.parameters(id)).get(0)
        .get(0);
    int userID = Integer.parseInt(userIDString);
    return Team.fromID(userID);
  }

  /**
   * @return Time the team joined this game
   * @throws DBAccessException if something goes wrong with the database
   */
  public Instant getJoinedTeamTime() throws DBAccessException {
    String joinedString = db
        .executeQuery("select joined_team from player where id = ?", DBMethods.parameters(id))
        .get(0).get(0);
    return DBMethods.convertTime(joinedString);
  }

  /**
   * @return Whether this team is alive (i.e. any members are alive)
   * @throws DBAccessException if something goes wrong with the database
   */
  public boolean isAlive() throws DBAccessException {
    String aliveString = db
        .executeQuery("select alive from player where id = ?", DBMethods.parameters(id)).get(0)
        .get(0);
    return (aliveString.equals("1"));
  }

  /**
   * @return Game this player is playing
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if the database contains an invalid game ID
   */
  public Game getGame() throws DBAccessException, InvalidAccessException {
    String gameIDString = db.executeQuery(
        "select team.game from player, team where " + "player.id = ? and player.team = team.id",
        DBMethods.parameters(id)).get(0).get(0);
    int gameID = Integer.parseInt(gameIDString);
    return Game.fromID(gameID);
  }

  /**
   * @return Kill code of this player
   * @throws DBAccessException if something goes wrong with the database
   */
  public String getKillCode() throws DBAccessException {
    return db.executeQuery("select kill_code from player where id = ?", DBMethods.parameters(id))
        .get(0).get(0);
  }

  // PLAYER ACTIONS ============================================================

  /**
   * Removes this player from the game.
   *
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if the database contains an invalid ID
   * @throws InvalidActionException if the player could not be removed
   */
  public void remove() throws DBAccessException, InvalidAccessException, InvalidActionException {
    if (getGame().getStatus() != GameStatus.FORMING) {
      throw new InvalidActionException("Cannot remove players once the game " + "has started.");
    }
    Team team = getTeam();
    db.updateDatabase("delete from player where id = ?", DBMethods.parameters(id));
    if (team.getPlayers().isEmpty()) {
      db.updateDatabase("delete from team where id = ?", DBMethods.parameters(team));
    }
  }

  /**
   * Sends an email containing subject and text to this player..
   *
   * @param subject Subject of the email.
   * @param text    Body of the email, which can be formatted like html.
   * @return Whether the email sent properly.
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if the database contains an invalid user ID
   */
  public boolean email(String subject, String text)
      throws InvalidAccessException, DBAccessException {
    List<String> emailList = Collections.singletonList(getUser().getEmail());
    return EmailSender.send(emailList, subject, text);
  }

  // KILL HANDLING =============================================================

  /**
   * KillResult class to take proper action when a Player tries to kill another
   * Player.
   */
  public class KillResult {
    private Player killer;
    private Player target;
    private Team team;
    private boolean wonGame;
    private String errorMessage;

    /**
     * Stores a killing with a killer and target.
     *
     * @param killer Player who killed target.
     * @param target Victim of target.
     */
    public KillResult(Player killer, Player target) {
      this.killer = killer;
      this.target = target;
      team = null;
      wonGame = false;
      errorMessage = null;
    }

    /**
     * Stores a killing with a killer, a target Player, and an eliminated Team.
     *
     * @param killer  Player who killed target.
     * @param target  Victim of target.
     * @param team    Team eliminated by target's death.
     * @param wonGame Whether this kill caused this team to win the game
     */
    public KillResult(Player killer, Player target, Team team, boolean wonGame) {
      this.killer = killer;
      this.target = target;
      this.team = team;
      this.wonGame = wonGame;
      errorMessage = null;
    }

    /**
     * Constructor used to record an erroneous killing.
     *
     * @param killer       Killer to committed the erroneous killing.
     * @param errorMessage Error message of how the killing went wrong.
     */
    public KillResult(Player killer, String errorMessage) {
      this.killer = killer;
      this.target = null;
      team = null;
      this.errorMessage = errorMessage;
    }

    /**
     * @return Whether a player was killed
     */
    public boolean didKill() {
      return target != null;
    }

    /**
     * @return Whether a team was eliminated
     */
    public boolean didEliminate() {
      return team != null;
    }

    /**
     * @return Whether this kill made this player’s team win the game
     */
    public boolean didWin() {
      return wonGame;
    }

    /**
     * @return Killer player
     */
    public Player getKiller() {
      return killer;
    }

    /**
     * @return Player killed, or null if no player was killed
     */
    public Player getTarget() {
      return target;
    }

    /**
     * @return Team eliminated, or null if no team was eliminated
     */
    public Team getTeam() {
      return team;
    }

    /**
     * @return Reason why the kill could not happen, or null if the kill did happen
     */
    public String getError() {
      return errorMessage;
    }
  }

  /**
   * Kills a target player. (Does not check whether the kill is allowed.)
   *
   * @param target Player being killed
   * @return Information about the kill
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if the database contains an invalid ID
   * @throws InvalidActionException if there is an issue eliminating the team
   */
  public KillResult kill(Player target)
      throws DBAccessException, InvalidAccessException, InvalidActionException {
    // Mark the player as killed
    db.updateDatabase("update player set alive = 0 where id = ?", DBMethods.parameters(target));

    // Eliminate their team if there are no living players left
    // (Also adds new targets)
    int eliminatedTeam = 0;
    Team targetTeam = target.getTeam();
    if (targetTeam.getAlivePlayers().isEmpty()) {
      targetTeam.eliminate(target, getTeam());
      eliminatedTeam = 1;
    }

    // Record the kill in the database
    String killCommand = "insert into kill (killer, target, kill_time, eliminated_team) "
        + "values (?, ?, ?, ?)";
    List<String> killParameters = DBMethods.parameters(id, target, DBMethods.timeString(),
        eliminatedTeam);
    db.updateDatabase(killCommand, killParameters);

    // If the team was eliminated, record that in a message
    if (eliminatedTeam == 1) {
      Game game = getGame();
      Team team = getTeam();
      Message.addMessage(game, MessageType.ELIMINATE, team.getCodename(), targetTeam.getCodename());

      // If this elimination won the game, record the win and end the game
      boolean wonGame = false;
      if (game.getAliveTeams().size() == 1) {
        game.end(team);
        wonGame = true;
      }

      // Return a record of what happened
      return new KillResult(this, target, targetTeam, wonGame);
    } else {
      return new KillResult(this, target);
    }
  }

  /**
   * Allows a player to surrender.
   *
   * @return a KillResult with details of the surrender.
   * @throws DBAccessException      if something goes wrong in the database.
   * @throws InvalidAccessException if the surrender access illegal information.
   * @throws InvalidActionException if the surrender cannot happen.
   */
  public KillResult surrender()
      throws DBAccessException, InvalidAccessException, InvalidActionException {
    if (!isAlive()) {
      return new KillResult(this, "You cannot surrender if you are dead.");
    }
    db.updateDatabase("update player set alive = 0 where id = ?", DBMethods.parameters(this));
    int eliminatedTeam = 0;
    Team team = getTeam();
    if (team.getAlivePlayers().isEmpty()) {
      team.eliminate(this, team);
      eliminatedTeam = 1;
    }

    // If the team was eliminated, record that in a message
    if (eliminatedTeam == 1) {
      Game game = getGame();
      Message.addMessage(game, MessageType.SURRENDER, team.getCodename());

      // If this elimination won the game, record the win and end the game
      boolean wonGame = false;
      if (game.getAliveTeams().size() == 1) {
        game.end(game.getAliveTeams().get(0));
        wonGame = true;
      }
      // Return a record of what happened
      return new KillResult(this, this, team, wonGame);
    } else {
      return new KillResult(this, this);
    }
  }

  /**
   * If the kill code matches that of any player this player is targeting, and
   * this player is able to kill them (i.e. they are both alive and not the same),
   * execute the kill and return the killed player. If not, return null.
   *
   * @param killCode Kill code input
   * @return Information about the kill
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if the database contains an invalid ID
   * @throws InvalidActionException if there is an issue killing the player
   */
  public KillResult killByCode(String killCode)
      throws DBAccessException, InvalidAccessException, InvalidActionException {
    if (!isAlive()) {
      return new KillResult(this, "You cannot kill a player if you are dead.");
    }
    String query = "select target_player.id from target inner join player as killer "
        + "inner join player as target_player on "
        + "killer.id = ? and killer.team = target.killer "
        + "and target_player.team = target.target and target_player.kill_code = ?";
    List<String> parameters = DBMethods.parameters(id, killCode);
    if (db.hasKey(query, parameters)) {
      Player target = fromKillCode(killCode);
      if (equals(target)) {
        return new KillResult(this, "You cannot kill yourself.");
      }
      if (!target.isAlive()) {
        return new KillResult(this, "That player is already dead.");
      }
      return kill(target);
    } else {
      return new KillResult(this, "You are not targeting any player with that kill code.");
    }
  }

  // CREATING/FINDING PLAYERS ==================================================

  /**
   * Adds a new player to the database.
   *
   * @param user User who is playing as the player
   * @param team Team the player is on
   * @return Player that was created
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if the database contains an invalid ID
   * @throws InvalidActionException if the player could not be added
   */
  public static Player addPlayer(User user, Team team)
      throws DBAccessException, InvalidAccessException, InvalidActionException {
    User.JoinResult joinResult = user.checkJoinTeam(team);
    if (!joinResult.joinTeam()) {
      throw new InvalidActionException(joinResult.getError());
    }

    String command = "insert into player (user, team, kill_code, joined_team)"
        + " values (?, ?, ?, ?)";
    String code = DBMethods.generateUniqueCode(db, "player", "kill_code");
    List<String> parameters = DBMethods.parameters(user, team, code, DBMethods.timeString());
    db.updateDatabase(command, parameters);

    List<List<String>> addedPlayerQueryResult = db
        .executeQuery("select last_insert_rowid() from player");
    int addedPlayerID = Integer.parseInt(addedPlayerQueryResult.get(0).get(0));
    return new Player(addedPlayerID);
  }

  /**
   * Gets the Player with a given ID.
   *
   * @param id The ID
   * @return Player with this ID
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if there is no player with this ID
   */
  public static Player fromID(int id) throws DBAccessException, InvalidAccessException {
    if (db.hasKey("select * from player where id = ?", DBMethods.parameters(id))) {
      return new Player(id);
    } else {
      throw new InvalidAccessException("There is no player with id " + id);
    }
  }

  /**
   * Gets the Player representing a given user playing in a given game.
   *
   * @param user User who is playing as this player
   * @param game Game this player is playing
   * @return Player for this user and game
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if this user is not playing on this game
   */
  public static Player fromUserAndGame(User user, Game game)
      throws DBAccessException, InvalidAccessException {
    String query = "select player.id from player, team where "
        + "player.user = ? and player.team = team.id and team.game = ?";
    List<String> parameters = DBMethods.parameters(user, game);
    List<List<String>> playerStrings = db.executeQuery(query, parameters);
    if (playerStrings.isEmpty()) {
      throw new InvalidAccessException("This user does not play in this game.");
    } else {
      return new Player(Integer.parseInt(playerStrings.get(0).get(0)));
    }
  }

  /**
   * Gets the Player with a given kill code.
   *
   * @param code Kill code
   * @return Player with this kill code
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if there is no player with this kill code
   */
  public static Player fromKillCode(String code) throws DBAccessException, InvalidAccessException {
    String query = "select id from player where kill_code = ?";
    List<String> parameters = DBMethods.parameters(code);
    List<List<String>> playerStrings = db.executeQuery(query, parameters);
    if (playerStrings.isEmpty()) {
      throw new InvalidAccessException("There is no player with that kill code.");
    } else {
      return new Player(Integer.parseInt(playerStrings.get(0).get(0)));
    }
  }

  // OVERRIDE METHODS ==========================================================

  @Override
  public String toString() {
    return "Player{" + "id=" + id + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Player player = (Player) o;
    return id == player.getID();
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
