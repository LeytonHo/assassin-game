package edu.brown.cs.assassin.game;

import static edu.brown.cs.assassin.DBTestMethods.addUsers;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.brown.cs.assassin.DBTestMethods;
import edu.brown.cs.assassin.database.DBProxy;
import edu.brown.cs.assassin.exception.DBAccessException;
import edu.brown.cs.assassin.exception.InvalidAccessException;
import edu.brown.cs.assassin.exception.InvalidActionException;
import edu.brown.cs.assassin.main.AssassinConstants;

/**
 * Tests that all the getter methods of game classes work correctly when used in
 * the most basic circumstances (only adding games, players, teams, messages; no
 * actions).
 */
public class GetterTests {
  private DBProxy gameDB;

  private User u1;
  private User u2;
  private User u3;
  private User u4;
  private User u5;
  private User u6;
  private User u7;
  private User u8;
  private Game g1;
  private Game g2;
  private Game g3;
  private Team t1;
  private Team t2;
  private Team t3;
  private Team t4;
  private Player p1;
  private Player p2;
  private Player p3;
  private Player p4;
  private Player p5;
  private Player p6;
  private Player p7;

  /**
   * Sets up the database and game classes.
   */
  @Before
  public void setUp() throws DBAccessException {
    DBTestMethods.setUp();
    gameDB = new DBProxy(AssassinConstants.GAME_TEST_DATA_PATH);
  }

  /**
   * Reconnects the game classes back to the main databases.
   */
  @After
  public void tearDown() {
    DBTestMethods.tearDown();
  }

  /**
   * Creates multiple games, users, players, teams, for testing different results
   * of getters.
   */
  public void addGames() throws DBAccessException, InvalidAccessException, InvalidActionException {
    setUp();

    addUsers(8);
    u1 = User.fromID(1);
    u2 = User.fromID(2);
    u3 = User.fromID(3);
    u4 = User.fromID(4);
    u5 = User.fromID(5);
    u6 = User.fromID(6);
    u7 = User.fromID(7);
    u8 = User.fromID(8);

    g1 = Game.addGame("", "", 1, Collections.singletonList(u4), 1, false);
    g2 = Game.addGame("", "", 1, Arrays.asList(u4, u5), 1, false);
    g3 = Game.addGame("", "", 3, Collections.singletonList(u2), 2, false);

    t1 = Team.addTeam("", g2);
    t2 = Team.addTeam("", g3);
    t3 = Team.addTeam("", g3);
    t4 = Team.addTeam("", g3);

    p1 = Player.addPlayer(u3, t1);
    p2 = Player.addPlayer(u3, t2);
    p3 = Player.addPlayer(u4, t3);
    p4 = Player.addPlayer(u5, t3);
    p5 = Player.addPlayer(u6, t4);
    p6 = Player.addPlayer(u7, t4);
    p7 = Player.addPlayer(u8, t4);
  }

  /**
   * Tests the getters of the User class.
   */
  @Test
  public void testUserGetters()
      throws DBAccessException, InvalidActionException, InvalidAccessException {
    setUp();

    User u1 = User.addUser("person@email.com", "Person", "very secure");
    User u2 = User.addUser("different_person@email.com", "nosreP", "not very secure");
    User fromID = User.fromID(1);

    assertEquals(u1, fromID);
    assertNotEquals(u1, u2);

    assertEquals(1, u1.getID());
    assertEquals("Person", u1.getName());
    assertEquals("person@email.com", u1.getEmail());
    assertEquals(Collections.emptyList(), u1.getPlayers());
    assertEquals(Collections.emptyList(), u1.getPlayingGames());
    assertEquals(Collections.emptyList(), u1.getAdminGames());
    assertEquals(u1, User.attemptLogin("person@email.com", "very secure"));
    User.resetPassword(1, "new pass");
    assertEquals(u1, User.attemptLogin("person@email.com", "new pass"));

    addGames();

    assertEquals(Collections.emptyList(), u1.getPlayers());
    assertEquals(Collections.emptyList(), u2.getPlayers());
    assertEquals(Arrays.asList(p1, p2), u3.getPlayers());
    assertEquals(Collections.singletonList(p3), u4.getPlayers());
    assertEquals(Collections.singletonList(p4), u5.getPlayers());
    assertEquals(Collections.singletonList(p5), u6.getPlayers());
    assertEquals(Collections.singletonList(p6), u7.getPlayers());
    assertEquals(Collections.singletonList(p7), u8.getPlayers());

    assertEquals(Collections.emptyList(), u1.getPlayingGames());
    assertEquals(Collections.emptyList(), u2.getPlayingGames());
    assertTrue(u3.getPlayingGames().contains(g3));
    assertTrue(u3.getPlayingGames().contains(g2));
    assertEquals(Collections.singletonList(g3), u4.getPlayingGames());
    assertEquals(Collections.singletonList(g3), u5.getPlayingGames());
    assertEquals(Collections.singletonList(g3), u6.getPlayingGames());
    assertEquals(Collections.singletonList(g3), u7.getPlayingGames());
    assertEquals(Collections.singletonList(g3), u8.getPlayingGames());

    assertEquals(Collections.emptyList(), u1.getAdminGames());
    assertEquals(Collections.singletonList(g3), u2.getAdminGames());
    assertEquals(Collections.emptyList(), u3.getAdminGames());
    assertEquals(Arrays.asList(g1, g2), u4.getAdminGames());
    assertEquals(Collections.singletonList(g2), u5.getAdminGames());
    assertEquals(Collections.emptyList(), u6.getAdminGames());
    assertEquals(Collections.emptyList(), u7.getAdminGames());
    assertEquals(Collections.emptyList(), u8.getAdminGames());

    tearDown();
  }

  /**
   * Tests the getters of the Game class.
   */
  @Test
  public void testGameGetters()
      throws DBAccessException, InvalidActionException, InvalidAccessException {
    setUp();

    addUsers(2);
    Game g1 = Game.addGame("Test game", "Have fun", 2, Collections.singletonList(User.fromID(1)), 3,
        false);
    Game g2 = Game.addGame("Another test game", "Don’t have fun", 3,
        Collections.singletonList(User.fromID(2)), 1, true);
    String joinCode = gameDB.executeQuery("select join_code from game where id = 1").get(0).get(0);
    Game fromID = Game.fromID(1);
    Game fromJoinCode = Game.fromJoinCode(joinCode);

    assertEquals(g1, fromID);
    assertEquals(g1, fromJoinCode);
    assertNotEquals(g1, g2);

    assertEquals(1, g1.getID());
    assertEquals("Test game", g1.getName());
    assertEquals("Have fun", g1.getRules());
    assertEquals(2, g1.getMaxTeamSize());
    assertEquals(Collections.singletonList(User.fromID(1)), g1.getAdmin());
    assertEquals(3, g1.getNumTargets());
    assertFalse(g1.isAnonymous());
    assertEquals(GameStatus.FORMING, g1.getStatus());
    assertEquals(Collections.emptyList(), g1.getTeams());
    assertEquals(Collections.emptyList(), g1.getAliveTeams());
    assertEquals(Collections.singletonList(User.fromID(1)), g1.getAdmin());
    assertEquals(Collections.emptyList(), g1.getMessages(1));
    assertEquals(joinCode, g1.getJoinCode());

    addGames();

    assertEquals(Collections.emptyList(), g1.getTeams());
    assertEquals(Collections.singletonList(t1), g2.getTeams());
    assertEquals(Arrays.asList(t2, t3, t4), g3.getTeams());

    assertEquals(Collections.emptyList(), g1.getAliveTeams());
    assertEquals(Collections.singletonList(t1), g2.getAliveTeams());
    assertEquals(Arrays.asList(t2, t3, t4), g3.getAliveTeams());

    assertEquals(Collections.singletonList(u4), g1.getAdmin());
    assertEquals(Arrays.asList(u4, u5), g2.getAdmin());
    assertEquals(Collections.singletonList(u2), g3.getAdmin());

    tearDown();
  }

  /**
   * Tests the getters of the Team class.
   */
  @Test
  public void testTeamGetters()
      throws DBAccessException, InvalidActionException, InvalidAccessException {
    setUp();

    addUsers(2);
    Game g1 = Game.addGame("Test game", "Have fun", 2, Collections.singletonList(User.fromID(1)), 3,
        false);
    Game g2 = Game.addGame("Another test game", "Don’t have fun", 3,
        Collections.singletonList(User.fromID(2)), 1, true);
    Team t1 = Team.addTeam("Test team", g1);
    Team t2 = Team.addTeam("Another test team", g2);
    String joinCode = gameDB.executeQuery("select join_code from team where id = 1").get(0).get(0);
    Team fromID = Team.fromID(1);
    Team fromJoinCode = Team.fromJoinCode(joinCode);

    assertEquals(t1, fromID);
    assertEquals(t1, fromJoinCode);
    assertNotEquals(t1, t2);

    assertEquals(1, t1.getID());
    assertEquals("Test team", t1.getCodename());
    assertEquals(g1, t1.getGame());
    assertTrue(t1.isAlive());
    assertEquals(joinCode, t1.getJoinCode());
    assertEquals(Collections.emptyList(), t1.getPlayers());
    assertEquals(Collections.emptyList(), t1.getAlivePlayers());
    assertEquals(Collections.emptyList(), t1.getTargets());
    assertEquals(Collections.emptyList(), t1.getDisplayedTargets());
    assertEquals(Collections.emptyList(), t1.getTargetingTeams());

    addGames();

    assertEquals(Collections.singletonList(p1), t1.getPlayers());
    assertEquals(Collections.singletonList(p2), t2.getPlayers());
    assertEquals(Arrays.asList(p3, p4), t3.getPlayers());
    assertEquals(Arrays.asList(p5, p6, p7), t4.getPlayers());

    assertEquals(Collections.singletonList(p1), t1.getAlivePlayers());
    assertEquals(Collections.singletonList(p2), t2.getAlivePlayers());
    assertEquals(Arrays.asList(p3, p4), t3.getAlivePlayers());
    assertEquals(Arrays.asList(p5, p6, p7), t4.getAlivePlayers());

    tearDown();
  }

  /**
   * Tests the getters of the Player class.
   */
  @Test
  public void testPlayerGetters()
      throws DBAccessException, InvalidActionException, InvalidAccessException {
    setUp();

    User u1 = User.addUser("person@email.com", "Person", "very secure");
    User u2 = User.addUser("different_person@email.com", "nosreP", "not very secure");
    Game g1 = Game.addGame("Test game", "Have fun", 2, Collections.singletonList(User.fromID(2)), 3,
        false);
    Game g2 = Game.addGame("Another test game", "Don’t have fun", 3,
        Collections.singletonList(User.fromID(1)), 1, true);
    Team t1 = Team.addTeam("Test team", g1);
    Team t2 = Team.addTeam("Another test team", g2);
    Player p1 = Player.addPlayer(u1, t1);
    Player p2 = Player.addPlayer(u2, t2);
    String killCode = gameDB.executeQuery("select kill_code from player where id = 1").get(0)
        .get(0);
    Player fromID = Player.fromID(1);
    Player fromUserAndGame = Player.fromUserAndGame(u1, g1);
    Player fromKillCode = Player.fromKillCode(killCode);

    assertEquals(p1, fromID);
    assertEquals(p1, fromUserAndGame);
    assertEquals(p1, fromKillCode);
    assertNotEquals(p1, p2);

    assertEquals(1, p1.getID());
    assertEquals(u1, p1.getUser());
    assertEquals(t1, p1.getTeam());
    assertTrue(p1.isAlive());
    assertEquals(g1, p1.getGame());
    assertEquals(killCode, p1.getKillCode());

    tearDown();
  }

  /**
   * Tests the getters of the Message class.
   */
  @Test
  public void testMessageGetters()
      throws DBAccessException, InvalidActionException, InvalidAccessException {
    setUp();

    addUsers(1);
    Game g1 = Game.addGame("Test game", "Have fun", 2, Collections.singletonList(User.fromID(1)), 3,
        false);
    Message m1 = Message.addMessage(g1, MessageType.CUSTOM, "A message");
    Message m2 = Message.addMessage(g1, MessageType.START);
    Message fromID = Message.fromID(1);

    assertEquals(m1, fromID);
    assertNotEquals(m1, m2);

    assertEquals(1, m1.getID());
    assertEquals(MessageType.CUSTOM, m1.getType());
    assertEquals("A message", m1.getField(0));
    assertEquals(g1, m1.getGame());

    tearDown();
  }
}
