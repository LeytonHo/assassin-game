package edu.brown.cs.assassin.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.junit.After;
import org.junit.Test;

public class GraphTest {
  List<TestPlayer> players;

  /**
   * Tester player class to avoid having to query db in graph tests.
   */
  private class TestPlayer {

    private String id;

    public TestPlayer(String id) {
      this.id = id;
    }

    public String getUsername() {
      return this.id;
    }

    @Override
    public boolean equals(Object object) {
      if (this == object)
        return true;
      if (object == null || getClass() != object.getClass())
        return false;
      if (!super.equals(object))
        return false;
      TestPlayer player = (TestPlayer) object;
      return java.util.Objects.equals(id, player.id);
    }

    @Override
    public int hashCode() {
      return Objects.hash(super.hashCode(), id);
    }
  }

  /**
   * Initialize n player game for testing.
   */
  public void setUp(int n) {
    List<TestPlayer> game1 = new ArrayList<>();

    for (int i = 1; i <= n; i++) {
      game1.add(new TestPlayer(Integer.toString(i)));
    }

    players = game1;
  }

  /**
   * Resets graph.
   */
  @After
  public void tearDown() {
    players = null;
  }

  /**
   * Checks if assassin/target allocations create balanced game. Every player
   * should be targeted the same number of times. Every player should have the
   * same number of targets.
   *
   * @param t       List of targets
   * @param targets Number of targets each player should have
   * @return True if game is balanced
   */
  public boolean balancedGame(List<Target<TestPlayer>> t, int targets) {
    HashMap<String, Integer> numTargets = new HashMap<>(); // Number of targets
                                                           // per player
    HashMap<String, Integer> numTargeted = new HashMap<>(); // Times targeted
                                                            // per player

    // Initialize counts to 0
    for (TestPlayer a : players) {
      numTargets.put(a.getUsername(), 0);
      numTargeted.put(a.getUsername(), 0);
    }

    // Count targets and times targeted
    for (Target<TestPlayer> p : t) {
      String assassin = p.getAssassin().getUsername();
      String target = p.getTarget().getUsername();

      int assassinCount = numTargets.get(assassin);
      int targetCount = numTargeted.get(target);
      numTargets.put(assassin, assassinCount + 1);
      numTargeted.put(target, targetCount + 1);
    }

    // Check that each player has correct number of targets
    Iterator i = numTargets.entrySet().iterator();
    while (i.hasNext()) {
      Map.Entry elt = (Map.Entry) i.next();
      int count = (int) elt.getValue();
      if (count != targets) {
        return false;
      }
    }

    // Check that each player is targeted the correct number of times
    Iterator it = numTargeted.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry elt = (Map.Entry) it.next();
      int count = (int) elt.getValue();
      if (count != targets) {
        return false;
      }
    }

    return true;
  }

  /**
   * Test game with n players and n-1 targets.
   */
  @Test
  public void oneLess() {
    setUp(4);
    // Three target game
    Graph<TestPlayer> game3 = new Graph<>(players, 3);
    assertEquals(game3.getTargets().size(), 12);
    assertTrue(balancedGame(game3.getTargets(), 3));
    tearDown();
  }

  /**
   * Tests game with 2 players.
   */
  @Test
  public void twoPlayers() {
    setUp(2);

    // One target game
    Graph<TestPlayer> game1 = new Graph<>(players, 1);
    assertEquals(game1.getTargets().size(), 2);
    assertTrue(balancedGame(game1.getTargets(), 1));

    tearDown();
  }

  /**
   * Tests game with 3 players.
   */
  @Test
  public void threePlayers() {
    setUp(3);

    // One target game
    Graph<TestPlayer> game1 = new Graph<>(players, 1);
    assertEquals(game1.getTargets().size(), 3);
    assertTrue(balancedGame(game1.getTargets(), 1));

    // Two target game
    Graph<TestPlayer> game2 = new Graph<>(players, 2);
    assertEquals(game2.getTargets().size(), 6);
    assertTrue(balancedGame(game2.getTargets(), 2));

    tearDown();
  }

  /**
   * Tests game with 8 players.
   */

  @Test
  public void eightPlayers() {
    setUp(8);

    // One target game
    Graph<TestPlayer> game1 = new Graph<>(players, 1);
    assertEquals(game1.getTargets().size(), 8);
    assertTrue(balancedGame(game1.getTargets(), 1));

    // Two target game
    Graph<TestPlayer> game2 = new Graph<>(players, 2);
    assertEquals(game2.getTargets().size(), 16);
    assertTrue(balancedGame(game2.getTargets(), 2));
    // This test exists to show how we computed the optimality graph metric
    assertEquals(game2.getMetric(), 46.4, 0.05);

    // Three target game
    Graph<TestPlayer> game3 = new Graph<>(players, 3);
    assertEquals(game3.getTargets().size(), 24);
    assertTrue(balancedGame(game3.getTargets(), 3));
    // This test exists to show how we computed the optimality graph metric
    assertEquals(game3.getMetric(), 43.4, 0.05);

    tearDown();
  }

  /**
   * Tests game with 13 players.
   * Prime number of players
   */
  @Test
  public void thirteenPlayers() {
    setUp(13);

    // One target game
    Graph<TestPlayer> game1 = new Graph<>(players, 1);
    assertEquals(game1.getTargets().size(), 13);
    assertTrue(balancedGame(game1.getTargets(), 1));

    // Two target game
    Graph<TestPlayer> game2 = new Graph<>(players, 2);
    assertEquals(game2.getTargets().size(), 26);
    assertTrue(balancedGame(game2.getTargets(), 2));

    // Three target game
    Graph<TestPlayer> game3 = new Graph<>(players, 3);
    assertEquals(game3.getTargets().size(), 39);
    assertTrue(balancedGame(game3.getTargets(), 3));

    tearDown();
  }

  /**
   * Tests game with 50 players.
   */
  @Test
  public void fiftyPlayers() {
    setUp(50);

    // One target game
    Graph<TestPlayer> game1 = new Graph<>(players, 1);
    assertEquals(game1.getTargets().size(), 50);
    assertTrue(balancedGame(game1.getTargets(), 1));

    // Two target game
    Graph<TestPlayer> game2 = new Graph<>(players, 2);
    assertEquals(game2.getTargets().size(), 100);
    assertTrue(balancedGame(game2.getTargets(), 2));

    // Three target game
    Graph<TestPlayer> game3 = new Graph<>(players, 3);
    assertEquals(game3.getTargets().size(), 150);
    assertTrue(balancedGame(game3.getTargets(), 3));

    tearDown();
  }

  /**
   * Tests game with 100 players.
   */
  @Test
  public void oneHundredPlayers() {
    setUp(100);

    // One target game
    Graph<TestPlayer> game1 = new Graph<>(players, 1);
    assertEquals(game1.getTargets().size(), 100);
    assertTrue(balancedGame(game1.getTargets(), 1));

    // Two target game
    Graph<TestPlayer> game2 = new Graph<>(players, 2);
    assertEquals(game2.getTargets().size(), 200);
    assertTrue(balancedGame(game2.getTargets(), 2));

    // Three target game
    Graph<TestPlayer> game3 = new Graph<>(players, 3);
    assertEquals(game3.getTargets().size(), 300);
    assertTrue(balancedGame(game3.getTargets(), 3));

    tearDown();
  }

  /**
   * Tests game with 500 players.
   */
  @Test
  public void fiveHundredPlayers() {
    setUp(500);

    // One target game
    Graph<TestPlayer> game1 = new Graph<>(players, 1);
    assertEquals(game1.getTargets().size(), 500);
    assertTrue(balancedGame(game1.getTargets(), 1));

    // Two target game
    Graph<TestPlayer> game2 = new Graph<>(players, 2);
    assertEquals(game2.getTargets().size(), 1000);
    assertTrue(balancedGame(game2.getTargets(), 2));

    // Three target game
    Graph<TestPlayer> game3 = new Graph<>(players, 3);
    assertEquals(game3.getTargets().size(), 1500);
    assertTrue(balancedGame(game3.getTargets(), 3));

    tearDown();
  }

  /**
   * Tests graph generation for more than 3 targets.
   * Will not be used in games (due to target cap), but shows extensibility.
   */
  @Test
  public void manyTargets() {
    setUp(30);

    // Four target game
    Graph<TestPlayer> game1 = new Graph<>(players, 4);
    assertEquals(game1.getTargets().size(), 120);
    assertTrue(balancedGame(game1.getTargets(), 4));

    // Five target game
    Graph<TestPlayer> game2 = new Graph<>(players, 5);
    assertEquals(game2.getTargets().size(), 150);
    assertTrue(balancedGame(game2.getTargets(), 5));

    // Six target game
    Graph<TestPlayer> game3 = new Graph<>(players, 6);
    assertEquals(game3.getTargets().size(), 180);
    assertTrue(balancedGame(game3.getTargets(), 6));

    tearDown();
  }
}
