package edu.brown.cs.assassin.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import edu.brown.cs.assassin.exception.DBAccessException;

/**
 * A Generic database proxy class, which handles db connection, query execution,
 * and caching.
 */
public class DBProxy {
  private Connection conn;

  /**
   * Constructor used to connect to database.
   *
   * @param filename the name of the database to connect to.
   * @throws DBAccessException if we cannot find the database.
   */
  public DBProxy(String filename) throws DBAccessException {
    try {
      Class.forName("org.sqlite.JDBC");
      String urlToDB = "jdbc:sqlite:" + filename;
      conn = DriverManager.getConnection(urlToDB);
    } catch (ClassNotFoundException | SQLException e) {
      throw new DBAccessException(e.getMessage());
    }
  }

  /**
   * Queries for DB connection.
   *
   * @return True if DB connected, false if not.
   */
  public boolean isConnected() {
    return conn != null;
  }

  /**
   * Disconnects the DB.
   */
  public void disconnect() {
    conn = null;
  }

  /**
   * Executes SQL Query directly.
   *
   * @param sqlCommand SQL Command.
   * @return SQL output.
   * @throws DBAccessException if we cannot find the database.
   */
  public List<List<String>> executeQuery(String sqlCommand) throws DBAccessException {
    return executeQuery(sqlCommand, new ArrayList<String>());
  }

  /**
   * Executes SQL Query with the specified parameters.
   *
   * @param sqlCommand SQL Command.
   * @param parameters Parameters to be placed into SQL Command.
   * @return SQL Output.
   * @throws DBAccessException if we cannot find the database.
   */
  public List<List<String>> executeQuery(String sqlCommand, List<String> parameters)
      throws DBAccessException {
    if (isConnected()) {
      try {
        List<List<String>> result = null;
        PreparedStatement prep = conn.prepareStatement(sqlCommand);
        for (int i = 0; i < parameters.size(); i++) {
          prep.setString(i + 1, parameters.get(i));
        }
        ResultSet rs = prep.executeQuery();
        ResultSetMetaData rsmd = rs.getMetaData();
        result = new ArrayList<>();
        // Create an array of array of strings from the DB.
        while (rs.next()) {
          ArrayList<String> row = new ArrayList<>();
          for (int i = 1; i < rsmd.getColumnCount() + 1; i++) {
            row.add("" + rs.getObject(i));
          }
          result.add(row);
        }
        prep.close();
        return result;
      } catch (SQLException e) {
        throw new DBAccessException(e.getMessage());
      }
    } else {
      throw new DBAccessException("Database not connected.");
    }
  }

  /**
   * Executes SQL Query to see if anything is returned.
   *
   * @param sqlCommand SQL Command.
   * @param parameters Parameters to be placed into SQL Command.
   * @return whether the given query returns any restuls.
   * @throws DBAccessException if we cannot find the database.
   */
  public boolean hasKey(String sqlCommand, List<String> parameters) throws DBAccessException {
    if (isConnected()) {
      try {
        boolean exists = false;
        PreparedStatement prep = conn.prepareStatement(sqlCommand);
        for (int i = 0; i < parameters.size(); i++) {
          prep.setString(i + 1, parameters.get(i));
        }
        ResultSet rs = prep.executeQuery();
        exists = rs.next();
        prep.close();
        return exists;
      } catch (SQLException e) {
        throw new DBAccessException(e.getMessage());
      }
    } else {
      throw new DBAccessException("Database not connected.");
    }
  }

  /**
   * Runs a SQL query to update a database.
   *
   * @param sqlCommand the command we wish to run.
   * @param parameters the list of parameters for this specific query.
   * @throws DBAccessException if we could not access the database.
   */
  public void updateDatabase(String sqlCommand, List<String> parameters) throws DBAccessException {
    if (isConnected()) {
      PreparedStatement prep = null;
      try {
        prep = conn.prepareStatement(sqlCommand);
        for (int i = 0; i < parameters.size(); i++) {
          prep.setString(i + 1, parameters.get(i));
        }
        prep.executeUpdate();
        prep.close();
      } catch (SQLException e) {
        throw new DBAccessException(e.getMessage());
      }
    } else {
      throw new DBAccessException("ERROR: Database not connected.");
    }
  }
}
