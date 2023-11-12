package com.data.extintor;

import static ch.qos.logback.classic.ClassicConstants.CONFIG_FILE_PROPERTY;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
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

/**
 * The DefaultConfigurationLoader class is responsible for loading, validating, and applying
 * configurations from a YAML file into an ExtintorConfig object. It implements the
 * ConfigurationLoader interface specifically for ExtintorConfig.
 */
public class DefaultConfigurationLoader implements ConfigurationLoader<ExtintorConfig> {

  private static final Logger log = LoggerFactory.getLogger(DefaultConfigurationLoader.class);

  private final ValidatorFactory validatorFactory;

  private final ObjectMapper objectMapper;

  public DefaultConfigurationLoader(ValidatorFactory validatorFactory, ObjectMapper objectMapper) {
    this.validatorFactory = validatorFactory;
    this.objectMapper = objectMapper;
  }

  public DefaultConfigurationLoader() {
    this(Validation.buildDefaultValidatorFactory(), new YAMLMapper());
  }

  /**
   * Loads the configuration from the specified object mapper and validates it.
   *
   * @param file The path to the YAML configuration file.
   * @return The ExtintorConfig object populated with the loaded configuration.
   * @throws RuntimeException If the file cannot be read or if validation errors are found.
   */
  @Override
  public ExtintorConfig loadConfigurationFile(String file) {
    log.info(String.format("Loading file %s", file));
    try {
      ExtintorConfig extintorConfig = objectMapper.readValue(new File(file), ExtintorConfig.class);
      Validator validator = validatorFactory.getValidator();
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

        log.info("Validation errors {}", errors);
      }
      loadLogSettings(extintorConfig, file);
      return extintorConfig;
    } catch (IOException e) {
      String message = "Could not read the file: " + file;
      log.info(message);
      e.printStackTrace();
      throw new RuntimeException(message);
    }
  }

  /**
   * Loads logging settings from the configuration and applies them.
   *
   * @param config The ExtintorConfig object containing the log settings path.
   * @param file The path to the configuration file.
   */
  private void loadLogSettings(ExtintorConfig config, String file) {
    var settingsFile = loadLogSettingsFile(config, file);
    reloadVendorLogSettings(settingsFile);
  }

  /**
   * Reloads vendor-specific logging settings from the given settings file.
   *
   * @param settingsFile The file containing log settings.
   * @throws RuntimeException If unable to reload log settings.
   */
  private void reloadVendorLogSettings(File settingsFile) {
    System.setProperty(CONFIG_FILE_PROPERTY, settingsFile.getPath());
    LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
    loggerContext.reset();
    ContextInitializer ci = new ContextInitializer(loggerContext);
    try {
      ci.autoConfig();
    } catch (JoranException e) {
      throw new RuntimeException("Could not reload log", e);
    }
  }

  /**
   * Validates if the provided settings file exists.
   *
   * @param settingsFile The file to be validated.
   * @throws IllegalArgumentException If the settings file does not exist.
   */
  private void validateSettingsFile(File settingsFile) {
    if (!settingsFile.exists()) {
      throw new IllegalArgumentException("Could not load log settings: " + settingsFile);
    }
  }

  /**
   * Loads the log settings file based on the configuration and file path.
   *
   * @param config The ExtintorConfig object containing the log settings path.
   * @param file The path to the configuration file.
   * @return The File object pointing to the log settings file.
   */
  private File loadLogSettingsFile(ExtintorConfig config, String file) {
    File configFolder = new File(file).getAbsoluteFile().getParentFile();
    File settingsFile = new File(configFolder, config.getLogSettingsPath());
    validateSettingsFile(settingsFile);
    return settingsFile;
  }
}
