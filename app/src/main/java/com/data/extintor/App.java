package com.data.extintor;

import com.data.extintor.database.DefaultConnectionFactory;
import com.data.extintor.purge.PurgeThread;
import com.data.extintor.purge.ThreadConfig;
import com.data.extintor.statistics.StatisticsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

  private static final Logger log = LoggerFactory.getLogger(App.class);

  public static void main(String[] args) {
    App app = new App();
    app.runApp(args);
  }

  public void runApp(String[] args) {
    log.info(":: Data Extintor 1.0.0 :: Starting application");
    log.info("arguments expected: --file properties.yaml");

    if (args.length < 1) {
      throw new IllegalArgumentException("Expecting argument --file");
    }
    String file = extractFileParam(args[0]);

    ConfigurationLoader<ExtintorConfig> loader =
        new DefaultConfigurationLoader(ExtintorConfig.class);

    ExtintorConfig extintorConfig = loader.loadConfigurationFile(file);

    createPurgeThreads(
        extintorConfig, new DefaultConnectionFactory(extintorConfig.getConnectionConfig()));
  }

  private static void createPurgeThreads(
      ExtintorConfig extintorConfig, DefaultConnectionFactory factory) {
    int threadSize = extintorConfig.getPurgeThreadsList().size();
    if (threadSize < 1) {
      threwInvalidThreadConfiguration();
    }
    log.info("Starting {} threads to purge tables.", threadSize);

    StatisticsManager statisticsManager = new StatisticsManager();

    for (int i = 0; i < threadSize; i++) {
      ThreadConfig config = extintorConfig.getPurgeThreadsList().get(i);
      PurgeThread thread = new PurgeThread(config, factory, statisticsManager);
      thread.setDryRun(extintorConfig.isDryRun());
      thread.start();
      thread.joinCurrentThread();
    }
    statisticsManager.shutdown();
    statisticsManager.logStatistics();
  }

  private static void threwInvalidThreadConfiguration() {
    throw new IllegalArgumentException(
        "Configuration error: At least one thread configuration is required. "
            + "Define a thread in your YAML configuration like this:\n"
            + "threads:\n"
            + "  - tableName: [your_table_name]\n"
            + "    whereFilter: [your_where_filter]\n"
            + "    limit: [your_limit_number]\n"
            + "Replace [your_table_name], [your_where_filter], and [your_limit_number] with your specific values.");
  }

  private static String extractFileParam(String arg) {
    if (!arg.startsWith("--file=")) {
      throw new IllegalArgumentException("Invalid argument. Expected format: --file=[value]");
    }
    String[] parts = arg.split("=", 2);
    if (parts.length < 2) {
      throw new IllegalArgumentException("Invalid format for --file argument. No value specified.");
    }
    return parts[1];
  }
}
