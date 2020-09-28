# Graph Generation

When a game of Assassin is started, a graph mapping assassins to their initial targets must be created. 
There are a variety of strategies to generate an initial graph. For a one-target per player Assassin game,
generating a graph is fairly straightforward — the graph is essentially one cycle. We implemented this by randomizing
the list of players in the game and assigning targets to assassins in order (i.e. the player in index 1 was assigned
the player in index 2 as a target and so on). 

The core algorithmic complexity was in generating multi-target Assassin games and modularizing our code for n-targets.
We followed the graph generation tactic outlined [here](https://zarvox.org/assassins/math.html) and elaborated on 
[here](https://zarvox.org/assassins/Paranoia.pdf).

## Algorithm Details

Let n = the number of players in the game. Let t = the number of targets per player:

1. Shuffle list of players for randomization
2. Without loss of generality, number the players from 0 to n - 1
3. Determine all possible target numbers. A "target number" is defined as follows. Imagine the players in a circle. 
Target number T means each player is assigned the Tth player going counter-clockwise.
4. Determine possible lists of unique "target numbers," using the following conditions to optimize (more details below):
    - One of the target numbers must be 1
    - The target numbers must be relatively prime to one another
5. For each list of t unique target numbers: Construct a graph and find the minimum distance using breadth-first search (BFS) from 
root node player 0 to all other players in the graph. Compute distance D, where D is sum of minimum distances from player 0 
to every other player. The score of the list of t unique target numbers is D / D0, where D0 is distance for player 0 to get back to 0. 
6. Use the list of unique target numbers that has the minimum score.
7. Compute the respective targets for the chosen target numbers.

## Justification for Optimizations

Initially, we generated possible lists of unique target numbers without any constraints: we checked all n choose t combinations of target numbers. The three optimizations made for possible lists of target numbers were based off thorough testing and mathematical intuition. The key property of a good graph is long "cycles" — as game operators we should try to prevent each player from being assigned themselves as a target (by killing their assassin) for as long as possible — and these optimizations addressed that.

1. Under the naive approach (checking every possible combination of target numbers), one of the target numbers was always 1. This makes intuitive sense — assigning each player the player "next to them" results in one having to kill every other player in the game to reach oneself. Therefore, it was safe to conclude that we must only check target number combinations that include 1.
2. We noticed that under the naive approach, the target numbers were always relatively prime to one another. This also makes intuitive sense. For example, a game with target numbers 1, 2, and 4 may result in quick cycles and redundancies (no new targets once a player assassinates their target).

## BFS Search Constraint:

For a list of target numbers to be valid, player 0 must be able to reach every other player in the game by assassinating his/her
targets (or BFS will look for a path that does not exist). Before conducting BFS, we added a check for this constraint. If a 
list of target numbers does not satisfy this constraint, it is removed from consideration. For player 0 to be able to reach 
all other players, the sum of any subset of the list of target numbers must be relatively prime to the number of players.
To check for this, we iteratively use Euclid's Algorithm to check if the greatest common divisor (GCD) of {sum of each possible 
subset, number of players} = 1. If the GCD = 1 for any subset, the list of target numbers is valid. 

## Graph Optimality Metrics

To compute the optimality of the target numbers this algorithm produces, we benchmarked against the average of all other target number combinations we considered (optimization conditions applied). We also threw away any combinations with outlier scores, where we defined outlier scores as at least 100 times greater than the score of chosen minimum combination.

The percent improvement computation was as follows: 100 * (avg. score - min. score) / (avg. score) 

| # players  | # targets  | % improvement  |
|------------|------------|----------------|
| 8 | 2 | 46.4% |
| 8 | 3 | 43.4% |
| 13 | 2 | 32.6% |
| 13 | 3 | 31.0% |
| 50 | 2 | 56.4% |
| 50 | 3 | 53.6% |
| 100 | 2 | 61.1% |
| 100 | 3 | 54.3% |
| 500 | 2 | 62.7% |
| 500 | 3 | 58.1% |

