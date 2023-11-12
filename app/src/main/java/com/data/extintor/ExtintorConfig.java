package com.data.extintor;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class ExtintorConfig {
  @JsonProperty("connection")
  @NotNull(message = "node required")
  private ConnectionConfig connectionConfig;

  @JsonProperty("purgeThreads")
  private List<ThreadConfig> purgeThreadsList;

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
