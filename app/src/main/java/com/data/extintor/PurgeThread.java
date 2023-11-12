package com.data.extintor;

import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PurgeThread extends Thread {

  private static final Logger log = LoggerFactory.getLogger(PurgeThread.class);

  private final ThreadConfig config;
  private final ConnectionFactory connectionFactory;
  private final StatisticsManager statisticsManager;

  public PurgeThread(
      ThreadConfig config,
      ConnectionFactory connectionFactory,
      StatisticsManager statisticsManager) {
    this.config = config;
    this.connectionFactory = connectionFactory;
    this.statisticsManager = statisticsManager;
    this.setName(config.getName());
  }

  @Override
  public void run() {
    try (var con = connectionFactory.getConnection()) {
      String deleteQuery = createDeleteQuery();
      String selectQuery = createSelectQuery();
      con.setAutoCommit(false);
      boolean hasMoreRecords = false;
      do {
        try (var selectStmt = con.prepareStatement(selectQuery);
            var deleteStmt = con.prepareStatement(deleteQuery);
            var rs = selectStmt.executeQuery()) {
          rs.next();
          hasMoreRecords = rs.getInt(1) > 0;
          int deletedRows = deleteStmt.executeUpdate();
          statisticsManager.incrementAffectRecords(deletedRows);
          log.debug("Deleted {} rows.", deletedRows);
        } catch (SQLException ex) {
          log.error("Error running query", ex);
          con.rollback();
        } finally {
          con.commit();
        }

      } while (hasMoreRecords);

    } catch (SQLException e) {
      log.error("Could not execute query", e);
    }
  }

  private String createSelectQuery() {
    String selectQuery =
        String.format(
            "SELECT EXISTS(SELECT 1 FROM %s %s LIMIT %s)",
            config.getTableName(), config.getWhereFilter(), config.getLimit());
    log.info("Select Query: {}", selectQuery);
    return selectQuery;
  }

  private String createDeleteQuery() {
    String deleteQuery =
        String.format(
            "DELETE FROM %s %s LIMIT %s",
            config.getTableName(), config.getWhereFilter(), config.getLimit());
    log.info("Delete Query: {}", deleteQuery);
    return deleteQuery;
  }
}
