package edu.brown.cs.assassin.gui;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;

import edu.brown.cs.assassin.exception.DBAccessException;
import edu.brown.cs.assassin.exception.InvalidAccessException;
import edu.brown.cs.assassin.exception.InvalidActionException;
import edu.brown.cs.assassin.game.Game;
import edu.brown.cs.assassin.game.GameStatus;
import edu.brown.cs.assassin.game.Message;
import edu.brown.cs.assassin.game.MessageType;
import edu.brown.cs.assassin.game.User;
import edu.brown.cs.assassin.login.SessionManager;
import spark.QueryParamsMap;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Static class for storing all of the GUI handlers for the actions
 * unique and specific to game admins, such as changing game settings or
 * reshuffling targets.
 */
public final class GameAdminGUI {

  private GameAdminGUI() { }

  /**
   * Handles a game start request, and returns a JSON response.
   */
  public static class StartGameHandler implements Route {
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

        } else if (!targetGame.getAdmin().contains(currUser)) {
          message = "Must be a game admin to perform this action.";

        } else if (targetGame.getStatus() != GameStatus.FORMING) {
          message = "Game must be forming to start.";
        } else {
          targetGame.start();
          success = true;
          message = "Success! Game started and targets assigned.";
        }
      } catch (

      DBAccessException e) {
        message = "An error occurred";
      } catch (InvalidAccessException | InvalidActionException | IllegalAccessError e) {
        message = e.getMessage();
      } catch (Exception e) {
        message = "Error: " + e.getMessage();
      }

      return new Gson().toJson(ImmutableMap.of("success", success, "message", message));
    }
  }

  /**
   * Handles requests to change a field of a game, and returns a JSON response.
   */
  public static class ChangeField implements Route {
    @Override
    public String handle(Request req, Response res) {
      // Extracts from website
      QueryParamsMap qm = req.queryMap();
      String rawGameID = qm.value("gameid");
      String newVal = qm.value("body");
      String action = qm.value("action");
      boolean success = false;
      String message = "";

      // Validates whether the change can happen
      if (!action.equals("rules") && (newVal == null || newVal.equals(""))) {
        if (action.equals("name")) {
          message = "Please enter a valid name.";
        } else if (action.equals("targets")) {
          message = "Please enter a valid number of targets.";
        } else if (action.equals("anon")) {
          message = "Please enter a valid anonymity status.";
        }
      } else {
        try {
          int gameID = Integer.parseInt(rawGameID);
          Game targetGame = Game.fromID(gameID);
          User currUser = SessionManager.getLoggedInUser(req);

          // Checks the different actions the user may have submitted and performs the
          // appropriate actions
          if (currUser == null) {
            message = "Must be logged in to perform this action.";
          } else if (!targetGame.getAdmin().contains(currUser)) {
            message = "Must be a game admin to perform this action.";
          } else {
            if (action.equals("name")) {
              String oldName = targetGame.getName();
              targetGame.changeName(newVal);
              message = "Game name changed from " + oldName + " to " + newVal
                  + ". Refresh to see changes.";
              success = true;
            } else if (action.equals("rules")) {
              if (newVal == null) {
                newVal = "";
              }
              targetGame.changeRules(newVal);
              message = "Rules updated successfully. Refresh to see changes.";
              success = true;
            } else if (action.equals("targets")) {
              int targetsInt = Integer.parseInt(newVal);
              int currTargets = targetGame.getNumTargets();
              targetGame.changeNumTargets(targetsInt);
              message = "Number of targets modified from " + currTargets + "  to " + targetsInt;
              success = true;
            } else if (action.equals("anon")) {
              boolean anon = newVal.equals("true");
              targetGame.changeAnonymity(anon);
              message = anon ? "Players are now anonymous" : "Players are now not anonymous";
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
      }
      return new Gson().toJson(ImmutableMap.of("success", success, "message", message));
    }
  }

  /**
   * Handles requests to perform actions to manipulate the game.
   */
  public static class ManipulateGame implements Route {

    @Override
    public Object handle(Request req, Response res) throws Exception {
      // Extracting from website
      QueryParamsMap qm = req.queryMap();
      String rawGameID = qm.value("gameid");
      String action = qm.value("action");
      boolean success = false;
      String message = "";

      try {
        // Getting game and user info
        int gameID = Integer.parseInt(rawGameID);
        Game targetGame = Game.fromID(gameID);
        User currUser = SessionManager.getLoggedInUser(req);

        // Checks the different actions the user may have submitted and performs the
        // appropriate actions
        if (currUser == null) {
          message = "Must be logged in to perform this action.";
        } else if (targetGame.getStatus() == GameStatus.FORMING) {
          message = "Must start game to perform this action.";
        } else if (!targetGame.getAdmin().contains(currUser)) {
          message = "Must be a game admin to perform this action.";
        } else {
          if (action.equals("revive")) {
            message = "All players on living teams revived.";
            targetGame.revivePlayersOnLivingTeams();
            success = true;
          } else if (action.equals("end")) {
            message = "Game ended with no winner.";
            targetGame.endWithNoWinner();
            success = true;
          } else if (action.equals("shuffle")) {
            message = "Target shuffled successfully.";
            targetGame.generateNewTargets();
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

  /**
   * Handles requests to email blast the players.
   */
  public static class SendEmail implements Route {

    @Override
    public Object handle(Request req, Response res) throws Exception {
      QueryParamsMap qm = req.queryMap();
      String rawGameID = qm.value("gameid");
      String subject = qm.value("subject");
      String body = qm.value("body");
      String email = qm.value("email");
      boolean success = false;
      String message;

      try {
        int gameID = Integer.parseInt(rawGameID);
        Game targetGame = Game.fromID(gameID);
        User currUser = SessionManager.getLoggedInUser(req);

        if (currUser == null) {
          message = "Must be logged in to perform this action.";
        } else {
          message = "Message posted succesfully to livefeed.";
          String messenger = "<b>" + subject + "</b><br>" + body;
          Message.addMessage(targetGame, MessageType.CUSTOM, messenger);
          success = true;
          if (email.equals("true")) {
            if (targetGame.emailBlast(subject, body)) {
              message = "Email sent successfully to everyone in this game.";
            } else {
              message = "Email could not send. Message posted anyway.";
              success = false;
            }
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
