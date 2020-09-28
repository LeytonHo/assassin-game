package edu.brown.cs.assassin.game;

import static edu.brown.cs.assassin.main.AssassinConstants.GAME_DATA_PATH;
import static edu.brown.cs.assassin.main.AssassinConstants.GAME_TEST_DATA_PATH;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import edu.brown.cs.assassin.database.DBMethods;
import edu.brown.cs.assassin.exception.DBAccessException;
import edu.brown.cs.assassin.database.DBProxy;
import edu.brown.cs.assassin.email.EmailSender;
import edu.brown.cs.assassin.exception.InvalidAccessException;
import edu.brown.cs.assassin.exception.InvalidActionException;

/**
 * Represents a team. Can be composed of multiple individual players/users
 */
public class Team implements Identifiable {
  private static DBProxy db;
  private int id;

  // DATABASE CONNECTIONS ======================================================

  /**
   * Connects to the database used for storing actual team data.
   */
  public static void connectToMainDB() {
    try {
      db = new DBProxy(GAME_DATA_PATH);
    } catch (DBAccessException e) {
      System.err.println("Error connecting to database: " + e.getMessage());
    }
  }

  /**
   * Connects to the database used for storing test team data.
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
   * Team Constructor.
   *
   * @param id team ID
   */
  public Team(int id) {
    this.id = id;
  }

  // GETTER METHODS ============================================================

  /**
   * @return The ID of this team
   */
  @Override
  public int getID() {
    return id;
  }

  /**
   * @return Codename of this team
   * @throws DBAccessException if something goes wrong with the database
   */
  public String getCodename() throws DBAccessException {
    return db.executeQuery("select codename from team where id = ?", DBMethods.parameters(id))
        .get(0).get(0);
  }

  /**
   * @return Game this team is playing
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if the database contains an invalid game ID
   */
  public Game getGame() throws DBAccessException, InvalidAccessException {
    String gameIDString = db
        .executeQuery("select game from team where id = ?", DBMethods.parameters(id)).get(0).get(0);
    int gameID = Integer.parseInt(gameIDString);
    return Game.fromID(gameID);
  }

  /**
   * @return Whether this team is alive (i.e. whether at least one player is
   *         alive)
   * @throws DBAccessException if something goes wrong with the database
   */
  public boolean isAlive() throws DBAccessException {
    String aliveString = db
        .executeQuery("select alive from team where id = ?", DBMethods.parameters(id)).get(0)
        .get(0);
    return (aliveString.equals("1"));
  }

  /**
   * @return Join code of this team
   * @throws DBAccessException if something goes wrong with the database
   */
  public String getJoinCode() throws DBAccessException {
    return db.executeQuery("select join_code from team where id = ?", DBMethods.parameters(id))
        .get(0).get(0);
  }

  /**
   * @return Players on this team
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if the database contains an invalid player ID
   */
  public List<Player> getPlayers() throws DBAccessException, InvalidAccessException {
    List<List<String>> playerStrings = db.executeQuery("select id from player where team = ?",
        DBMethods.parameters(id));
    List<Player> players = new ArrayList<>();
    for (List<String> playerString : playerStrings) {
      players.add(Player.fromID(Integer.parseInt(playerString.get(0))));
    }
    return players;
  }

  /**
   * @return Players on this team who are still alive
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if the database contains an invalid player ID
   */
  public List<Player> getAlivePlayers() throws DBAccessException, InvalidAccessException {
    List<List<String>> playerStrings = db.executeQuery(
        "select id from player where team = ? and alive = 1", DBMethods.parameters(id));
    List<Player> players = new ArrayList<>();
    for (List<String> playerString : playerStrings) {
      players.add(Player.fromID(Integer.parseInt(playerString.get(0))));
    }
    return players;
  }

  /**
   * @return Teams this team is targeting
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if the database contains an invalid team ID
   */
  public List<Team> getTargets() throws DBAccessException, InvalidAccessException {
    List<List<String>> teamStrings = db.executeQuery("select target from target where killer = ?",
        DBMethods.parameters(id));
    List<Team> teams = new ArrayList<>();
    for (List<String> teamString : teamStrings) {
      teams.add(Team.fromID(Integer.parseInt(teamString.get(0))));
    }
    return teams;
  }

  /**
   * Target list to be used by the GUI. A team might have repeats of the same
   * target and might target itself, but that should not appear to the users.
   *
   * @return Teams this team is targeting, with no repeats and excluding
   *         self-targets
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if the database contains an invalid team ID
   */
  public List<Team> getDisplayedTargets() throws DBAccessException, InvalidAccessException {
    List<Team> targets = getTargets();
    Set<Team> targetSet = new HashSet<>(targets);
    targetSet.remove(this);
    return new ArrayList<>(targetSet);
  }

  /**
   * @return Teams targeting this team
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if the database contains an invalid team ID
   */
  public List<Team> getTargetingTeams() throws DBAccessException, InvalidAccessException {
    List<List<String>> teamStrings = db.executeQuery("select killer from target where target = ?",
        DBMethods.parameters(id));
    List<Team> teams = new ArrayList<>();
    for (List<String> teamString : teamStrings) {
      teams.add(Team.fromID(Integer.parseInt(teamString.get(0))));
    }
    return teams;
  }

  // TEAM ACTIONS ==============================================================

  /**
   * Eliminates a team from the game and reassigns its targets.
   *
   * @param killedPlayer Last player killed whose death eliminated this team
   * @param killer       Team that killed this team
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if the database contains an invalid ID
   * @throws InvalidActionException if the elimination could not happen
   */
  public void eliminate(Player killedPlayer, Team killer)
      throws DBAccessException, InvalidAccessException, InvalidActionException {
    if (getAlivePlayers().size() > 0) {
      throw new InvalidActionException("This team still has living players.");
    }

    // Record elimination in database
    db.updateDatabase("update team set alive = 0 where id = ?", DBMethods.parameters(id));

    // Email the team members to tell them they were eliminated
    String emailSubject = String.format("Your team %s was eliminated!", getCodename());
    String emailText = String.format("The last remaining player, %s, was killed by team %s.",
        killedPlayer.getUser().getName(), killer.getCodename());
    emailEveryone(emailSubject, emailText);

    // Remember the teams that this team was targeting and was targeted by
    List<Team> targets = getTargets();
    List<Team> targeting = getTargetingTeams();
    // The number of target teams and number of targeting teams could be different
    int numTargeting = targeting.size();

    // Remove that information from the database
    db.updateDatabase("delete from target where killer = ?", DBMethods.parameters(id));
    db.updateDatabase("delete from target where target = ?", DBMethods.parameters(id));

    /*
    Since this team has been eliminated, we do not want it to pass on itself as a target to any
    other team.
     */
    targets.removeAll(Collections.singletonList(this));
    int newNumTargets = targets.size();
    /*
     * Redistribute this team's targets, one to each team that targeted it. Since
     * one of the targeting teams might already have one of these as a target,
     * choose an assignment that results in a higher number of distinct targets on
     * each team.
     */
    List<List<Team>> possibleTargetAssignments = permutations(targets);
    List<Team> optimalAssignment = targets;

    /*
     * Each possible target assignment could end up adding a target to a team who
     * already has that target, or giving a team itself as a target. We want each
     * team to have the as many distinct targets as possible, not including itself.
     * So, we want to make the lowest number of distinct targets as high as possible
     * across all teams.
     */
    int greatestMinUniqueTargets = 0;
    for (List<Team> assignment : possibleTargetAssignments) {
      /*
       * For each assignment, see what results from putting the assigned team into
       * each targeting team's target list. Find the one with the lowest number of
       * unique targets other than itself.
       */
      int minUniqueTargets = Integer.MAX_VALUE;
      for (int i = 0; i < numTargeting; i++) {
        List<Team> potentialNewTargetsForTeam = targeting.get(i).getTargets();
        potentialNewTargetsForTeam.add(assignment.get(i % newNumTargets));
        Set<Team> uniqueTargets = new HashSet<>(potentialNewTargetsForTeam);
        uniqueTargets.remove(this);
        int numUniqueTargets = uniqueTargets.size();
        if (numUniqueTargets < minUniqueTargets) {
          minUniqueTargets = numUniqueTargets;
        }
      }
      if (minUniqueTargets > greatestMinUniqueTargets) {
        greatestMinUniqueTargets = minUniqueTargets;
        optimalAssignment = assignment;
      }
    }

    // Targeting teams inherit target teams
    for (int i = 0; i < numTargeting; i++) {
      targeting.get(i).addTarget(optimalAssignment.get(i % newNumTargets));
    }
  }

  private static <T> List<List<T>> permutations(List<T> items) {
    if (items.isEmpty()) {
      return Collections.singletonList(items);
    }
    List<List<T>> permutations = new ArrayList<>();
    for (int i = 0; i < items.size(); i++) {
      T item = items.get(i);
      List<T> sublist = new ArrayList<>(items);
      sublist.remove(i);
      List<List<T>> permutationsOfSublist = permutations(sublist);
      for (List<T> permutationOfSublist : permutationsOfSublist) {
        permutationOfSublist.add(item);
        permutations.add(permutationOfSublist);
      }
    }
    return permutations;
  }

  /**
   * Sends an email containing subject and text to all the players in the team.
   *
   * @param subject Subject of the email.
   * @param text    Body of the email, which can be formatted like html.
   * @return Whether the email sent properly.
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if the database contains an invalid user ID
   */
  public boolean emailEveryone(String subject, String text)
      throws InvalidAccessException, DBAccessException {
    List<String> recipients = new ArrayList<>();
    for (Player player : getPlayers()) {
      recipients.add(player.getUser().getEmail());
    }
    String subjectWithGame = String.format("[Game %s] %s", getGame().getName(), subject);
    return EmailSender.send(recipients, subjectWithGame, text);
  }

  // EDITING TARGETS ===========================================================

  /**
   * Adds a new target to this team's target list.
   *
   * @param target Target team
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if the database contains an invalid ID
   * @throws InvalidActionException if the new target could not be added
   */
  public void addTarget(Team target)
      throws DBAccessException, InvalidAccessException, InvalidActionException {
    if (getTargets().size() >= getGame().getNumTargets()) {
      throw new InvalidActionException("This team is at the maximum number of targets.");
    }
    db.updateDatabase("insert into target (killer, target) values (?, ?)",
        DBMethods.parameters(id, target));
  }
  // CREATING/FINDING TEAMS ====================================================

  /**
   * Adds a new team to the database.
   *
   * @param codename Codename of the team
   * @param game     Game the team is playing
   * @return Team that was created
   * @throws DBAccessException      if the database contains an invalid ID
   * @throws InvalidActionException if something goes wrong with the database
   */
  public static Team addTeam(String codename, Game game)
      throws InvalidActionException, DBAccessException {
    if (game.getStatus() != GameStatus.FORMING) {
      throw new InvalidActionException("You cannot add a team once the game has started.");
    }
    String command = "insert into team (codename, game, join_code) values (?, ?, ?)";
    String code = DBMethods.generateUniqueCode(db, "team", "join_code");
    List<String> parameters = DBMethods.parameters(codename, game, code);
    db.updateDatabase(command, parameters);

    List<List<String>> addedTeamQueryResult = db
        .executeQuery("select last_insert_rowid() from team");
    int addedTeamID = Integer.parseInt(addedTeamQueryResult.get(0).get(0));
    return new Team(addedTeamID);
  }

  /**
   * Gets the Team with a given ID.
   *
   * @param id The ID
   * @return Team with this ID
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if there is no team with this ID
   */
  public static Team fromID(int id) throws DBAccessException, InvalidAccessException {
    if (db.hasKey("select * from team where id = ?", DBMethods.parameters(id))) {
      return new Team(id);
    } else {
      throw new InvalidAccessException("There is no team with id " + id);
    }
  }

  /**
   * Gets the Team with a given join code.
   *
   * @param code Join code
   * @return Team with this join code
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if there is no team with this join code
   */
  public static Team fromJoinCode(String code) throws DBAccessException, InvalidAccessException {
    List<List<String>> teamQueryResult = db.executeQuery("select id from team where join_code = ?",
        DBMethods.parameters(code));
    if (teamQueryResult.isEmpty()) {
      throw new InvalidAccessException("There is no team with that join code.");
    } else {
      int teamID = Integer.parseInt(teamQueryResult.get(0).get(0));
      return new Team(teamID);
    }
  }

  // OVERRIDE METHODS ==========================================================

  @Override
  public String toString() {
    return "Team{" + "id=" + id + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Team team = (Team) o;
    return id == team.id;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
