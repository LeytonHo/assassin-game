package edu.brown.cs.assassin.login;

import java.util.List;

import edu.brown.cs.assassin.exception.DBAccessException;
import edu.brown.cs.assassin.exception.InvalidAccessException;
import edu.brown.cs.assassin.game.Game;
import edu.brown.cs.assassin.game.User;
import spark.Request;

/**
 * Class that manages session details associated with Spark server requests,
 * such as setting an associated account (logging in), removing that account
 * (logging out), and retrieving information related to a session.
 *
 * @see edu.brown.cs.assassin.game.User
 * @see edu.brown.cs.assassin.gui.LoginGUI
 */
public final class SessionManager {

  public static final String EMAIL_ATTR = "username";
  public static final String NAME_ATTR = "name";
  public static final String ID_ATTR = "id";

  /**
   * Private Constructor.
   */
  private SessionManager() {
  }

  /**
   * Returns account of requester OR null if no one is logged in.
   *
   * @param req The request to get session from
   * @return User object for requester
   */
  public static User getLoggedInUser(Request req) {
    if (req.session().attribute(EMAIL_ATTR) != null) {
      return new User(req.session().attribute(ID_ATTR));
    }
    return null;
  }

  /**
   * Takes a User object and sets this user as the logged in user for this
   * session.
   *
   * @param req Session request
   * @param u   User
   * @throws DBAccessException if we cannot connect to the database.
   */
  public static void setLoggedInUser(Request req, User u) throws DBAccessException {
    req.session().attribute(EMAIL_ATTR, u.getEmail());
    req.session().attribute(NAME_ATTR, u.getName());
    req.session().attribute(ID_ATTR, u.getID());
  }

  /**
   * Remove logged in user for this session.
   *
   * @param req Session request
   */
  public static void removeLoggedInUser(Request req) {
    req.session().removeAttribute(EMAIL_ATTR);
    req.session().removeAttribute(NAME_ATTR);
    req.session().removeAttribute(ID_ATTR);
  }

  /**
   * Get most recent game for user currently logged in.
   *
   * @param req Session request
   * @return Game ID
   */
  public static String getRecentGamePathForUser(Request req) {
    User acct = getLoggedInUser(req);
    if (acct == null) {
      return null;
    }

    try {
      List<Game> games = acct.getPlayingGames();
      games.addAll(acct.getAdminGames());
      if (games.size() == 0) {
        return null;
      } else {
        int gameID = games.get(0).getID();
        return Integer.toString(gameID);
      }
    } catch (DBAccessException | InvalidAccessException e) {
      return null;
    }
  }

}
