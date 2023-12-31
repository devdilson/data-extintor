package com.data.extintor;

import com.data.extintor.database.ConnectionConfig;
import com.data.extintor.purge.ThreadConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class ExtintorConfig extends BaseConfig {

  @JsonProperty("dry-run")
  private Boolean isDryRun;

  @JsonProperty("connection")
  @NotNull(message = "node required")
  private ConnectionConfig connectionConfig;

  @JsonProperty("purgeThreads")
  private List<ThreadConfig> purgeThreadsList;

  public Boolean isDryRun() {
    return isDryRun;
  }

  public void setIsDryRun(Boolean isDryRun) {
    this.isDryRun = isDryRun;
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
