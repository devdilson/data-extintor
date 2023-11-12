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

public class YamlConfigurationLoader implements ConfigurationLoader<ExtintorConfig> {

  private static final Logger log = LoggerFactory.getLogger(YamlConfigurationLoader.class);

  @Override
  public ExtintorConfig loadConfigurationFile(String file) {
    log.info(String.format("Loading file %s", file));
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

        log.info("Validation errors {}", errors);
      }

      return extintorConfig;
    } catch (IOException e) {
      String message = "Could not read the file: " + file;
      log.info(message);
      e.printStackTrace();
      throw new RuntimeException(message);
    }
  }

  public void loadLogSettings(ExtintorConfig config, String file) {
    File configFolder = new File(file).getAbsoluteFile().getParentFile();
    File settingsFile = new File(configFolder, config.getLogSettingsPath());
    if (!settingsFile.exists()) {
      throw new IllegalArgumentException("Could not load log settings: " + settingsFile);
    }
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
}
