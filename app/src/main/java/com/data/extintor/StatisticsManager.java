package com.data.extintor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatisticsManager {

  private Logger log = LoggerFactory.getLogger(StatisticsManager.class);

  private Statistics purgeRowsStatistics;

  public StatisticsManager() {
    this.purgeRowsStatistics = new Statistics();
  }

  public int incrementAffectRecords(int rows) {
    return this.purgeRowsStatistics.incrementAffectRecords(rows);
  }

  public void logStatistics() {
    log.info("Purged rows: {}", purgeRowsStatistics.getAffectedRecords());
  }
}
