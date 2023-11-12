package com.data.extintor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PurgeThread extends Thread {

  private static final Logger log = LoggerFactory.getLogger(PurgeThread.class);

  private final ThreadConfig config;
  private final ConnectionFactory connectionFactory;

  public PurgeThread(ThreadConfig config, ConnectionFactory connectionFactory) {
    this.config = config;
    this.connectionFactory = connectionFactory;
  }

  @Override
  public void run() {
    try (Connection con = connectionFactory.getConnection()) {
      String deleteQuery =
          String.format(
              "DELETE FROM %s %s %s",
              config.getTableName(), config.getWhereFilter(), config.getLimit());
      log.info("Delete Query: {}", deleteQuery);
      String selectQuery =
          String.format(
              "SELECT * FROM %s %s %s",
              config.getTableName(), config.getWhereFilter(), config.getLimit());
      log.info("Select Query: {}", selectQuery);

      while (true) {
        try (PreparedStatement selectStmt = con.prepareStatement(selectQuery);
            ResultSet rs = selectStmt.executeQuery()) {
          if (!rs.next()) {
            log.info("No more records to delete. Exiting.");
            break;
          }
        }
        try (PreparedStatement deleteStmt = con.prepareStatement(deleteQuery)) {
          int deletedRows = deleteStmt.executeUpdate();
          log.info("Deleted {} rows.", deletedRows);
        }
      }
    } catch (SQLException e) {
      log.error("Could not execute query", e);
    }
  }
}
