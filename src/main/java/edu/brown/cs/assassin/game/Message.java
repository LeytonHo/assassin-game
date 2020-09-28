package edu.brown.cs.assassin.game;

import edu.brown.cs.assassin.database.DBMethods;
import edu.brown.cs.assassin.database.DBProxy;
import edu.brown.cs.assassin.exception.DBAccessException;
import edu.brown.cs.assassin.exception.InvalidAccessException;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import static edu.brown.cs.assassin.main.AssassinConstants.GAME_DATA_PATH;
import static edu.brown.cs.assassin.main.AssassinConstants.GAME_TEST_DATA_PATH;

/**
 * Represents a message within the context of a game.
 */
public class Message implements Identifiable {
  private static DBProxy db;
  private int id;
  private static BiMap<String, MessageType> types;

  static {
    types = HashBiMap.create();
    types.put("start", MessageType.START);
    types.put("win", MessageType.WIN);
    types.put("end", MessageType.END);
    types.put("change name", MessageType.CHANGE_NAME);
    types.put("change rules", MessageType.CHANGE_RULES);
    types.put("change anon", MessageType.CHANGE_ANON);
    types.put("change not anon", MessageType.CHANGE_NOT_ANON);
    types.put("change num targets", MessageType.CHANGE_NUM_TARGETS);
    types.put("new targets", MessageType.NEW_TARGETS);
    types.put("revive", MessageType.REVIVE);
    types.put("eliminate", MessageType.ELIMINATE);
    types.put("surrender", MessageType.SURRENDER);
    types.put("custom", MessageType.CUSTOM);
    types.put("none", MessageType.NONE);
  }

  // DATABASE CONNECTIONS ======================================================

  /**
   * Connects to the database used for storing actual user data.
   */
  public static void connectToMainDB() {
    try {
      db = new DBProxy(GAME_DATA_PATH);
    } catch (DBAccessException e) {
      System.err.println("Error connecting to database: " + e.getMessage());
    }
  }

  /**
   * Connects to the database used for storing test message data.
   */
  public static void connectToTestDB() {
    try {
      db = new DBProxy(GAME_TEST_DATA_PATH);
    } catch (DBAccessException e) {
      System.err.println("Error connecting to database: " + e.getMessage());
    }
  }

  // CONSTRUCTOR ===============================================================

  /**
   * Constructor to specify the unique id of this Message.
   *
   * @param id the unique id of this message.
   */
  public Message(int id) {
    this.id = id;
  }

  // GETTER METHODS ============================================================

  /**
   * @return ID of this message
   */
  @Override
  public int getID() {
    return id;
  }

  /**
   * @return Time this message was sent
   * @throws DBAccessException if something goes wrong with the database
   */
  public Instant getTime() throws DBAccessException {
    String timeString = db
            .executeQuery("select time from message where id = ?", DBMethods.parameters(id)).get(0)
            .get(0);
    return DBMethods.convertTime(timeString);
  }

  /**
   * @return the type of this message
   * @throws DBAccessException if something goes wrong with the database
   */
  public MessageType getType() throws DBAccessException {
    String typeString = db.executeQuery(
            "select type from message where id = ?", DBMethods.parameters(id))
            .get(0).get(0);
    return convertType(typeString);
  }

  /**
   * @return Game in which this message was sent
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if the database contains an invalid game ID
   */
  public Game getGame() throws DBAccessException, InvalidAccessException {
    String gameIDString = db.executeQuery(
            "select game from message where id = ?", DBMethods.parameters(id))
            .get(0).get(0);
    int gameID = Integer.parseInt(gameIDString);
    return Game.fromID(gameID);
  }

  /**
   * Returns a given content field of a message.
   *
   * @param index the index of the content field
   * @return the content in this field
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if there is no field with this ID and index
   */
  public String getField(int index) throws DBAccessException, InvalidAccessException {
    List<List<String>> queryResult = db.executeQuery(
            "select content from message_field where message = ? and field_index = ?",
            DBMethods.parameters(id, index));
    if (queryResult.isEmpty()) {
      throw new InvalidAccessException("No such message field.");
    }
    return queryResult.get(0).get(0);
  }

  // CREATING/FINDING MESSAGES =================================================

  /**
   * Adds a new message to the database.
   *
   * @param game   Game in which this message was sent
   * @param type   Ttype of the message
   * @param fields Content fields of the message
   * @return Message that was added
   * @throws DBAccessException if something goes wrong with the database
   */
  public static Message addMessage(Game game, MessageType type, String... fields)
          throws DBAccessException {
    String messageCommand = "insert into message (game, time, type) values (?, ?, ?)";
    List<String> messageParameters = DBMethods.parameters(game, DBMethods.timeString(),
            convertType(type));
    db.updateDatabase(messageCommand, messageParameters);

    List<List<String>> addedMessageQueryResult = db.executeQuery(
            "select last_insert_rowid() from message");
    int addedMessageID = Integer.parseInt(addedMessageQueryResult.get(0).get(0));

    String fieldCommand = String.format("insert into message_field "
                    + "(message, field_index, content) values (%d, ?, ?)", addedMessageID);
    for (int i = 0; i < fields.length; i++) {
      List<String> fieldParameters = DBMethods.parameters(i, fields[i]);
      db.updateDatabase(fieldCommand, fieldParameters);
    }

    return new Message(addedMessageID);
  }

  /**
   * Gets the Message with a given ID.
   *
   * @param id The ID
   * @return The message with this ID
   * @throws DBAccessException      if something goes wrong with the database
   * @throws InvalidAccessException if there is no message with this ID
   */
  public static Message fromID(int id) throws DBAccessException, InvalidAccessException {
    if (db.hasKey("select * from message where id = ?", DBMethods.parameters(id))) {
      return new Message(id);
    } else {
      throw new InvalidAccessException("There is no message with id " + id);
    }
  }

  // CONVERTING MESSAGE TYPES AND DATABASE STRINGS =============================

  /**
   * Converts MessageType to a String.
   *
   * @param type MessageType
   * @return converted String
   */
  public static String convertType(MessageType type) {
    return types.inverse().get(type);
  }

  /**
   * Converts a String to a MessageType.
   *
   * @param type String
   * @return converted MessageType
   */
  public static MessageType convertType(String type) {
    return types.get(type);
  }

  // OVERRIDE METHODS ==========================================================

  @Override
  public String toString() {
    return "Message{" + "id=" + id + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Message message = (Message) o;
    return id == message.id;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
