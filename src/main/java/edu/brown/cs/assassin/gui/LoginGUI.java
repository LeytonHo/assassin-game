package edu.brown.cs.assassin.gui;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import edu.brown.cs.assassin.exception.DBAccessException;
import edu.brown.cs.assassin.exception.InvalidAccessException;
import edu.brown.cs.assassin.exception.InvalidActionException;
import edu.brown.cs.assassin.game.User;
import edu.brown.cs.assassin.main.Main;
import edu.brown.cs.assassin.login.SessionManager;
import spark.ModelAndView;
import spark.QueryParamsMap;
import spark.Request;
import spark.Response;
import spark.TemplateViewRoute;

/**
 * Stores GUI information about the possible commands for logging in and out.
 */
public class LoginGUI {

  /**
   * Handle requests to the front page of the login screen.
   */
  public static class LoginScreenHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) {
      Map<String, Object> variables = ImmutableMap.of("title", "Assassin Game: Login",
          "loginMessage", "");
      return new ModelAndView(variables, "login.ftl");
    }
  }

  /**
   * Handle requests to the front page of the account creation screen.
   */
  public static class AccountCreateLanding implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) {
      Map<String, Object> variables = ImmutableMap.of("title", "Assassin Game: Login",
          "creationMessage", "");
      return new ModelAndView(variables, "createaccount.ftl");
    }
  }

  /**
   * Handle requests for when the user tries to login.
   */
  public static class AccountLogin implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) {

      // Grab account details from login
      QueryParamsMap qm = req.queryMap();
      String email = qm.value("username");
      String password = qm.value("password");

      // Try accessing account using details
      User account = null;
      String output = "";
      try {
        account = User.attemptLogin(email, password);
      } catch (InvalidAccessException e) {
        output = e.getMessage();
      } catch (Exception e) {
        output = "An error occurred";
      }

      // If account was found and password matches, direct to homepage/dashboard
      try {
        if (account != null) {
          SessionManager.setLoggedInUser(req, account);
          res.redirect(Main.HOME_PAGE);
          return null;
        }
      } catch (Exception e) {
        output = "An error occurred";
      }

      // Else, direct back to login page with error message
      Map<String, Object> variables = ImmutableMap.of("title", "Assassin Game: Login",
          "loginMessage", output);
      return new ModelAndView(variables, "login.ftl");
    }
  }

  /**
   * Handle requests for when the user wants to create an account.
   */
  public static class AccountCreate implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) {

      // Grab account creation details
      QueryParamsMap qm = req.queryMap();
      String output = "";
      String username = qm.value("username");
      String email = qm.value("email");
      String password = qm.value("password");
      String verify = qm.value("verify");

      try {

        // If password matches, register account, and report success
        if (password.equals(verify)) {
          User newUser = User.addUser(email, username, password);
          SessionManager.setLoggedInUser(req, newUser);
          Map<String, Object> variables = ImmutableMap.of("header", "Account created successfully!",
              "message", "Click below to proceed to your new account.");
          return new ModelAndView(variables, "message.ftl");
        } else {
          output = "Password did not match";
        }

      } catch (InvalidActionException e) {
        output = e.getMessage();
      } catch (DBAccessException e) {
        output = "An error occurred";
      }
      Map<String, Object> variables = ImmutableMap.of("title", "Assassin Game: Login",
          "creationMessage", output);
      return new ModelAndView(variables, "createaccount.ftl");
    }
  }

  /**
   * Handle requests for when the user tries to logout.
   */
  public static class Logout implements TemplateViewRoute {

    @Override
    public ModelAndView handle(Request request, Response response) {
      SessionManager.removeLoggedInUser(request);
      response.redirect(Main.LANDING_PAGE);
      return null;
    }
  }

  /**
   * Homepage for resetting passwords.
   */
  public static class PasswordResetLanding implements TemplateViewRoute {

    @Override
    public ModelAndView handle(Request request, Response response) throws Exception {
      Map<String, Object> variables = ImmutableMap.of("title", "Password Reset", "message", "");
      return new ModelAndView(variables, "resetpassword.ftl");
    }
  }

  /**
   * Handles requests to reset the user's password.
   */
  public static class PasswordReset implements TemplateViewRoute {

    @Override
    public ModelAndView handle(Request request, Response response) throws Exception {

      // Grab account creation details
      QueryParamsMap qm = request.queryMap();
      String output = "";
      String email = qm.value("email");
      String current = qm.value("current");
      String newPass = qm.value("new");
      String verify = qm.value("verify");

      try {

        if (newPass.equals(verify)) {
          User updater = User.attemptLogin(email, current);
          User.resetPassword(updater.getID(), newPass);
          output = "Password updated successfully";
        } else {
          output = "Password did not match";
        }

      } catch (InvalidAccessException e) {
        output = e.getMessage();
      } catch (DBAccessException e) {
        output = "An error occurred";
      }

      Map<String, Object> variables = ImmutableMap.of("title", "Password Reset", "message", output);
      return new ModelAndView(variables, "resetpassword.ftl");
    }
  }
}
