# CS0320 Term Project 2020
## [Assassin Game Online](https://assassingame.herokuapp.com/)

An online management tool that allows users to host, generate, and run [assassin games](https://en.wikipedia.org/wiki/Assassin_(game)) online. A live deployed version of the project can be found [here](https://assassingame.herokuapp.com/). For details on deploying the project yourself, see [How to Build and Run](#how-to-build-and-run).

A tradition across many American high schools and colleges, Assassin is a multiplayer, live-action game where players try to “assassinate” their targets and be the last survivor. Players are assigned a “weapon” or method of assassination that they must use to eliminate opponents. Althougth running and managing a game of Assassin is a considerable workload that lends well to automated management, there does not currently exist a good online hosting platform for the game. Our project aims to address this need.

## How to Build and Run

To build the project, run the following command from the root directory:
```
mvn package
```

*Note that Java 11 may be required to compile the project.*

*Building the project with this command will also run all associated JUnit tests. These tests do not
cover all aspects of functionality. See [Testing](#testing) for details on system-level
tests. If errors are encountered while running JUnit tests, you can include the flag `-DskipTests` to skip running JUnit tests!*

Once built, run the program with the following command to launch a server interface
that can be accessed at `http://localhost:<portnumber>/` (or, replace `localhost:<portnumber>` with the hostname):
```
./run [--port <portnumber>]
```

Including the `--port` option will attempt to launch the server on the
given port number. If no port is specified, this value defaults to `4567`.

Alternatively, a live deployed version of this application can be found at https://assassingame.herokuapp.com/.

## Features
  * Account and session management for users.
  * Users can create games and invite players to join wih a game PIN. 
  * Generates optimally-structured assassin games by following [certain algorithmic and fairness principles](https://zarvox.org/assassins/math.html) to provide the most fair and enjoyable playing experience.
  * Allows players to see their current target(s) and a live feed displaying kills and game-wide events (e.g., targets shuffled).
  * Allows game admins to customize options of the game (e.g., team size, game name, game rules, number of targets, anonymity of players).
  * Provides game admins with privileges such as shuffling targets, reviving players on living teams, sending emails to all players in the game, and ending the game prematurely.
  * Easy navigation between different games.
  
## Core Algorithmic Complexity
  * Assassin games are effectively directed graphs, where each player or team has one (or multiple) edges pointing outwards at their targets.
  * We implemented a modular graph generation algorithm to assign t targets to each player in the game.  
  * The graph generation algorithm aimed to generate well-formed and fair games, optimizing for certain properties:
      - We assign targets to players such that the edge distance between each player and the player who is targeting them is maximized and equal between players (since "killing" the person who has you as a target makes you immortal). 
      - In games where players have multiple targets, the edge distance between players who have the same target should be maximized as well, since a player who "kills" one of their targets inherits that target's target.
  * At a high level, we want the game to proceed as smoothly as possible without assassins becoming stuck (lack of new targets or targeting themselves) and minimize intervention from game admins.
  * The algorithm generates optimal graphs using a few key steps:
      - For a game with n players and t targets per player, there are n choose t "target number" combinations. To construct the graph, we initially visualize all players essentially standing in a circle, with two adjacent players and thus two connected edges. A "target number" represents the number of edges (taken clockwise) that separate each player from their target.
      - We don't check every target combination, using mathematical intuition to eliminate possible combinations (most prominently using relative primality of the target numbers).
      - After narrowing down the possible target number combinations, a breadth-first search is run starting from an arbitrary player 0 in the circle (without loss of generality). 
      - The BFS computes a metric based on number of kills needed for player 0 to kill every other player in the game and number of kills needed for player 0 to kill themself (what we want to prevent). The target number combination with the best metric score is used as our policy for assigning assassin/target pairs.
  * Additional algorithmic details and metrics can be found [here](https://github.com/cs032-2020/term-project-chansen6-hdandapa-lho11-sli96-1/blob/master/src/main/java/edu/brown/cs/assassin/graph/README.md).
  
## Design Details

### Package Structure
* `database`: Stores database proxy class (handles database connection, query execution and caching) and misc. utility database methods.
* `email`: Stores functionality for sending emails.
* `exception`: Stores specific Exception classes.
* `game`: Stores functionality for creating, maintaining, and operating a game of Assassin. Game, Player, Team, User, and Message contain database methods specific to the objects they represent.
* `graph`: Stores graph generation functionaliy for creating assassin/target pairs. 
* `gui`: Stores routing and backend logic handling for GUI interaction, such as all POST and GET routes. Includes routes for JavaScript querying from the frontend.
* `login`: Stores login functionality and session management.
* `main`: Stores `Main.java`. Also contains constants used throughout.

### Player accounts
* Player accounts made sense for our platform because each user needs to have access to personalized game information (like secret code and targets).
* Players will log in or sign up with their email address and a password and must set a username (preferably their name) to allow other players to identify them.
* Once players have accounts and are logged in, they can be added to games or create their own game.
* Players can also reset their passwords as they see necessary. 

### Creating a game
* When logged in, anyone can initiate the process of creating a new game by clicking a button. They become a game admin for that game. To create a game, they enter the following information:
    - The name of the game
    - The number of players on each team
    - The number of targets each team has
    - Game rules (Markdown supported)
    - An option to hide the names of players on teams, so teams can only see the codenames of the teams they are targeting

### Managing a game
* Games for which a user is a game admin for are marked by a special icon
* Game admin privileges include:
    - Modify any game settings
    - End game without a winner
    - Revive all players on living teams
    - Shuffle targets
    - Send a message or email to all players in the game
* These privileges were based on personal experiences playing Assassin and talking to people that have. Our team agreed that they were necessary for games to run as smoothly as possible.

### Joining a game
* When logged in, a user can input a game code to join the game
* When joining a game, a user has the option either to create a new team or to join a preexisting team. A user can join a preexisting team by entering that team’s join code.
* We considered admin invites via email to allow players to join games, but ultimately decided a join code was simpler and just as effective.

### Recording Kills
* Kills are recorded by the assassin entering the target's kill code
* This method of recording kills was based off of prior experiences playing Assassin and we believe reduces potential issues because the target must surrender their own kill code (validating the kill).
* Killing a team leaves the assassin team, and any other teams that were targeting the killed team, with one fewer target. To replenish the targets, the killed team’s targets are reassigned to all the teams that were targeting it, so that each team that lost a target gains a new target. However, toward the end of the game, teams could end up with repeat targets or self-targets; the target reassignment algorithm chooses the assignment that causes this to happen the least.
* Once there is only one team left in a game, the game has a winner, and the game will end.
