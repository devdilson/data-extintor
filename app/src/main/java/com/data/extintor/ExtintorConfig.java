package com.data.extintor;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public class ExtintorConfig {

	@JsonProperty("log-settings-path")
	private String logSettingsPath = "logback.xml";

	@JsonProperty("connection")
	@NotNull(message = "node required")
	private ConnectionConfig connectionConfig;

	@JsonProperty("purgeThreads")
	private List<ThreadConfig> purgeThreadsList;

	public String getLogSettingsPath() {
		return logSettingsPath;
	}

	public void setLogSettingsPath(String logSettingsPath) {
		this.logSettingsPath = logSettingsPath;
	}

	public ConnectionConfig getConnectionConfig() {
		return connectionConfig;
	}

	public void setConnectionConfig(ConnectionConfig connectionConfig) {
		this.connectionConfig = connectionConfig;
	}

	public List<ThreadConfig> getPurgeThreadsList() {
		return purgeThreadsList;
	}

	public void setPurgeThreadsList(List<ThreadConfig> purgeThreadsList) {
		this.purgeThreadsList = purgeThreadsList;
	}
}
