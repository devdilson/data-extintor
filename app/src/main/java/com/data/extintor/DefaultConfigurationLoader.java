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

public class DefaultConfigurationLoader<T extends BaseConfig> implements ConfigurationLoader<T> {

  private static final Logger log = LoggerFactory.getLogger(DefaultConfigurationLoader.class);

  private final ValidatorFactory validatorFactory;

  private final ObjectMapper objectMapper;

  private final Class<?> targetConfigType;

  public DefaultConfigurationLoader(
      ValidatorFactory validatorFactory, ObjectMapper objectMapper, Class<?> targetConfigType) {
    this.validatorFactory = validatorFactory;
    this.objectMapper = objectMapper;
    this.targetConfigType = targetConfigType;
  }

  public DefaultConfigurationLoader(Class<?> targetConfigType) {
    this(Validation.buildDefaultValidatorFactory(), new YAMLMapper(), targetConfigType);
  }

  @Override
  public T loadConfigurationFile(String file) {
    log.info(String.format("Loading file %s", file));
    try {
      T config = (T) objectMapper.readValue(new File(file), targetConfigType);
      Validator validator = validatorFactory.getValidator();
      Set<ConstraintViolation<T>> constraintViolations = validator.validate(config);

      if (!constraintViolations.isEmpty()) {
        StringBuilder errors = new StringBuilder();
        errors.append("Validation errors found: \n");

        for (ConstraintViolation<T> violation : constraintViolations) {
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
      loadLogSettings(config, file);
      return config;
    } catch (IOException e) {
      String message = "Could not read the file: " + file;
      log.info(message);
      e.printStackTrace();
      throw new RuntimeException(message);
    }
  }

  private void loadLogSettings(T config, String file) {
    var settingsFile = loadLogSettingsFile(config, file);
    reloadVendorLogSettings(settingsFile);
  }

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

  private void validateSettingsFile(File settingsFile) {
    if (!settingsFile.exists()) {
      throw new IllegalArgumentException("Could not load log settings: " + settingsFile);
    }
  }

  private File loadLogSettingsFile(T config, String file) {
    File configFolder = new File(file).getAbsoluteFile().getParentFile();
    File settingsFile = new File(configFolder, config.getLogSettingsPath());
    validateSettingsFile(settingsFile);
    return settingsFile;
  }
}
