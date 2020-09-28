package edu.brown.cs.assassin.main;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import edu.brown.cs.assassin.database.DBMethods;
import edu.brown.cs.assassin.exception.DBAccessException;
import edu.brown.cs.assassin.gui.GameAdminGUI;
import edu.brown.cs.assassin.gui.LoginGUI;
import edu.brown.cs.assassin.gui.UserGUI;
import edu.brown.cs.assassin.gui.UtilGUI;
import freemarker.template.Configuration;
import spark.ExceptionHandler;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.TemplateViewRoute;
import spark.template.freemarker.FreeMarkerEngine;

/**
 * Main class to be run when the program is run. Handles all actions that occur
 * within the game of Assassin.
 */
public final class Main {

  private static final int DEFAULT_PORT = 4567;

  // URL PATHS
  public static final String LANDING_PAGE = "/";
  public static final String LOGIN_PAGE = "/login";
  public static final String NEW_ACCT_PAGE = "/create-account";
  public static final String LOGOUT_POST = "/logout";
  public static final String HOME_PAGE = "/home";
  public static final String GAME_PAGE = "/game/:gameID";
  public static final String GAME_PAGE_REDIR = "/game/";
  public static final String CREATE_GAME = "/create-game";
  public static final String RESET_PASSWORD = "/reset-password";

  /**
   * The initial method called when execution begins.
   *
   * @param args An array of command line arguments
   * @throws DBAccessException if the databases cannot be reached.
   */
  public static void main(String[] args) throws DBAccessException {
    new Main(args).run();
  }

  private Main(String[] args) {
  }

  private void run() {
    DBMethods.connectToMainDB();
    runSparkServer();
  }

  static int getHerokuAssignedPort() {
    ProcessBuilder processBuilder = new ProcessBuilder();
    if (processBuilder.environment().get("PORT") != null) {
      return Integer.parseInt(processBuilder.environment().get("PORT"));
    }
    return DEFAULT_PORT; // return default port if heroku-port isn't set (i.e. on localhost)
  }

  private static FreeMarkerEngine createEngine() {
    Configuration config = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
    File templates = new File("src/main/resources/spark/template/freemarker");
    try {
      config.setDirectoryForTemplateLoading(templates);
    } catch (IOException ioe) {
      System.out.printf("ERROR: Unable use %s for template loading.%n", templates);
      System.exit(1);
    }
    return new FreeMarkerEngine(config);
  }

  private void runSparkServer() {
    Spark.port(getHerokuAssignedPort());
    Spark.externalStaticFileLocation("src/main/resources/static");
    Spark.exception(Exception.class, new ExceptionPrinter());

    FreeMarkerEngine freeMarker = createEngine();

    // Setup Spark Routes
    // Login routes
    Spark.get(LANDING_PAGE, new SplashPageHandler(), freeMarker);
    Spark.post(LANDING_PAGE, new SplashPageHandler(), freeMarker); // FOR REDIRECTION

    Spark.get(LOGIN_PAGE, new LoginGUI.LoginScreenHandler(), freeMarker);
    Spark.post(LOGIN_PAGE, new LoginGUI.AccountLogin(), freeMarker);

    Spark.post(NEW_ACCT_PAGE, new LoginGUI.AccountCreate(), freeMarker);
    Spark.get(NEW_ACCT_PAGE, new LoginGUI.AccountCreateLanding(), freeMarker);

    Spark.get(RESET_PASSWORD, new LoginGUI.PasswordResetLanding(), freeMarker);
    Spark.post(RESET_PASSWORD, new LoginGUI.PasswordReset(), freeMarker);

    Spark.get(LOGOUT_POST, new LoginGUI.Logout(), freeMarker);
    Spark.post(LOGOUT_POST, new LoginGUI.Logout(), freeMarker);

    // Main pages
    Spark.post(HOME_PAGE, new UserGUI.UserHome(), freeMarker);
    Spark.get(HOME_PAGE, new UserGUI.UserHome(), freeMarker);

    Spark.post(GAME_PAGE, new UserGUI.UserGame(), freeMarker);
    Spark.get(GAME_PAGE, new UserGUI.UserGame(), freeMarker);

    // Game joining and creation
    Spark.get(CREATE_GAME, new UserGUI.CreateGame(), freeMarker);
    Spark.post(CREATE_GAME, new UserGUI.CreateGame(), freeMarker);

    Spark.get("/join-game", new UserGUI.JoinGame(), freeMarker);
    Spark.post("/create-team", new UserGUI.CreateTeam(), freeMarker);
    Spark.post("/join-team", new UserGUI.JoinExistingTeam(), freeMarker);

    // User controls
    Spark.post("/check-game-code", new UserGUI.GameCodeHandler());
    Spark.post("/check-kill-code", new UserGUI.KillRegistrationHandler());
    Spark.post("/surrender", new UserGUI.Surrender());

    // Game master controls
    Spark.post("/change-name", new GameAdminGUI.ChangeField());
    Spark.post("/change-rules", new GameAdminGUI.ChangeField());
    Spark.post("/change-targets", new GameAdminGUI.ChangeField());
    Spark.post("/change-anon", new GameAdminGUI.ChangeField());
    Spark.post("/revive", new GameAdminGUI.ManipulateGame());
    Spark.post("/end-game", new GameAdminGUI.ManipulateGame());
    Spark.post("/shuffle", new GameAdminGUI.ManipulateGame());
    Spark.post("/send-email", new GameAdminGUI.SendEmail());
    Spark.post("/check-start-game", new GameAdminGUI.StartGameHandler());

    Spark.get("/game-info", new UtilGUI.GameInfo(), freeMarker);
    Spark.get("*", new NotFoundHandler(), freeMarker);
  }

  /**
   * Display an error page when an exception occurs in the server.
   */
  private static class ExceptionPrinter implements ExceptionHandler<Exception> {
    @Override
    public void handle(Exception e, Request req, Response res) {
      res.status(500);
      StringWriter stacktrace = new StringWriter();
      try (PrintWriter pw = new PrintWriter(stacktrace)) {
        pw.println("<pre>");
        e.printStackTrace(pw);
        pw.println("</pre>");
      }
      res.body(stacktrace.toString());
      // Redirects to home page on error & prints error to stderr
      e.printStackTrace(System.err);
      res.redirect(HOME_PAGE);
    }
  }

  /**
   * Handle requests to the landing page of the website.
   */
  public static class SplashPageHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) {
      Map<String, Object> variables = ImmutableMap.of("title", "Assassin Game Online", "loginURL",
          LOGIN_PAGE, "signupURL", NEW_ACCT_PAGE);
      return new ModelAndView(variables, "index.ftl");
    }
  }

  /**
   * Handle requests to pages that do not exist.
   */
  public static class NotFoundHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) {
      Map<String, Object> variables = ImmutableMap.of("error",
          "Oops! The page you're looking for doesn't exist.");
      return new ModelAndView(variables, "error.ftl");
    }
  }
}
