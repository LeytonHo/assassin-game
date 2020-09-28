package edu.brown.cs.assassin.gui;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.TemplateViewRoute;

/**
 * UtilGUI class to easily produce ModelAndViews for error pages.
 */
public final class UtilGUI {

  private UtilGUI() {
  }

  /**
   * Renders a simple error page.
   *
   * @return a simple error page.
   */
  public static ModelAndView renderErrorPage() {
    return renderErrorPage("An error occurred");
  }

  /**
   * Renders an error page with the specified error.
   *
   * @param error the error to display on the page.
   * @return the error page.
   */
  public static ModelAndView renderErrorPage(String error) {
    Map<String, Object> variables = ImmutableMap.of("error", error);
    return new ModelAndView(variables, "error.ftl");
  }

  /**
   * Class to handle the game info page.
   */
  public static class GameInfo implements TemplateViewRoute {

    @Override
    public ModelAndView handle(Request request, Response response) throws Exception {
      String message = "Assassin is a live-action, multiplayer game where players try to "
              + "'assassinate' their targets and be the last one standing. Our online Assassin"
              + " game management tool allow users to host, generate, and run Assassin games "
              + "smoothly and efficiently.<br><br> Players can join games using a unique game code "
              + "distributed by the game's admin. From there, players can form teams or join "
              + "existing teams depending on the game settings. Once the game has started, players "
              + "are assigned targets that they must 'kill' using a specified method of "
              + "assassination "
              + "and obtain their kill code, following the rules set by the game admin. Players "
              + "can enter the kill code of their target on their game dashboard to register the "
              + "kill. Classically, the game continues until only one team is left standing. "
              + "<br><br>"
              + "Our tool provides admins with many game management settings. After creating a "
              + "game with settings like number of targets per team, team size, and team "
              + "anonymity, admins possess the ability to start the game, modify game settings, "
              + "message everyone in the game, revive players on living teams, shuffle targets, "
              + "and end the game prematurely. "
              + "<br><br> "
              + "We sincerely hope that you enjoy this game!";

      Map<String, Object> variables = ImmutableMap.of("header", "Using the Assassin Game App", "message",
          message);
      return new ModelAndView(variables, "message.ftl");
    }
  }
}
