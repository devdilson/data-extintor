package com.data.extintor;

import static ch.qos.logback.classic.ClassicConstants.CONFIG_FILE_PROPERTY;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

  private Logger rootLogger;

  public static void main(String[] args) {
    App app = new App();
    app.runApp(args);
  }

  public void runApp(String[] args) {
    System.out.println(":: Data Extintor 1.0.0 :: Starting application");
    System.out.println("arguments expected: --file properties.yaml");

    if (args.length < 1) {
      throw new IllegalArgumentException("Expecting argument --file");
    }
    String file = extractFileParam(args[0]);

    loadLogSettings(file);

    boolean isDryRun = args.length > 1 && extractDryRun(args[1]);

    ExtintorConfig extintorConfig = loadConfigurationFile(file);

    ConnectionFactory factory = new ConnectionFactory(extintorConfig.getConnectionConfig());

    int threadSize = extintorConfig.getPurgeThreadsList().size();
    if (threadSize < 1) {
      threwInvalidThreadConfiguration();
    }
    rootLogger.info("Starting {} threads to purge tables.", threadSize);

    StatisticsManager statisticsManager = new StatisticsManager();

    for (int i = 0; i < threadSize; i++) {
      ThreadConfig config = extintorConfig.getPurgeThreadsList().get(i);
      PurgeThread thread = new PurgeThread(config, factory, statisticsManager);
      thread.setTryRun(isDryRun);
      thread.start();
      joinCurrentThread(thread);
    }
    statisticsManager.shutdown();
    statisticsManager.logStatistics();
  }

  private void loadLogSettings(String file) {
    File configFolder = new File(file).getParentFile();
    File settingsFile = new File(configFolder, "logback.xml");
    if (!settingsFile.exists()) {
      throw new IllegalArgumentException("Could not load log settings: " + settingsFile);
    }
    System.setProperty(CONFIG_FILE_PROPERTY, settingsFile.toURI().getPath());
    rootLogger = LoggerFactory.getLogger(App.class);
  }

  private static void joinCurrentThread(PurgeThread thread) {
    try {
      thread.join();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Thread interrupted", e);
    }
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

  private ExtintorConfig loadConfigurationFile(String file) {
    rootLogger.info("Loading file {}", file);
    ObjectMapper objectMapper = new YAMLMapper();
    try {
      ExtintorConfig extintorConfig = objectMapper.readValue(new File(file), ExtintorConfig.class);
      ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
      Validator validator = factory.getValidator();
      Set<ConstraintViolation<ExtintorConfig>> constraintViolations =
          validator.validate(extintorConfig);

      if (!constraintViolations.isEmpty()) {
        StringBuilder errors = new StringBuilder();
        errors.append("Validation errors found: \n");

        for (ConstraintViolation<ExtintorConfig> violation : constraintViolations) {
          errors
              .append("Property '")
              .append(violation.getPropertyPath())
              .append("' ")
              .append(violation.getMessage())
              .append("; ")
              .append("\n");
        }

        rootLogger.error(errors.toString());
      }

      return extintorConfig;
    } catch (IOException e) {
      String message = "Could not read the file: " + file;
      rootLogger.error(message, e);
      throw new RuntimeException(message);
    }
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

  private static boolean extractDryRun(String arg) {
    if (arg.startsWith("--dry-run")) {
      return true;
    }
    return false;
  }
}
