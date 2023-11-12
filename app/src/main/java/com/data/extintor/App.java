package com.data.extintor;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.qos.logback.classic.ClassicConstants.CONFIG_FILE_PROPERTY;

public class App {

	private static final Logger rootLogger = LoggerFactory.getLogger(App.class);

	public static void main(String[] args) {
		App app = new App();
		app.runApp(args);
	}


	public void runApp(String[] args) {
		rootLogger.info(":: Data Extintor 1.0.0 :: Starting application");
		rootLogger.info("arguments expected: --file properties.yaml");

		if (args.length < 1) {
			throw new IllegalArgumentException("Expecting argument --file");
		}
		String file = extractFileParam(args[0]);

		boolean isDryRun = args.length > 1 && extractDryRun(args[1]);

		ExtintorConfig extintorConfig = loadConfigurationFile(file);
		loadLogSettings(extintorConfig, file);

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

	private ExtintorConfig loadConfigurationFile(String file) {
		rootLogger.info(String.format("Loading file %s", file));
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

				rootLogger.info("Validation errors {}", errors);
			}

			return extintorConfig;
		}
		catch (IOException e) {
			String message = "Could not read the file: " + file;
			rootLogger.info(message);
			e.printStackTrace();
			throw new RuntimeException(message);
		}
	}

	private static void joinCurrentThread(PurgeThread thread) {
		try {
			thread.join();
		}
		catch (InterruptedException e) {
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

	private void loadLogSettings(ExtintorConfig config, String file) {
		File configFolder = new File(file).getAbsoluteFile().getParentFile();
		File settingsFile = new File(configFolder, config.getLogSettingsPath());
		if (!settingsFile.exists()) {
			throw new IllegalArgumentException("Could not load log settings: " + settingsFile);
		}
		reloadVendorLogSettings(settingsFile);
	}

	private static void reloadVendorLogSettings(File settingsFile) {
		System.setProperty(CONFIG_FILE_PROPERTY, settingsFile.getPath());
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		loggerContext.reset();
		ContextInitializer ci = new ContextInitializer(loggerContext);
		try {
			ci.autoConfig();
		}
		catch (JoranException e) {
			throw new RuntimeException("Could not reload log", e);
		}
	}

}
