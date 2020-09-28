package edu.brown.cs.assassin.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;

import edu.brown.cs.assassin.exception.DBAccessException;
import edu.brown.cs.assassin.exception.InvalidAccessException;
import edu.brown.cs.assassin.exception.InvalidActionException;
import edu.brown.cs.assassin.game.Game;
import edu.brown.cs.assassin.game.GameStatus;
import edu.brown.cs.assassin.game.Message;
import edu.brown.cs.assassin.game.MessageType;
import edu.brown.cs.assassin.game.Player;
import edu.brown.cs.assassin.game.Team;
import edu.brown.cs.assassin.game.User;
import edu.brown.cs.assassin.login.SessionManager;
import edu.brown.cs.assassin.main.Main;
import spark.ModelAndView;
import spark.QueryParamsMap;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.TemplateViewRoute;

/**
 * UserGUI class used to store information about all of the pages that users can
 * see in the GUI.
 */
public final class UserGUI {
  private static final int MESSAGES = 15;

  private UserGUI() {
  }

  // RETRIEVAL METHODS

  /**
   * Retrieves games for a given user.
   *
   * @param user Given user
   * @return Return type for working with FreeMarker
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if the database contains an invalid user ID
   */
  public static List<Map<String, String>> getGamesForUser(User user)
      throws DBAccessException, InvalidAccessException {
    List<Map<String, String>> res = new ArrayList<>();

    // Get all games that the player is an admin in and add them to map
    List<Game> gameList = user.getAdminGames();
    for (Game g : gameList) {
      res.add(ImmutableMap.of("gameID", Integer.toString(g.getID()), "gameName", g.getName(),
          "isAdmin", "true", "status", g.getStatus().name()));
    }

    // Get all games the user is a player in and add them to map
    gameList = user.getPlayingGames();
    for (Game g : gameList) {
      res.add(ImmutableMap.of("gameID", Integer.toString(g.getID()), "gameName", g.getName(),
          "isAdmin", "false", "status", g.getStatus().name()));
    }

    return res;
  }

  /**
   * Retrieves team members for a given team.
   *
   * @param t Given team
   * @return Return type for working with FreeMarker
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if the database contains an invalid team ID
   */
  public static List<String> getTeamMembersForTeam(Team t)
      throws DBAccessException, InvalidAccessException {
    List<String> output = new ArrayList<>();
    List<Player> teammates = t.getPlayers();
    for (Player p : teammates) {
      String line = "<b>Player:</b> " + p.getUser().getName()
          + (p.isAlive() ? "; <b>Status:</b> Alive<br>" : "; <b>Status:</b> Dead<br>");
      output.add(line);
    }
    return output;
  }

  /**
   * Retrieves targets for a given team.
   *
   * @param t Given team
   * @return Targets
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if the database contains an invalid team ID
   */
  public static List<String> getTargetsForTeam(Team t)
      throws DBAccessException, InvalidAccessException {
    List<String> res = new ArrayList<>();
    if (t.getGame().isAnonymous()) {
      List<Team> targets = t.getDisplayedTargets();
      for (Team target : targets) {
        res.add(target.getCodename());
      }
    } else {
      List<Team> targets = t.getDisplayedTargets();
      for (Team target : targets) {
        List<Player> alivePlayers = target.getAlivePlayers();
        String teamName = target.getCodename();
        for (Player p : alivePlayers) {
          res.add("Team: " + teamName + "; Player: " + p.getUser().getName());
        }
      }
    }
    return res;
  }

  /**
   * Retrieves all teams and players in a given game.
   *
   * @param gamer Given game
   * @return Teams and players
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if the database contains an invalid game ID
   */
  public static List<String> getFormattablePlayers(Game gamer)
      throws DBAccessException, InvalidAccessException {
    List<String> output = new ArrayList<>();
    List<Team> teams = gamer.getTeams();
    for (Team team : teams) {
      String teamName = team.getCodename();
      List<Player> players = team.getPlayers();
      for (Player player : players) {
        String line = "<b>Team:</b> " + teamName + "; <b>Player:</b> " + player.getUser().getName()
            + (player.isAlive() ? "; <b>Status:</b> Alive<br>" : "; <b>Status:</b> Dead<br>");
        output.add(line);
      }
    }
    return output;
  }

  private static String renderMarkdown(String s) {
    Parser parser = Parser.builder().build();
    Node document = parser.parse(s);
    HtmlRenderer renderer = HtmlRenderer.builder().build();
    return renderer.render(document);
  }

  private static String emphasize(String s) {
    return String.format("<span class = \"message-emphasis\">%s</span>", s);
  }

  /**
   * Retrieve kill feed.
   *
   * @param messages Kill feed messages
   * @return Kill feed as a string with embedded HTML
   * @throws DBAccessException if something goes wrong with the database
   */
  private static String killfeedString(List<Message> messages)
      throws DBAccessException, InvalidAccessException {
    StringBuilder killfeedString = new StringBuilder();
    for (Message m : messages) {
      MessageType type = m.getType();
      if (type != null) {
        killfeedString.append("<div class=\"feed-message\">");
        String content;
        switch (type) {
          case START:
            content = "The game has started!";
            break;
          case WIN:
            content = String.format("%s won the game!", emphasize(m.getField(0)));
            break;
          case END:
            content = "The game has ended!";
            break;
          case CHANGE_NAME:
            content = String.format("The name of the game has been changed to %s",
                emphasize(m.getField(0)));
            break;
          case CHANGE_RULES:
            content = "The rules have been changed.";
            break;
          case CHANGE_ANON:
            content = "The game has been made anonymous. You cannot see who is playing on your "
                + "target team(s).";
            break;
          case CHANGE_NOT_ANON:
            content = "The game has been made not anonymous. You can see who is playing on your "
                + "target team(s).";
            break;
          case CHANGE_NUM_TARGETS:
            content = String.format("The maximum number of targets has been changed to %s. This "
                + "will take effect when new targets are assigned.", m.getField(0));
            break;
          case NEW_TARGETS:
            content = "New targets have been assigned.";
            break;
          case REVIVE:
            content = "All dead players on living teams have been revived.";
            break;
          case ELIMINATE:
            content = String.format("%s eliminated %s!", emphasize(m.getField(0)),
                emphasize(m.getField(1)));
            break;
          case SURRENDER:
            content = String.format("%s surrendered.", emphasize(m.getField(0)));
            break;
          case CUSTOM:
            content = m.getField(0);
            break;
          case NONE:
            content = "[old message whose content has been lost]";
            break;
          default:
            content = "";
        }

        killfeedString.append(content);
        killfeedString.append("</div>");
      }
    }
    return killfeedString.toString();
  }

  /**
   * Given a user and a game, renders the home screen that the user should see for
   * that game.
   *
   * @param g the game the user wishes to see.
   * @param u the currently logged in user.
   * @return the variables to be shown on the page.
   */
  public static ModelAndView handlePlayerGameHome(Game g, User u) {
    String userName;
    String killCode;
    boolean alive;
    boolean teamAlive;
    boolean gamePlaying;
    String teamName = "";
    String gameName = "";
    String gameDescription = "";
    String gameDescriptionRaw = "";
    String gameRules = "";
    String teamJoin = "";
    String message = "The game is on!";
    String killfeed = "";

    List<String> targets;
    List<String> teamMembers;
    List<Map<String, String>> gameList;

    try {

      // Get game information
      gameName = g.getName();
      gamePlaying = g.getStatus() == GameStatus.PLAYING;
      gameDescriptionRaw = g.getRules();
      gameRules = "The maximum size for a team in this game is " + g.getMaxTeamSize() + "." + "<br>"
          + "The maximum number of targets is " + g.getNumTargets() + "." + "<br>"
          + (g.isAnonymous() ? "This game has player anonymity turned on."
              : "This game has player anonymity turned off.");
      gameDescription = renderMarkdown(gameDescriptionRaw);

      // Get user information
      userName = u.getName();
      gameList = getGamesForUser(u);

      // Get user's kill code (if the user isn't a member of this game, an IAE is
      // thrown)
      Player thisPlayer = Player.fromUserAndGame(u, g);
      killCode = thisPlayer.getKillCode();

      // Gets player's targets and teammates
      Team thisTeam = thisPlayer.getTeam();
      teamName = thisTeam.getCodename();
      teamMembers = getTeamMembersForTeam(thisTeam);
      targets = getTargetsForTeam(thisTeam);
      teamJoin = thisTeam.getJoinCode();

      // Gets the status of this player and team
      alive = thisPlayer.isAlive();
      teamAlive = thisTeam.isAlive();

      // Gets killfeed
      killfeed = killfeedString(g.getMessages(MESSAGES));

      if (g.getStatus() == GameStatus.DONE) {
        List<Team> aliveTeams = g.getAliveTeams();
        if (aliveTeams.contains(thisTeam)) {
          message = "Your team won this game!";
        } else {
          message = "Your team did not win this game.";
        }
      } else if (g.getStatus() == GameStatus.FORMING) {
        message = "This game hasn't started yet!";
      }

    } catch (NumberFormatException nfe) {
      return UtilGUI.renderErrorPage("This game does not exist.");
    } catch (DBAccessException e) {
      return UtilGUI.renderErrorPage("An error occurred: " + e.getMessage());
    } catch (InvalidAccessException e) {
      return UtilGUI.renderErrorPage(e.getMessage());
    }

    Map<String, Object> variables = ImmutableMap.<String, Object>builder().put("name", userName)
        .put("gameID", g.getID()).put("activeGame", gameName).put("gameList", gameList)
        .put("killCode", killCode).put("targets", targets).put("teamMembers", teamMembers)
        .put("teamName", teamName).put("gameDescription", gameDescription).put("teamJoin", teamJoin)
        .put("headMessage", message).put("killfeed", killfeed).put("alive", alive)
        .put("teamAlive", teamAlive).put("gamePlaying", gamePlaying)
        .put("gameDescriptionRaw", gameDescriptionRaw).put("gameRules", gameRules).build();
    return new ModelAndView(variables, "home/gamepage.ftl");
  }

  /**
   * Given a user and a game, renders the home screen that the admin user should
   * see for that game.
   *
   * @param g the game the user wishes to see.
   * @param u the currently logged in user.
   * @return the variables to be shown on the page.
   */
  public static ModelAndView handleAdminGameHome(Game g, User u) {
    String userName;
    String gameName = "";
    String gameDescription = "";
    String gameDescriptionRaw = "";
    String gameRules = "";
    String gameJoinCode = "";
    boolean gamePlaying = false;
    boolean canStart = false;
    List<Map<String, String>> gameList;
    List<String> playerList;
    String killfeed = "";

    try {

      // Get game information
      gameName = g.getName();
      canStart = g.getStatus() == GameStatus.FORMING;
      gameJoinCode = g.getJoinCode();
      playerList = getFormattablePlayers(g);
      gameDescriptionRaw = g.getRules();
      gameRules = "The maximum size for a team in this game is " + g.getMaxTeamSize() + "." + "<br>"
          + "The maximum number of targets for this game is " + g.getNumTargets() + "." + "<br>"
          + (g.isAnonymous() ? "This game has player anonymity turned on."
              : "This game has player anonymity turned off.");
      gameDescription = renderMarkdown(gameDescriptionRaw);
      gamePlaying = g.getStatus() == GameStatus.PLAYING;

      // Get user information
      userName = u.getName();
      gameList = getGamesForUser(u);

      // Gets killfeed
      killfeed = killfeedString(g.getMessages(MESSAGES));

    } catch (NumberFormatException nfe) {
      return UtilGUI.renderErrorPage("This game does not exist.");
    } catch (DBAccessException e) {
      return UtilGUI.renderErrorPage("An error occurred: " + e.getMessage());
    } catch (InvalidAccessException e) {
      return UtilGUI.renderErrorPage(e.getMessage());
    }

    Map<String, Object> variables = ImmutableMap.<String, Object>builder().put("name", userName)
        .put("gameID", g.getID()).put("activeGame", gameName).put("canStart", canStart)
        .put("gameJoinCode", gameJoinCode).put("gameList", gameList)
        .put("gameDescription", gameDescription).put("playerList", playerList)
        .put("killfeed", killfeed).put("gameDescriptionRaw", gameDescriptionRaw)
        .put("gamePlaying", gamePlaying).put("gameRules", gameRules).build();
    return new ModelAndView(variables, "home/adminpage.ftl");
  }

  // ROUTE HANDLERS

  /**
   * Supplies the home screen for the user.
   *
   */
  public static class UserHome implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) {

      User sessionAcct;
      sessionAcct = SessionManager.getLoggedInUser(req);

      // If user isn't logged in, redirect to index
      if (sessionAcct == null) {
        res.redirect(Main.LANDING_PAGE);
        return null;
      }

      // Otherwise, attempt to get user name and redirect to most recent game
      String recentGame = SessionManager.getRecentGamePathForUser(req);
      if (recentGame != null) {
        res.redirect(Main.GAME_PAGE_REDIR + recentGame);
        return null;
      }

      String name = "";
      String error = "";
      try {
        name = sessionAcct.getName();
      } catch (DBAccessException u) {
        error = "An error occurred";
      }

      // Else, load user homepage/dashboard
      Map<String, Object> variables = ImmutableMap.of("name", name, "joinMessage", error);
      return new ModelAndView(variables, "home.ftl");
    }
  }

  /**
   * Supplies a game page for a user.
   */
  public static class UserGame implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) {
      User thisUser = SessionManager.getLoggedInUser(req);

      // If user isn't logged in, redirect to index
      if (thisUser == null) {
        res.redirect(Main.LANDING_PAGE);
        return null;
      }

      // Try to retrieve the desired game, and determine if user is an admin
      Game targetGame;
      boolean isAdmin;
      try {
        int id = Integer.parseInt(req.params(":gameID"));
        targetGame = Game.fromID(id);
        isAdmin = targetGame.getAdmin().contains(thisUser);
      } catch (InvalidAccessException e) {
        return UtilGUI.renderErrorPage("Game not found");
      } catch (DBAccessException e) {
        return UtilGUI.renderErrorPage();
      }

      // Return either an admin or player homepage
      if (isAdmin) {
        return handleAdminGameHome(targetGame, thisUser);
      } else {
        return handlePlayerGameHome(targetGame, thisUser);
      }
    }
  }

  /**
   * Called when user tries to create a game.
   */
  public static class CreateGame implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) {
      User currentUser = SessionManager.getLoggedInUser(req);

      // If no user is logged in, redirect to main
      if (currentUser == null) {
        return UtilGUI.renderErrorPage("Must be logged in to view this page.");
      }
      Map<String, Object> variables = null;
      try {
        // Extracting parameters from form
        QueryParamsMap qm = req.queryMap();
        String name = qm.value("name");
        String description = qm.value("description");
        if (description == null) {
          description = "";
        }
        int size = Integer.parseInt(qm.value("size"));
        int targets = Integer.parseInt(qm.value("targets"));
        int userID = req.session().attribute(SessionManager.ID_ATTR);
        boolean allowAnon = !(qm.value("anon") == null);

        List<User> users = new ArrayList<User>(Collections.singletonList(User.fromID(userID)));

        Game gamer = Game.addGame(name, description, size, users, targets, allowAnon);

        variables = ImmutableMap.of("header", "Game Created! ", "message",
            "The invite code for your game is : <code class='inline'>" + gamer.getJoinCode()
                + "</code>");
        return new ModelAndView(variables, "message.ftl");
      } catch (NumberFormatException e) {
        variables = ImmutableMap.of();
        return new ModelAndView(variables, "create.ftl");
      } catch (DBAccessException | InvalidAccessException e) {
        return UtilGUI.renderErrorPage(e.getMessage());
      }
    }
  }

  /**
   * Called when a user submits the form to create a team.
   */
  public static class CreateTeam implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) {
      Map<String, Object> variables;

      QueryParamsMap qm = req.queryMap();
      String teamName = qm.value("teamname");
      String gameCode = qm.value("game-code-create");

      User currentUser = SessionManager.getLoggedInUser(req);

      // If no user is logged in, redirect to main
      if (currentUser == null) {
        return UtilGUI.renderErrorPage("Must be logged in to view this page.");
      }

      // Otherwise, attempt to create a new team
      try {
        Game game = Game.fromJoinCode(gameCode);
        User.JoinResult joinResult = currentUser.formNewTeam(game, teamName);
        if (joinResult.joinTeam()) {
          variables = ImmutableMap.of("header", "New Team Created", "message",
              "Team formed with join code: <b>" + joinResult.getTeam().getJoinCode() + "</b>");
          return new ModelAndView(variables, "message.ftl");
        } else {
          return UtilGUI.renderErrorPage(joinResult.getError());
        }
      } catch (DBAccessException e) {
        return UtilGUI.renderErrorPage("An error occurred");
      } catch (NumberFormatException nfe) {
        return UtilGUI.renderErrorPage("Invalid game ID");
      } catch (InvalidAccessException | InvalidActionException e) {
        return UtilGUI.renderErrorPage(e.getMessage());
      }
    }
  }

  /**
   * Called when the user submits the form to join a game.
   */
  public static class JoinGame implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) {
      User currentUser = SessionManager.getLoggedInUser(req);

      // If no user is logged in, redirect to main
      if (currentUser == null) {
        return UtilGUI.renderErrorPage("Must be logged in to view this page");
      }

      Map<String, Object> variables = ImmutableMap.of();
      return new ModelAndView(variables, "joingame.ftl");
    }
  }

  /**
   * Called when a user requests to join an existing team.
   */
  public static class JoinExistingTeam implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) {
      Map<String, Object> variables;

      QueryParamsMap qm = req.queryMap();
      String joinCode = qm.value("joincode");

      User currentUser = SessionManager.getLoggedInUser(req);

      // If no user is logged in, redirect to main
      if (currentUser == null) {
        return UtilGUI.renderErrorPage("Must be logged in to view this page");
      }

      // Otherwise, attempt to join the team
      try {
        User.JoinResult joinResult = currentUser.joinTeamFromCode(joinCode);
        if (joinResult.joinTeam()) {
          variables = ImmutableMap.of("header", "Team Joined", "message",
              "Team joined with team name: <b>" + joinResult.getTeam().getCodename() + "</b>");
          return new ModelAndView(variables, "message.ftl");
        } else {
          return UtilGUI.renderErrorPage(joinResult.getError());
        }
      } catch (DBAccessException e) {
        return UtilGUI.renderErrorPage("An error occurred");
      } catch (NumberFormatException nfe) {
        return UtilGUI.renderErrorPage("Invalid game ID");
      } catch (InvalidAccessException | InvalidActionException e) {
        return UtilGUI.renderErrorPage(e.getMessage());
      }
    }
  }

  /**
   * Called when a user enters a game code to try to join a game.
   */
  public static class GameCodeHandler implements Route {
    @Override
    public String handle(Request req, Response res) {
      QueryParamsMap qm = req.queryMap();
      String gameCode = qm.value("gameJoinCode");
      int gameID = -1;
      boolean success = false;
      String message = "";

      try {
        Game targetGame = Game.fromJoinCode(gameCode);
        User currUser = SessionManager.getLoggedInUser(req);
        if (currUser == null) {
          message = "Must be logged in to join a game.";
        } else {
          User.JoinResult joinResult = currUser.checkJoinGame(targetGame);
          if (joinResult.joinGame()) {
            success = true;
            gameID = targetGame.getID();
          } else {
            message = joinResult.getError();
          }
        }
      } catch (InvalidAccessException e) {
        message = e.getMessage();
      } catch (Exception e) {
        message = "An error occurred";
      }

      return new Gson()
          .toJson(ImmutableMap.of("gameID", gameID, "success", success, "message", message));
    }
  }

  /**
   * Called when a user enters a kill code to kill another player.
   */
  public static class KillRegistrationHandler implements Route {
    @Override
    public String handle(Request req, Response res) {
      QueryParamsMap qm = req.queryMap();
      String rawGameID = qm.value("gameid");
      String killCode = qm.value("killcode");
      boolean success = false;
      String message;

      if (killCode == null) {
        message = "Please enter a valid kill code.";
      } else {

        try {
          int gameID = Integer.parseInt(rawGameID);
          Game targetGame = Game.fromID(gameID);
          User currUser = SessionManager.getLoggedInUser(req);

          if (currUser == null) {
            message = "Must be logged in to perform this action.";

          } else {
            Player thisPlayer = Player.fromUserAndGame(currUser, targetGame);
            Player.KillResult killResult = thisPlayer.killByCode(killCode);
            if (killResult.didKill()) {
              success = true;
              if (!killResult.didEliminate()) {
                message = "Kill successfully recorded! Refresh to see your new target.";
              } else if (!killResult.didWin()) {
                message = String.format("You eliminated Team %s! Refresh to see your new target.",
                    killResult.getTeam().getCodename());
              } else {
                message = String.format("You eliminated Team %s and won the game!",
                    killResult.getTeam().getCodename());
              }
            } else {
              message = killResult.getError();
            }
          }
        } catch (DBAccessException e) {
          message = "An error occurred";
        } catch (InvalidAccessException | IllegalAccessError e) {
          message = e.getMessage();
        } catch (Exception e) {
          message = "Error: " + e.getMessage();
        }
      }

      return new Gson().toJson(ImmutableMap.of("success", success, "message", message));
    }
  }

  /**
   * Called when a user tries to surrender in a game.
   */
  public static class Surrender implements Route {
    @Override
    public String handle(Request req, Response res) {
      QueryParamsMap qm = req.queryMap();
      String rawGameID = qm.value("gameid");
      boolean success = false;
      String message;

      try {
        int gameID = Integer.parseInt(rawGameID);
        Game targetGame = Game.fromID(gameID);
        User currUser = SessionManager.getLoggedInUser(req);

        if (currUser == null) {
          message = "Must be logged in to perform this action.";

        } else {
          Player thisPlayer = Player.fromUserAndGame(currUser, targetGame);
          if (!thisPlayer.isAlive()) {
            message = "You are already dead. You can't surrender";
          } else if (targetGame.getStatus() == GameStatus.FORMING) {
            message = "You cannot surrender while the game is forming.";
          } else if (targetGame.getStatus() == GameStatus.DONE) {
            message = "Cannot surrender after the game has completed.";
          } else {
            thisPlayer.surrender();
            message = "Surrender successful.";
            success = true;
          }
        }
      } catch (DBAccessException e) {
        message = "An error occurred";
      } catch (InvalidAccessException | IllegalAccessError e) {
        message = e.getMessage();
      } catch (Exception e) {
        message = "Error: " + e.getMessage();
      }

      return new Gson().toJson(ImmutableMap.of("success", success, "message", message));
    }
  }
}
