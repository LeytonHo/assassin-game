package edu.brown.cs.assassin.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Sets;

import edu.brown.cs.assassin.main.AssassinConstants;

/**
 * Graph operations for Assassin game. Graph generation based off:
 * https://zarvox.org/assassins/math.html
 *
 * @param <U> Parametrizes player object
 */
public class Graph<U> {

  private int numPlayers;
  private List<Target<U>> targets;
  private double metric; // metric for generated graph optimality

  /**
   * Assassin Game Graph constructor.
   *
   * @param p List of players in game
   * @param t Number of targets
   */
  public Graph(List<U> p, int t) {
    numPlayers = p.size();
    t = Math.max(1, Math.min(t, numPlayers - 1));
    if (t == 1) {
      // Faster and as effective as calling generateNTarget for t = 1
      targets = generateSingleTargetGraph(p);
    } else {
      targets = generateNTargetGraph(p, t);
    }
  }

  /**
   * Getter method to return list of assassin/target pairs. Used for testing.
   *
   * @return List of assassin/target pairs.
   */
  public List<Target<U>> getTargets() {
    return new ArrayList<Target<U>>(this.targets);
  }

  /**
   * Getter method to return graph optimality metric.
   *
   * @return graph optimality metric
   */
  public double getMetric() {
    return this.metric;
  }

  /**
   * Computes graph optimality with percent difference.
   *
   * @param avg Average score of all non-optimal target number combinations
   * @param min Score of optimal target number combination
   */
  public void graphMetrics(double avg, double min) {
    this.metric = (avg - min) * AssassinConstants.PERCENTAGE / avg;
  }

  /**
   * Generates Assassin game graph for single-target game.
   *
   * @param players Players in game
   * @return List of target/assassin pairs
   */
  public List<Target<U>> generateSingleTargetGraph(List<U> players) {
    // Shuffle collection for randomization
    Collections.shuffle(players);
    ArrayList<Target<U>> compiledTargets = new ArrayList<>();

    // Add player-target pairs in order
    for (int i = 0; i < players.size() - 1; i++) {
      compiledTargets.add(new Target<>(players.get(i), players.get(i + 1)));
    }

    // Last player should have first player as target to complete cycle
    compiledTargets.add(new Target<>(players.get(players.size() - 1), players.get(0)));
    return compiledTargets;
  }

  /**
   * BFS search for minimum distances from Player 0 to every other player. Returns
   * score for list of "target numbers"
   *
   * @param tNum List of "target numbers"
   * @return Score
   */
  public double bfs(Set<Integer> tNum) {
    // Check that it's possible to reach all indices with given tNum
    // If the sum of any subset of tNum is relatively prime to numPlayers, reachable
    boolean reachable = false;
    for (Set<Integer> s : DiscreteCalculations.powerSet(tNum)) {
      int sum = 0;
      // Compute sum of elements in set
      Iterator<Integer> sumIter = s.iterator();
      while (sumIter.hasNext()) {
        sum += sumIter.next();
      }

      if (DiscreteCalculations.gcd(numPlayers, sum) == 1) {
        reachable = true;
        break;
      }
    }

    // If not all indices are reachable, return maximum possible score.
    if (!reachable) {
      return Double.MAX_VALUE;
    }

    int root = 0; // Represents number corresponding to Player 0

    // Initialize array of 0s, where 0 represents not visited
    int[] visited = new int[numPlayers];

    // Initialize array to keep track of minimum distances
    int[] minDist = new int[numPlayers];

    // Store list of seen vertices
    int[] seen = new int[numPlayers];

    // Create a queue for BFS. Vertices stored in queue as [Vertex Index, Edge
    // Weight]
    LinkedList<int[]> queue = new LinkedList<>();

    // Add initial adjacent vertices to queue. Current edge weight is 1.
    Iterator<Integer> it = tNum.iterator();
    while (it.hasNext()) {
      int[] temp = {
          it.next() + root, 1
      };
      queue.add(temp);
    }

    // Run BFS
    while (queue.size() != 0) {
      // Check if all nodes have been visited
      int sum = 0;
      for (int i : visited) {
        sum += i;
      }

      // If all nodes visited, break.
      if (sum == numPlayers) {
        break;
      }

      // Dequeue a vertex from queue
      int[] vtx = queue.poll();
      int vIndex = vtx[0] % numPlayers;
      int currWeight = vtx[1];

      // If vertex hasn't been visited, store min edge weight and mark as visited.
      if (visited[vIndex] == 0) {
        visited[vIndex] = 1;
        minDist[vIndex] = currWeight;
      }

      // Add adjacent vertices of the dequeued vertex v to queue
      Iterator<Integer> i = tNum.iterator();
      while (i.hasNext()) {
        int newVtx = (vIndex + i.next()) % numPlayers;

        // If vertex hasn't been seen, add to queue
        if (seen[newVtx] == 0) {
          seen[newVtx] = 1;
          int[] temp = {
              newVtx, currWeight + 1
          };
          queue.add(temp);
        }
      }
    }

    // Sum of minimum distances from player 0 to every other player
    int total = 0;
    for (int i = 1; i < minDist.length; i++) {
      total += minDist[i];
    }

    // Return score = D / D0, where D0 is distance for player 0 to get back to 0 & D
    // is above sum.
    return total / minDist[0];
  }

  /**
   * Generates Assassin game graph for n targets.
   *
   * @param players Players in game
   * @param t       Number of targets
   * @return List of target/assassin pairs
   */
  public List<Target<U>> generateNTargetGraph(List<U> players, int t) {
    // Shuffle collection for randomization
    Collections.shuffle(players);

    // Without loss of generality, number the players from 0 to n - 1
    BiMap<Integer, U> index = HashBiMap.create(); // Using a BiMap to avoid two separate HashMaps
    for (int i = 0; i < players.size(); i++) {
      index.put(i, players.get(i));
    }

    // All possible unique "target numbers" besides 1
    /*
     * Imagine the players in a circle. Target number T means each player is
     * assigned the Tth player going counter-clockwise
     */
    Set<Integer> playerSet = new HashSet<>();
    for (int j = 2; j < players.size(); j++) {
      playerSet.add(j);
    }

    // All possible lists of t - 1 unique "target numbers"
    // We make the assumption that 1 must be part of any target number allocation.
    Set<Set<Integer>> combos = Sets.combinations(playerSet, t - 1);

    /*
     * For each list of t unique "target numbers": Construct a graph and find
     * minimum distance with BFS using player 0 as root node. Compute distance D,
     * where D is sum of minimum distances from player 0 to every other player.
     * Score = D / D0, where D0 is distance for player 0 to get back to 0. Use list
     * of target numbers that has minimum score.
     */
    Set<Integer> minCombo = new HashSet<>();
    double minScore = Double.MAX_VALUE;
    List<Double> scores = new ArrayList<>(); // Stores all scores for metric computation

    for (Set<Integer> s : combos) {
      // s is unmodifiable set. Copy contents into temp.
      Set<Integer> sTemp = new HashSet<>();
      for (Integer a : s) {
        sTemp.add(a);
      }
      boolean valid = true;

      // If number of targets >= 3, check condition to avoid cycles:
      // The non-1 target numbers are relatively prime
      if (t >= 3) {
        for (Set<Integer> i : Sets.combinations(s, 2)) {
          // i is unmodifiable set. Copy contents into iTemp.
          ArrayList<Integer> iTemp = new ArrayList<>(i);
          int first = iTemp.get(0);
          int second = iTemp.get(1);
          // Check condition
          if (DiscreteCalculations.gcd(first, second) != 1) {
            valid = false;
          }
        }
      }

      // If valid combination, add 1 to set and compute score with BFS
      if (valid) {
        sTemp.add(1);
        double score = bfs(sTemp);
        if (score < minScore) {
          minCombo = sTemp;
          minScore = score;
        }
        scores.add(score);
      }
    }

    // Graph optimality metric computation
    double total = 0.0;
    double count = 0;
    for (Double s : scores) {
      // Throwing out outlier scores
      if (s < minScore * AssassinConstants.PERCENTAGE) {
        total += s;
        count += 1;
      }
    }
    double avgScore = (total - minScore) / (count - 1);
    graphMetrics(avgScore, minScore);

    // Compute respective targets for chosen target numbers.
    ArrayList<Target<U>> compiledTargets = new ArrayList<>();
    for (U p : players) {
      // Get index of player
      int pIndex = index.inverse().get(p);

      Iterator<Integer> i = minCombo.iterator();
      while (i.hasNext()) {
        int tIndex = (i.next() + pIndex) % numPlayers; // Get index of target
        U tempTarget = index.get(tIndex);
        compiledTargets.add(new Target<>(p, tempTarget)); // Add assassin/target pair
      }
    }

    return compiledTargets;
  }
}
