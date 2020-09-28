package edu.brown.cs.assassin.graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Houses discrete math computations needed for graph generation.
 */
public final class DiscreteCalculations {
  private DiscreteCalculations() {
  }

  /**
   * Calculates the power set.
   *
   * Pulled from https://stackoverflow.com/questions/4640034/calculating-all-of-the-subsets-of-a-set-of-numbers
   * @param originalSet Set of integers
   * @return Power set
   */
  public static Set<Set<Integer>> powerSet(Set<Integer> originalSet) {
    Set<Set<Integer>> sets = new HashSet<>();
    if (originalSet.isEmpty()) {
      sets.add(new HashSet<>());
      return sets;
    }
    List<Integer> list = new ArrayList<>(originalSet);
    // Choose arbitrary element in set as head
    Integer head = list.get(0);
    Set<Integer> rest = new HashSet<>(list.subList(1, list.size()));
    // Cycle through the power set of the remaining elements
    for (Set<Integer> set : powerSet(rest)) {
      Set<Integer> newSet = new HashSet<>();
      newSet.add(head);
      newSet.addAll(set);
      sets.add(newSet);
      sets.add(set);
    }
    return sets;
  }


  /**
   * Uses Euclid's Algorithm to solve for the gcd of two numbers.
   *
   * @param a First number
   * @param b Second number
   * @return GCD of a and b
   */
  public static int gcd(int a, int b) {
    int temp;
    while (b != 0) {
      temp = a;
      a = b;
      b = temp % b;
    }
    return a;
  }
}
