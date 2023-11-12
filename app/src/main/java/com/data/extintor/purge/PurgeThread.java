package com.data.extintor.purge;

import com.data.extintor.database.DefaultConnectionFactory;
import com.data.extintor.statistics.StatisticsManager;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PurgeThread extends Thread {

  private static final Logger log = LoggerFactory.getLogger(PurgeThread.class);

  private final ThreadConfig config;

  private final DefaultConnectionFactory defaultConnectionFactory;

  private final StatisticsManager statisticsManager;

  private boolean isDryRun;

  public PurgeThread(
      ThreadConfig config,
      DefaultConnectionFactory defaultConnectionFactory,
      StatisticsManager statisticsManager) {
    this.config = config;
    this.defaultConnectionFactory = defaultConnectionFactory;
    this.statisticsManager = statisticsManager;
    this.setName(config.getName());
  }

  public void setDryRun(boolean isDryRun) {
    this.isDryRun = isDryRun;
  }

  @Override
  public void run() {
    try (var con = defaultConnectionFactory.getConnection()) {
      String deleteQuery = createDeleteQuery(config);
      log.info("Delete Query: {}", deleteQuery);
      String selectQuery = createSelectQuery(config);
      log.info("Select Query: {}", selectQuery);
      if (isDryRun) {
        log.info("Ran in dry-run mode.");
        return;
      }
      con.setAutoCommit(false);
      boolean hasMoreRecords = false;
      int errorCount = 0;
      do {
        try (var selectStmt = con.prepareStatement(selectQuery);
            var deleteStmt = con.prepareStatement(deleteQuery);
            var rs = selectStmt.executeQuery()) {
          if (errorCount > 3) {
            log.error("Too many errors when running the queries, aborting thread");
            return;
          }
          rs.next();
          hasMoreRecords = rs.getInt(1) > 0;
          int deletedRows = deleteStmt.executeUpdate();
          statisticsManager.incrementAffectRecords(deletedRows);
          con.commit();
          log.debug("Deleted {} rows.", deletedRows);
        } catch (SQLException ex) {
          log.error("Error running query", ex);
          errorCount++;
          con.rollback();
        }

      } while (hasMoreRecords);

    } catch (SQLException e) {
      log.error("Could not execute query", e);
    }
  }

  public void joinCurrentThread() {
    try {
      join();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Thread interrupted", e);
    }
  }

  private static String createSelectQuery(ThreadConfig config) {
    return String.format(
        "SELECT EXISTS(SELECT 1 FROM %s %s LIMIT %s)",
        config.getTableName(), config.getWhereFilter(), config.getLimit());
  }

  private static String createDeleteQuery(ThreadConfig config) {
    return String.format(
        "DELETE FROM %s %s LIMIT %s",
        config.getTableName(), config.getWhereFilter(), config.getLimit());
  }
}
