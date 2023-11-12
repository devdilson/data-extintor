package com.data.extintor;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BaseConfig {

  @JsonProperty("log-settings-path")
  private String logSettingsPath = "logback.xml";

  public String getLogSettingsPath() {
    return logSettingsPath;
  }

  public void setLogSettingsPath(String logSettingsPath) {
    this.logSettingsPath = logSettingsPath;
  }
}
