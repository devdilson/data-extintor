package com.data.extintor.statistics;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatisticsManager {

	private static final Logger log = LoggerFactory.getLogger(StatisticsManager.class);

	private final Statistics purgeRowsStatistics;

	private final ScheduledExecutorService executorService;

	public StatisticsManager() {
		this.purgeRowsStatistics = new Statistics();
		this.executorService = Executors.newSingleThreadScheduledExecutor();
		this.executorService.scheduleAtFixedRate(this::logStatistics, 1, 1, TimeUnit.MINUTES);
	}

	public int incrementAffectRecords(int rows) {
		return this.purgeRowsStatistics.incrementAffectRecords(rows);
	}

	public void logStatistics() {
		log.info("Purged rows: {}", purgeRowsStatistics.getAffectedRecords());
	}

	public void shutdown() {
		executorService.shutdown();
	}
}
