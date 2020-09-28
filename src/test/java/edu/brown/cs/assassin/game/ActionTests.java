package edu.brown.cs.assassin.game;

import edu.brown.cs.assassin.DBTestMethods;
import edu.brown.cs.assassin.exception.DBAccessException;
import edu.brown.cs.assassin.exception.InvalidAccessException;
import edu.brown.cs.assassin.exception.InvalidActionException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThrows;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ActionTests {
  private User u1;
  private User u9;
  private User admin;
  private Game g;
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
  private Player p8;

  /**
   * Sets up the database and game classes, and creates users, teams, players, and a game to test
   * with.
   */
  @Before
  public void setUp() throws DBAccessException, InvalidActionException, InvalidAccessException {
    DBTestMethods.setUp();

    DBTestMethods.addUsers(10);
    u1 = User.fromID(1);
    User u2 = User.fromID(2);
    User u3 = User.fromID(3);
    User u4 = User.fromID(4);
    User u5 = User.fromID(5);
    User u6 = User.fromID(6);
    User u7 = User.fromID(7);
    User u8 = User.fromID(8);
    u9 = User.fromID(9);
    admin = User.fromID(10);

    g = Game.addGame("Test game", "Have fun", 3, Collections.singletonList(admin), 2, false);

    t1 = Team.addTeam("Calder's team", g);
    t2 = Team.addTeam("Ell's team", g);
    t3 = Team.addTeam("Hari's team", g);
    t4 = Team.addTeam("Leyton's team", g);

    p1 = Player.addPlayer(u1, t1);
    p2 = Player.addPlayer(u2, t2);
    p3 = Player.addPlayer(u3, t2);
    p4 = Player.addPlayer(u4, t3);
    p5 = Player.addPlayer(u5, t3);
    p6 = Player.addPlayer(u6, t4);
    p7 = Player.addPlayer(u7, t4);
    p8 = Player.addPlayer(u8, t4);
  }

  /**
   * Reconnects the game classes back to the main databases.
   */
  @After
  public void tearDown() {
    DBTestMethods.tearDown();
  }

  /**
   * Tests that User.checkJoinGame and User.checkJoinTeam returns the right results for attempting
   * to join games and teams (both successes and failures).
   */
  @Test
  public void testUserCheckJoin() throws DBAccessException, InvalidActionException, InvalidAccessException {
    setUp();

    User.JoinResult jr1 = u1.checkJoinGame(g);
    User.JoinResult jr2 = admin.checkJoinGame(g);
    User.JoinResult jr3 = u9.checkJoinGame(g);
    User.JoinResult jr4 = u9.checkJoinTeam(t3);
    User.JoinResult jr5 = u9.checkJoinTeam(t4);
    g.start();
    User.JoinResult jr6 = u1.checkJoinGame(g);
    g.endWithNoWinner();
    User.JoinResult jr7 = u1.checkJoinGame(g);

    assertFalse(jr1.joinGame());
    assertFalse(jr2.joinGame());
    assertTrue(jr3.joinGame());
    assertTrue(jr4.joinGame());
    assertTrue(jr4.joinTeam());
    assertFalse(jr5.joinGame());
    assertFalse(jr5.joinTeam());
    assertFalse(jr6.joinGame());
    assertFalse(jr7.joinGame());

    assertEquals("You are already playing this game.", jr1.getError());
    assertEquals("You are already an admin of this game.", jr2.getError());
    assertEquals("This team is already at the maximum size.", jr5.getError());
    assertEquals("This game has already started.", jr6.getError());
    assertEquals("This game has finished.", jr7.getError());

    tearDown();
  }

  /**
   * Tests that User.formNewTeam either correctly creates a team with the right players or
   * appropriately fails to create a team.
   */
  @Test
  public void testUserFormNewTeam() throws DBAccessException, InvalidActionException,
          InvalidAccessException {
    setUp();

    User.JoinResult jr1 = u1.formNewTeam(g, "team 0");
    User.JoinResult jr2 = admin.formNewTeam(g, "team 0");
    User.JoinResult jr3 = u9.formNewTeam(g, "team 5");
    Team newTeam = jr3.getTeam();

    assertFalse(jr1.joinGame());
    assertFalse(jr2.joinGame());
    assertTrue(jr3.joinGame());
    assertTrue(g.getTeams().contains(newTeam));
    assertTrue(newTeam.getPlayers().contains(Player.fromUserAndGame(u9, g)));

    tearDown();
  }

  /**
   * Tests that User.joinExistingTeam either correctly creates adds the user to the team or
   * appropriately fails to add the player.
   */
  @Test
  public void testUserJoinExistingTeam() throws DBAccessException, InvalidAccessException,
          InvalidActionException {
    setUp();

    User.JoinResult jr1 = u1.joinExistingTeam(t2);
    User.JoinResult jr2 = admin.joinExistingTeam(t2);
    User.JoinResult jr3 = u9.joinExistingTeam(t4);
    User.JoinResult jr4 = u9.joinExistingTeam(t3);

    assertFalse(jr1.joinGame());
    assertFalse(jr2.joinGame());
    assertFalse(jr3.joinGame());
    assertTrue(jr4.joinGame());
    assertTrue(t3.getPlayers().contains(Player.fromUserAndGame(u9, g)));

    tearDown();
  }

  /**
   * Tests that User.joinExistingTeam either correctly creates adds the user to the team with
   * this join code or appropriately fails to add the player (if there is no player with this
   * join code or the team could not be joined).
   */
  @Test
  public void testUserJoinTeamFromCode() throws DBAccessException, InvalidAccessException,
          InvalidActionException {
    setUp();

    User.JoinResult jr1 = u1.joinTeamFromCode(t2.getJoinCode());
    User.JoinResult jr2 = u9.joinTeamFromCode(t3.getJoinCode());

    assertFalse(jr1.joinGame());
    assertTrue(jr2.joinTeam());
    assertEquals(t3, jr2.getTeam());
    assertThrows(InvalidAccessException.class,
            () -> u9.joinTeamFromCode("not a join code"));

    tearDown();
  }


  /**
   * Tests that Player.remove makes them inaccessible from player finding methods, removes them
   * from their team, and removes the team if they were the last player on their team.
   */
  @Test
  public void testPlayerRemove() throws DBAccessException, InvalidActionException,
          InvalidAccessException {
    setUp();

    p1.remove();

    assertThrows(InvalidAccessException.class,
            () -> Player.fromID(1));
    assertThrows(InvalidAccessException.class,
            () -> Team.fromID(1));

    assertTrue(t2.getPlayers().contains(p2));
    p2.remove();
    assertFalse(t2.getPlayers().contains(p2));

    tearDown();
  }

  /**
   * Tests that Player.kill marks the target player as killed, and that if this kill eliminated the
   * target team or won the game, that elimination/win is recorded.
   */
  @Test
  public void testPlayerKill() throws DBAccessException, InvalidActionException,
          InvalidAccessException {
    setUp();

    g.start();
    Team targetTeam1;
    Player targetTeam1Player1;
    Player targetTeam1Player2;
    Player targetTeam2Player1;
    Player targetTeam2Player2;
    if (t1.getTargets().contains(t2)) {
      targetTeam1 = t2;
      targetTeam1Player1 = p2;
      targetTeam1Player2 = p3;
      targetTeam2Player1 = p4;
      targetTeam2Player2 = p5;
    } else {
      targetTeam1 = t3;
      targetTeam1Player1 = p4;
      targetTeam1Player2 = p5;
      targetTeam2Player1 = p2;
      targetTeam2Player2 = p3;
    }
    Player.KillResult kr1 = p1.kill(targetTeam1Player1);

    assertFalse(targetTeam1Player1.isAlive());
    assertFalse(targetTeam1.getAlivePlayers().contains(targetTeam1Player1));
    assertTrue(kr1.didKill());
    assertFalse(kr1.didEliminate());
    assertFalse(kr1.didWin());

    Player.KillResult kr2 = p1.kill(targetTeam1Player2);

    assertFalse(targetTeam1Player2.isAlive());
    assertFalse(targetTeam1.getAlivePlayers().contains(targetTeam1Player2));
    assertTrue(kr2.didKill());
    assertTrue(kr2.didEliminate());
    assertFalse(kr2.didWin());
    Message m1 = g.getMessages(1).get(0);
    assertEquals(MessageType.ELIMINATE, m1.getType());
    assertEquals("Calder's team", m1.getField(0));
    assertEquals(targetTeam1.getCodename(), m1.getField(1));

    p1.kill(targetTeam2Player1);
    p1.kill(targetTeam2Player2);
    p1.kill(p6);
    p1.kill(p7);
    Player.KillResult kr3 = p1.kill(p8);

    assertTrue(kr3.didKill());
    assertTrue(kr3.didEliminate());
    assertTrue(kr3.didWin());
    Message m2 = g.getMessages(1).get(0);
    assertEquals(MessageType.WIN, m2.getType());
    assertEquals("Calder's team", m2.getField(0));

    tearDown();
  }

  /**
   * Tests that Player.killByCode kills the target player in valid circumstances, and otherwise
   * gives the right reason why it could not kill the target player.
   */
  @Test
  public void testPlayerKillByCode() throws DBAccessException, InvalidActionException,
          InvalidAccessException {
    setUp();

    g.start();
    List<Team> targets = t1.getTargets();
    Player targetPlayer1 = targets.get(0).getPlayers().get(0);
    Player targetPlayer2 = targets.get(1).getPlayers().get(0);
    Team notTargetTeam;
    if (!targets.contains(t2)) {
      notTargetTeam = t2;
    } else if (!targets.contains(t3)) {
      notTargetTeam = t3;
    } else {
      notTargetTeam = t4;
    }
    Player notTargetPlayer = notTargetTeam.getPlayers().get(0);
    Player targetingPlayer = t1.getTargetingTeams().get(0).getPlayers().get(1);
    p1.kill(targetPlayer1);
    p1.kill(targetingPlayer);
    g.changeNumTargets(4);
    t1.addTarget(t1);

    Player.KillResult kr1 = p1.killByCode(targetPlayer1.getKillCode());
    assertEquals("That player is already dead.", kr1.getError());
    Player.KillResult kr2 = targetingPlayer.killByCode(p1.getKillCode());
    assertEquals("You cannot kill a player if you are dead.", kr2.getError());
    Player.KillResult kr3 = p1.killByCode(p1.getKillCode());
    assertEquals("You cannot kill yourself.", kr3.getError());
    Player.KillResult kr4 = p1.killByCode(notTargetPlayer.getKillCode());
    assertEquals("You are not targeting any player with that kill code.", kr4.getError());
    Player.KillResult kr5 = p1.killByCode("not a kill code");
    assertEquals("You are not targeting any player with that kill code.", kr5.getError());
    Player.KillResult kr6 = p1.killByCode(targetPlayer2.getKillCode());
    assertTrue(kr6.didKill());

    tearDown();
  }

  /**
   * Tests that Player.surrender correctly kills the player, that a team is eliminated when the
   * last player surrenders, that surrendering can lead to a win, and that the right messages are
   * added.
   */
  @Test
  public void testPlayerSurrender () throws DBAccessException, InvalidAccessException,
          InvalidActionException {
    setUp();

    g.start();
    Player.KillResult sr1 = p2.surrender();

    assertFalse(p2.isAlive());
    assertTrue(t2.isAlive());
    assertEquals(1, t2.getAlivePlayers().size());
    assertTrue(sr1.didKill());
    assertFalse(sr1.didEliminate());

    Player.KillResult sr2 = p3.surrender();
    Message m1 = g.getMessages(1).get(0);

    assertFalse(p3.isAlive());
    assertFalse(t2.isAlive());
    assertEquals(0, t2.getAlivePlayers().size());
    assertTrue(sr2.didKill());
    assertTrue(sr2.didEliminate());
    assertEquals(MessageType.SURRENDER, m1.getType());
    assertEquals(t2.getCodename(), m1.getField(0));

    p1.surrender();
    Player.KillResult sr3 = p1.surrender();

    assertFalse(sr3.didKill());
    assertEquals("You cannot surrender if you are dead.", sr3.getError());

    p4.surrender();
    Player.KillResult sr4 = p5.surrender();
    Message m2 = g.getMessages(1).get(0);

    assertTrue(sr4.didEliminate());
    assertTrue(sr4.didWin());
    assertEquals(MessageType.WIN, m2.getType());
    assertEquals(t4.getCodename(), m2.getField(0));

    tearDown();
  }

  /**
   * Tests that Team.eliminate correctly marks the team as killed and redistributes the team's
   * targets.
   */
  @Test
  public void testTeamEliminate() throws DBAccessException, InvalidActionException,
          InvalidAccessException {
    setUp();

    g.start();
    Team targetTeam1;
    Player targetTeam1Player1;
    Player targetTeam1Player2;
    Team targetTeam2;
    Player targetTeam2Player1;
    Player targetTeam2Player2;
    if (t1.getTargets().contains(t2)) {
      targetTeam1 = t2;
      targetTeam1Player1 = p2;
      targetTeam1Player2 = p3;
      targetTeam2 = t3;
      targetTeam2Player1 = p4;
      targetTeam2Player2 = p5;
    } else {
      targetTeam1 = t3;
      targetTeam1Player1 = p4;
      targetTeam1Player2 = p5;
      targetTeam2 = t2;
      targetTeam2Player1 = p2;
      targetTeam2Player2 = p3;
    }
    List<Team> targetingTeams = targetTeam1.getTargetingTeams();
    Team otherTargetingTeam;
    if (targetingTeams.get(0) == t1) {
      otherTargetingTeam = targetingTeams.get(1);
    } else {
      otherTargetingTeam = targetingTeams.get(0);
    }

    assertThrows(InvalidActionException.class,
            () -> targetTeam1.eliminate(targetTeam1Player1, t1));

    p1.kill(targetTeam1Player1);
    p1.kill(targetTeam1Player2);

    assertFalse(targetTeam1.isAlive());
    assertFalse(g.getAliveTeams().contains(targetTeam1));
    assertTrue(targetTeam1.getTargets().isEmpty());
    assertFalse(t1.getTargets().contains(targetTeam1));
    assertEquals(2, t1.getTargets().size());
    assertFalse(otherTargetingTeam.getTargets().contains(targetTeam1));
    assertEquals(2, otherTargetingTeam.getTargets().size());

    p1.kill(targetTeam2Player1);
    p1.kill(targetTeam2Player2);

    assertFalse(targetTeam2.isAlive());
    assertFalse(g.getAliveTeams().contains(targetTeam2));
    assertTrue(targetTeam2.getTargets().isEmpty());
    assertFalse(t1.getTargets().contains(targetTeam2));
    assertEquals(2, t1.getTargets().size());

    tearDown();
  }

  /**
   * Tests that team.getDisplayedTargets does not include repeats or self-targets.
   */
  @Test
  public void testTeamGetDisplayedTargets() throws DBAccessException, InvalidActionException,
          InvalidAccessException {
    setUp();

    g.changeNumTargets(3);
    g.start();
    p1.kill(p2);
    p1.kill(p3);

    assertEquals(2, t1.getDisplayedTargets().size());
    assertFalse(t1.getDisplayedTargets().contains(t1));

    p1.kill(p4);
    p1.kill(p5);

    assertEquals(1, t1.getDisplayedTargets().size());
    assertFalse(t1.getDisplayedTargets().contains(t1));

    tearDown();
  }

  /**
   * Tests that Team.addTarget assigns a team's target correctly, or errors if the team has the
   * maximum number of targets.
   */
  @Test
  public void testTeamAddTarget() throws DBAccessException, InvalidActionException,
          InvalidAccessException {
    setUp();

    t1.addTarget(t2);
    assertEquals(Collections.singletonList(t2), t1.getTargets());
    assertEquals(Collections.singletonList(t1), t2.getTargetingTeams());

    t1.addTarget(t3);
    assertEquals(Arrays.asList(t2, t3), t1.getTargets());
    assertEquals(Collections.singletonList(t1), t3.getTargetingTeams());

    assertThrows(InvalidActionException.class,
            () -> t1.addTarget(t4));

    tearDown();
  }

  /**
   * Tests that Game.start marks the game as started and assigns targets, and that a game with 0
   * or 1 teams cannot be started.
   */
  @Test
  public void testGameStart() throws DBAccessException, InvalidActionException,
          InvalidAccessException {
    setUp();

    g.start();

    assertEquals(GameStatus.PLAYING, g.getStatus());
    assertEquals(MessageType.START, g.getMessages(1).get(0).getType());
    assertEquals(2, t1.getTargets().size());
    assertThrows(InvalidActionException.class,
            () -> g.start());

    Game tooSmallGame = Game.addGame("", "", 1, Collections.singletonList(admin), 1,
            false);

    assertThrows(InvalidActionException.class,
            () -> tooSmallGame.start());

    Team teamForTooSmallGame = Team.addTeam("", tooSmallGame);
    Player.addPlayer(u1, teamForTooSmallGame);

    assertEquals(1, tooSmallGame.getTeams().size());
    assertThrows(InvalidActionException.class,
            () -> tooSmallGame.start());

    tearDown();
  }

  /**
   * Tests that Game.end is called on the last kill, and that it correctly marks the game as
   * ended and adds a win message.
   */
  @Test
  public void testGameEnd() throws DBAccessException, InvalidActionException,
          InvalidAccessException {
    setUp();

    g.changeNumTargets(3);

    assertThrows(InvalidActionException.class,
            () -> g.end(t1));

    g.start();
    p1.kill(p2);
    p1.kill(p3);
    p1.kill(p4);
    p1.kill(p5);
    p1.kill(p6);
    p1.kill(p7);
    p1.kill(p8);

    assertEquals(GameStatus.DONE, g.getStatus());
    assertEquals(MessageType.WIN, g.getMessages(1).get(0).getType());

    tearDown();
  }

  /**
   * Tests that Game.endWithNoWinner correctly marks the game as ended and adds a game end message.
   */
  @Test
  public void testGameEndWithNoWinner() throws DBAccessException, InvalidActionException,
          InvalidAccessException {
    setUp();

    assertThrows(InvalidActionException.class,
            () -> g.endWithNoWinner());

    g.start();
    g.endWithNoWinner();

    assertEquals(GameStatus.DONE, g.getStatus());
    assertEquals(MessageType.END, g.getMessages(1).get(0).getType());

    tearDown();
  }

  /**
   * Tests that Game.changeName correctly changes the game's name and records this change in a
   * message.
   */
  @Test
  public void testGameChangeName() throws DBAccessException, InvalidActionException,
          InvalidAccessException {
    setUp();

    g.changeName("New name");
    Message m = g.getMessages(1).get(0);

    assertEquals("New name", g.getName());
    assertEquals(MessageType.CHANGE_NAME, m.getType());
    assertEquals("New name", m.getField(0));

    tearDown();
  }

  /**
   * Tests that Game.changeRules correctly changes the game's rules and records this change in a
   * message.
   */
  @Test
  public void testGameChangeRules() throws DBAccessException, InvalidActionException,
          InvalidAccessException {
    setUp();

    g.changeRules("New rules");
    Message m = g.getMessages(1).get(0);

    assertEquals("New rules", g.getRules());
    assertEquals(MessageType.CHANGE_RULES, m.getType());
    assertEquals("New rules", m.getField(0));

    g.changeRules("");

    assertEquals("", g.getRules());

    g.changeRules("Newer rules");

    assertEquals("Newer rules", g.getRules());

    tearDown();
  }

  /**
   * Tests that Game.changeAnonymity correctly changes the game's anonymity settings and records
   * this change in the right type of message.
   */
  @Test
  public void testGameChangeAnonymity() throws DBAccessException, InvalidActionException,
          InvalidAccessException {
    setUp();

    g.changeAnonymity(true);
    Message m1 = g.getMessages(1).get(0);

    assertTrue(g.isAnonymous());
    assertEquals(MessageType.CHANGE_ANON, m1.getType());

    g.changeAnonymity(false);
    Message m2 = g.getMessages(1).get(0);

    assertFalse(g.isAnonymous());
    assertEquals(MessageType.CHANGE_NOT_ANON, m2.getType());

    tearDown();
  }

  /**
   * Tests that Game.changeNumTargets correctly changes the game's number of targets and records
   * this change in a message, that the number of targets cannot be changed to zero, and that the
   * game correctly assigns the new number of targets when generating targets.
   */
  @Test
  public void testGameChangeNumTargets() throws DBAccessException, InvalidActionException,
          InvalidAccessException {
    setUp();

    g.changeNumTargets(1);
    Message m = g.getMessages(1).get(0);

    assertEquals(1, g.getNumTargets());
    assertEquals(MessageType.CHANGE_NUM_TARGETS, m.getType());
    assertEquals("1", m.getField(0));
    assertThrows(InvalidActionException.class,
            () -> g.changeNumTargets(0));

    g.start();

    assertEquals(1, t1.getTargets().size());
    assertEquals(1, t2.getTargets().size());
    assertEquals(1, t3.getTargets().size());
    assertEquals(1, t4.getTargets().size());

    tearDown();
  }

  /**
   * Tests that Game.clearTargets correctly clears all team's targets.
   */
  @Test
  public void testGameClearTargets() throws DBAccessException, InvalidActionException,
          InvalidAccessException {
    setUp();

    g.start();
    g.clearTargets();

    assertEquals(0, t1.getTargets().size());
    assertEquals(0, t2.getTargets().size());
    assertEquals(0, t3.getTargets().size());
    assertEquals(0, t4.getTargets().size());

    tearDown();
  }

  /**
   * Tests that Game.generateNewTargets gives teams the right number of targets and records this
   * in a message.
   */
  @Test
  public void testGameGenerateNewTargets() throws DBAccessException, InvalidActionException,
          InvalidAccessException {
    setUp();

    g.generateNewTargets();
    Message m = g.getMessages(1).get(0);

    assertEquals(2, t1.getTargets().size());
    assertEquals(2, t2.getTargets().size());
    assertEquals(2, t3.getTargets().size());
    assertEquals(2, t4.getTargets().size());
    assertEquals(MessageType.NEW_TARGETS, m.getType());

    tearDown();
  }

  /**
   * Tests that Game.revivePlayersOnLivingTeams makes the right players living and keeps the
   * right players dead, assigns new kill codes to revived players, and recores this in a message.
   */
  @Test
  public void testGameRevivePlayersOnLivingTeams() throws DBAccessException, InvalidActionException,
          InvalidAccessException {
    setUp();

    g.changeNumTargets(3);
    g.start();
    String p2killCode = p2.getKillCode();
    String p3killCode = p3.getKillCode();
    String p6killCode = p6.getKillCode();
    String p7killCode = p7.getKillCode();
    String p8killCode = p8.getKillCode();
    p1.kill(p5);
    p2.kill(p1);
    p3.kill(p6);
    p4.kill(p7);
    p8.kill(p4);
    g.revivePlayersOnLivingTeams();
    Message m = g.getMessages(1).get(0);

    /*
    1: dead → dead
    2: alive → alive
    3: alive → alive
    4: dead → dead
    5: dead → dead
    6: dead → alive
    7: dead → alive
    8: alive → alive
     */

    assertFalse(p1.isAlive());
    assertTrue(p2.isAlive());
    assertTrue(p3.isAlive());
    assertFalse(p4.isAlive());
    assertFalse(p5.isAlive());
    assertTrue(p6.isAlive());
    assertTrue(p7.isAlive());
    assertTrue(p8.isAlive());

    assertFalse(t1.isAlive());
    assertTrue(t2.isAlive());
    assertFalse(t3.isAlive());
    assertTrue(t4.isAlive());

    assertEquals(Collections.emptyList(), t1.getAlivePlayers());
    assertEquals(t2.getPlayers(), t2.getAlivePlayers());
    assertEquals(Collections.emptyList(), t3.getAlivePlayers());
    assertEquals(t4.getPlayers(), t4.getAlivePlayers());

    assertEquals(p2killCode, p2.getKillCode());
    assertEquals(p3killCode, p3.getKillCode());
    assertNotEquals(p6killCode, p6.getKillCode());
    assertNotEquals(p7killCode, p7.getKillCode());
    assertEquals(p8killCode, p8.getKillCode());

    assertEquals(MessageType.REVIVE, m.getType());

    tearDown();
  }
}
