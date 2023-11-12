package com.data.extintor.statistics;

import java.util.concurrent.atomic.AtomicInteger;

public class Statistics {

  private AtomicInteger recordsAffected = new AtomicInteger(0);

  public int getAffectedRecords() {
    return this.recordsAffected.get();
  }

  public int incrementAffectRecords(int records) {
    return recordsAffected.addAndGet(records);
  }
}
